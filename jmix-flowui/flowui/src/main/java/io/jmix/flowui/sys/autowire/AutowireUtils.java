/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.sys.autowire;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasValue;
import io.jmix.core.DevelopmentException;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.facet.Facet;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentOwner;
import io.jmix.flowui.fragment.FragmentUtils;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.HasActions;
import io.jmix.flowui.kit.component.HasSubParts;
import io.jmix.flowui.kit.component.dropdownbutton.ActionItem;
import io.jmix.flowui.kit.component.dropdownbutton.ComponentItem;
import io.jmix.flowui.model.InstallSubject;
import io.jmix.flowui.model.ViewData;
import io.jmix.flowui.sys.autowire.ReflectionCacheManager.AnnotatedMethod;
import io.jmix.flowui.sys.autowire.ReflectionCacheManager.AutowireElement;
import io.jmix.flowui.sys.delegate.*;
import io.jmix.flowui.view.*;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.jmix.flowui.sys.ValuePathHelper.*;
import static java.lang.reflect.Proxy.newProxyInstance;
import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;

/**
 * Helper class for component dependencies autowiring.
 */
public final class AutowireUtils {

    private static final Logger log = LoggerFactory.getLogger(AutowireUtils.class);

    private AutowireUtils() {
    }

    /**
     * Finds the target of the annotated method in passed composite by ID.
     *
     * @param composite       composite for search
     * @param targetId        target ID
     * @param componentFinder hook to find components in the composite
     * @return found target object or {@code null} if target no found
     * @throws IllegalStateException if the view content is not a container
     */
    @Nullable
    public static Object findMethodTarget(Composite<?> composite, String targetId,
                                          BiFunction<Component, String, Optional<Component>> componentFinder) {
        String[] elements = parse(targetId);
        if (elements.length == 1) {
            Action action = findActionCandidate(composite, targetId);
            if (action != null) {
                return action;
            }

            Optional<Component> component = UiComponentUtils.findComponent(composite, targetId);
            if (component.isPresent()) {
                return component.get();
            }

            return findFacetCandidate(composite, targetId);
        } else if (elements.length > 1) {
            if (targetId.contains(".@")) {
                return findSubTargetRecursively(composite, elements);
            }

            String id = elements[elements.length - 1];

            Optional<Component> componentOpt = UiComponentUtils.findComponent(composite, pathPrefix(elements));

            if (componentOpt.isPresent()) {
                Component component = componentOpt.get();

                if (component instanceof HasSubParts hasSubParts) {
                    Object part = hasSubParts.getSubPart(id);
                    if (part != null) {
                        return part;
                    }
                }

                if (component instanceof HasActions hasActions) {
                    Action action = hasActions.getAction(id);
                    if (action != null) {
                        return action;
                    }
                }

                if (UiComponentUtils.isContainer(component)) {
                    return componentFinder.apply(component, id)
                            .orElse(null);
                }
            }

            Facet facet = findFacetCandidate(composite, pathPrefix(elements));
            if (facet instanceof HasSubParts hasSubParts) {
                return hasSubParts.getSubPart(id);
            }

            Object dropdownItemCandidate = findMethodTarget(composite, pathPrefix(elements), componentFinder);
            if (dropdownItemCandidate instanceof ComponentItem componentItem) {
                Component content = componentItem.getContent();
                if (content == null) {
                    return null;
                }

                if (UiComponentUtils.sameId(content, id) || FragmentUtils.sameId(content, id)) {
                    return content;
                }

                return componentFinder.apply(content, id)
                        .orElse(null);
            } else if (dropdownItemCandidate instanceof ActionItem actionItem) {
                Action action = actionItem.getAction();
                if (action == null) {
                    return null;
                }

                if (id.equals(action.getId())) {
                    return action;
                }
            } else if (dropdownItemCandidate instanceof Component dropdownContent) {
                // For case where the method's targetId is deeper than the first level of children for the componentItem
                return componentFinder.apply(dropdownContent, id)
                        .orElse(null);
            }
        }

        return null;
    }

