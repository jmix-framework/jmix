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

import com.vaadin.flow.component.Composite;
import io.jmix.core.JmixOrder;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.sys.event.UiEventListenerMethodAdapter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An injector that autowires view methods that are annotated by the {@link EventListener} annotation.
 */
@Order(JmixOrder.LOWEST_PRECEDENCE)
@Component("flowui_EventListenerDependencyInjector")
public class EventListenerDependencyInjector implements DependencyInjector {

    protected ApplicationContext applicationContext;
    protected ReflectionCacheManager reflectionCacheManager;

    public EventListenerDependencyInjector(ApplicationContext applicationContext,
                                           ReflectionCacheManager reflectionCacheManager) {
        this.applicationContext = applicationContext;
        this.reflectionCacheManager = reflectionCacheManager;
    }

    @Override
    public void autowire(AutowireContext<?> autowireContext) {
        Composite<?> composite = autowireContext.getTarget();
        //noinspection rawtypes
        Class<? extends Composite> compositeClass = composite.getClass();

        List<Method> eventListenerMethods = reflectionCacheManager.getEventListenerMethods(compositeClass);

        if (!eventListenerMethods.isEmpty()) {
            Collection<Object> autowired = autowireContext.getAutowired();

            List<ApplicationListener<?>> listeners = eventListenerMethods.stream()
                    .filter(m -> !autowired.contains(m))
                    .peek(autowired::add)
                    .map(m -> new UiEventListenerMethodAdapter(composite, compositeClass, m, applicationContext))
                    .collect(Collectors.toList());

            setEventListenersInternal(composite, listeners);
        }
    }

    protected void setEventListenersInternal(Composite<?> composite, List<ApplicationListener<?>> listeners) {
        if (listeners.isEmpty()) {
            return;
        }

        UiComponentUtils.setApplicationEventListeners(composite, listeners);
    }

    @Override
    public boolean isApplicable(AutowireContext<?> autowireContext) {
        return autowireContext instanceof ViewAutowireContext
                || autowireContext instanceof FragmentAutowireContext;
    }
}
