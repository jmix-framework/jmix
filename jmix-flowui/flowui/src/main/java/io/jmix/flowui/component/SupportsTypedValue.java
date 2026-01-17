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

/**
 * Interface that defines a contract for components supporting a typed value.
 *
 * @param <C> the type of the component implementing this interface
 * @param <E> the type of the value change event
 * @param <V> the type of the value supported by the component
 * @param <P> the presentation type of the value
 */
public interface SupportsTypedValue<C extends Component, E extends HasValue.ValueChangeEvent<P>, V, P> extends HasValue<E, P> {

    /**
     * Returns the typed value associated with the component.
     *
     * @return the typed value of the component, or {@code null} if no value is set
     */
    @Nullable
    V getTypedValue();

    /**
     * Sets the typed value of the component to the specified value.
     *
     * @param value the new typed value to set, or {@code null} if the value should be unset
     */
    @StudioIgnore
    void setTypedValue(@Nullable V value);

    /**
     * Adds a listener to be notified of changes to the typed value of the component.
     *
     * @param listener the listener to be added; it will handle {@link TypedValueChangeEvent} triggered
     *                 by changes to the component's typed value
     * @return a {@link Registration} object that can be used to remove the listener
     */
    Registration addTypedValueChangeListener(ComponentEventListener<TypedValueChangeEvent<C, V>> listener);

    /**
     * Represents a value change event for components that support typed values.
     *
     * @param <C> the type of the component that fires this event
     * @param <V> the type of the value associated with the component
     */
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
