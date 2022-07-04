package io.jmix.flowui.sys;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasComponents;
import io.jmix.core.DevelopmentException;
import io.jmix.flowui.component.EnhancedHasComponents;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.facet.Facet;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.HasActions;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.model.InstallSubject;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.model.ViewData;
import io.jmix.flowui.view.*;
import io.jmix.flowui.sys.UiControllerReflectionInspector.AnnotatedMethod;
import io.jmix.flowui.sys.UiControllerReflectionInspector.InjectElement;
import io.jmix.flowui.sys.UiControllerReflectionInspector.ViewIntrospectionData;
import io.jmix.flowui.sys.delegate.*;
import io.jmix.flowui.sys.event.UiEventListenerMethodAdapter;
import io.jmix.flowui.sys.event.UiEventsManager;
import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.*;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.jmix.flowui.sys.ValuePathHelper.pathPrefix;
import static java.lang.reflect.Proxy.newProxyInstance;
import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;

@org.springframework.stereotype.Component("flowui_UiControllerDependencyInjector")
public class UiControllerDependencyInjector {

    private static final Logger log = LoggerFactory.getLogger(UiControllerDependencyInjector.class);

    protected ApplicationContext applicationContext;
    protected UiControllerReflectionInspector reflectionInspector;

    public UiControllerDependencyInjector(ApplicationContext applicationContext,
                                          UiControllerReflectionInspector reflectionInspector) {
        this.applicationContext = applicationContext;
        this.reflectionInspector = reflectionInspector;
    }

    public void inject(View<?> controller) {
        ViewIntrospectionData viewIntrospectionData =
                reflectionInspector.getViewIntrospectionData(controller.getClass());

        injectElements(controller, viewIntrospectionData);
        initSubscribeListeners(controller, viewIntrospectionData);
        initInstallMethods(controller, viewIntrospectionData);
        initUiEventListeners(controller, viewIntrospectionData);
    }

    protected void injectElements(View<?> controller, ViewIntrospectionData viewIntrospectionData) {
        List<InjectElement> injectElements = viewIntrospectionData.getInjectElements();

        for (InjectElement entry : injectElements) {
            doInjection(entry, controller);
        }
    }

    protected void doInjection(InjectElement injectElement, View<?> controller) {
        String name = getInjectionName(injectElement);
        Class<?> type = getInjectionType(injectElement);

        Object instance = getInjectedInstance(type, name, injectElement, controller);

        if (instance != null) {
            assignValue(injectElement.getElement(), instance, controller);
        } else {
            // TODO: gg, implement?
        }
    }

