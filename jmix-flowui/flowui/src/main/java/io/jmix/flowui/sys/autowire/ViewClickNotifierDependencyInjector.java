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
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.ComponentEventListener;
import io.jmix.core.DevelopmentException;
import io.jmix.core.JmixOrder;
import io.jmix.flowui.sys.ViewDescriptorUtils;
import io.jmix.flowui.sys.autowire.ReflectionCacheManager.AnnotatedMethod;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.View;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.List;

import static io.jmix.flowui.view.Target.COMPONENT;

/**
 * A special injector that autowired click listener methods that are annotated by the {@link Subscribe} annotation.
 * That clicks can be default, double or single. Methods for adding listeners have the same signature, which in
 * the injection mechanism is perceived as the same method.
 *
 * @see ClickNotifier
 */
@Order(JmixOrder.LOWEST_PRECEDENCE - 50)
@Component("flowui_ViewClickNotifierDependencyInjector")
public class ViewClickNotifierDependencyInjector implements DependencyInjector {

    protected static final String DEFAULT_CLICK_LISTENER_METHOD_NAME = "addClickListener";

    private static final Logger log = LoggerFactory.getLogger(ViewClickNotifierDependencyInjector.class);

    protected ReflectionCacheManager reflectionCacheManager;

    public ViewClickNotifierDependencyInjector(ReflectionCacheManager reflectionCacheManager) {
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
            // skip already autowired elements
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
        Parameter parameter = method.getParameters()[0];
        Class<?> eventType = parameter.getType();
        Subscribe annotation = annotatedMethod.getAnnotation();

        String target = ViewDescriptorUtils.getInferredSubscribeId(annotation);

        if (Strings.isNullOrEmpty(target)
                || !COMPONENT.equals(annotation.target())
                || !ClickEvent.class.isAssignableFrom(eventType)) {
            return;
        }

        Object eventTarget = AutowireUtils.findMethodTarget(view, target);

        if (eventTarget == null) {
            if (annotation.required()) {
                throw new DevelopmentException(String.format("Unable to find @%s target %s in %s",
                        Subscribe.class.getSimpleName(), target, viewClass.getSimpleName()));
            }

            log.trace("Skip @{} method {} of {} : it is not required and target not found",
                    Subscribe.class.getSimpleName(), annotatedMethod.getMethod().getName(), viewClass);

            return;
        }

        MethodHandle addListenerMethod = reflectionCacheManager.getTargetAddListenerMethod(
                eventTarget.getClass(),
                eventType,
                Strings.isNullOrEmpty(annotation.subject())
                        ? DEFAULT_CLICK_LISTENER_METHOD_NAME
                        : convertSubjectToMethodName(annotation.subject())
        );
        if (addListenerMethod == null) {
            throw new DevelopmentException(String.format("Target %s does not support event type %s",
                    eventTarget.getClass().getName(), eventType));
        }

        ComponentEventListener<?> listener = AutowireUtils.getComponentEventListener(getClass(), view,
                annotatedMethod, eventType, reflectionCacheManager);

        try {
            addListenerMethod.invoke(eventTarget, listener);
        } catch (Error e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException("Unable to add listener " + method, e);
        }

        autowired.add(annotatedMethod);
    }

    protected String convertSubjectToMethodName(String methodName) {
        return "add" + StringUtils.capitalize(methodName);
    }

    @Override
    public boolean isApplicable(AutowireContext autowireContext) {
        return autowireContext instanceof ViewAutowireContext;
    }
}
