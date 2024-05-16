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
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import io.jmix.core.DevelopmentException;
import io.jmix.core.JmixOrder;
import io.jmix.flowui.model.ViewData;
import io.jmix.flowui.sys.ViewDescriptorUtils;
import io.jmix.flowui.sys.autowire.ReflectionCacheManager.AnnotatedMethod;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewControllerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * An injector that autowires method that are annotated by the {@link Subscribe} annotation.
 * These can be subscriptions to view events or to components events on the view.
 */
@Order(JmixOrder.LOWEST_PRECEDENCE - 30)
@org.springframework.stereotype.Component("flowui_SubscribeDependencyInjector")
public class SubscribeDependencyInjector implements DependencyInjector {

    private static final Logger log = LoggerFactory.getLogger(SubscribeDependencyInjector.class);

    protected ReflectionCacheManager reflectionCacheManager;

    public SubscribeDependencyInjector(ReflectionCacheManager reflectionCacheManager) {
        this.reflectionCacheManager = reflectionCacheManager;
    }

    @Override
    public void autowire(AutowireContext autowireContext) {
        ViewAutowireContext viewAutowireContext = (ViewAutowireContext) autowireContext;
        View<?> view = viewAutowireContext.getView();
        //noinspection rawtypes
        Class<? extends View> viewClass = view.getClass();

        List<AnnotatedMethod<Subscribe>> subscribeMethods = reflectionCacheManager.getViewSubscribeMethods(viewClass);
        Collection<Object> autowired = viewAutowireContext.getAutowired();

        for (AnnotatedMethod<Subscribe> annotatedMethod : subscribeMethods) {
            if (!autowired.contains(annotatedMethod)) {
                doAutowiring(annotatedMethod, view, viewClass, autowired);
            }
        }
    }

    protected void doAutowiring(AnnotatedMethod<Subscribe> annotatedMethod,
                                View<?> view,
                                @SuppressWarnings("rawtypes")
                                Class<? extends View> viewClass,
                                Collection<Object> autowired) {
        Method method = annotatedMethod.getMethod();
        Subscribe annotation = annotatedMethod.getAnnotation();

        String targetId = ViewDescriptorUtils.getInferredSubscribeId(annotation);

        Object eventTarget = null;

        ViewData viewData = ViewControllerUtils.getViewData(view);

        if (Strings.isNullOrEmpty(targetId)) {
            eventTarget = switch (annotation.target()) {
                case COMPONENT, CONTROLLER -> view;
                case DATA_CONTEXT -> viewData.getDataContext();
                default -> throw new UnsupportedOperationException(String.format("Unsupported @%s targetId %s",
                        Subscribe.class.getSimpleName(), annotation.target()));
            };
        } else {
            switch (annotation.target()) {
                case COMPONENT -> eventTarget = AutowireUtils.findMethodTarget(view, targetId);
                case DATA_LOADER -> {
                    if (viewData.getLoaderIds().contains(targetId)) {
                        eventTarget = viewData.getLoader(targetId);
                    }
                }
                case DATA_CONTAINER -> {
                    if (viewData.getContainerIds().contains(targetId)) {
                        eventTarget = viewData.getContainer(targetId);
                    }
                }
                default -> throw new UnsupportedOperationException(String.format("Unsupported @%s targetId %s",
                        Subscribe.class.getSimpleName(), annotation.target()));
            }
        }

        if (eventTarget == null) {
            if (annotation.required()) {
                throw new DevelopmentException(String.format("Unable to find @%s targetId %s in %s",
                        Subscribe.class.getSimpleName(), targetId, viewClass.getSimpleName()));
            }

            log.trace("Skip @{} method {} of {} : it is not required and targetId not found",
                    Subscribe.class.getSimpleName(), annotatedMethod.getMethod().getName(), viewClass);

            return;
        }

        Parameter parameter = method.getParameters()[0];
        Class<?> eventType = parameter.getType();

        MethodHandle addListenerMethod = reflectionCacheManager.getTargetAddListenerMethod(
                eventTarget.getClass(),
                eventType,
                annotation.subject()
        );
        if (addListenerMethod == null) {
            throw new DevelopmentException(String.format("Target %s does not support event type %s",
                    eventTarget.getClass().getName(), eventType));
        }

        Object listener;
        if (ComponentEventListener.class.isAssignableFrom(addListenerMethod.type().parameterType(1))) {
            listener = getComponentEventListener(view, annotatedMethod, eventType);
        } else if (ValueChangeListener.class.isAssignableFrom(addListenerMethod.type().parameterType(1))) {
            listener = getValueChangeEventListener(view, annotatedMethod, eventType);
        } else {
            listener = getConsumerListener(view, annotatedMethod, eventType);
        }

        try {
            addListenerMethod.invoke(eventTarget, listener);
        } catch (Error e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException("Unable to add listener " + method, e);
        }

        autowired.add(annotatedMethod);
    }

