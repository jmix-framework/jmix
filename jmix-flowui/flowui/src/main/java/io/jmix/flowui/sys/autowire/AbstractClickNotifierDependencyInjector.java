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
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Composite;
import io.jmix.core.DevelopmentException;
import io.jmix.flowui.sys.ViewDescriptorUtils;
import io.jmix.flowui.sys.autowire.ReflectionCacheManager.AnnotatedMethod;
import io.jmix.flowui.view.Subscribe;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.List;

import static io.jmix.flowui.view.Target.COMPONENT;

public abstract class AbstractClickNotifierDependencyInjector implements DependencyInjector {

    protected static final String DEFAULT_CLICK_LISTENER_METHOD_NAME = "addClickListener";

    private static final Logger log = LoggerFactory.getLogger(AbstractClickNotifierDependencyInjector.class);

    protected ReflectionCacheManager reflectionCacheManager;

    protected AbstractClickNotifierDependencyInjector(ReflectionCacheManager reflectionCacheManager) {
        this.reflectionCacheManager = reflectionCacheManager;
    }

    @Override
    public void autowire(AutowireContext<?> autowireContext) {
        Composite<?> composite = autowireContext.getTarget();
        //noinspection rawtypes
        Class<? extends Composite> compositeClass = composite.getClass();

        List<AnnotatedMethod<Subscribe>> subscribeMethods = reflectionCacheManager.getSubscribeMethods(compositeClass);
        Collection<Object> autowired = autowireContext.getAutowired();

        for (AnnotatedMethod<Subscribe> annotatedMethod : subscribeMethods) {
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

        String target = ViewDescriptorUtils.getInferredSubscribeId(annotation);

        if (Strings.isNullOrEmpty(target)
                || !COMPONENT.equals(annotation.target())
                || !(ClickEvent.class == eventType)) {
            return;
        }

        Object eventTarget = getEventTarget(composite, target);

        if (eventTarget == null) {
            if (annotation.required()) {
                throw new DevelopmentException(String.format("Unable to find @%s target %s in %s",
                        Subscribe.class.getSimpleName(), target, compositeClass.getSimpleName()));
            }

            log.trace("Skip @{} method {} of {} : it is not required and target not found",
                    Subscribe.class.getSimpleName(), annotatedMethod.getMethod().getName(), compositeClass);

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

        ComponentEventListener<?> listener = AutowireUtils.getComponentEventListener(getClass(), composite,
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

    @Nullable
    protected abstract Object getEventTarget(Composite<?> composite, String target);

    protected String convertSubjectToMethodName(String methodName) {
        return "add" + StringUtils.capitalize(methodName);
    }
}