    /**
     * Finds the target for the annotated method in passed view by targetType and targetId.
     *
     * @param annotation annotation of the method for which target will be found
     * @param view       view for search
     * @param targetId   target ID
     * @param targetType type of the target
     * @param <A>        type of the annotation
     * @return found target object or {@code null} if target not found
     * @throws UnsupportedOperationException if the targetId is {@code null} and the targetType
     *                                       is not {@link Target#COMPONENT}, {@link Target#CONTROLLER}
     *                                       or {@link Target#DATA_CONTEXT}
     */
    @Nullable
    public static <A extends Annotation> Object getViewTargetInstance(A annotation, View<?> view,
                                                                      @Nullable String targetId, Target targetType) {
        return Strings.isNullOrEmpty(targetId) ? switch (targetType) {
            case COMPONENT, CONTROLLER -> view;
            case DATA_CONTEXT -> ViewControllerUtils.getViewData(view).getDataContext();
            default -> throw new UnsupportedOperationException(String.format("Unsupported @%s target '%s'",
                    annotation.getClass().getSimpleName(), targetType));
        } : switch (targetType) {
            case DATA_LOADER -> ViewControllerUtils.getViewData(view).getLoader(targetId);
            case DATA_CONTAINER -> ViewControllerUtils.getViewData(view).getContainer(targetId);
            default -> findMethodTarget(view, targetId, UiComponentUtils::findComponent);
        };
    }

    /**
     * Find the target fot the {@link Subscribe} annotated method in passed view by targetType and targetId.
     *
     * @param view       view for search
     * @param targetId   target ID
     * @param targetType type of the target
     * @return found target object or {@code null} if target not found
     * @throws UnsupportedOperationException if the targetId is {@code null} and the targetType
     *                                       is not {@link Target#COMPONENT}, {@link Target#CONTROLLER}
     *                                       or {@link Target#DATA_CONTEXT}
     */
    @Nullable
    public static Object getViewSubscribeTargetInstance(View<?> view, @Nullable String targetId, Target targetType) {
        ViewData viewData = ViewControllerUtils.getViewData(view);

        return Strings.isNullOrEmpty(targetId) ? switch (targetType) {
            case COMPONENT, CONTROLLER -> view;
            case DATA_CONTEXT -> viewData.getDataContext();
            default -> throw new UnsupportedOperationException(String.format("Unsupported @%s targetId %s",
                    Subscribe.class.getSimpleName(), targetType));
        } : switch (targetType) {
            case COMPONENT -> AutowireUtils.findMethodTarget(view, targetId, UiComponentUtils::findComponent);
            case DATA_LOADER -> viewData.getLoaderIds().contains(targetId)
                    ? viewData.getLoader(targetId)
                    : null;
            case DATA_CONTAINER -> viewData.getContainerIds().contains(targetId)
                    ? viewData.getContainer(targetId)
                    : null;
            default -> throw new UnsupportedOperationException(String.format("Unsupported @%s targetId %s",
                    Subscribe.class.getSimpleName(), targetType));
        };
    }

    /**
     * Find the target for the annotated method is passed fragment component by targetType and targetId.
     *
     * @param annotation annotation of the method for which target will be found
     * @param fragment   fragment for search
     * @param targetId   target ID
     * @param targetType type of the target
     * @param <A>        type of the annotation
     * @return found target object or null if target not found
     * @throws UnsupportedOperationException if the target is not supported
     */
    @Nullable
    public static <A extends Annotation> Object getFragmentTargetInstance(A annotation,
                                                                          Fragment<?> fragment,
                                                                          @Nullable String targetId,
                                                                          Target targetType) {
        return Strings.isNullOrEmpty(targetId) ? switch (targetType) {
            case COMPONENT, CONTROLLER -> fragment;
            case HOST_CONTROLLER -> findHostView(fragment);
            case DATA_CONTEXT -> FragmentUtils.getFragmentData(fragment).getDataContext();
            default -> throw new UnsupportedOperationException(String.format("Unsupported @%s target '%s'",
                    annotation.getClass().getSimpleName(), targetType));
        } : switch (targetType) {
            case DATA_LOADER -> FragmentUtils.getFragmentData(fragment).getLoader(targetId);
            case DATA_CONTAINER -> FragmentUtils.getFragmentData(fragment).getContainer(targetId);
            default -> findMethodTarget(fragment, targetId,
                    (component, id) -> UiComponentUtils.findComponent(component, id, FragmentUtils::sameId));
        };
    }

