/*
 * Copyright 2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.ui.sys;

import com.google.common.base.Strings;
import io.jmix.core.DevelopmentException;
import io.jmix.ui.WindowParam;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.*;
import io.jmix.ui.download.Downloader;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.model.DataLoader;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.model.ScreenData;
import io.jmix.ui.screen.*;
import io.jmix.ui.sys.UiControllerReflectionInspector.AnnotatedMethod;
import io.jmix.ui.sys.UiControllerReflectionInspector.InjectElement;
import io.jmix.ui.sys.UiControllerReflectionInspector.ScreenIntrospectionData;
import io.jmix.ui.sys.delegate.*;
import io.jmix.ui.sys.event.UiEventListenerMethodAdapter;
import io.jmix.ui.theme.ThemeConstants;
import io.jmix.ui.theme.ThemeConstantsManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.jmix.ui.screen.UiControllerUtils.getScreenData;
import static io.jmix.ui.sys.ValuePathHelper.pathPrefix;
import static java.lang.reflect.Proxy.newProxyInstance;
import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;

/**
 * Wires {@link Inject}, {@link Autowired}, {@link Resource}, {@link Named} fields/setters
 * and {@link Subscribe}, {@link Install} and {@link EventListener} methods.
 */
@org.springframework.stereotype.Component("ui_UiControllerDependencyInjector")
public class UiControllerDependencyInjector {

    private static final Logger log = LoggerFactory.getLogger(UiControllerDependencyInjector.class);

    protected ApplicationContext applicationContext;
    protected UiControllerReflectionInspector reflectionInspector;

    @Autowired
    public UiControllerDependencyInjector(ApplicationContext applicationContext,
                                          UiControllerReflectionInspector reflectionInspector) {
        this.applicationContext = applicationContext;
        this.reflectionInspector = reflectionInspector;
    }

    public void inject(FrameOwner frameOwner, ScreenOptions options) {
        ScreenIntrospectionData screenIntrospectionData =
                reflectionInspector.getScreenIntrospectionData(frameOwner.getClass());

        injectValues(frameOwner, options, screenIntrospectionData);

        initSubscribeListeners(frameOwner, screenIntrospectionData);

        initInstallMethods(frameOwner, screenIntrospectionData);

        initUiEventListeners(frameOwner, screenIntrospectionData);
    }

    protected void initInstallMethods(FrameOwner frameOwner, ScreenIntrospectionData screenIntrospectionData) {
        List<AnnotatedMethod<Install>> installMethods = screenIntrospectionData.getInstallMethods();

        for (AnnotatedMethod<Install> annotatedMethod : installMethods) {
            Install annotation = annotatedMethod.getAnnotation();

            Frame frame = UiControllerUtils.getFrame(frameOwner);

            Object targetInstance = getInstallTargetInstance(frameOwner, annotation, frame);

            if (targetInstance == null) {
                if (annotation.required()) {
                    throw new DevelopmentException(
                            String.format("Unable to find @Install target for method %s in %s",
                                    annotatedMethod.getMethod(), frameOwner.getClass()));
                }

                log.trace("Skip @Install method {} of {} : it is not required and target not found",
                        annotatedMethod.getMethod().getName(), frameOwner.getClass());

                continue;
            }

            Class<?> instanceClass = targetInstance.getClass();
            Method installMethod = annotatedMethod.getMethod();

            MethodHandle targetSetterMethod =
                    getInstallTargetSetterMethod(annotation, frame, instanceClass, installMethod);
            Class<?> targetParameterType = targetSetterMethod.type().parameterList().get(1);

            Object handler = null;
            if (targetInstance instanceof InstallTargetHandler) {
                handler = ((InstallTargetHandler) targetInstance).createInstallHandler(targetParameterType,
                        frameOwner, installMethod);
            }

            if (handler == null) {
                handler = createInstallHandler(frameOwner, installMethod, targetParameterType);
            }

            try {
                targetSetterMethod.invoke(targetInstance, handler);
            } catch (Error e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException("Unable to set declarative @Install handler for " + installMethod, e);
            }
        }
    }

