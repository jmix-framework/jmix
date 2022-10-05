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

package io.jmix.flowui.data.grid;

import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.data.DataUnit;
import io.jmix.flowui.data.HasType;

import javax.annotation.Nullable;
import java.util.EventObject;
import java.util.function.Consumer;

public interface DataGridItems<T> extends DataUnit, HasType<T> {

    /**
     * @return the current item contained in the source
     */
    @Nullable
    T getSelectedItem();

    /**
     * Set current item in the source.
     *
     * @param item the item to set
     */
    void setSelectedItem(@Nullable T item);

    /**
     * Registers a new value change listener.
     *
     * @param listener the listener to be added
     * @return a registration object for removing an event listener added to a source
     */
    Registration addValueChangeListener(Consumer<ValueChangeEvent<T>> listener);

    /**
     * Registers a new item set change listener.
     *
     * @param listener the listener to be added
     * @return a registration object for removing an event listener added to a source
     */
    Registration addItemSetChangeListener(Consumer<ItemSetChangeEvent<T>> listener);

    /**
     * Registers a new selected item change listener.
     *
     * @param listener the listener to be added
     * @return a registration object for removing an event listener added to a source
     */
    Registration addSelectedItemChangeListener(Consumer<SelectedItemChangeEvent<T>> listener);

    /**
     * @param item an item to check
     * @return {@code true} if the underlying collection contains an item, {@code false} otherwise
     */
    boolean containsItem(T item);

    /**
     * The DataGridItems that supports sorting.
     *
     * @param <T> items type
     */
    interface Sortable<T> extends DataGridItems<T> {

        void sort(Object[] propertyId, boolean[] ascending);

        void resetSortOrder();

        default void suppressSorting() {
        }

        default void enableSorting() {
        }
    }

    /**
     * An event that is fired when value of item property is changed.
     *
     * @param <T> row item type
     */
    class ValueChangeEvent<T> extends EventObject {
        protected final T item;
        protected final String property;
        protected final Object prevValue;
        protected final Object value;

        public ValueChangeEvent(DataGridItems<T> source,
                                T item,
                                String property,
                                @Nullable Object prevValue,
                                @Nullable Object value) {
            super(source);

            this.item = item;
            this.property = property;
            this.prevValue = prevValue;
            this.value = value;
        }

        @SuppressWarnings("unchecked")
        @Override
        public DataGridItems<T> getSource() {
            return (DataGridItems<T>) super.getSource();
        }

        /**
         * @return the item which value is changed
         */
        public T getItem() {
            return item;
        }

        /**
         * @return changed property name
         */
        public String getProperty() {
            return property;
        }

        /**
         * @return a previous value of the item property
         */
        @Nullable
        public Object getPrevValue() {
            return prevValue;
        }

        /**
         * @return a new value of the item property
         */
        @Nullable
        public Object getValue() {
            return value;
        }
    }

    /**
     * An event that is fired when item set is changed.
     *
     * @param <T> row item type
     */
    class ItemSetChangeEvent<T> extends EventObject {

        public ItemSetChangeEvent(DataGridItems<T> source) {
            super(source);
        }

        @SuppressWarnings("unchecked")
        @Override
        public DataGridItems<T> getSource() {
            return (DataGridItems<T>) super.getSource();
        }
    }

    /**
     * An event that is fired when selected item is changed.
     *
     * @param <T> row item type
     */
    class SelectedItemChangeEvent<T> extends EventObject {

        protected final T selectedItem;

        public SelectedItemChangeEvent(DataGridItems<T> source, @Nullable T selectedItem) {
            super(source);

            this.selectedItem = selectedItem;
        }

        @SuppressWarnings("unchecked")
        @Override
        public DataGridItems<T> getSource() {
            return (DataGridItems<T>) super.getSource();
        }

        /**
         * @return a new selected item
         */
        @Nullable
        public T getSelectedItem() {
            return selectedItem;
        }
    }
}
