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
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.common.event.Subscription;
import io.jmix.flowui.view.Install;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.Supply;
import io.jmix.flowui.view.ViewComponent;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;

/**
 * Loads and caches data for a fields and methods that are used for autowiring.
 * <p>
 * Introspects the passed component for methods and fields that are annotated with UI system annotations. Also analyzes
 * target classes for injection, collecting and caching data for methods-candidates for autowiring.
 * </p>
 */
@Component("flowui_ReflectionCacheManager")
public class ReflectionCacheManager {

    protected final LoadingCache<Class<?>, ComponentIntrospectionData> componentIntrospectionCache =
            CacheBuilder.newBuilder()
                    .weakKeys()
                    .build(CacheLoader.from(this::getComponentIntrospectionDataNotCached));

    protected final LoadingCache<Class<?>, TargetIntrospectionData> targetIntrospectionCache =
            CacheBuilder.newBuilder()
                    .weakKeys()
                    .build(CacheLoader.from(this::getTargetIntrospectionDataNotCached));

    // key - method of FrameOwner, value - lambda factory that produces Consumer instances
    protected final Cache<MethodHandle, MethodHandle> lambdaMethodsCache =
            CacheBuilder.newBuilder()
                    .weakKeys()
                    .build();

    protected final Function<Class<?>, MethodHandles.Lookup> lambdaLookupProvider;

    public ReflectionCacheManager() {
        MethodHandles.Lookup original = MethodHandles.lookup();

        MethodHandle privateLookupInMh;
        try {
            MethodType methodType = MethodType.methodType(MethodHandles.Lookup.class,
                    Class.class, MethodHandles.Lookup.class);

            privateLookupInMh = original.findStatic(MethodHandles.class, "privateLookupIn", methodType);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            privateLookupInMh = null;
        }

        // required by compiler
        final MethodHandle privateLookupInMhFinal = privateLookupInMh;

        this.lambdaLookupProvider = clazz -> {
            try {
                return (MethodHandles.Lookup) Objects.requireNonNull(privateLookupInMhFinal).invokeExact(clazz, original);
            } catch (Error e) {
                throw e;
            } catch (Throwable t) {
                throw new RuntimeException("Unable to get private lookup in class " + clazz, t);
            }
        };
    }

    /**
     * Introspects component class if it has never introspected before and finds fields
     * annotated by {@link ViewComponent} for autowiring.
     *
     * @param componentClass component class to introspect
     * @return list of {@link AutowireElement}
     */
    public List<AutowireElement> getAutowireElements(Class<?> componentClass) {
        return componentIntrospectionCache.getUnchecked(componentClass).getAutowireElements();
    }

    /**
     * Introspects component class if it has never introspected before and
     * finds methods annotated by {@link Subscribe} for autowiring.
     *
     * @param componentClass component class to introspect
     * @return list of {@link AnnotatedMethod}
     */
    public List<AnnotatedMethod<Subscribe>> getSubscribeMethods(Class<?> componentClass) {
        return componentIntrospectionCache.getUnchecked(componentClass).getSubscribeMethods();
    }

    /**
     * Introspects component class if it has never introspected before and
     * finds methods annotated by {@link Install} for autowiring.
     *
     * @param componentClass composite class to introspect
     * @return list of {@link AnnotatedMethod}
     */
    public List<AnnotatedMethod<Install>> getInstallMethods(Class<?> componentClass) {
        return componentIntrospectionCache.getUnchecked(componentClass).getInstallMethods();
    }

    /**
     * Introspects component class if it has never introspected before and
     * finds methods annotated by {@link Supply} for autowiring.
     *
     * @param componentClass composite class to introspect
     * @return list of {@link AnnotatedMethod}
     */
    public List<AnnotatedMethod<Supply>> getSupplyMethods(Class<?> componentClass) {
        return componentIntrospectionCache.getUnchecked(componentClass).getSupplyMethods();
    }

    /**
     * Introspects component class if it has never introspected before and
     * finds methods annotated by {@link EventListener} for autowiring.
     *
     * @param componentClass composite class to introspect
     * @return list of {@link Method}
     */
    public List<Method> getEventListenerMethods(Class<?> componentClass) {
        return componentIntrospectionCache.getUnchecked(componentClass).getEventListenerMethods();
    }

