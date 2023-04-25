/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.component.delegate;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.data.binding.ValueBinding;
import io.jmix.flowui.data.binding.impl.AbstractValueBinding;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.kit.event.EventBus;

import jakarta.annotation.Nullable;
import java.util.function.Consumer;

public abstract class AbstractValueComponentDelegate<C extends Component & HasValue<?, V>, T, V>
        extends AbstractComponentDelegate<C> implements ValueBindingDelegate<T> {

    protected ValueBinding<T> valueBinding;

    private EventBus eventBus;

    public AbstractValueComponentDelegate(C component) {
        super(component);
    }

    @Override
    public ValueBinding<T> getValueBinding() {
        return valueBinding;
    }

    @Nullable
    public ValueSource<T> getValueSource() {
        return valueBinding != null ? valueBinding.getValueSource() : null;
    }

    public void setValueSource(@Nullable ValueSource<T> valueSource) {
        if (valueBinding != null) {
            valueBinding.unbind();
            valueBinding = null;
        }

        if (valueSource != null) {
            valueBinding = createValueBinding(valueSource);
            valueBinding.bind();

            valueBinding.activate();

        }

        valueBindingChanged(valueBinding);
    }

    protected void valueBindingChanged(@Nullable ValueBinding<T> valueBinding) {
        ValueBindingChangeEvent<T> event = new ValueBindingChangeEvent<>(this, valueBinding);
        getEventBus().fireEvent(event);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Registration addValueBindingChangeListener(Consumer<ValueBindingChangeEvent<T>> listener) {
        return getEventBus().addListener(ValueBindingChangeEvent.class, ((Consumer) listener));
    }

    protected abstract AbstractValueBinding<T> createValueBinding(ValueSource<T> valueSource);

    protected EventBus getEventBus() {
        if (eventBus == null) {
            eventBus = new EventBus();
        }

        return eventBus;
    }
}