    protected MethodHandle getInstallTargetSetterMethod(Install annotation, Frame frame, Class<?> instanceClass,
                                                        Method provideMethod) {
        String subjectProperty;
        if (Strings.isNullOrEmpty(annotation.subject()) && annotation.type() == Object.class) {
            InstallSubject installSubjectAnnotation = findMergedAnnotation(instanceClass, InstallSubject.class);
            if (installSubjectAnnotation != null) {
                subjectProperty = installSubjectAnnotation.value();
            } else {
                throw new DevelopmentException(
                        String.format("Unable to determine @Install subject of %s in %s", provideMethod, frame.getId())
                );
            }
        } else if (annotation.type() != Object.class) {
            subjectProperty = StringUtils.uncapitalize(annotation.type().getSimpleName());
        } else {
            subjectProperty = annotation.subject();
        }

        String subjectSetterName = "set" + StringUtils.capitalize(subjectProperty);
        // Check if addSubject is supported, e.g: addValidator(), addStyleProvider()
        String subjectAddName = "add" + StringUtils.capitalize(subjectProperty);

        MethodHandle targetSetterMethod = reflectionInspector.getInstallTargetMethod(instanceClass, subjectAddName);
        if (targetSetterMethod == null) {
            targetSetterMethod = reflectionInspector.getInstallTargetMethod(instanceClass, subjectSetterName);
        }

        if (targetSetterMethod == null) {
            throw new DevelopmentException(
                    String.format("Unable to find @Install target method %s in %s", subjectProperty, instanceClass)
            );
        }

        return targetSetterMethod;
    }

    @Nullable
    protected Object getInstallTargetInstance(FrameOwner frameOwner, Install annotation, Frame frame) {
        Object targetInstance;
        String target = UiDescriptorUtils.getInferredProvideId(annotation);
        if (Strings.isNullOrEmpty(target)) {

            switch (annotation.target()) {
                // if kept default value
                case COMPONENT:
                case CONTROLLER:
                    targetInstance = frameOwner;
                    break;

                case FRAME:
                    targetInstance = frame;
                    break;

                case PARENT_CONTROLLER:
                    if (frameOwner instanceof Screen) {
                        throw new DevelopmentException(
                                String.format("Screen %s cannot use @Install with target = PARENT_CONTROLLER",
                                        frame.getId())
                        );
                    }
                    targetInstance = ((ScreenFragment) frameOwner).getHostController();
                    break;

                case DATA_CONTEXT:
                    targetInstance = getScreenData(frameOwner).getDataContext();
                    break;

                default:
                    throw new UnsupportedOperationException("Unsupported @Install target " + annotation.target());
            }
        } else if (annotation.target() == Target.DATA_LOADER) {
            targetInstance = getScreenData(frameOwner).getLoader(target);
        } else if (annotation.target() == Target.DATA_CONTAINER) {
            targetInstance = getScreenData(frameOwner).getContainer(target);
        } else {
            targetInstance = findMethodTarget(frame, target);
        }
        return targetInstance;
    }

    protected Object createInstallHandler(FrameOwner frameOwner, Method method, Class<?> targetObjectType) {
        if (targetObjectType == Function.class) {
            return new InstalledFunction(frameOwner, method);
        } else if (targetObjectType == Consumer.class) {
            return new InstalledConsumer(frameOwner, method);
        } else if (targetObjectType == Supplier.class) {
            return new InstalledSupplier(frameOwner, method);
        } else if (targetObjectType == BiFunction.class) {
            return new InstalledBiFunction(frameOwner, method);
        } else if (targetObjectType == Runnable.class) {
            return new InstalledRunnable(frameOwner, method);
        } else {
            ClassLoader classLoader = getClass().getClassLoader();
            return newProxyInstance(classLoader, new Class[]{targetObjectType},
                    new InstalledProxyHandler(frameOwner, method)
            );
        }
    }

    protected void injectValues(FrameOwner frameOwner, ScreenOptions options,
                                ScreenIntrospectionData screenIntrospectionData) {
        List<InjectElement> injectElements = screenIntrospectionData.getInjectElements();

        for (InjectElement entry : injectElements) {
            doInjection(entry, frameOwner, options);
        }
    }