    /**
     * Introspects target class if it has never introspected before and
     * finds method suitable for adding an event handler of a specific type for autowiring.
     *
     * @param targetClass class to search for method
     * @param eventType   type of the target event parameter
     * @param methodName  name of the target method
     * @return the found {@link MethodHandle} or {@code null} if the method is not found
     */
    @Nullable
    public MethodHandle getTargetAddListenerMethod(Class<?> targetClass,
                                                   Class<?> eventType,
                                                   @Nullable String methodName) {
        Map<SubscribeMethodSignature, MethodHandle> methods =
                targetIntrospectionCache.getUnchecked(targetClass).getAddListenerMethods();
        return methods.get(new SubscribeMethodSignature(eventType, methodName, !Strings.isNullOrEmpty(methodName)));
    }

    /**
     * Introspects target class if it has never introspected before and
     * finds method with passed name which suitable for setting a handler for autowiring.
     *
     * @param targetClass class to search for method
     * @param methodName  name of the method
     * @return the found {@link MethodHandle} or {@code null} if the method is not found
     */
    @Nullable
    public MethodHandle getTargetInstallMethod(Class<?> targetClass, String methodName) {
        Map<String, MethodHandle> methods = targetIntrospectionCache.getUnchecked(targetClass).getInstallTargetMethods();
        return methods.get(methodName);
    }

    /**
     * Introspects target class if it has never introspected before and
     * finds method with passed name and parameter type which suitable for setting a handler for autowiring.
     *
     * @param targetClass   class to search for method
     * @param methodName    name of the method
     * @param parameterType parameter type of the parameter
     * @return the found {@link MethodHandle} or {@code null} if the method is not found
     */
    @Nullable
    public MethodHandle getTargetSupplyMethod(Class<?> targetClass, String methodName, Class<?> parameterType) {
        Map<SupplyMethodSignature, MethodHandle> methods =
                targetIntrospectionCache.getUnchecked(targetClass).getSupplyTargetMethods();
        return methods.get(new SupplyMethodSignature(methodName, List.of(parameterType)));
    }

    /**
     * Creates or gets from cache a method factory for consumer methods.
     *
     * @param ownerClass      owner class for target consumer
     * @param annotatedMethod annotated method
     * @param eventClass      event class
     * @return a factory to create consumer methods
     */
    public MethodHandle getConsumerMethodFactory(Class<?> ownerClass,
                                                 AnnotatedMethod<Subscribe> annotatedMethod,
                                                 Class<?> eventClass) {
        MethodHandle lambdaMethodFactory;
        MethodHandle methodHandle = annotatedMethod.getMethodHandle();
        try {
            lambdaMethodFactory = lambdaMethodsCache.get(methodHandle, () -> {
                MethodType type = MethodType.methodType(void.class, eventClass);
                MethodType consumerType = MethodType.methodType(Consumer.class, ownerClass);

                Class<?> callerClass = Modifier.isPrivate(annotatedMethod.getMethod().getModifiers())
                        ? annotatedMethod.getMethod().getDeclaringClass()
                        : ownerClass;

                MethodHandles.Lookup caller = lambdaLookupProvider.apply(callerClass);
                CallSite site;
                try {
                    site = LambdaMetafactory.metafactory(
                            caller, "accept", consumerType,
                            type.changeParameterType(0, Object.class), methodHandle, type
                    );
                } catch (LambdaConversionException e) {
                    throw new RuntimeException("Unable to build lambda consumer " + methodHandle, e);
                }

                return site.getTarget();
            });
        } catch (ExecutionException e) {
            throw new RuntimeException("Unable to get lambda factory", e);
        }

        return lambdaMethodFactory;
    }