    protected String getInjectionName(InjectElement injectElement) {
        AnnotatedElement element = injectElement.getElement();
        Class<?> annotationClass = injectElement.getAnnotationClass();

        String name = null;
        if (annotationClass == ComponentId.class) {
            name = element.getAnnotation(ComponentId.class).value();
        }

        if (Strings.isNullOrEmpty(name)) {
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

    @Nullable
    protected Object getInjectedInstance(Class<?> type, String name, InjectElement injectElement, View<?> controller) {
        // TODO: gg, exception?
        if (!(controller.getContent() instanceof EnhancedHasComponents)) {
            return null;
        }

        AnnotatedElement element = injectElement.getElement();
        Class<?> annotationClass = injectElement.getAnnotationClass();

        EnhancedHasComponents content = ((EnhancedHasComponents) controller.getContent());

        if (Component.class.isAssignableFrom(type)) {
            /// if legacy frame - inject controller
            Optional<Component> component = content.findComponent(name);
            // Injecting a UI component
            // TODO: gg, rework after all types will be handled
            return component.orElse(null);
        } else if (InstanceContainer.class.isAssignableFrom(type)) {
            // Injecting a container
            ViewData data = UiControllerUtils.getViewData(controller);
            return data.getContainer(name);
        } else if (DataLoader.class.isAssignableFrom(type)) {
            // Injecting a loader
            ViewData data = UiControllerUtils.getViewData(controller);
            return data.getLoader(name);
        } else if (Action.class.isAssignableFrom(type)) {
            // Injecting an action
            String[] elements = ValuePathHelper.parse(name);
            if (elements.length == 1) {
                ViewActions viewActions = UiControllerUtils.getViewActions(controller);
                return viewActions.getAction(name);
            }

            String prefix = pathPrefix(elements);
            Optional<HasActions> hasActions = content
                    .findComponent(prefix)
                    .filter(c -> c instanceof HasActions)
                    .map(c -> ((HasActions) c));
            if (hasActions.isPresent()) {
                return hasActions.get().getAction(elements[elements.length - 1]);
            }
        } else if (Facet.class.isAssignableFrom(type)) {
            String[] elements = ValuePathHelper.parse(name);
            if (elements.length != 1) {
                throw new IllegalStateException(
                        String.format("Can't inject %s. Incorrect path: '%s'", Facet.class.getSimpleName(), name));
            }

            return UiControllerUtils.getViewFacets(controller).getFacet(name);
        } else if (MessageBundle.class == type) {
            return createMessageBundle(controller);
        }

        // TODO: gg, handle other types
        // TODO: gg, DataContext?

        return null;
    }

    protected MessageBundle createMessageBundle(View<?> controller) {
        MessageBundle messageBundle = applicationContext.getBean(MessageBundle.class);
        messageBundle.setMessageGroup(UiControllerUtils.getPackage(controller.getClass()));

        if (!controller.getId().isPresent()) {
            return messageBundle;
        }

        ViewInfo viewInfo = applicationContext.getBean(ViewRegistry.class)
                .getViewInfo(controller.getId().get());

        ViewXmlLoader viewXmlLoader = applicationContext.getBean(ViewXmlLoader.class);
        Optional<String> templatePath = viewInfo.getTemplatePath();
        Element element = templatePath.map(viewXmlLoader::load).orElse(null);
        if (element != null) {
            String messagesGroup = element.attributeValue("messagesGroup");
            if (!Strings.isNullOrEmpty(messagesGroup)) {
                messageBundle.setMessageGroup(messagesGroup);
            }
        }
        return messageBundle;
    }

    protected void assignValue(AnnotatedElement element, Object value, View<?> controller) {
        if (element instanceof Field) {
            Field field = (Field) element;

            try {
                field.set(controller, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("CDI - Unable to assign value to field " + field.getName(), e);
            }
        } else {
            Method method = (Method) element;

            Object[] params = new Object[1];
            params[0] = value;
            try {
                method.invoke(controller, params);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("CDI - Unable to assign value through setter "
                        + method.getName(), e);
            }
        }
    }

    protected void initUiEventListeners(View<?> controller, ViewIntrospectionData viewIntrospectionData) {
        List<Method> eventListenerMethods = viewIntrospectionData.getEventListenerMethods();

        if (!eventListenerMethods.isEmpty()) {
            List<ApplicationListener<?>> listeners = eventListenerMethods.stream()
                    .map(m -> new UiEventListenerMethodAdapter(controller, controller.getClass(), m, applicationContext))
                    .collect(Collectors.toList());

            UiEventsManager eventsMulticaster = applicationContext.getBean(UiEventsManager.class);
            for (ApplicationListener<?> listener : listeners) {
                eventsMulticaster.addApplicationListener(controller, listener);
            }
        }
    }

    protected void initInstallMethods(View<?> controller, ViewIntrospectionData viewIntrospectionData) {
        List<AnnotatedMethod<Install>> installMethods = viewIntrospectionData.getInstallMethods();

        for (AnnotatedMethod<Install> annotatedMethod : installMethods) {
            Install annotation = annotatedMethod.getAnnotation();

            Object targetInstance = getInstallTargetInstance(controller, annotation);

            if (targetInstance == null) {
                if (annotation.required()) {
                    throw new DevelopmentException(
                            String.format("Unable to find @%s target for method %s in %s",
                                    Install.class.getSimpleName(), annotatedMethod.getMethod(), controller.getClass()));
                }

                log.trace("Skip @{} method {} of {} : it is not required and target not found",
                        Install.class.getSimpleName(), annotatedMethod.getMethod().getName(), controller.getClass());

                continue;
            }

            Class<?> instanceClass = targetInstance.getClass();
            Method installMethod = annotatedMethod.getMethod();

            MethodHandle targetSetterMethod =
                    getInstallTargetSetterMethod(annotation, controller, instanceClass, installMethod);
            Class<?> targetParameterType = targetSetterMethod.type().parameterList().get(1);

            Object handler = null;
            if (targetInstance instanceof InstallTargetHandler) {
                handler = ((InstallTargetHandler) targetInstance)
                        .createInstallHandler(targetParameterType, controller, installMethod);
            }

            if (handler == null) {
                handler = createInstallHandler(controller, installMethod, targetParameterType);
            }

            try {
                targetSetterMethod.invoke(targetInstance, handler);
            } catch (Error e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException(String.format("Unable to set declarative @%s handler for %s",
                        Install.class.getSimpleName(), installMethod), e);
            }
        }
    }

    protected MethodHandle getInstallTargetSetterMethod(Install annotation, View<?> controller,
                                                        Class<?> instanceClass, Method provideMethod) {
        String subjectProperty;
        if (Strings.isNullOrEmpty(annotation.subject()) && annotation.type() == Object.class) {
            InstallSubject installSubjectAnnotation = findMergedAnnotation(instanceClass, InstallSubject.class);
            if (installSubjectAnnotation != null) {
                subjectProperty = installSubjectAnnotation.value();
            } else {
                throw new DevelopmentException(
                        String.format("Unable to determine @%s subject of %s in %s", Install.class.getSimpleName(),
                                provideMethod, controller.getId().orElse(""))
                );
            }
        } else if (annotation.type() != Object.class) {
            subjectProperty = StringUtils.uncapitalize(annotation.type().getSimpleName());
        } else {
            subjectProperty = annotation.subject();
        }

        String subjectSetterName = "set" + StringUtils.capitalize(subjectProperty);
        // Check if addSubject is supported
        String subjectAddName = "add" + StringUtils.capitalize(subjectProperty);

        MethodHandle targetSetterMethod = reflectionInspector.getInstallTargetMethod(instanceClass, subjectAddName);
        if (targetSetterMethod == null) {
            targetSetterMethod = reflectionInspector.getInstallTargetMethod(instanceClass, subjectSetterName);
        }

        if (targetSetterMethod == null) {
            throw new DevelopmentException(
                    String.format("Unable to find @%s target method %s in %s", Install.class.getSimpleName(),
                            subjectProperty, instanceClass)
            );
        }

        return targetSetterMethod;
    }

    @Nullable
    protected Object getInstallTargetInstance(View<?> controller, Install annotation) {
        Object targetInstance;
        String target = UiDescriptorUtils.getInferredProvideId(annotation);
        if (Strings.isNullOrEmpty(target)) {

            switch (annotation.target()) {
                // if kept default value
                case COMPONENT:
                case CONTROLLER:
                    targetInstance = controller;
                    break;
                case DATA_CONTEXT:
                    targetInstance = UiControllerUtils.getViewData(controller).getDataContext();
                    break;

                default:
                    throw new UnsupportedOperationException(String.format("Unsupported @%s target %s",
                            Install.class.getSimpleName(), annotation.target()));
            }
        } else if (annotation.target() == Target.DATA_LOADER) {
            targetInstance = UiControllerUtils.getViewData(controller).getLoader(target);
        } else if (annotation.target() == Target.DATA_CONTAINER) {
            targetInstance = UiControllerUtils.getViewData(controller).getContainer(target);
        } else {
            targetInstance = findMethodTarget(controller, target);
        }
        return targetInstance;
    }

    protected Object createInstallHandler(View<?> controller, Method method, Class<?> targetObjectType) {
        if (targetObjectType == Function.class) {
            return new InstalledFunction(controller, method);
        } else if (targetObjectType == Consumer.class) {
            return new InstalledConsumer(controller, method);
        } else if (targetObjectType == Supplier.class) {
            return new InstalledSupplier(controller, method);
        } else if (targetObjectType == BiFunction.class) {
            return new InstalledBiFunction(controller, method);
        } else if (targetObjectType == Runnable.class) {
            return new InstalledRunnable(controller, method);
        } else {
            ClassLoader classLoader = getClass().getClassLoader();
            return newProxyInstance(classLoader, new Class[]{targetObjectType},
                    new InstalledProxyHandler(controller, method)
            );
        }
    }

    protected void initSubscribeListeners(View<?> controller, ViewIntrospectionData viewIntrospectionData) {
        Class<? extends View> clazz = controller.getClass();

        List<AnnotatedMethod<Subscribe>> eventListenerMethods = viewIntrospectionData.getSubscribeMethods();

        for (AnnotatedMethod<Subscribe> annotatedMethod : eventListenerMethods) {
            Method method = annotatedMethod.getMethod();
            Subscribe annotation = annotatedMethod.getAnnotation();

            String target = UiDescriptorUtils.getInferredSubscribeId(annotation);

            Parameter parameter = method.getParameters()[0];
            Class<?> eventType = parameter.getType();

            Object eventTarget = null;

            ViewData viewData = UiControllerUtils.getViewData(controller);

            if (Strings.isNullOrEmpty(target)) {
                switch (annotation.target()) {
                    case COMPONENT:
                    case CONTROLLER:
                        eventTarget = controller;
                        break;
                    case DATA_CONTEXT:
                        eventTarget = viewData.getDataContext();
                        break;
                    default:
                        throw new UnsupportedOperationException(String.format("Unsupported @%s target %s",
                                Subscribe.class.getSimpleName(), annotation.target()));
                }
            } else {
                switch (annotation.target()) {
                    case COMPONENT:
                        // component event
                        eventTarget = findMethodTarget(controller, target);
                        break;
                    case DATA_LOADER:
                        if (viewData.getLoaderIds().contains(target)) {
                            eventTarget = viewData.getLoader(target);
                        }
                        break;
                    case DATA_CONTAINER:
                        if (viewData.getContainerIds().contains(target)) {
                            eventTarget = viewData.getContainer(target);
                        }
                        break;
                    default:
                        throw new UnsupportedOperationException(String.format("Unsupported @%s target %s",
                                Subscribe.class.getSimpleName(), annotation.target()));
                }
            }

            if (eventTarget == null) {
                if (annotation.required()) {
                    throw new DevelopmentException(String.format("Unable to find @%s target %s in %s",
                            Subscribe.class.getSimpleName(), target, controller.getClass().getSimpleName()));
                }

                log.trace("Skip @{} method {} of {} : it is not required and target not found",
                        Subscribe.class.getSimpleName(), annotatedMethod.getMethod().getName(), controller.getClass());

                continue;
            }

            MethodHandle addListenerMethod = reflectionInspector.getAddListenerMethod(eventTarget.getClass(), eventType);
            if (addListenerMethod == null) {
                throw new DevelopmentException(String.format("Target %s does not support event type %s",
                        eventTarget.getClass().getName(), eventType));
            }

            Object listener;
            if (ComponentEventListener.class.isAssignableFrom(addListenerMethod.type().parameterType(1))) {
                listener = getComponentEventListener(controller, clazz, annotatedMethod, eventType);
            } else {
                listener = getConsumerListener(controller, clazz, annotatedMethod, eventType);
            }

            try {
                addListenerMethod.invoke(eventTarget, listener);
            } catch (Error e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException("Unable to add listener " + method, e);
            }
        }
    }

    protected ComponentEventListener getComponentEventListener(View<?> controller,
                                                               Class<? extends View> clazz,
                                                               AnnotatedMethod<Subscribe> annotatedMethod,
                                                               Class<?> eventType) {
        ComponentEventListener listener;
        // If view controller class was hot-deployed, then it will be loaded
        // by different class loader. This will make impossible to create lambda
        // using LambdaMetaFactory for producing the listener method in Java 17+
        if (SystemUtils.isJavaVersionAtMost(JavaVersion.JAVA_16) ||
                getClass().getClassLoader() == controller.getClass().getClassLoader()) {
            MethodHandle consumerMethodFactory =
                    reflectionInspector.getComponentEventListenerMethodFactory(clazz, annotatedMethod, eventType);
            try {
                listener = (ComponentEventListener) consumerMethodFactory.invoke(controller);
            } catch (Error e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException(String.format("Unable to bind %s handler",
                        ComponentEventListener.class.getSimpleName()), e);
            }
        } else {
            listener = event -> {
                try {
                    annotatedMethod.getMethodHandle().invoke(controller, event);
                } catch (Throwable e) {
                    throw new RuntimeException(String.format("Error subscribe %s listener method invocation",
                            ComponentEventListener.class.getSimpleName()), e);
                }
            };
        }

        return listener;
    }

    protected Consumer<?> getConsumerListener(View<?> controller,
                                              Class<? extends View> clazz,
                                              AnnotatedMethod<Subscribe> annotatedMethod,
                                              Class<?> eventType) {
        Consumer<?> listener;
        // If view controller class was hot-deployed, then it will be loaded
        // by different class loader. This will make impossible to create lambda
        // using LambdaMetaFactory for producing the listener method in Java 17+
        if (SystemUtils.isJavaVersionAtMost(JavaVersion.JAVA_16) ||
                getClass().getClassLoader() == controller.getClass().getClassLoader()) {
            MethodHandle consumerMethodFactory =
                    reflectionInspector.getConsumerMethodFactory(clazz, annotatedMethod, eventType);
            try {
                listener = (Consumer<?>) consumerMethodFactory.invoke(controller);
            } catch (Error e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException(String.format("Unable to bind %s handler", Consumer.class.getSimpleName()), e);
            }
        } else {
            listener = event -> {
                try {
                    annotatedMethod.getMethodHandle().invoke(controller, event);
                } catch (Throwable e) {
                    throw new RuntimeException(String.format("Error subscribe %s listener method invocation",
                            Consumer.class.getSimpleName()), e);
                }
            };
        }

        return listener;
    }

    @Nullable
    protected Object findMethodTarget(View<?> controller, String target) {
        // TODO: gg, exception?
        if (!(controller.getContent() instanceof EnhancedHasComponents)) {
            return null;
        }

        ViewFacets viewFacets = UiControllerUtils.getViewFacets(controller);

        String[] elements = ValuePathHelper.parse(target);
        EnhancedHasComponents viewLayout = ((EnhancedHasComponents) controller.getContent());
        if (elements.length == 1) {
            ViewActions viewActions = UiControllerUtils.getViewActions(controller);
            Action action = viewActions.getAction(target);
            if (action != null) {
                return action;
            }

            Optional<Component> component = viewLayout.findComponent(target);
            if (component.isPresent()) {
                return component.get();
            }

            return viewFacets.getFacet(target);
        } else if (elements.length > 1) {
            String id = elements[elements.length - 1];

            Optional<Component> componentOpt = viewLayout.findComponent(pathPrefix(elements));

            if (componentOpt.isPresent()) {
                Component component = componentOpt.get();

                if (component instanceof HasActions) {
                    Action action = ((HasActions) component).getAction(id);
                    if (action != null) {
                        return action;
                    }
                }

                if (component instanceof HasComponents) {
                    Optional<Component> childComponent = UiComponentUtils.findComponent((HasComponents) component, id);
                    if (childComponent.isPresent()) {
                        return childComponent.get();
                    }
                }
            }

            // TODO: gg, do we need HasSubParts?
            /*Facet facet = viewFacets.getFacet(pathPrefix(elements));
            if (facet instanceof HasSubParts) {
                return ((HasSubParts) facet).getSubPart(id);
            }*/
        }

        return null;
    }
}