    protected void initSubscribeListeners(FrameOwner frameOwner, ScreenIntrospectionData screenIntrospectionData) {
        Class<? extends FrameOwner> clazz = frameOwner.getClass();

        List<AnnotatedMethod<Subscribe>> eventListenerMethods = screenIntrospectionData.getSubscribeMethods();

        Frame frame = UiControllerUtils.getFrame(frameOwner);
        ScreenData screenData = getScreenData(frameOwner);

        for (AnnotatedMethod<Subscribe> annotatedMethod : eventListenerMethods) {
            Method method = annotatedMethod.getMethod();
            Subscribe annotation = annotatedMethod.getAnnotation();

            String target = UiDescriptorUtils.getInferredSubscribeId(annotation);

            Parameter parameter = method.getParameters()[0];
            Class<?> eventType = parameter.getType();

            Object eventTarget = null;

            if (Strings.isNullOrEmpty(target)) {
                switch (annotation.target()) {
                    // if kept default value
                    case COMPONENT:
                    case CONTROLLER:
                        eventTarget = frameOwner;
                        break;

                    case FRAME:
                        eventTarget = frame;
                        break;

                    case PARENT_CONTROLLER:
                        if (frameOwner instanceof Screen) {
                            throw new DevelopmentException(
                                    String.format("Screen %s cannot use @Subscribe with target = PARENT_CONTROLLER",
                                            frame.getId())
                            );
                        }
                        eventTarget = ((ScreenFragment) frameOwner).getHostController();
                        break;

                    case DATA_CONTEXT:
                        eventTarget = screenData.getDataContext();
                        break;

                    default:
                        throw new UnsupportedOperationException("Unsupported @Subscribe target " + annotation.target());
                }
            } else {
                switch (annotation.target()) {
                    case CONTROLLER:
                        Object componentTarget = findMethodTarget(frame, target);
                        if (!(componentTarget instanceof Fragment)) {
                            throw new UnsupportedOperationException(
                                    "Unsupported @Subscribe target " + annotation.target() + ". It is not a Fragment.");
                        }
                        eventTarget = ((Fragment) componentTarget).getFrameOwner();
                        break;

                    case COMPONENT:
                        // component event
                        eventTarget = findMethodTarget(frame, target);
                        break;

                    case DATA_LOADER:
                        if (screenData.getLoaderIds().contains(target)) {
                            eventTarget = screenData.getLoader(target);
                        }
                        break;

                    case DATA_CONTAINER:
                        if (screenData.getContainerIds().contains(target)) {
                            eventTarget = screenData.getContainer(target);
                        }
                        break;

                    default:
                        throw new UnsupportedOperationException("Unsupported @Subscribe target " + annotation.target());
                }
            }

            if (eventTarget == null) {
                if (annotation.required()) {
                    throw new DevelopmentException(String.format("Unable to find @Subscribe target %s in %s",
                            target, frame.getId()));
                }

                log.trace("Skip @Subscribe method {} of {} : it is not required and target not found",
                        annotatedMethod.getMethod().getName(), frameOwner.getClass());

                continue;
            }

            Consumer listener;

            //If screen controller class was hot-deployed, then it will be loaded by different class loader.
            //This will make impossible to create lambda using LambdaMetaFactory for producing the listener method
            //in Java 17+
            if (SystemUtils.isJavaVersionAtMost(JavaVersion.JAVA_16) ||
                    getClass().getClassLoader() == frameOwner.getClass().getClassLoader()) {
                MethodHandle consumerMethodFactory =
                        reflectionInspector.getConsumerMethodFactory(clazz, annotatedMethod, eventType);
                try {
                    listener = (Consumer) consumerMethodFactory.invoke(frameOwner);
                } catch (Error e) {
                    throw e;
                } catch (Throwable e) {
                    throw new RuntimeException("Unable to bind consumer handler", e);
                }
            } else {
                listener = event -> {
                    try {
                        annotatedMethod.getMethodHandle().invoke(frameOwner, event);
                    } catch (Throwable e) {
                        throw new RuntimeException("Error subscribe listener method invocation", e);
                    }
                };
            }

            MethodHandle addListenerMethod = reflectionInspector.getAddListenerMethod(eventTarget.getClass(), eventType);
            if (addListenerMethod == null) {
                throw new DevelopmentException(String.format("Target %s does not support event type %s",
                        eventTarget.getClass().getName(), eventType));
            }

            try {
                addListenerMethod.invoke(eventTarget, listener);
            } catch (Error e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException("Unable to add listener" + method, e);
            }
        }
    }

