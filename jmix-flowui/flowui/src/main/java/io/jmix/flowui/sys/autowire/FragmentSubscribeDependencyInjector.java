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
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.sys.ViewDescriptorUtils;
import io.jmix.flowui.view.Subscribe;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;

/**
 * An injector that autowires method that are annotated by the {@link Subscribe} annotation.
 * These can be subscriptions to {@link Fragment} events or to components events in the fragment.
 */
@Order(JmixOrder.LOWEST_PRECEDENCE - 80)
@org.springframework.stereotype.Component("flowui_FragmentSubscribeDependencyInjector")
public class FragmentSubscribeDependencyInjector extends AbstractSubscribeDependencyInjector {

    public FragmentSubscribeDependencyInjector(ReflectionCacheManager reflectionCacheManager) {
        super(reflectionCacheManager);
    }

    @Nullable
    @Override
    protected Object getEventTarget(Subscribe annotation, Composite<?> composite) {
        return AutowireUtils.getFragmentTargetInstance(annotation, (Fragment<?>) composite,
                ViewDescriptorUtils.getInferredSubscribeId(annotation), annotation.target());
    }

    @Override
    public boolean isApplicable(AutowireContext<?> autowireContext) {
        return autowireContext instanceof FragmentAutowireContext;
    }
}
