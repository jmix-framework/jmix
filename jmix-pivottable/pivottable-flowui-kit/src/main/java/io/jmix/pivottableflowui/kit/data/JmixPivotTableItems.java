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

package io.jmix.pivottableflowui.kit.data;

import com.vaadin.flow.shared.Registration;
import io.jmix.pivottableflowui.kit.component.JmixPivotTable;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.EventObject;
import java.util.function.Consumer;

/**
 * Base interface for {@link JmixPivotTable} component items element implementation.
 *
 * @param <T> type of items contained
 */
public interface JmixPivotTableItems<T> extends Serializable {

    /**
     * @return unmodifiable collection of items
     */
    Collection<T> getItems();

    /**
     * Finds an item by its id.
     *
     * @param itemId the item id
     * @return the item by the given id
     */
    T getItem(Object itemId);

    /**
     * @param item         the item
     * @param propertyPath the property path
     * @return the item value
     */
    @Nullable
    <V> V getItemValue(T item, String propertyPath);

    /**
     * @param item the item
     * @return the id of the item
     */
    @Nullable
    Object getItemId(T item);

    /**
     * Sets the value to the item.
     *
     * @param item         the item
     * @param propertyPath the property path
     * @param value        the value to set
     */
    void setItemValue(T item, String propertyPath, @Nullable Object value);

    /**
     * Finds an item by its string id.
     *
     * @param stringId the item string id
     * @return the item by the given id
     */
    T getItem(String stringId);

    /**
     * If the item with the same id exists, it is replaced with the given instance.
     * If not, the given instance is added to the items list.
     *
     * @param item item to update
     */
    void updateItem(T item);

    /**
     * @param item an item to check
     * @return {@code true} if the underlying collection contains an item, {@code false} otherwise
     */
    boolean containsItem(T item);

    /**
     * Adds a listener that will be called when the associated collection or the property of an item of this collection
     * changes.
     *
     * @param listener a listener to add
     * @return a handle that can be used for removing the listener
     */
    Registration addItemsChangeListener(Consumer<ItemsChangeEvent<T>> listener);

    /**
     * Event sent on changes in the items collection - adding, removing, replacing elements.
     *
     * @param <T> type of the elements
     */
    class ItemsChangeEvent<T> extends EventObject {

        protected ItemsChangeType changeType;
        protected Collection<? extends T> items;

        public ItemsChangeEvent(JmixPivotTableItems<T> source, ItemsChangeType changeType,
                                Collection<? extends T> items) {
            super(source);

            this.changeType = changeType;
            this.items = items;
        }

        @SuppressWarnings("unchecked")
        @Override
        public JmixPivotTableItems<T> getSource() {
            return (JmixPivotTableItems<T>) super.getSource();
        }

        /**
         * @return type of items change
         */
        public ItemsChangeType getChangeType() {
            return changeType;
        }

        /**
         * @return collection of changed items
         */
        public Collection<? extends T> getItems() {
            return items;
        }
    }

    /**
     * Type of items change.
     */
    enum ItemsChangeType {
        REFRESH,
        UPDATE,
        ADD,
        REMOVE
    }
}
