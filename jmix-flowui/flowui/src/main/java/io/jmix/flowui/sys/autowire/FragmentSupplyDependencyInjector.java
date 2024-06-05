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
import io.jmix.flowui.view.Supply;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * An injection that autowires methods that are annotated by the {@link Supply} annotation.
 * These can be suppliers for the components on the {@link Fragment}.
 */
@Order(JmixOrder.LOWEST_PRECEDENCE - 60)
@Component("flowui_FragmentSupplyDependencyInjector")
public class FragmentSupplyDependencyInjector extends AbstractSupplyDependencyInjector {

    public FragmentSupplyDependencyInjector(ReflectionCacheManager reflectionCacheManager) {
        super(reflectionCacheManager);
    }

    @Nullable
    @Override
    protected Object getSupplyTargetInstance(Composite<?> composite, Supply annotation) {
        return AutowireUtils.getFragmentTargetInstance(annotation, (Fragment<?>) composite,
                ViewDescriptorUtils.getInferredProvideId(annotation), annotation.target());
    }

    @Override
    public boolean isApplicable(AutowireContext<?> autowireContext) {
        return autowireContext instanceof FragmentAutowireContext;
    }
}