    /**
     * Creates {@link AnnotatedMethod} instance based on the passed {@code annotationClass} and {@link Method} instance.
     *
     * @param annotationClass the annotation class with which the method should be annotated
     * @param method          the method to search for annotation
     * @param <A>             type of the annotation
     * @return {@link AnnotatedMethod} instance or {@code null} if the passed method doesn't have required annotation
     * @throws RuntimeException if it fails to create a method handle
     */
    @Nullable
    public static <A extends Annotation> AnnotatedMethod<A> createAnnotatedMethod(
            Class<A> annotationClass, Method method) {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        A annotation = findMergedAnnotation(method, annotationClass);

        if (annotation != null) {
            method.trySetAccessible();

            MethodHandle methodHandle;
            try {
                methodHandle = lookup.unreflect(method);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Unable to get method handle " + method);
            }

            return new AnnotatedMethod<>(annotation, method, methodHandle);
        }

        return null;
    }

    /**
     * Compares two {@link AnnotatedMethod} by class name, {@link Order} annotation value, and method name.
     * Can be used as a sort comparator.
     *
     * @param am1 first {@link AnnotatedMethod} for comparison
     * @param am2 second {@link AnnotatedMethod} for comparison
     * @param <A> type of the annotation
     * @return the value {@code 0} if the first annotated method is equal to the second one;
     * a value less than {@code 0} if the first annotated method is less by comparison logic;
     * and a value greater than {@code 0} if the first annotated method is greater by comparison logic
     */
    public static <A extends Annotation> int compareMethods(AnnotatedMethod<A> am1,
                                                            AnnotatedMethod<A> am2) {
        Method m1 = am1.getMethod();
        Method m2 = am2.getMethod();

        if (m1 == m2) {
            // fulfill comparator contract
            return 0;
        }

        Class<?> pt1 = m1.getParameterTypes()[0];
        Class<?> pt2 = m2.getParameterTypes()[0];

        if (pt1 != pt2) {
            // if type of event different - compare by class name
            return pt1.getCanonicalName().compareTo(pt2.getCanonicalName());
        }

        Order o1 = findMergedAnnotation(m1, Order.class);
        Order o2 = findMergedAnnotation(m2, Order.class);

        if (o1 != null && o2 != null) {
            return Integer.compare(o1.value(), o2.value());
        }

        if (o1 != null) {
            return -1;
        }

        if (o2 != null) {
            return 1;
        }

        Class<?> dc1 = getDeclaringClass(m1);
        Class<?> dc2 = getDeclaringClass(m2);

        if (dc1 == dc2) {
            // if declaring class is the same - compare by method name
            return m1.getName().compareTo(m2.getName());
        }

        // if there is no @Order - parent first

        if (dc1.isAssignableFrom(dc2)) {
            return -1;
        }

        if (dc2.isAssignableFrom(dc1)) {
            return 1;
        }

        // return 0 as fallback
        return 0;
    }

    /**
     * Returns the override hierarchy of the passed method.
     *
     * @param method method to introspect its hierarchy
     * @return set of overridden methods
     */
    public static Set<Method> getOverrideHierarchy(Method method) {
        Set<Method> result = new LinkedHashSet<>();
        result.add(method);

        Class<?>[] parameterTypes = method.getParameterTypes();

        Class<?> declaringClass = method.getDeclaringClass();

        Iterator<Class<?>> hierarchy = ClassUtils.hierarchy(declaringClass, ClassUtils.Interfaces.INCLUDE).iterator();
        //skip the declaring class :P
        hierarchy.next();
        hierarchyTraversal:
        while (hierarchy.hasNext()) {
            final Class<?> c = hierarchy.next();
            Method m;
            try {
                m = c.getDeclaredMethod(method.getName(), parameterTypes);
            } catch (NoSuchMethodException e) {
                m = null;
            }

            if (m == null) {
                continue;
            }
            if (Arrays.equals(m.getParameterTypes(), parameterTypes)) {
                // matches without generics
                result.add(m);
                continue;
            }
            // necessary to get arguments every time in the case that we are including interfaces
            Map<TypeVariable<?>, Type> typeArguments = TypeUtils.getTypeArguments(declaringClass,
                    m.getDeclaringClass());
            for (int i = 0; i < parameterTypes.length; i++) {
                Type childType = TypeUtils.unrollVariables(typeArguments, method.getGenericParameterTypes()[i]);
                Type parentType = TypeUtils.unrollVariables(typeArguments, m.getGenericParameterTypes()[i]);
                if (!TypeUtils.equals(childType, parentType)) {
                    continue hierarchyTraversal;
                }
            }
            result.add(m);
        }

        return result;
    }

