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
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.data.selection.SelectionListener;
import io.jmix.core.DevelopmentException;
import io.jmix.core.JmixOrder;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.sys.ViewDescriptorUtils;
import io.jmix.flowui.sys.autowire.ReflectionCacheManager.AnnotatedMethod;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.Target;
import io.jmix.flowui.view.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.EventObject;
import java.util.List;

import static io.jmix.flowui.view.Target.COMPONENT;

/**
 * An injector that autowires {@link SelectionListener} methods that are annotated by the {@link Subscribe} annotation.
 * A special dependency injector is required because {@link SelectionEvent} is not an inheritor of {@link EventObject}.
 */
@Order(JmixOrder.LOWEST_PRECEDENCE - 110)
@Component("flowui_SelectionListenerDependencyInjector")
public class SelectionListenerDependencyInjector implements DependencyInjector {

    private static final Logger log = LoggerFactory.getLogger(SelectionListenerDependencyInjector.class);

    protected ReflectionCacheManager reflectionCacheManager;

    protected final LoadingCache<Class<?>, List<AnnotatedMethod<Subscribe>>> selectionListenerIntrospectionCache =
            CacheBuilder.newBuilder()
                    .weakKeys()
                    .build(CacheLoader.from(this::getSelectionListenersMethodsNotCached));

    protected SelectionListenerDependencyInjector(ReflectionCacheManager reflectionCacheManager) {
        this.reflectionCacheManager = reflectionCacheManager;
    }

    @Override
    public void autowire(AutowireContext<?> autowireContext) {
        Composite<?> composite = autowireContext.getTarget();
        //noinspection rawtypes
        Class<? extends Composite> compositeClass = composite.getClass();

        List<AnnotatedMethod<Subscribe>> selectionListenersMethods = getSelectionListenersMethods(compositeClass);
        Collection<Object> autowired = autowireContext.getAutowired();

        for (AnnotatedMethod<Subscribe> annotatedMethod : selectionListenersMethods) {
            if (!autowired.contains(annotatedMethod)) {
                doAutowiring(annotatedMethod, composite, compositeClass, autowired);
            }
        }
    }

    protected void doAutowiring(AnnotatedMethod<Subscribe> annotatedMethod,
                                Composite<?> composite,
                                @SuppressWarnings("rawtypes")
                                Class<? extends Composite> compositeClass,
                                Collection<Object> autowired) {
        Method method = annotatedMethod.getMethod();
        Parameter parameter = method.getParameters()[0];
        Class<?> eventType = parameter.getType();
        Subscribe annotation = annotatedMethod.getAnnotation();

        String targetId = ViewDescriptorUtils.getInferredSubscribeId(annotation);


        if (Strings.isNullOrEmpty(targetId)
                || !COMPONENT.equals(annotation.target())
                || !(SelectionEvent.class.isAssignableFrom(eventType))) {
            return;
        }

        Object eventTarget = getEventTarget(annotation, composite);

        if (eventTarget == null) {
            if (annotation.required()) {
                throw new DevelopmentException(String.format("Unable to find @%s targetId %s in %s",
                        Subscribe.class.getSimpleName(), targetId, compositeClass.getSimpleName()));
            }

            log.trace("Skip @{} method {} of {} : it is not required and targetId not found",
                    Subscribe.class.getSimpleName(), annotatedMethod.getMethod().getName(), compositeClass);

            return;
        }

        MethodHandle addListenerMethod = getTargetAddListenerMethod(eventTarget.getClass());
        if (addListenerMethod == null) {
            throw new DevelopmentException(String.format("Target %s does not support event type %s",
                    eventTarget.getClass().getName(), eventType));
        }

        SelectionListener<?, ?> listener = getSelectionEventListener(getClass(), composite, annotatedMethod, eventType);

        try {
            addListenerMethod.invoke(eventTarget, listener);
        } catch (Error e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException("Unable to add listener %s".formatted(method), e);
        }

        autowired.add(annotatedMethod);
    }

    @Override
    public boolean isApplicable(AutowireContext<?> autowireContext) {
        return autowireContext instanceof ViewAutowireContext
                || autowireContext instanceof FragmentAutowireContext;
    }

    @Nullable
    protected Object getEventTarget(Subscribe annotation, Composite<?> composite) {
        String inferredSubscribeId = ViewDescriptorUtils.getInferredSubscribeId(annotation);
        Target targetType = annotation.target();

        return composite instanceof Fragment<?>
                ? AutowireUtils.getFragmentTargetInstance(annotation, (Fragment<?>) composite,
                inferredSubscribeId, targetType)
                : AutowireUtils.getViewSubscribeTargetInstance(((View<?>) composite),
                inferredSubscribeId, targetType);
    }

    @Nullable
    protected MethodHandle getTargetAddListenerMethod(Class<?> targetClass) {
        Method[] uniqueDeclaredMethods = ReflectionUtils.getUniqueDeclaredMethods(targetClass, m ->
                m.getParameterCount() == 1
                        && SelectionListener.class.isAssignableFrom(m.getParameterTypes()[0]));
        if (uniqueDeclaredMethods.length != 1) {
            return null;
        }

        Method method = uniqueDeclaredMethods[0];
        MethodHandle methodHandle;

        try {
            methodHandle = MethodHandles.lookup().unreflect(method);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to use subscription method %s".formatted(method), e);
        }

        return methodHandle;
    }

    protected SelectionListener<?, ?> getSelectionEventListener(Class<?> callerClass,
                                                                com.vaadin.flow.component.Component component,
                                                                AnnotatedMethod<Subscribe> annotatedMethod,
                                                                Class<?> eventType) {
        SelectionListener<?, ?> listener;

        // If component class was hot-deployed, then it will be loaded
        // by different class loader. This will make impossible to create lambda
        // using LambdaMetaFactory for producing the listener method in Java 17+
        if (callerClass.getClassLoader() == component.getClass().getClassLoader()) {
            MethodHandle consumerMethodFactory = reflectionCacheManager.getEventListenerMethodFactory(
                    component.getClass(), annotatedMethod,
                    "selectionChange", SelectionListener.class, eventType
            );

            try {
                listener = (SelectionListener<?, ?>) consumerMethodFactory.invoke(component);
            } catch (Error e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException(String.format("Unable to bind %s handler",
                        SelectionListener.class.getSimpleName()), e);
            }
        } else {
            listener = event -> {
                try {
                    annotatedMethod.getMethodHandle().invoke(component, event);
                } catch (Throwable e) {
                    throw new RuntimeException(String.format("Error subscribe %s listener method invocation",
                            SelectionListener.class.getSimpleName()), e);
                }
            };
        }

        return listener;
    }

    protected List<AnnotatedMethod<Subscribe>> getSelectionListenersMethods(Class<?> compositeClass) {
        return selectionListenerIntrospectionCache.getUnchecked(compositeClass);
    }

    protected List<AnnotatedMethod<Subscribe>> getSelectionListenersMethodsNotCached(Class<?> compositeClass) {
        Method[] uniqueDeclaredMethods = ReflectionUtils.getUniqueDeclaredMethods(compositeClass, method ->
                method.getParameterCount() == 1
                        && SelectionEvent.class.isAssignableFrom(method.getParameterTypes()[0]));

        List<AnnotatedMethod<Subscribe>> annotatedMethods =
                AutowireUtils.getAnnotatedMethodsNotCached(Subscribe.class, uniqueDeclaredMethods, m -> true);

        annotatedMethods.sort(AutowireUtils::compareMethods);

        return ImmutableList.copyOf(annotatedMethods);
    }
}
