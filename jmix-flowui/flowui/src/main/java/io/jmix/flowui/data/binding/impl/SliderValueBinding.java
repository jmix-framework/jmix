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

import java.util.Objects;

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

    protected Runnable componentValueSetHandler;

    public SliderValueBinding(ValueSource<V> valueSource, HasValue<?, V> component) {
        super(valueSource, component);
    }

    /**
     * Sets a handler invoked after a value source value has been set to the component.
     * <p>
     * The component substitutes an absent value with its minimum value, so a value source value
     * can change the empty state of the component without changing the component value, i.e.
     * without firing a value change event.
     *
     * @param componentValueSetHandler the handler to set, or {@code null} to remove it
     */
    public void setComponentValueSetHandler(@Nullable Runnable componentValueSetHandler) {
        this.componentValueSetHandler = componentValueSetHandler;
    }

    @Override
    protected void onComponentValueChange() {
        // Intentionally empty: the component writes the value to the value source itself, before
        // validating it, see AbstractSliderDelegate#onValueSet. A second writer here would also
        // propagate the minimum value the component substitutes for an absent one.
    }

    /**
     * Writes the current component value to the value source.
     * <p>
     * The component cannot represent an absent value and shows its minimum value instead, so
     * setting the value the component already shows fires no value change event, and the binding
     * has nothing to react to, although the value source value becomes present.
     */
    public void writeComponentValueToSource() {
        V componentValue = getComponentValue();

        // The value source may already hold the value, e.g. when the binding itself is pushing
        // it. Writing it back would re-enter this binding through a value source that fires a
        // value change event unconditionally, such as BufferedContainerValueSource.
        if (!Objects.equals(valueSource.getValue(), componentValue)) {
            setValueToSource(componentValue);
        }
    }

    /**
     * Clears the value of the component and of the value source.
     * <p>
     * The component cannot represent an absent value and falls back to its minimum value, so the
     * propagation to the value source is suspended while the component is being cleared and the
     * value source receives {@code null} instead of that fallback.
     *
     * @param clearComponentValueAction action that clears the component value
     */
    public void clearValue(Runnable clearComponentValueAction) {
        boolean suspendRequired = !suspended();
        if (suspendRequired) {
            suspend();
        }

        try {
            clearComponentValueAction.run();
        } finally {
            if (suspendRequired) {
                resume();
            }
        }

        setValueToSource(null);
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

        if (componentValueSetHandler != null) {
            componentValueSetHandler.run();
        }
    }
}
