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

import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.Composite;
import io.jmix.core.JmixOrder;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.fragment.FragmentUtils;
import io.jmix.flowui.view.Subscribe;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * A special injector that autowired click listener methods that are annotated by the {@link Subscribe} annotation.
 * That clicks can be default, double or single. Methods for adding listeners have the same signature, which in
 * the injection mechanism is perceived as the same method.
 *
 * @see ClickNotifier
 */
@Order(JmixOrder.LOWEST_PRECEDENCE - 100)
@Component("flowui_FragmentClickNotifierDependencyInjector")
public class FragmentClickNotifierDependencyInjector extends AbstractClickNotifierDependencyInjector {

    public FragmentClickNotifierDependencyInjector(ReflectionCacheManager reflectionCacheManager) {
        super(reflectionCacheManager);
    }

    @Override
    protected Object getEventTarget(Composite<?> composite, String target) {
        return AutowireUtils.findMethodTarget(composite, target,
                (component, id) -> UiComponentUtils.findComponent(component, id, FragmentUtils::sameId));
    }

    @Override
    public boolean isApplicable(AutowireContext<?> autowireContext) {
        return autowireContext instanceof FragmentAutowireContext;
    }
}
