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

package io.jmix.flowui.data;

import com.vaadin.flow.shared.Registration;
import org.springframework.lang.Nullable;

import java.util.EventObject;
import java.util.function.Consumer;

/**
 * Data component holding a typed value.
 */
public interface ValueSource<V> extends DataUnit, HasType<V> {

    /**
     * Returns the current value stored in the value source.
     *
     * @return the current value, or {@code null} if no value is set
     */
    @Nullable
    V getValue();

    /**
     * Sets the given value to this value source.
     *
     * @param value the value to be set
     */
    void setValue(@Nullable V value);

    /**
     * Checks whether the value source is read-only.
     *
     * @return {@code true} if the value source is read-only, {@code false} otherwise
     */
    boolean isReadOnly();

    /**
     * Registers a new value change listener.
     *
     * @param listener the listener to be added
     * @return a registration object for removing an event listener added to a source
     */
    Registration addValueChangeListener(Consumer<ValueChangeEvent<V>> listener);

    /**
     * An event that is fired when value of source is changed.
     *
     * @param <V> value type
     */
    class ValueChangeEvent<V> extends EventObject {

        private final V prevValue;
        private final V value;

        public ValueChangeEvent(ValueSource<V> source, @Nullable V prevValue, @Nullable V value) {
            super(source);
            this.prevValue = prevValue;
            this.value = value;
        }

        @SuppressWarnings("unchecked")
        @Override
        public ValueSource<V> getSource() {
            return (ValueSource<V>) super.getSource();
        }

        /**
         * Returns the previous value associated with the {@link ValueSource}.
         *
         * @return the previous value, or {@code null} if no previous value exists
         */
        @Nullable
        public V getPrevValue() {
            return prevValue;
        }

        /**
         * Returns the current value stored in the {@link ValueSource}.
         *
         * @return the current value, or {@code null} if no value is set
         */
        @Nullable
        public V getValue() {
            return value;
        }
    }
}
