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

package io.jmix.flowui.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.shared.Registration;

import io.jmix.flowui.kit.meta.StudioIgnore;
import org.springframework.lang.Nullable;

public interface SupportsTypedValue<C extends Component, E extends HasValue.ValueChangeEvent<P>, V, P> extends HasValue<E, P> {

    @Nullable
    V getTypedValue();

    @StudioIgnore
    void setTypedValue(@Nullable V value);

    Registration addTypedValueChangeListener(ComponentEventListener<TypedValueChangeEvent<C, V>> listener);

    class TypedValueChangeEvent<C extends Component, V> extends ComponentEvent<C>
            implements HasValue.ValueChangeEvent<V> {

        protected V value;
        protected V oldValue;

        public TypedValueChangeEvent(C source, @Nullable V value, @Nullable V oldValue, boolean fromClient) {
            super(source, fromClient);

            this.value = value;
            this.oldValue = oldValue;
        }

        @SuppressWarnings("unchecked")
        @Override
        public HasValue<?, V> getHasValue() {
            // It's safe to cast, because SupportsTypedValue extends HasValue
            return ((HasValue<?, V>) getSource());
        }

        @Nullable
        @Override
        public V getValue() {
            return value;
        }

        @Nullable
        @Override
        public V getOldValue() {
            return oldValue;
        }
    }
}
