package io.jmix.flowui.sys;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.common.event.Subscription;
import io.jmix.flowui.screen.ComponentId;
import io.jmix.flowui.screen.Install;
import io.jmix.flowui.screen.Screen;
import io.jmix.flowui.screen.Subscribe;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;

@Component("flowui_UiControllerReflectionInspector")
public class UiControllerReflectionInspector {

    protected final LoadingCache<Class<?>, ScreenIntrospectionData> screenIntrospectionCache =
            CacheBuilder.newBuilder()
                    .weakKeys()
                    .build(CacheLoader.from(this::getScreenIntrospectionDataNotCached));

    protected final LoadingCache<Class<?>, TargetIntrospectionData> targetIntrospectionCache =
            CacheBuilder.newBuilder()
                    .weakKeys()
                    .build(CacheLoader.from(this::getTargetIntrospectionDataNotCached));

    // key - method of FrameOwner, value - lambda factory that produces Consumer instances
    protected final Cache<MethodHandle, MethodHandle> lambdaMethodsCache =
            CacheBuilder.newBuilder()
                    .weakKeys()
                    .build();

    protected final Function<Class, MethodHandles.Lookup> lambdaLookupProvider;

    public UiControllerReflectionInspector() {
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

        if (privateLookupInMhFinal == null) {
            // Java 8
            MethodHandles.Lookup trusted;
            try {
                Field internal = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
                internal.setAccessible(true);
                trusted = (MethodHandles.Lookup) internal.get(original);

            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new IllegalStateException("MethodHandles.Lookup IMPL_LOOKUP is not available", e);
            }

            this.lambdaLookupProvider = trusted::in;
        } else {
            // Java 10+
            this.lambdaLookupProvider = clazz -> {
                try {
                    return (MethodHandles.Lookup) privateLookupInMhFinal.invokeExact(clazz, original);
                } catch (Error e) {
                    throw e;
                } catch (Throwable t) {
                    throw new RuntimeException("Unable to get private lookup in class " + clazz, t);
                }
            };
        }
    }

    /**
     * Introspects screen class and finds annotated fields and methods for dependency injection.
     *
     * @param clazz screen class
     * @return screen data
     */
    public ScreenIntrospectionData getScreenIntrospectionData(Class<?> clazz) {
        return screenIntrospectionCache.getUnchecked(clazz);
    }

    @Nullable
    public MethodHandle getAddListenerMethod(Class<?> clazz, Class<?> eventType) {
        Map<Class, MethodHandle> methods = targetIntrospectionCache.getUnchecked(clazz).getAddListenerMethods();
        return methods.get(eventType);
    }

    @Nullable
    public MethodHandle getInstallTargetMethod(Class<?> clazz, String methodName) {
        Map<String, MethodHandle> methods = targetIntrospectionCache.getUnchecked(clazz).getInstallTargetMethods();
        return methods.get(methodName);
    }

