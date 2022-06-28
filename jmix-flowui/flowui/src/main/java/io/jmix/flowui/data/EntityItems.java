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

package io.jmix.flowui.data;

import com.vaadin.flow.shared.Registration;

import javax.annotation.Nullable;
import java.util.EventObject;
import java.util.List;
import java.util.function.Consumer;

public interface EntityItems<E> extends EntityDataUnit {
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

    /**
     * Adds an items change listener. The listener is called when
     * the item collection is changed.
     *
     * @param listener a listener to register, not null
     * @return a registration for the listener
     */
    Registration addItemsChangeListener(Consumer<ItemsChangeEvent<E>> listener);

    /**
     * Event that is fired then item collection is changed.
     *
     * @param <T> item type
     */
    class ItemsChangeEvent<T> extends EventObject {

        protected final List<T> items;

        public ItemsChangeEvent(EntityItems<T> source, List<T> items) {
            super(source);
            this.items = items;
        }

        @SuppressWarnings("unchecked")
        @Override
        public EntityItems<T> getSource() {
            return (EntityItems<T>) super.getSource();
        }

        public List<T> getItems() {
            return items;
        }
    }
}
