/*
 * Copyright 2025 Haulmont.
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

package io.jmix.vaadincommercialcomponents.sys.autowire;

import com.google.common.base.Strings;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import io.jmix.core.DevelopmentException;
import io.jmix.core.JmixOrder;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.sys.ViewDescriptorUtils;
import io.jmix.flowui.sys.autowire.*;
import io.jmix.flowui.sys.autowire.ReflectionCacheManager.AnnotatedMethod;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.Target;
import io.jmix.flowui.view.View;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static io.jmix.flowui.view.Target.COMPONENT;

/**
 * An injector that autowires listener methods that are annotated by the {@link Subscribe}
 * annotation. A special dependency injector is required because {@link Spreadsheet} listeners is not an inheritor of
 * {@link ComponentEventListener}.
 */
@Order(JmixOrder.LOWEST_PRECEDENCE - 120)
@Component("vcc_SpreadsheetListenerDependencyInjector")
public class SpreadsheetListenerDependencyInjector implements DependencyInjector {

    private static final Logger log = LoggerFactory.getLogger(SpreadsheetListenerDependencyInjector.class);

    protected ReflectionCacheManager reflectionCacheManager;

    Map<Class<?>, Class<?>> eventToListenerMap = Map.of(
            Spreadsheet.SelectionChangeEvent.class, Spreadsheet.SelectionChangeListener.class,
            Spreadsheet.CellValueChangeEvent.class, Spreadsheet.CellValueChangeListener.class,
            Spreadsheet.FormulaValueChangeEvent.class, Spreadsheet.FormulaValueChangeListener.class,
            Spreadsheet.ProtectedEditEvent.class, Spreadsheet.ProtectedEditListener.class,
            Spreadsheet.SheetChangeEvent.class, Spreadsheet.SheetChangeListener.class,
            Spreadsheet.RowHeaderDoubleClickEvent.class, Spreadsheet.RowHeaderDoubleClickListener.class
    );

    protected SpreadsheetListenerDependencyInjector(ReflectionCacheManager reflectionCacheManager) {
        this.reflectionCacheManager = reflectionCacheManager;
    }

    @Override
    public void autowire(AutowireContext<?> autowireContext) {
        Composite<?> composite = autowireContext.getTarget();
        //noinspection rawtypes
        Class<? extends Composite> compositeClass = composite.getClass();

        List<AnnotatedMethod<Subscribe>> selectionListenersMethods =
                reflectionCacheManager.getSubscribeMethods(compositeClass);
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
                || eventToListenerMap.keySet().stream()
                .noneMatch(subscribeMethodParam -> subscribeMethodParam.isAssignableFrom(eventType))
        ) {
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

        MethodHandle addListenerMethod = getTargetAddListenerMethod(eventTarget.getClass(), eventType);
        if (addListenerMethod == null) {
            throw new DevelopmentException(String.format("Target %s does not support event type %s",
                    eventTarget.getClass().getName(), eventType));
        }

        Object listener = getEventListener(getClass(), composite, annotatedMethod,
                eventType, eventToListenerMap.get(eventType));

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
    protected MethodHandle getTargetAddListenerMethod(Class<?> targetClass, Class<?> eventType) {
        Method[] uniqueDeclaredMethods = ReflectionUtils.getUniqueDeclaredMethods(targetClass, m ->
                m.getParameterCount() == 1
                        && eventToListenerMap.get(eventType).isAssignableFrom(m.getParameterTypes()[0])
        );
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

    protected <T> T getEventListener(Class<?> callerClass,
                                     com.vaadin.flow.component.Component component,
                                     AnnotatedMethod<Subscribe> annotatedMethod,
                                     Class<?> eventType,
                                     Class<T> listenerClass) {
        T listener;

        // If a component class was hot-deployed, then it will be loaded
        // by a different class loader. This will make it impossible to create a lambda
        // using LambdaMetaFactory for producing the listener method in Java 17+
        if (callerClass.getClassLoader() == component.getClass().getClassLoader()) {
            String methodName = StringUtils.capitalize(
                    listenerClass.getSimpleName()
                            .replaceFirst("add", "")
                            .replace("Listener", "")
            );

            MethodHandle consumerMethodFactory = reflectionCacheManager.getEventListenerMethodFactory(
                    component.getClass(), annotatedMethod, "on%s".formatted(methodName), listenerClass, eventType
            );

            try {
                //noinspection unchecked
                listener = (T) consumerMethodFactory.invoke(component);
            } catch (Error e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException(String.format("Unable to bind %s handler",
                        listenerClass.getSimpleName()), e);
            }
        } else {
            //noinspection SuspiciousInvocationHandlerImplementation
            listener = listenerClass.cast(
                    Proxy.newProxyInstance(listenerClass.getClassLoader(), new Class<?>[]{listenerClass},
                            (proxy, method, args) -> {
                                try {
                                    annotatedMethod.getMethodHandle().invoke(component, args[0]);
                                    return null;
                                } catch (Throwable e) {
                                    throw new RuntimeException(String.format("Error subscribe %s listener method invocation",
                                            listenerClass.getSimpleName()), e);
                                }
                            }
                    )
            );
        }

        return listener;
    }
}