    @Nullable
    protected Object findMethodTarget(Frame frame, String target) {
        String[] elements = ValuePathHelper.parse(target);
        if (elements.length == 1) {
            Object part = frame.getSubPart(target);
            if (part != null) {
                return part;
            }

            Component component = frame.getComponent(target);
            if (component != null) {
                return component;
            }

            return frame.getFacet(target);
        } else if (elements.length > 1) {
            String id = elements[elements.length - 1];

            Component component = frame.getComponent(pathPrefix(elements));

            if (component != null) {
                if (component instanceof HasSubParts) {
                    Object part = ((HasSubParts) component).getSubPart(id);
                    if (part != null) {
                        return part;
                    }
                }

                if (component instanceof HasComponents) {
                    Component childComponent = ((HasComponents) component).getComponent(id);
                    if (childComponent != null) {
                        return childComponent;
                    }
                }

                if (component instanceof Fragment) {
                    Facet facet = ((Fragment) component).getFacet(id);
                    if (facet != null) {
                        return facet;
                    }
                }
            }

            Facet facet = frame.getFacet(pathPrefix(elements));
            if (facet instanceof HasSubParts) {
                return ((HasSubParts) facet).getSubPart(id);
            }
        }

        return null;
    }

    protected void initUiEventListeners(FrameOwner frameOwner, ScreenIntrospectionData screenIntrospectionData) {
        Class<? extends FrameOwner> clazz = frameOwner.getClass();

        List<Method> eventListenerMethods = screenIntrospectionData.getEventListenerMethods();

        if (!eventListenerMethods.isEmpty()) {
            List<ApplicationListener> listeners = eventListenerMethods.stream()
                    .map(m -> new UiEventListenerMethodAdapter(frameOwner, clazz, m, applicationContext))
                    .collect(Collectors.toList());

            UiControllerUtils.setUiEventListeners(frameOwner, listeners);
        }
    }

    protected void doInjection(InjectElement injectElement, FrameOwner frameOwner, ScreenOptions options) {
        String name = getInjectionName(injectElement);
        Class<?> type = getInjectionType(injectElement);

        Object instance = getInjectedInstance(type, name, injectElement, frameOwner, options);

        if (instance != null) {
            assignValue(injectElement.getElement(), instance, frameOwner);
        } else if (isInjectionRequired(injectElement)) {
            Class<?> declaringClass = ((Member) injectElement.getElement()).getDeclaringClass();
            Class<? extends FrameOwner> frameClass = frameOwner.getClass();

            String msg;
            if (frameClass == declaringClass) {
                msg = String.format(
                        "Unable to find an instance of type '%s' named '%s' for instance of '%s'",
                        type, name, frameClass.getCanonicalName());
            } else {
                msg = String.format(
                        "Unable to find an instance of type '%s' named '%s' declared in '%s' for instance of '%s'",
                        type, name, declaringClass.getCanonicalName(), frameClass.getCanonicalName());
            }

            throw new DevelopmentException(msg);
        } else {
            log.trace("Skip injection {} of {} as it is optional and instance not found",
                    name, frameOwner.getClass());
        }
    }

    protected String getInjectionName(InjectElement injectElement) {
        AnnotatedElement element = injectElement.getElement();
        Class annotationClass = injectElement.getAnnotationClass();

        String name = null;
        if (annotationClass == Named.class) {
            name = element.getAnnotation(Named.class).value();
        } else if (annotationClass == Resource.class) {
            name = element.getAnnotation(Resource.class).name();
        } else if (annotationClass == Autowired.class) {
            if (element.isAnnotationPresent(Qualifier.class)) {
                name = element.getAnnotation(Qualifier.class).value();
            }
        } else if (annotationClass == WindowParam.class) {
            name = element.getAnnotation(WindowParam.class).name();
        }

        if (StringUtils.isEmpty(name)) {
            if (element instanceof Field) {
                name = ((Field) element).getName();
            } else if (element instanceof Method) {
                if (((Method) element).getName().startsWith("set")) {
                    name = StringUtils.uncapitalize(((Method) element).getName().substring(3));
                } else {
                    name = ((Method) element).getName();
                }
            } else {
                throw new IllegalStateException("Can inject to fields and setter methods only");
            }
        }
        return name;
    }