    /**
     * Creates or gets from cache a method factory for value change event methods.
     *
     * @param ownerClass      owner class for target value change event method handle
     * @param annotatedMethod annotated method
     * @param eventClass      event class
     * @return a factory to create value change event method handle
     */
    public MethodHandle getValueChangeEventMethodFactory(Class<?> ownerClass,
                                                         AnnotatedMethod<Subscribe> annotatedMethod,
                                                         Class<?> eventClass) {
        MethodHandle lambdaMethodFactory;
        MethodHandle methodHandle = annotatedMethod.getMethodHandle();
        try {
            lambdaMethodFactory = lambdaMethodsCache.get(methodHandle, () -> {
                MethodType type = MethodType.methodType(void.class, eventClass);
                MethodType listenerType = MethodType.methodType(HasValue.ValueChangeListener.class, ownerClass);

                Class<?> callerClass = Modifier.isPrivate(annotatedMethod.getMethod().getModifiers())
                        ? annotatedMethod.getMethod().getDeclaringClass()
                        : ownerClass;

                MethodHandles.Lookup caller = lambdaLookupProvider.apply(callerClass);
                CallSite site;
                try {
                    site = LambdaMetafactory.metafactory(
                            caller, "valueChanged", listenerType,
                            type.changeParameterType(0, HasValue.ValueChangeEvent.class), methodHandle, type);
                } catch (LambdaConversionException e) {
                    throw new RuntimeException("Unable to build lambda consumer " + methodHandle, e);
                }

                return site.getTarget();
            });
        } catch (ExecutionException e) {
            throw new RuntimeException("Unable to get lambda factory", e);
        }

        return lambdaMethodFactory;
    }

    /**
     * Creates or gets from cache a method factory for component event listener methods.
     *
     * @param ownerClass      owner class for target component event listener method handle
     * @param annotatedMethod annotated method
     * @param eventClass      event class
     * @return a factory to create component event listener method handle
     */
    public MethodHandle getComponentEventListenerMethodFactory(Class<?> ownerClass,
                                                               AnnotatedMethod<Subscribe> annotatedMethod,
                                                               Class<?> eventClass) {
        MethodHandle lambdaMethodFactory;
        MethodHandle methodHandle = annotatedMethod.getMethodHandle();

        try {
            lambdaMethodFactory = lambdaMethodsCache.get(methodHandle, () -> {
                MethodType type = MethodType.methodType(void.class, eventClass);
                MethodType listenerType = MethodType.methodType(ComponentEventListener.class, ownerClass);

                Class<?> callerClass = Modifier.isPrivate(annotatedMethod.getMethod().getModifiers())
                        ? annotatedMethod.getMethod().getDeclaringClass()
                        : ownerClass;

                MethodHandles.Lookup caller = lambdaLookupProvider.apply(callerClass);
                CallSite site;
                try {
                    site = LambdaMetafactory.metafactory(
                            caller, "onComponentEvent", listenerType,
                            type.changeParameterType(0, ComponentEvent.class), methodHandle, type);
                } catch (LambdaConversionException e) {
                    throw new RuntimeException("Unable to build lambda consumer " + methodHandle, e);
                }

                return site.getTarget();
            });
        } catch (ExecutionException e) {
            throw new RuntimeException("Unable to get lambda factory", e);
        }

        return lambdaMethodFactory;
    }

    /**
     * Creates or gets from cache a method factory for any event listener methods.
     *
     * @param ownerClass          owner class for target event listener method handle
     * @param annotatedMethod     annotated method
     * @param interfaceMethodName name of the target interface method
     * @param listenerClass       listener class
     * @param eventClass          event class
     * @return a factory to create event listener method handle
     */
    public MethodHandle getEventListenerMethodFactory(Class<?> ownerClass,
                                                      AnnotatedMethod<Subscribe> annotatedMethod,
                                                      String interfaceMethodName,
                                                      Class<?> listenerClass,
                                                      Class<?> eventClass) {
        MethodHandle lambdaMethodFactory;
        MethodHandle methodHandle = annotatedMethod.getMethodHandle();

        try {
            lambdaMethodFactory = lambdaMethodsCache.get(methodHandle, () -> {
                MethodType type = MethodType.methodType(void.class, eventClass);
                MethodType listenerType = MethodType.methodType(listenerClass, ownerClass);

                Class<?> callerClass = Modifier.isPrivate(annotatedMethod.getMethod().getModifiers())
                        ? annotatedMethod.getMethod().getDeclaringClass()
                        : ownerClass;

                MethodHandles.Lookup caller = lambdaLookupProvider.apply(callerClass);
                CallSite site;
                try {
                    site = LambdaMetafactory.metafactory(
                            caller, interfaceMethodName, listenerType,
                            type, methodHandle, type);
                } catch (LambdaConversionException e) {
                    throw new RuntimeException("Unable to build lambda consumer " + methodHandle, e);
                }

                return site.getTarget();
            });
        } catch (ExecutionException e) {
            throw new RuntimeException("Unable to get lambda factory", e);
        }

        return lambdaMethodFactory;
    }

