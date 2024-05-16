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
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.facet.Facet;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.HasActions;
import io.jmix.flowui.kit.component.HasSubParts;
import io.jmix.flowui.kit.component.dropdownbutton.ActionItem;
import io.jmix.flowui.kit.component.dropdownbutton.ComponentItem;
import io.jmix.flowui.sys.autowire.ReflectionCacheManager.AnnotatedMethod;
import io.jmix.flowui.sys.autowire.ReflectionCacheManager.AutowireElement;
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

import static io.jmix.flowui.sys.ValuePathHelper.*;
import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;

/**
 * Helper class for component dependencies autowiring.
 */
public final class AutowireUtils {

    private static final Logger log = LoggerFactory.getLogger(AutowireUtils.class);

    private AutowireUtils() {
    }

    /**
     * Finds the target of the annotated method in passed view by ID.
     *
     * @param view     view for search
     * @param targetId target ID
     * @return found target object or {@code null} if target no found
     * @throws IllegalStateException if the view content is not a container
     */
    @Nullable
    public static Object findMethodTarget(View<?> view, String targetId) {
        Component viewLayout = view.getContent();
        if (!UiComponentUtils.isContainer(viewLayout)) {
            throw new IllegalStateException(View.class.getSimpleName() + "'s layout component " +
                    "doesn't support child components");
        }

        ViewFacets viewFacets = ViewControllerUtils.getViewFacets(view);

        String[] elements = parse(targetId);
        if (elements.length == 1) {
            ViewActions viewActions = ViewControllerUtils.getViewActions(view);
            Action action = viewActions.getAction(targetId);
            if (action != null) {
                return action;
            }

            Optional<Component> component = UiComponentUtils.findComponent(viewLayout, targetId);
            if (component.isPresent()) {
                return component.get();
            }

            return viewFacets.getFacet(targetId);
        } else if (elements.length > 1) {
            if (targetId.contains(".@")) {
                return findSubTargetRecursively(viewLayout, elements);
            }

            String id = elements[elements.length - 1];

            Optional<Component> componentOpt = UiComponentUtils.findComponent(viewLayout, pathPrefix(elements));

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
                    Optional<Component> childComponent = UiComponentUtils.findComponent(component, id);
                    if (childComponent.isPresent()) {
                        return childComponent.get();
                    }
                }
            }

            Facet facet = viewFacets.getFacet(pathPrefix(elements));
            if (facet instanceof HasSubParts hasSubParts) {
                return hasSubParts.getSubPart(id);
            }

            Object dropdownItemCandidate = findMethodTarget(view, pathPrefix(elements));
            if (dropdownItemCandidate instanceof ComponentItem componentItem) {
                Component content = componentItem.getContent();
                if (content == null) {
                    return null;
                }

                if (content.getId().isPresent() && content.getId().get().equals(id)) {
                    return content;
                }

                Optional<Component> childComponent = UiComponentUtils.findComponent(content, id);
                if (childComponent.isPresent()) {
                    return childComponent.get();
                }
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
                Optional<Component> childComponent = UiComponentUtils.findComponent(dropdownContent, id);
                if (childComponent.isPresent()) {
                    return childComponent.get();
                }
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
    public static <A extends Annotation> Object getTargetInstance(A annotation, View<?> view,
                                                                  @Nullable String targetId, Target targetType) {
        return Strings.isNullOrEmpty(targetId) ? switch (targetType) {
            case COMPONENT, CONTROLLER -> view;
            case DATA_CONTEXT -> ViewControllerUtils.getViewData(view).getDataContext();
            default -> throw new UnsupportedOperationException(String.format("Unsupported @%s target '%s'",
                    annotation.getClass().getSimpleName(), targetType));
        } : switch (targetType) {
            case DATA_LOADER -> ViewControllerUtils.getViewData(view).getLoader(targetId);
            case DATA_CONTAINER -> ViewControllerUtils.getViewData(view).getContainer(targetId);
            default -> findMethodTarget(view, targetId);
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

    @Nullable
    private static Object findSubTargetRecursively(Component viewLayout, String[] elements) {
        String parentComponentId = pathPrefix(elements, elements.length - 1);
        String[] subTargets = parse(pathSuffix(elements));

        Optional<Component> component = UiComponentUtils.findComponent(viewLayout, parentComponentId);

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