    /**
     * Gets the name of the field or setter method for the autowire element.
     *
     * @param autowireElement the element to find for a name for autowiring
     * @return the name for autowiring
     * @throws IllegalStateException if the search target is not a field or setter method
     */
    public static String getAutowiringName(AutowireElement autowireElement) {
        AnnotatedElement element = autowireElement.getElement();
        Class<?> annotationClass = autowireElement.getAnnotationClass();

        String name = null;
        if (annotationClass == ViewComponent.class) {
            name = element.getAnnotation(ViewComponent.class).value();
        }

        if (Strings.isNullOrEmpty(name)) {
            if (element instanceof Field field) {
                name = field.getName();
            } else if (element instanceof Method method) {
                if (method.getName().startsWith("set")) {
                    name = StringUtils.uncapitalize(method.getName().substring(3));
                } else {
                    name = method.getName();
                }
            } else {
                throw new IllegalStateException("Can autowire to fields and setter methods only");
            }
        }

        return name;
    }

    /**
     * Gets the type of the field or setter method for the autowire element.
     *
     * @param autowireElement the element to find for a type for autowiring
     * @return the type for autowiring
     * @throws IllegalStateException if the target method doesn't have only a parameter or the autowire element
     *                               is not a field or setter method
     */
    public static Class<?> getAutowiringType(AutowireElement autowireElement) {
        AnnotatedElement element = autowireElement.getElement();

        if (element instanceof Field field) {
            return field.getType();
        } else if (element instanceof Method method) {
            Class<?>[] types = method.getParameterTypes();
            if (types.length != 1) {
                throw new IllegalStateException("Can autowire to methods with one parameter only");
            }

            return types[0];
        } else {
            throw new IllegalStateException("Can autowire to fields and setter methods only");
        }
    }