    /**
     * Clear underlying reflection caches.
     */
    public void clearCache() {
        componentIntrospectionCache.invalidateAll();
        targetIntrospectionCache.invalidateAll();

        lambdaMethodsCache.invalidateAll();
    }

    protected ComponentIntrospectionData getComponentIntrospectionDataNotCached(Class<?> viewClass) {
        Method[] methods = ReflectionUtils.getUniqueDeclaredMethods(viewClass);

        List<AutowireElement> autowireElements = getAnnotatedAutowireElementsNotCached(viewClass, methods);

        List<AnnotatedMethod<Subscribe>> subscribeMethods = getAnnotatedSubscribeMethodsNotCached(methods);
        List<AnnotatedMethod<Install>> installMethods = getAnnotatedInstallMethodsNotCached(methods);
        List<AnnotatedMethod<Supply>> supplyMethods = getAnnotatedSupplyMethodsNotCached(methods);

        List<Method> eventListenerMethods = getAnnotatedListenerMethodsNotCached(methods);

        return new ComponentIntrospectionData(
                autowireElements,
                subscribeMethods,
                installMethods,
                supplyMethods,
                eventListenerMethods
        );
    }

    protected TargetIntrospectionData getTargetIntrospectionDataNotCached(Class<?> targetClass) {
        Method[] methods = ReflectionUtils.getUniqueDeclaredMethods(targetClass);

        Map<SubscribeMethodSignature, MethodHandle> addListenerMethods = getAddListenerMethodsNotCached(methods);
        Map<String, MethodHandle> installTargetMethods = getInstallTargetMethodsNotCached(methods);
        Map<SupplyMethodSignature, MethodHandle> supplyTargetMethods = getSupplyTargetMethodsNotCached(methods);

        return new TargetIntrospectionData(
                addListenerMethods,
                installTargetMethods,
                supplyTargetMethods
        );
    }

    protected List<AutowireElement> getAnnotatedAutowireElementsNotCached(Class<?> componentClass, Method[] methods) {
        Map<AnnotatedElement, Class<?>> toAutowire = Collections.emptyMap(); // lazily initialized

        List<Class<?>> classes = ClassUtils.getAllSuperclasses(componentClass);
        classes.add(0, componentClass);
        Collections.reverse(classes);

        for (Field field : getAllFields(classes)) {
            Class<?> aClass = getAutowiringAnnotationClass(field);
            if (aClass != null) {
                if (toAutowire.isEmpty()) {
                    toAutowire = new HashMap<>();
                }
                field.trySetAccessible();

                toAutowire.put(field, aClass);
            }
        }

        for (Method method : methods) {
            Class<?> aClass = getAutowiringAnnotationClass(method);
            if (aClass != null) {
                if (toAutowire.isEmpty()) {
                    toAutowire = new HashMap<>();
                }
                method.trySetAccessible();

                toAutowire.put(method, aClass);
            }
        }

        return toAutowire.entrySet().stream()
                .map(entry -> new AutowireElement(entry.getKey(), entry.getValue()))
                .collect(ImmutableList.toImmutableList());
    }

    protected List<AnnotatedMethod<Subscribe>> getAnnotatedSubscribeMethodsNotCached(Method[] uniqueDeclaredMethods) {
        List<AnnotatedMethod<Subscribe>> annotatedMethods =
                getAnnotatedMethodsNotCached(Subscribe.class, uniqueDeclaredMethods,
                        m -> m.getParameterCount() == 1
                                && EventObject.class.isAssignableFrom(m.getParameterTypes()[0]));

        annotatedMethods.sort(AutowireUtils::compareMethods);

        return ImmutableList.copyOf(annotatedMethods);
    }

    protected List<AnnotatedMethod<Install>> getAnnotatedInstallMethodsNotCached(Method[] uniqueDeclaredMethods) {
        List<AnnotatedMethod<Install>> annotatedMethods =
                getAnnotatedMethodsNotCached(Install.class, uniqueDeclaredMethods, method -> true);
        return ImmutableList.copyOf(annotatedMethods);
    }