    protected Class<?> getInjectionType(InjectElement injectElement) {
        AnnotatedElement element = injectElement.getElement();

        if (element instanceof Field) {
            return ((Field) element).getType();
        } else if (element instanceof Method) {
            Class<?>[] types = ((Method) element).getParameterTypes();
            if (types.length != 1) {
                throw new IllegalStateException("Can inject to methods with one parameter only");
            }
            return types[0];
        } else {
            throw new IllegalStateException("Can inject to fields and setter methods only");
        }
    }

    protected boolean isInjectionRequired(InjectElement injectElement) {
        AnnotatedElement element = injectElement.getElement();
        if (element.isAnnotationPresent(WindowParam.class)) {
            return element.getAnnotation(WindowParam.class).required();
        } else if (element.isAnnotationPresent(Autowired.class)) {
            return element.getAnnotation(Autowired.class).required();
        }
        return true;
    }

    @Nullable
    protected Object getInjectedInstance(Class<?> type, String name, InjectElement injectElement,
                                         FrameOwner frameOwner, ScreenOptions options) {
        AnnotatedElement element = injectElement.getElement();
        Class annotationClass = injectElement.getAnnotationClass();

        Frame frame = UiControllerUtils.getFrame(frameOwner);

        if (annotationClass == WindowParam.class) {
            if (options instanceof MapScreenOptions) {
                return ((MapScreenOptions) options).getParams().get(name);
            }
            // Injecting a parameter
            return null;

        } else if (annotationClass == Value.class) {
            AutowireCapableBeanFactory autowireCapableBeanFactory = applicationContext.getAutowireCapableBeanFactory();
            return ((AbstractBeanFactory) autowireCapableBeanFactory)
                    .resolveEmbeddedValue(element.getAnnotation(Value.class).value());

        } else if (ScreenFragment.class.isAssignableFrom(type)) {
            // Injecting inner fragment controller
            Component fragment = frame.getComponent(name);
            if (fragment == null) {
                return null;
            }
            return ((Fragment) fragment).getFrameOwner();

        } else if (Component.class.isAssignableFrom(type)) {
            /// if legacy frame - inject controller
            Component component = frame.getComponent(name);
            if (component instanceof Fragment) {
                ScreenFragment fragmentFrameOwner = ((Fragment) component).getFrameOwner();
                if (type.isAssignableFrom(fragmentFrameOwner.getClass())) {
                    return fragmentFrameOwner;
                }
            }

            // Injecting a UI component
            return component;

        } else if (InstanceContainer.class.isAssignableFrom(type)) {
            // Injecting a container
            ScreenData data = getScreenData(frameOwner);
            return data.getContainer(name);

        } else if (DataLoader.class.isAssignableFrom(type)) {
            // Injecting a loader
            ScreenData data = getScreenData(frameOwner);
            return data.getLoader(name);

        } else if (DataContext.class.isAssignableFrom(type)) {
            // Injecting the data context
            ScreenData data = getScreenData(frameOwner);
            return data.getDataContext();

        } else if (FrameContext.class.isAssignableFrom(type)) {
            // Injecting the FrameContext
            return frame.getContext();

        } else if (Action.class.isAssignableFrom(type)) {
            // Injecting an action
            return ComponentsHelper.findAction(name, frame);

        } else if (Facet.class.isAssignableFrom(type)) {
            // Injecting non-visual component

            String[] elements = ValuePathHelper.parse(name);
            if (elements.length == 1) {
                return frame.getFacet(name);
            }

            String prefix = pathPrefix(elements);
            Component component = frame.getComponent(prefix);

            if (component == null) {
                return null;
            }

            if (!(component instanceof Fragment)) {
                throw new UnsupportedOperationException(
                        String.format("Unable to inject facet with id %s and type %s. Component %s is not a fragment",
                                name, type, prefix)
                );
            }

            String facetId = elements[elements.length - 1];
            return ((Fragment) component).getFacet(facetId);

        } else if (Downloader.class.isAssignableFrom(type)) {
            // Injecting a Downloader
            return applicationContext.getBean(Downloader.class);

        } else if (MessageBundle.class == type) {
            return createMessageBundle(element, frameOwner, frame);

        } else if (ThemeConstants.class == type) {
            // Injecting a Theme
            ThemeConstantsManager themeManager = applicationContext.getBean(ThemeConstantsManager.class);
            return themeManager.getConstants();

        } else if (BeanFactory.class.isAssignableFrom(type)) {
            return applicationContext;
        } else if (ObjectProvider.class.isAssignableFrom(type)) {
            if (!(element instanceof Field
                    && ((Field) element).getGenericType() instanceof ParameterizedType)
                    && !(element instanceof Method
                    && ((Method) element).getGenericParameterTypes().length > 0
                    && ((Method) element).getGenericParameterTypes()[0] instanceof ParameterizedType)) {
                throw new UnsupportedOperationException("Unable to inject ObjectProvider without generic parameter");
            }

            Type genericType;
            if (element instanceof Field) {
                genericType = ((ParameterizedType) ((Field) element).getGenericType())
                        .getActualTypeArguments()[0];
            } else {
                genericType = ((ParameterizedType) ((Method) element).getGenericParameterTypes()[0])
                        .getActualTypeArguments()[0];
            }

            if (genericType instanceof ParameterizedType) {
                genericType = ((ParameterizedType) genericType).getRawType();
            }
            return applicationContext.getBeanProvider((Class<?>) genericType);
        } else if (ActionsAwareDialogFacet.DialogAction.class.isAssignableFrom(type)) {
            // facet's action

            Facet facet;
            String actionId;

            String[] path = ValuePathHelper.parse(name);
            if (path.length == 2) {
                facet = frame.getFacet(path[0]);
                actionId = path[1];
            } else {
                String prefix = ValuePathHelper.pathPrefix(path, 2);
                Component component = frame.getComponent(prefix);
                if (component == null) {
                    return null;
                }
                if (!(component instanceof Fragment)) {
                    throw new UnsupportedOperationException(
                            String.format("Unable to inject dialog action with id '%s'. Component '%s' is not a fragment",
                                    name, prefix)
                    );
                }
                actionId = path[path.length - 1];
                facet = ((Fragment) component).getFacet(path[path.length - 2]);
            }

            if (!(facet instanceof ActionsAwareDialogFacet)) {
                return null;
            }

            //noinspection unchecked
            Collection<ActionsAwareDialogFacet.DialogAction<Facet>> actions =
                    ((ActionsAwareDialogFacet<Facet>) facet).getActions();

            if (CollectionUtils.isNotEmpty(actions)) {
                return actions.stream()
                        .filter(action -> action.getId().equals(actionId))
                        .findFirst()
                        .orElse(null);
            }
        } else {
            Object instance;
            // Try to find a Spring bean
            Map<String, ?> beans = applicationContext.getBeansOfType(type);
            if (!beans.isEmpty()) {
                instance = beans.get(name);
                // If a bean with required name found, return it
                if (instance != null) {
                    return instance;
                } else {
                    // Otherwise get a bean from the context again to respect @Primary annotation
                    return applicationContext.getBean(type);
                }
            }
        }

        return null;
    }