    protected ComponentEventListener<?> getComponentEventListener(View<?> view,
                                                                  AnnotatedMethod<Subscribe> annotatedMethod,
                                                                  Class<?> eventType) {
        ComponentEventListener<?> listener;

        // If view controller class was hot-deployed, then it will be loaded
        // by different class loader. This will make impossible to create lambda
        // using LambdaMetaFactory for producing the listener method in Java 17+
        if (getClass().getClassLoader() == view.getClass().getClassLoader()) {
            MethodHandle consumerMethodFactory =
                    reflectionCacheManager.getComponentEventListenerMethodFactory(
                            view.getClass(), annotatedMethod, eventType
                    );
            try {
                listener = (ComponentEventListener<?>) consumerMethodFactory.invoke(view);
            } catch (Error e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException(String.format("Unable to bind %s handler",
                        ComponentEventListener.class.getSimpleName()), e);
            }
        } else {
            listener = event -> {
                try {
                    annotatedMethod.getMethodHandle().invoke(view, event);
                } catch (Throwable e) {
                    throw new RuntimeException(String.format("Error subscribe %s listener method invocation",
                            ComponentEventListener.class.getSimpleName()), e);
                }
            };
        }

        return listener;
    }

    protected ValueChangeListener<?> getValueChangeEventListener(View<?> view,
                                                                 AnnotatedMethod<Subscribe> annotatedMethod,
                                                                 Class<?> eventType) {
        ValueChangeListener<?> listener;

        // If view controller class was hot-deployed, then it will be loaded
        // by different class loader. This will make impossible to create lambda
        // using LambdaMetaFactory for producing the listener method in Java 17+
        if (getClass().getClassLoader() == view.getClass().getClassLoader()) {
            MethodHandle consumerMethodFactory =
                    reflectionCacheManager.getValueChangeEventMethodFactory(
                            view.getClass(), annotatedMethod, eventType
                    );
            try {
                listener = (ValueChangeListener<?>) consumerMethodFactory.invokeWithArguments(view);
            } catch (Error e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException(String.format("Unable to bind %s handler",
                        ValueChangeListener.class.getSimpleName()), e);
            }
        } else {
            listener = event -> {
                try {
                    annotatedMethod.getMethodHandle().invoke(view, event);
                } catch (Throwable e) {
                    throw new RuntimeException(String.format("Error subscribe %s listener method invocation",
                            ValueChangeListener.class.getSimpleName()), e);
                }
            };
        }

        return listener;
    }

    protected Consumer<?> getConsumerListener(View<?> view,
                                              AnnotatedMethod<Subscribe> annotatedMethod,
                                              Class<?> eventType) {
        Consumer<?> listener;

        // If view controller class was hot-deployed, then it will be loaded
        // by different class loader. This will make impossible to create lambda
        // using LambdaMetaFactory for producing the listener method in Java 17+
        if (getClass().getClassLoader() == view.getClass().getClassLoader()) {
            MethodHandle consumerMethodFactory =
                    reflectionCacheManager.getConsumerMethodFactory(view.getClass(), annotatedMethod, eventType);
            try {
                listener = (Consumer<?>) consumerMethodFactory.invoke(view);
            } catch (Error e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException(String.format("Unable to bind %s handler", Consumer.class.getSimpleName()), e);
            }
        } else {
            listener = event -> {
                try {
                    annotatedMethod.getMethodHandle().invoke(view, event);
                } catch (Throwable e) {
                    throw new RuntimeException(String.format("Error subscribe %s listener method invocation",
                            Consumer.class.getSimpleName()), e);
                }
            };
        }


        return listener;
    }

    @Override
    public boolean isApplicable(AutowireContext autowireContext) {
        return autowireContext instanceof ViewAutowireContext;
    }
}
