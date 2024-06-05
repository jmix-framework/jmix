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

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasValue;
import io.jmix.core.DevelopmentException;
import io.jmix.flowui.sys.ViewDescriptorUtils;
import io.jmix.flowui.sys.autowire.ReflectionCacheManager.AnnotatedMethod;
import io.jmix.flowui.view.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.List;

public abstract class AbstractSubscribeDependencyInjector implements DependencyInjector {

    private static final Logger log = LoggerFactory.getLogger(AbstractSubscribeDependencyInjector.class);

    protected ReflectionCacheManager reflectionCacheManager;

    protected AbstractSubscribeDependencyInjector(ReflectionCacheManager reflectionCacheManager) {
        this.reflectionCacheManager = reflectionCacheManager;
    }

    @Override
    public void autowire(AutowireContext<?> autowireContext) {
        Composite<?> target = autowireContext.getTarget();
        //noinspection rawtypes
        Class<? extends Composite> targetClass = target.getClass();

        List<AnnotatedMethod<Subscribe>> subscribeMethods = reflectionCacheManager.getSubscribeMethods(targetClass);
        Collection<Object> autowired = autowireContext.getAutowired();

        for (AnnotatedMethod<Subscribe> annotatedMethod : subscribeMethods) {
            if (!autowired.contains(annotatedMethod)) {
                doAutowiring(annotatedMethod, target, targetClass, autowired);
            }
        }
    }

    protected void doAutowiring(AnnotatedMethod<Subscribe> annotatedMethod,
                                Composite<?> composite,
                                @SuppressWarnings("rawtypes")
                                Class<? extends Composite> compositeClass,
                                Collection<Object> autowired) {
        Method method = annotatedMethod.getMethod();
        Subscribe annotation = annotatedMethod.getAnnotation();

        String targetId = ViewDescriptorUtils.getInferredSubscribeId(annotation);
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
            listener = AutowireUtils.getComponentEventListener(getClass(), composite,
                    annotatedMethod, eventType, reflectionCacheManager);
        } else if (HasValue.ValueChangeListener.class.isAssignableFrom(addListenerMethod.type().parameterType(1))) {
            listener = AutowireUtils.getValueChangeEventListener(getClass(), composite,
                    annotatedMethod, eventType, reflectionCacheManager);
        } else {
            listener = AutowireUtils.getConsumerListener(getClass(), composite,
                    annotatedMethod, eventType, reflectionCacheManager);
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

    @Nullable
    protected abstract Object getEventTarget(Subscribe annotation, Composite<?> composite);
}