    protected List<AnnotatedMethod<Supply>> getAnnotatedSupplyMethodsNotCached(Method[] uniqueDeclaredMethods) {
        List<AnnotatedMethod<Supply>> annotatedMethods =
                getAnnotatedMethodsNotCached(Supply.class, uniqueDeclaredMethods,
                        method -> method.getParameterCount() == 0);
        return ImmutableList.copyOf(annotatedMethods);
    }

    protected List<Method> getAnnotatedListenerMethodsNotCached(Method[] uniqueDeclaredMethods) {
        return Arrays.stream(uniqueDeclaredMethods)
                .filter(m -> findMergedAnnotation(m, EventListener.class) != null)
                .peek(AccessibleObject::trySetAccessible)
                .collect(ImmutableList.toImmutableList());
    }

    protected <T extends Annotation> List<AnnotatedMethod<T>> getAnnotatedMethodsNotCached(Class<T> annotationClass,
                                                                                           Method[] uniqueDeclaredMethods,
                                                                                           Predicate<Method> filter) {
        List<AnnotatedMethod<T>> annotatedMethods = new ArrayList<>();
        for (Method method : uniqueDeclaredMethods) {
            if (filter.test(method)) {
                AnnotatedMethod<T> annotatedMethod = AutowireUtils.createAnnotatedMethod(annotationClass, method);
                if (annotatedMethod != null) {
                    annotatedMethods.add(annotatedMethod);
                }
            }
        }

        return annotatedMethods;
    }

    protected Map<SubscribeMethodSignature, MethodHandle> getAddListenerMethodsNotCached(Method[] uniqueDeclaredMethods) {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        Map<SubscribeMethodSignature, MethodHandle> subscriptionMethods = new HashMap<>();

        for (Method m : uniqueDeclaredMethods) {
            if (m.getParameterCount() == 1
                    && (Consumer.class.isAssignableFrom(m.getParameterTypes()[0])
                    || ComponentEventListener.class.isAssignableFrom(m.getParameterTypes()[0])
                    || HasValue.ValueChangeListener.class.isAssignableFrom(m.getParameterTypes()[0]))) {
                // setXxxListener or addXxxListener
                if (m.getReturnType() == Void.TYPE && m.getName().startsWith("set")
                        || (m.getReturnType() == Registration.class && m.getName().startsWith("add"))
                        || (m.getReturnType() == Subscription.class && m.getName().startsWith("add"))) {

                    Method targetTypedMethod = m;
                    if (!(m.getGenericParameterTypes()[0] instanceof ParameterizedType)) {
                        // try to find original method in hierarchy with defined Consumer<T> parameter

                        Set<Method> overrideHierarchy = AutowireUtils.getOverrideHierarchy(m);
                        Method originalMethod = Iterables.getLast(overrideHierarchy);

                        if (originalMethod.getGenericParameterTypes()[0] instanceof ParameterizedType) {
                            targetTypedMethod = originalMethod;
                        } else {
                            continue;
                        }
                    }

                    Class<?> actualTypeArgument = getActualTypeArgument(targetTypedMethod);
                    if (actualTypeArgument != null) {
                        m.trySetAccessible();

                        MethodHandle mh;
                        try {
                            mh = lookup.unreflect(m);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException("Unable to use subscription method " + m, e);
                        }

                        SubscribeMethodSignature subscribeMethodSignature = new SubscribeMethodSignature(
                                actualTypeArgument,
                                targetTypedMethod.getName()
                        );
                        subscriptionMethods.put(subscribeMethodSignature, mh);
                    }
                }
            }
        }

        return ImmutableMap.copyOf(subscriptionMethods);
    }

    protected Map<String, MethodHandle> getInstallTargetMethodsNotCached(Method[] uniqueDeclaredMethods) {
        Map<String, MethodHandle> handlesMap = new HashMap<>();
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        for (Method m : uniqueDeclaredMethods) {
            if (Modifier.isPublic(m.getModifiers())
                    && m.getParameterCount() == 1
                    && (m.getName().startsWith("set") || m.getName().startsWith("add"))) {

                Class<?> parameterType = m.getParameterTypes()[0];

                if (Consumer.class.isAssignableFrom(parameterType)
                        || Supplier.class.isAssignableFrom(parameterType)
                        || Function.class.isAssignableFrom(parameterType)
                        || parameterType.isInterface()) {

                    m.trySetAccessible();

                    MethodHandle methodHandle;
                    try {
                        methodHandle = lookup.unreflect(m);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("unable to get method handle " + m);
                    }

                    handlesMap.put(m.getName(), methodHandle);
                }
            }
        }

        return ImmutableMap.copyOf(handlesMap);
    }