    public MethodHandle getConsumerMethodFactory(Class<?> ownerClass, AnnotatedMethod annotatedMethod, Class<?> eventClass) {
        MethodHandle lambdaMethodFactory;
        MethodHandle methodHandle = annotatedMethod.getMethodHandle();
        try {
            lambdaMethodFactory = lambdaMethodsCache.get(methodHandle, () -> {
                MethodType type = MethodType.methodType(void.class, eventClass);
                MethodType consumerType = MethodType.methodType(Consumer.class, ownerClass);

                Class<?> callerClass;
                if (Modifier.isPrivate(annotatedMethod.getMethod().getModifiers())) {
                    callerClass = annotatedMethod.getMethod().getDeclaringClass();
                } else {
                    callerClass = ownerClass;
                }

                MethodHandles.Lookup caller = lambdaLookupProvider.apply(callerClass);
                CallSite site;
                try {
                    site = LambdaMetafactory.metafactory(
                            caller, "accept", consumerType, type.changeParameterType(0, Object.class), methodHandle, type);
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

    public MethodHandle getComponentEventListenerMethodFactory(Class<?> ownerClass,
                                                               AnnotatedMethod annotatedMethod, Class<?> eventClass) {
        MethodHandle lambdaMethodFactory;
        MethodHandle methodHandle = annotatedMethod.getMethodHandle();
        try {
            lambdaMethodFactory = lambdaMethodsCache.get(methodHandle, () -> {
                MethodType type = MethodType.methodType(void.class, eventClass);
                MethodType listenerType = MethodType.methodType(ComponentEventListener.class, ownerClass);

                Class<?> callerClass;
                if (Modifier.isPrivate(annotatedMethod.getMethod().getModifiers())) {
                    callerClass = annotatedMethod.getMethod().getDeclaringClass();
                } else {
                    callerClass = ownerClass;
                }

                MethodHandles.Lookup caller = lambdaLookupProvider.apply(callerClass);
                CallSite site;
                try {
                    site = LambdaMetafactory.metafactory(
                            caller, "onComponentEvent", listenerType,
                            type.changeParameterType(0, ComponentEvent.class), methodHandle, type);
                    /*site = LambdaMetafactory.metafactory(
                            caller, "accept", listenerType, type.changeParameterType(0, Object.class), methodHandle, type);*/
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
        screenIntrospectionCache.invalidateAll();
        targetIntrospectionCache.invalidateAll();

        lambdaMethodsCache.invalidateAll();
    }

    protected ScreenIntrospectionData getScreenIntrospectionDataNotCached(Class<?> concreteClass) {
        Method[] methods = ReflectionUtils.getUniqueDeclaredMethods(concreteClass);

        List<InjectElement> injectElements = getAnnotatedInjectElementsNotCached(concreteClass);
        List<AnnotatedMethod<Subscribe>> subscribeMethods = getAnnotatedSubscribeMethodsNotCached(methods);
        List<AnnotatedMethod<Install>> installMethods = getAnnotatedInstallMethodsNotCached(methods);
//        List<Method> eventListenerMethods = getAnnotatedListenerMethodsNotCached(concreteClass, methods);
//        List<Method> propertySetters = getPropertySettersNotCached(methods);

        return new ScreenIntrospectionData(injectElements, /*eventListenerMethods,*/ subscribeMethods, installMethods/*, propertySetters*/);
    }

    protected TargetIntrospectionData getTargetIntrospectionDataNotCached(Class<?> concreteClass) {
        Method[] methods = ReflectionUtils.getUniqueDeclaredMethods(concreteClass);

        Map<Class, MethodHandle> addListenerMethods = getAddListenerMethodsNotCached(concreteClass, methods);
        Map<String, MethodHandle> installTargetMethods = getInstallTargetMethodsNotCached(concreteClass, methods);

        return new TargetIntrospectionData(addListenerMethods, installTargetMethods);
    }

    protected List<InjectElement> getAnnotatedInjectElementsNotCached(Class<?> clazz) {
        Map<AnnotatedElement, Class> toInject = Collections.emptyMap(); // lazily initialized

        List<Class<?>> classes = ClassUtils.getAllSuperclasses(clazz);
        classes.add(0, clazz);
        Collections.reverse(classes);

        for (Field field : getAllFields(classes)) {
            Class aClass = injectionAnnotation(field);
            if (aClass != null) {
                if (toInject.isEmpty()) {
                    toInject = new HashMap<>();
                }
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                toInject.put(field, aClass);
            }
        }

        for (Method method : ReflectionUtils.getUniqueDeclaredMethods(clazz)) {
            Class aClass = injectionAnnotation(method);
            if (aClass != null) {
                if (toInject.isEmpty()) {
                    toInject = new HashMap<>();
                }
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }

                toInject.put(method, aClass);
            }
        }

        return toInject.entrySet().stream()
                .map(entry -> new InjectElement(entry.getKey(), entry.getValue()))
                .collect(ImmutableList.toImmutableList());
    }

    protected List<Field> getAllFields(List<Class<?>> classes) {
        List<Field> list = new ArrayList<>();

        for (Class c : classes) {
            if (c != Object.class) {
                Collections.addAll(list, c.getDeclaredFields());
            }
        }
        return list;
    }

    @Nullable
    protected Class injectionAnnotation(AnnotatedElement element) {
        /*if (element.isAnnotationPresent(Named.class)) {
            return Named.class;
        }

        if (element.isAnnotationPresent(Resource.class)) {
            return Resource.class;
        }

        if (element.isAnnotationPresent(Inject.class)) {
            return Inject.class;
        }

        if (element.isAnnotationPresent(Autowired.class)) {
            return Autowired.class;
        }

        if (element.isAnnotationPresent(WindowParam.class)) {
            return WindowParam.class;
        }*/

        if (element.isAnnotationPresent(ComponentId.class)) {
            return ComponentId.class;
        }

        return null;
    }

    protected List<AnnotatedMethod<Install>> getAnnotatedInstallMethodsNotCached(Method[] uniqueDeclaredMethods) {
        List<AnnotatedMethod<Install>> annotatedMethods = new ArrayList<>();

        for (Method m : uniqueDeclaredMethods) {
            AnnotatedMethod<Install> annotatedMethod = createAnnotatedMethod(Install.class, m);
            if (annotatedMethod != null) {
                annotatedMethods.add(annotatedMethod);
            }
        }

        return ImmutableList.copyOf(annotatedMethods);
    }

    protected List<AnnotatedMethod<Subscribe>> getAnnotatedSubscribeMethodsNotCached(Method[] uniqueDeclaredMethods) {
        List<AnnotatedMethod<Subscribe>> annotatedMethods = new ArrayList<>();

        for (Method m : uniqueDeclaredMethods) {
            if (m.getParameterCount() == 1 && EventObject.class.isAssignableFrom(m.getParameterTypes()[0])) {
                AnnotatedMethod<Subscribe> annotatedMethod = createAnnotatedMethod(Subscribe.class, m);
                if (annotatedMethod != null) {
                    annotatedMethods.add(annotatedMethod);
                }
            }
        }

        annotatedMethods.sort(this::compareSubscribeMethods);

        return ImmutableList.copyOf(annotatedMethods);
    }

    @Nullable
    protected <A extends Annotation> AnnotatedMethod<A> createAnnotatedMethod(Class<A> annotationClass, Method method) {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        A annotation = findMergedAnnotation(method, annotationClass);
        if (annotation != null) {
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
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

    protected int compareSubscribeMethods(AnnotatedMethod<Subscribe> am1, AnnotatedMethod<Subscribe> am2) {
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

    protected Class<?> getDeclaringClass(Method method) {
        Class<?> declaringClass = method.getDeclaringClass();
        if (declaringClass.getSuperclass() == Screen.class
            /*|| declaringClass.getSuperclass() == ScreenFragment.class*/) {
            // speed up search of declaring class for simple cases
            return declaringClass;
        }

        Set<Method> overrideHierarchy = getOverrideHierarchy(method);
        return Iterables.getLast(overrideHierarchy).getDeclaringClass();
    }

    protected Set<Method> getOverrideHierarchy(Method method) {
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
            Map<TypeVariable<?>, Type> typeArguments = TypeUtils.getTypeArguments(declaringClass, m.getDeclaringClass());
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

    protected Map<Class, MethodHandle> getAddListenerMethodsNotCached(Class<?> clazz,
                                                                      Method[] uniqueDeclaredMethods) {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        Map<Class, MethodHandle> subscriptionMethods = new HashMap<>();

        for (Method m : uniqueDeclaredMethods) {
            if (m.getParameterCount() == 1
                    && (Consumer.class.isAssignableFrom(m.getParameterTypes()[0])
                    || ComponentEventListener.class.isAssignableFrom(m.getParameterTypes()[0]))) {
                // setXxxListener or addXxxListener
                if (m.getReturnType() == Void.TYPE && m.getName().startsWith("set")
                        || (m.getReturnType() == Registration.class && m.getName().startsWith("add"))
                        || (m.getReturnType() == Subscription.class && m.getName().startsWith("add"))) {

                    Method targetTypedMethod = m;
                    if (!(m.getGenericParameterTypes()[0] instanceof ParameterizedType)) {
                        // try to find original method in hierarchy with defined Consumer<T> parameter

                        Set<Method> overrideHierarchy = getOverrideHierarchy(m);
                        Method originalMethod = Iterables.getLast(overrideHierarchy);

                        if (originalMethod.getGenericParameterTypes()[0] instanceof ParameterizedType) {
                            targetTypedMethod = originalMethod;
                        } else {
                            continue;
                        }
                    }

                    ParameterizedType genericParameterType = (ParameterizedType) targetTypedMethod.getGenericParameterTypes()[0];
                    Type eventArgumentType = genericParameterType.getActualTypeArguments()[0];

                    Class actualTypeArgument = null;
                    if (eventArgumentType instanceof Class) {
                        // case of plain ClickEvent
                        actualTypeArgument = (Class) eventArgumentType;
                    } else if (eventArgumentType instanceof ParameterizedType) {
                        // case of ValueChangeEvent<V>
                        actualTypeArgument = (Class) ((ParameterizedType) eventArgumentType).getRawType();
                    }

                    if (actualTypeArgument != null) {
                        if (!m.isAccessible()) {
                            m.setAccessible(true);
                        }

                        MethodHandle mh;
                        try {
                            mh = lookup.unreflect(m);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException("Unable to use subscription method " + m, e);
                        }
                        subscriptionMethods.put(actualTypeArgument, mh);
                    }
                }
            }
        }

        return ImmutableMap.copyOf(subscriptionMethods);
    }

    protected Map<String, MethodHandle> getInstallTargetMethodsNotCached(Class<?> clazz,
                                                                         Method[] uniqueDeclaredMethods) {
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

                    if (!m.isAccessible()) {
                        m.setAccessible(true);
                    }
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

    public static class InjectElement {
        protected final AnnotatedElement element;
        protected final Class annotationClass;

        public InjectElement(AnnotatedElement element, Class annotationClass) {
            this.element = element;
            this.annotationClass = annotationClass;
        }

        public AnnotatedElement getElement() {
            return element;
        }

        public Class getAnnotationClass() {
            return annotationClass;
        }

        @Override
        public String toString() {
            return "InjectElement{" +
                    "element=" + element +
                    ", annotationClass=" + annotationClass +
                    '}';
        }
    }

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
            return "AnnotatedMethod{" +
                    "annotation=" + annotation +
                    ", method=" + method +
                    '}';
        }
    }

    public static class ScreenIntrospectionData {
        private final List<InjectElement> injectElements;

//        private final List<Method> eventListenerMethods;

        private final List<AnnotatedMethod<Subscribe>> subscribeMethods;
        private final List<AnnotatedMethod<Install>> installMethods;

//        private final List<Method> propertySetters;

        public ScreenIntrospectionData(List<InjectElement> injectElements,
                /*List<Method> eventListenerMethods,*/
                                       List<AnnotatedMethod<Subscribe>> subscribeMethods,
                                       List<AnnotatedMethod<Install>> installMethods
                /*List<Method> propertySetters*/) {
            this.injectElements = injectElements;
//            this.eventListenerMethods = eventListenerMethods;
            this.subscribeMethods = subscribeMethods;
            this.installMethods = installMethods;
//            this.propertySetters = propertySetters;
        }

        public List<InjectElement> getInjectElements() {
            return injectElements;
        }

        public List<AnnotatedMethod<Subscribe>> getSubscribeMethods() {
            return subscribeMethods;
        }

        public List<AnnotatedMethod<Install>> getInstallMethods() {
            return installMethods;
        }
    }

    public static class TargetIntrospectionData {
        private final Map<Class, MethodHandle> addListenerMethods;

        private final Map<String, MethodHandle> installTargetMethods;

        public TargetIntrospectionData(Map<Class, MethodHandle> addListenerMethods,
                                       Map<String, MethodHandle> installTargetMethods) {
            this.addListenerMethods = addListenerMethods;
            this.installTargetMethods = installTargetMethods;
        }

        public Map<Class, MethodHandle> getAddListenerMethods() {
            return addListenerMethods;
        }

        public Map<String, MethodHandle> getInstallTargetMethods() {
            return installTargetMethods;
        }
    }
}