    /**
     * Assigns the passed value to a component field or method.
     *
     * @param element   annotated element that should be autowired, can be a field or setter method
     * @param value     value to be set
     * @param component the component for which it is necessary to assign a value to the element
     * @throws RuntimeException if it is not possible to assign value to a field or setter method
     */
    public static void assignValue(AnnotatedElement element, Object value, Component component) {
        if (element instanceof Field field) {
            try {
                field.set(component, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("CDI - Unable to assign value to field " + field.getName(), e);
            }
        } else if (element instanceof Method method) {
            Object[] params = new Object[1];
            params[0] = value;

            try {
                method.invoke(component, params);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("CDI - Unable to assign value through setter " + method.getName(), e);
            }
        }
    }

    /**
     * Creates a component event listener method using the factory if the component class has not been reloaded
     * by hot-deploy, otherwise uses the {@link MethodHandle}
     *
     * @param callerClass            caller class
     * @param component              owner class for target component event listener method handle
     * @param annotatedMethod        annotated method
     * @param eventType              event class
     * @param reflectionCacheManager reflection cache manager to get a method factory
     * @return lambda-proxy for listener
     * @see ReflectionCacheManager#getComponentEventListenerMethodFactory(Class, AnnotatedMethod, Class)
     */
    public static ComponentEventListener<?> getComponentEventListener(Class<?> callerClass,
                                                                      Component component,
                                                                      AnnotatedMethod<Subscribe> annotatedMethod,
                                                                      Class<?> eventType,
                                                                      ReflectionCacheManager reflectionCacheManager) {
        ComponentEventListener<?> listener;

        // If component class was hot-deployed, then it will be loaded
        // by different class loader. This will make impossible to create lambda
        // using LambdaMetaFactory for producing the listener method in Java 17+
        if (callerClass.getClassLoader() == component.getClass().getClassLoader()) {
            MethodHandle consumerMethodFactory =
                    reflectionCacheManager.getComponentEventListenerMethodFactory(
                            component.getClass(), annotatedMethod, eventType
                    );
            try {
                listener = (ComponentEventListener<?>) consumerMethodFactory.invoke(component);
            } catch (Error e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException(String.format("Unable to bind %s handler",
                        ComponentEventListener.class.getSimpleName()), e);
            }
        } else {
            listener = event -> {
                try {
                    annotatedMethod.getMethodHandle().invoke(component, event);
                } catch (Throwable e) {
                    throw new RuntimeException(String.format("Error subscribe %s listener method invocation",
                            ComponentEventListener.class.getSimpleName()), e);
                }
            };
        }

        return listener;
    }

    /**
     * Creates a component value change listener method using the factory if the component class has not been reloaded
     * by hot-deploy, otherwise uses the {@link MethodHandle}
     *
     * @param callerClass            caller class
     * @param component              owner class for target component value change event listener method handle
     * @param annotatedMethod        annotated method
     * @param eventType              event class
     * @param reflectionCacheManager reflection cache manager to get a method factory
     * @return lambda-proxy for listener
     * @see ReflectionCacheManager#getValueChangeEventMethodFactory(Class, AnnotatedMethod, Class)
     */
    public static HasValue.ValueChangeListener<?> getValueChangeEventListener(Class<?> callerClass,
                                                                              Component component,
                                                                              AnnotatedMethod<Subscribe> annotatedMethod,
                                                                              Class<?> eventType,
                                                                              ReflectionCacheManager reflectionCacheManager) {
        HasValue.ValueChangeListener<?> listener;

        // If component class was hot-deployed, then it will be loaded
        // by different class loader. This will make impossible to create lambda
        // using LambdaMetaFactory for producing the listener method in Java 17+
        if (callerClass.getClassLoader() == component.getClass().getClassLoader()) {
            MethodHandle consumerMethodFactory =
                    reflectionCacheManager.getValueChangeEventMethodFactory(
                            component.getClass(), annotatedMethod, eventType
                    );
            try {
                listener = (HasValue.ValueChangeListener<?>) consumerMethodFactory.invokeWithArguments(component);
            } catch (Error e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException(String.format("Unable to bind %s handler",
                        HasValue.ValueChangeListener.class.getSimpleName()), e);
            }
        } else {
            listener = event -> {
                try {
                    annotatedMethod.getMethodHandle().invoke(component, event);
                } catch (Throwable e) {
                    throw new RuntimeException(String.format("Error subscribe %s listener method invocation",
                            HasValue.ValueChangeListener.class.getSimpleName()), e);
                }
            };
        }

        return listener;
    }

    /**
     * Creates a consumer listener method using the factory if the component class has not been reloaded
     * by hot-deploy, otherwise uses the {@link MethodHandle}
     *
     * @param callerClass            caller class
     * @param component              owner class for target consumer listener method handle
     * @param annotatedMethod        annotated method
     * @param eventType              event class
     * @param reflectionCacheManager reflection cache manager to get a method factory
     * @return lambda-proxy for listener
     * @see ReflectionCacheManager#getConsumerMethodFactory(Class, AnnotatedMethod, Class)
     */
    public static Consumer<?> getConsumerListener(Class<?> callerClass,
                                                  Component component,
                                                  AnnotatedMethod<Subscribe> annotatedMethod,
                                                  Class<?> eventType,
                                                  ReflectionCacheManager reflectionCacheManager) {
        Consumer<?> listener;

        // If component class was hot-deployed, then it will be loaded
        // by different class loader. This will make impossible to create lambda
        // using LambdaMetaFactory for producing the listener method in Java 17+
        if (callerClass.getClassLoader() == component.getClass().getClassLoader()) {
            MethodHandle consumerMethodFactory =
                    reflectionCacheManager.getConsumerMethodFactory(component.getClass(), annotatedMethod, eventType);
            try {
                listener = (Consumer<?>) consumerMethodFactory.invoke(component);
            } catch (Error e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException(String.format("Unable to bind %s handler", Consumer.class.getSimpleName()), e);
            }
        } else {
            listener = event -> {
                try {
                    annotatedMethod.getMethodHandle().invoke(component, event);
                } catch (Throwable e) {
                    throw new RuntimeException(String.format("Error subscribe %s listener method invocation",
                            Consumer.class.getSimpleName()), e);
                }
            };
        }


        return listener;
    }

    /**
     * Finds a corresponding setter method for install annotation.
     *
     * @param annotation             annotation for search
     * @param component              origin component
     * @param instanceClass          class to search annotation
     * @param provideMethod          provideMethod
     * @param reflectionCacheManager reflection cache manager to get target install method
     * @return setter method handle
     */
    public static MethodHandle getInstallTargetSetterMethod(Install annotation, Component component,
                                                            Class<?> instanceClass, Method provideMethod,
                                                            ReflectionCacheManager reflectionCacheManager) {
        String subjectProperty;

        if (Strings.isNullOrEmpty(annotation.subject()) && annotation.type() == Object.class) {
            InstallSubject installSubjectAnnotation = findMergedAnnotation(instanceClass, InstallSubject.class);
            if (installSubjectAnnotation != null) {
                subjectProperty = installSubjectAnnotation.value();
            } else {
                throw new DevelopmentException(
                        String.format("Unable to determine @%s subject of %s in %s", Install.class.getSimpleName(),
                                provideMethod, component.getId().orElse(""))
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

        MethodHandle targetSetterMethod = reflectionCacheManager.getTargetInstallMethod(instanceClass, subjectAddName);
        if (targetSetterMethod == null) {
            targetSetterMethod = reflectionCacheManager.getTargetInstallMethod(instanceClass, subjectSetterName);
        }

        if (targetSetterMethod == null) {
            throw new DevelopmentException(
                    String.format("Unable to find @%s target method %s in %s", Install.class.getSimpleName(),
                            subjectProperty, instanceClass)
            );
        }

        return targetSetterMethod;
    }

    /**
     * Created proxy instance for install method handler.
     *
     * @param callerClass      caller class
     * @param targetObjectType target object type
     * @param component        component to be passed
     * @param method           method to be invoked
     * @return proxy instance for install handler
     */
    public static Object createInstallHandler(Class<?> callerClass, Class<?> targetObjectType,
                                              Component component, Method method) {
        if (targetObjectType == Function.class) {
            return new InstalledFunction(component, method);
        } else if (targetObjectType == Consumer.class) {
            return new InstalledConsumer(component, method);
        } else if (targetObjectType == Supplier.class) {
            return new InstalledSupplier(component, method);
        } else if (targetObjectType == BiFunction.class) {
            return new InstalledBiFunction(component, method);
        } else if (targetObjectType == Runnable.class) {
            return new InstalledRunnable(component, method);
        }

        ClassLoader classLoader = callerClass.getClassLoader();
        return newProxyInstance(classLoader, new Class[]{targetObjectType},
                new InstalledProxyHandler(component, method)
        );
    }

    @Nullable
    private static Object findSubTargetRecursively(Composite<?> layout, String[] elements) {
        String parentComponentId = pathPrefix(elements, elements.length - 1);
        String[] subTargets = parse(pathSuffix(elements));

        Optional<Component> component = UiComponentUtils.findComponent(layout, parentComponentId);

        if (component.isPresent()) {
            Object subTarget = component.get();
            Class<?> targetClass = subTarget.getClass();

            for (String element : subTargets) {
                String subTargetGetterMethod = "get" + StringUtils.capitalize(element);

                try {
                    Method elementGetterMethod = targetClass.getMethod(subTargetGetterMethod);

                    subTarget = elementGetterMethod.invoke(subTarget);
                    targetClass = subTarget.getClass();

                } catch (NoSuchMethodException e) {
                    log.trace("Skip @{} method for {} : can't find getter {} for subclass {} ",
                            Install.class.getSimpleName(),
                            component.getClass().getSimpleName(),
                            subTargetGetterMethod,
                            targetClass.getSimpleName()
                    );

                } catch (InvocationTargetException | IllegalAccessException e) {
                    log.trace("Can't invoke getter {} for class {} ",
                            subTargetGetterMethod,
                            targetClass.getSimpleName()
                    );
                }
            }

            return subTarget;
        }

        return null;
    }

    @Nullable
    private static View<?> findHostView(FragmentOwner fragmentOwner) {
        if (fragmentOwner instanceof View<?> view) {
            return view;
        } else if (fragmentOwner instanceof Fragment<?> fragment) {
            return findHostView(FragmentUtils.getParentController(fragment));
        }

        throw new IllegalStateException("Unknown parent type: " + fragmentOwner.getClass().getName());
    }

    @Nullable
    private static Action findActionCandidate(Composite<?> component, String targetId) {
        HasActions hasActions = null;
        if (component instanceof View<?> view) {
            hasActions = ViewControllerUtils.getViewActions(view);
        } else if (component instanceof Fragment<?> fragment) {
            hasActions = FragmentUtils.getFragmentActions(fragment);
        }

        return hasActions == null
                ? null
                : hasActions.getAction(targetId);
    }

    @Nullable
    private static Facet findFacetCandidate(Composite<?> component, String targetId) {
        if (component instanceof View<?> view) {
            return ViewControllerUtils.getViewFacets(view).getFacet(targetId);
        }

        return null;
    }

    private static Class<?> getDeclaringClass(Method method) {
        Class<?> declaringClass = method.getDeclaringClass();
        if (declaringClass.getSuperclass() == View.class) {
            // speed up search of declaring class for simple cases
            return declaringClass;
        }

        Set<Method> overrideHierarchy = getOverrideHierarchy(method);
        return Iterables.getLast(overrideHierarchy).getDeclaringClass();
    }
}