    protected Map<SupplyMethodSignature, MethodHandle> getSupplyTargetMethodsNotCached(Method[] uniqueDeclaredMethods) {
        Map<SupplyMethodSignature, MethodHandle> handlesMap = new HashMap<>();
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        for (Method m : uniqueDeclaredMethods) {
            if (Modifier.isPublic(m.getModifiers())
                    && m.getParameterCount() == 1
                    && m.getName().startsWith("set")
                    && (m.getName().contains("Renderer") || m.getName().contains("EditorComponent"))) {

                m.trySetAccessible();
                MethodHandle methodHandle;
                try {
                    methodHandle = lookup.unreflect(m);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("unable to get method handle " + m);
                }
                SupplyMethodSignature supplyMethodSignature =
                        new SupplyMethodSignature(m.getName(), Arrays.asList(m.getParameterTypes()));
                handlesMap.put(supplyMethodSignature, methodHandle);
            }
        }

        return ImmutableMap.copyOf(handlesMap);
    }

    @Nullable
    protected Class<?> getAutowiringAnnotationClass(AnnotatedElement element) {
        // TODO: kd, remove after major release
        //  deprecated function,
        //  the @ViewComponent should be the only annotation for the fields that is processed by the framework.
        //  currently, @Autowired is only needed for messageBundle bean. not removed for backward compatibility.
        if (element.isAnnotationPresent(Autowired.class)) {
            return Autowired.class;
        }

        if (element.isAnnotationPresent(ViewComponent.class)) {
            return ViewComponent.class;
        }

        return null;
    }

    protected List<Field> getAllFields(List<Class<?>> classes) {
        List<Field> list = new ArrayList<>();

        for (Class<?> c : classes) {
            if (c != Object.class) {
                Collections.addAll(list, c.getDeclaredFields());
            }
        }
        return list;
    }

    @Nullable
    protected Class<?> getActualTypeArgument(Method targetTypedMethod) {
        ParameterizedType genericParameterType = (ParameterizedType) targetTypedMethod.getGenericParameterTypes()[0];
        Type eventArgumentType = genericParameterType.getActualTypeArguments()[0];

        Class<?> actualTypeArgument = null;
        if (eventArgumentType instanceof Class) {
            // case of plain ClickEvent
            actualTypeArgument = (Class<?>) eventArgumentType;
        } else if (eventArgumentType instanceof ParameterizedType parameterizedType) {
            // case of ValueChangeEvent<V>
            actualTypeArgument = (Class<?>) parameterizedType.getRawType();
        } else if (eventArgumentType instanceof WildcardType wildcardType
                && wildcardType.getLowerBounds()[0] instanceof ParameterizedType parameterizedType) {
            actualTypeArgument = (Class<?>) parameterizedType.getRawType();
        }
        return actualTypeArgument;
    }

    @SuppressWarnings("ClassCanBeRecord")
    public static class ComponentIntrospectionData {

        protected final List<AutowireElement> autowireElements;

        protected final List<AnnotatedMethod<Subscribe>> subscribeMethods;
        protected final List<AnnotatedMethod<Install>> installMethods;
        protected final List<AnnotatedMethod<Supply>> supplyMethods;

        protected final List<Method> eventListenerMethods;

        public ComponentIntrospectionData(List<AutowireElement> autowireElements,
                                          List<AnnotatedMethod<Subscribe>> subscribeMethods,
                                          List<AnnotatedMethod<Install>> installMethods,
                                          List<AnnotatedMethod<Supply>> supplyMethods,
                                          List<Method> eventListenerMethods) {
            this.autowireElements = autowireElements;
            this.subscribeMethods = subscribeMethods;
            this.installMethods = installMethods;
            this.supplyMethods = supplyMethods;
            this.eventListenerMethods = eventListenerMethods;
        }

        public List<AutowireElement> getAutowireElements() {
            return autowireElements;
        }

        public List<AnnotatedMethod<Subscribe>> getSubscribeMethods() {
            return subscribeMethods;
        }

        public List<AnnotatedMethod<Install>> getInstallMethods() {
            return installMethods;
        }

