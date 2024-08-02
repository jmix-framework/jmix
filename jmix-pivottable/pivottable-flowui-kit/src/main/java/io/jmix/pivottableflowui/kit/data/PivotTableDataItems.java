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

import java.util.Collection;
import java.util.EventObject;
import java.util.List;
import java.util.function.Consumer;

public interface PivotTableDataItems<T extends DataItem> {

    /**
     * @return unmodifiable collection of items
     */
    List<T> getItems();

    /**
     * @param itemId the item id
     * @return the item by the given id
     */
    DataItem getItem(Object itemId);

    /**
     * Registers a new item set change listener.
     *
     * @param listener the listener to be added
     * @return a registration object for removing an event listener added to a source
     */
    Registration addItemSetChangeListener(Consumer<DataSetChangeEvent<T>> listener);

    /**
     * An event that is fired when item set is changed.
     */
    class DataSetChangeEvent<T extends DataItem> extends EventObject {

        protected final Collection<T> items;

        public DataSetChangeEvent(PivotTableDataItems<T> source, List<T> items) {
            super(source);

            this.items = items;
        }

        @SuppressWarnings("unchecked")
        @Override
        public PivotTableDataItems<T> getSource() {
            return (PivotTableDataItems<T>) super.getSource();
        }

        /**
         * @return items which used in operation
         */
        public Collection<T> getItems() {
            return items;
        }
    }
}