    protected MessageBundle createMessageBundle(@SuppressWarnings("unused") AnnotatedElement element,
                                                FrameOwner frameOwner,
                                                Frame frame) {
        MessageBundle messageBundle = applicationContext.getBean(MessageBundle.class);

        if (frame instanceof Component.HasXmlDescriptor) {
            Element xmlDescriptor = ((Component.HasXmlDescriptor) frame).getXmlDescriptor();
            if (xmlDescriptor != null) {
                String messagesGroup = xmlDescriptor.attributeValue("messagesGroup");
                if (messagesGroup != null) {
                    messageBundle.setMessageGroup(messagesGroup);
                    return messageBundle;
                }
            }
        }

        Class<? extends FrameOwner> screenClass = frameOwner.getClass();
        String packageName = UiControllerUtils.getPackage(screenClass);
        messageBundle.setMessageGroup(packageName);

        return messageBundle;
    }

    protected void assignValue(AnnotatedElement element, Object value, FrameOwner frameOwner) {
        // element is already marked as accessible in UiControllerReflectionInspector

        if (element instanceof Field) {
            Field field = (Field) element;

            try {
                field.set(frameOwner, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("CDI - Unable to assign value to field " + field.getName(), e);
            }
        } else {
            Method method = (Method) element;

            Object[] params = new Object[1];
            params[0] = value;
            try {
                method.invoke(frameOwner, params);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("CDI - Unable to assign value through setter "
                        + method.getName(), e);
            }
        }
    }
}