        public List<AnnotatedMethod<Supply>> getSupplyMethods() {
            return supplyMethods;
        }

        public List<Method> getEventListenerMethods() {
            return eventListenerMethods;
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    public static class TargetIntrospectionData {

        protected final Map<SubscribeMethodSignature, MethodHandle> addListenerMethods;
        protected final Map<String, MethodHandle> installTargetMethods;
        protected final Map<SupplyMethodSignature, MethodHandle> supplyTargetMethods;

        public TargetIntrospectionData(Map<SubscribeMethodSignature, MethodHandle> addListenerMethods,
                                       Map<String, MethodHandle> installTargetMethods,
                                       Map<SupplyMethodSignature, MethodHandle> supplyTargetMethods) {
            this.addListenerMethods = addListenerMethods;
            this.installTargetMethods = installTargetMethods;
            this.supplyTargetMethods = supplyTargetMethods;
        }

        public Map<SubscribeMethodSignature, MethodHandle> getAddListenerMethods() {
            return addListenerMethods;
        }

        public Map<String, MethodHandle> getInstallTargetMethods() {
            return installTargetMethods;
        }

        public Map<SupplyMethodSignature, MethodHandle> getSupplyTargetMethods() {
            return supplyTargetMethods;
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    public static class AutowireElement {

        protected final AnnotatedElement element;
        protected final Class<?> annotationClass;

        public AutowireElement(AnnotatedElement element, Class<?> annotationClass) {
            this.element = element;
            this.annotationClass = annotationClass;
        }

        public AnnotatedElement getElement() {
            return element;
        }

        public Class<?> getAnnotationClass() {
            return annotationClass;
        }

        @Override
        public String toString() {
            return "%s{element=%s, annotationClass=%s}".formatted(
                    getClass().getSimpleName(),
                    element,
                    annotationClass
            );
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    public static class AnnotatedMethod<T> {

        private final T annotation;
        private final Method method;
        private final MethodHandle methodHandle;

        public AnnotatedMethod(T annotation, Method method, MethodHandle methodHandle) {
            this.annotation = annotation;
            this.method = method;
            this.methodHandle = methodHandle;
        }

        public T getAnnotation() {
            return annotation;
        }

        public Method getMethod() {
            return method;
        }

        public MethodHandle getMethodHandle() {
            return methodHandle;
        }

        @Override
        public String toString() {
            return "%s{annotation=%s, method=%s}".formatted(
                    getClass().getSimpleName(),
                    annotation,
                    method
            );
        }
    }

    public static class SubscribeMethodSignature {

        protected final Class<?> eventType;
        protected final String name;
        protected boolean nameMatchingRequired = true;

        public SubscribeMethodSignature(Class<?> eventType, @Nullable String name) {
            this.eventType = eventType;
            this.name = name;
        }

        public SubscribeMethodSignature(Class<?> eventType, @Nullable String name, boolean nameMatchingRequired) {
            this.eventType = eventType;
            this.name = name;
            this.nameMatchingRequired = nameMatchingRequired;
        }

        public Class<?> getEventType() {
            return eventType;
        }

        @Nullable
        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SubscribeMethodSignature that = (SubscribeMethodSignature) o;
            return Objects.equals(eventType, that.eventType) &&
                    (!nameMatchingRequired || !that.nameMatchingRequired || Objects.equals(name, that.name));
        }

        @Override
        public int hashCode() {
            return Objects.hash(eventType);
        }

        @Override
        public String toString() {
            return "%s{eventType=%s, name=%s}".formatted(
                    getClass().getSimpleName(),
                    eventType.getSimpleName(),
                    name
            );
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    public static class SupplyMethodSignature {

        protected final String name;
        protected final List<Class<?>> parameters;

        public SupplyMethodSignature(String name, List<Class<?>> parameters) {
            this.name = name;
            this.parameters = parameters;
        }

        public String getName() {
            return name;
        }

        public List<Class<?>> getParameters() {
            return parameters;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SupplyMethodSignature that = (SupplyMethodSignature) o;
            return Objects.equals(name, that.name) && Objects.equals(parameters, that.parameters);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, parameters);
        }

        @Override
        public String toString() {
            return "%s{name=%s, parameters=(%s)}".formatted(
                    getClass().getSimpleName(),
                    name,
                    parameters
            );
        }
    }
}
