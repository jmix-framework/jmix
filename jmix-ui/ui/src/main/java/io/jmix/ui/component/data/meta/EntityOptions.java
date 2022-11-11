/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.component.data.meta;

import io.jmix.core.common.event.Subscription;
import io.jmix.ui.component.data.BindingState;
import io.jmix.ui.component.data.Options;

import javax.annotation.Nullable;
import java.util.EventObject;
import java.util.function.Consumer;

public interface EntityOptions<E> extends Options<E>, EntityDataUnit {
    /**
     * Set current item in the source.
     *
     * @param item the item to set
     */
    void setSelectedItem(@Nullable E item);

    /**
     * @return true if the underlying collection contains an item with the specified ID
     */
    boolean containsItem(@Nullable E item);

    /**
     * Update an item in the collection if it is already there.
     */
    void updateItem(E item);

    /**
     * Refreshes the source moving it to the {@link BindingState#ACTIVE} state
     */
    void refresh();

    Subscription addValueChangeListener(Consumer<ValueChangeEvent<E>> listener);

    class ValueChangeEvent<T> extends EventObject {
        private final T prevValue;
        private final T value;

        public ValueChangeEvent(Options<T> source, @Nullable T prevValue, @Nullable T value) {
            super(source);
            this.prevValue = prevValue;
            this.value = value;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Options<T> getSource() {
            return (Options<T>) super.getSource();
        }

        @Nullable
        public T getPrevValue() {
            return prevValue;
        }

        @Nullable
        public T getValue() {
            return value;
        }
    }
}
