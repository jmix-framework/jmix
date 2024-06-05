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
import io.jmix.flowui.sys.autowire.ReflectionCacheManager.AutowireElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;

public abstract class AbstractElementsDependencyInjector implements DependencyInjector {

    private static final Logger log = LoggerFactory.getLogger(AbstractElementsDependencyInjector.class);

    protected ReflectionCacheManager reflectionCacheManager;

    protected AbstractElementsDependencyInjector(ReflectionCacheManager reflectionCacheManager) {
        this.reflectionCacheManager = reflectionCacheManager;
    }

    @Override
    public void autowire(AutowireContext<?> autowireContext) {
        Composite<?> target = autowireContext.getTarget();

        List<AutowireElement> autowireElements = reflectionCacheManager.getAutowireElements(target.getClass());
        Collection<Object> autowired = autowireContext.getAutowired();

        for (AutowireElement element : autowireElements) {
            if (!autowired.contains(element)) {
                doAutowiring(element, target, autowired);
            }
        }
    }

    protected void doAutowiring(AutowireElement autowireElement, Composite<?> view, Collection<Object> autowired) {
        String name = AutowireUtils.getAutowiringName(autowireElement);
        Class<?> type = AutowireUtils.getAutowiringType(autowireElement);

        Object instance = getAutowiredInstance(type, name, view);

        if (instance != null) {
            AutowireUtils.assignValue(autowireElement.getElement(), instance, view);
            autowired.add(autowireElement);
        } else {
            log.trace("Skip autowiring {} of {} because instance not found",
                    name, view.getClass());
        }
    }

    @Nullable
    protected abstract Object getAutowiredInstance(Class<?> type, String name, Composite<?> composite);
}
