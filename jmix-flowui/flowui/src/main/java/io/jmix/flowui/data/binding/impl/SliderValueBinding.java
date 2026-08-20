/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.data.binding.impl;

import com.vaadin.flow.component.HasValue;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.data.binding.ValueBinding;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link ValueBinding} for working with slider components.
 * <p>
 * A slider cannot represent an absent value and falls back to its minimum value,
 * so the fallback is not propagated to the value source.
 *
 * @param <V> the value type
 */
@Component("flowui_SliderValueBinding")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SliderValueBinding<V> extends AbstractValueBinding<V> {

    public SliderValueBinding(ValueSource<V> valueSource, HasValue<?, V> component) {
        super(valueSource, component);
    }

    @Nullable
    @Override
    protected V getComponentValue() {
        return component instanceof SupportsTypedValue
                ? ((SupportsTypedValue<?, ?, V, ?>) component).getTypedValue()
                : component.getValue();
    }

    @Override
    protected void setComponentValue(@Nullable V value) {
        // The component replaces the absent value with its minimum value, which fires a value
        // change event. The binding is suspended to prevent this fallback from being written
        // back to the value source and from changing the modified state of the DataContext.
        boolean suspendRequired = value == null && !suspended();
        if (suspendRequired) {
            suspend();
        }

        try {
            if (component instanceof SupportsTypedValue) {
                ((SupportsTypedValue<?, ?, V, ?>) component).setTypedValue(value);
            } else {
                component.setValue(value);
            }
        } finally {
            if (suspendRequired) {
                resume();
            }
        }
    }
}
