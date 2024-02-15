/*
 * Copyright 2023 Haulmont.
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

package io.jmix.chartsflowui.kit.data.chart;

import com.vaadin.flow.shared.Registration;
import io.jmix.chartsflowui.kit.component.model.HasEnumId;
import jakarta.annotation.Nullable;

import java.util.Collection;
import java.util.EventObject;
import java.util.List;
import java.util.function.Consumer;

public interface ChartItems<T extends DataItem> {

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
    Registration addItemSetChangeListener(Consumer<ItemSetChangeEvent<T>> listener);

    /**
     * An event that is fired when item set is changed.
     */
    class ItemSetChangeEvent<T extends DataItem> extends EventObject {

        protected final DataChangeOperation operation;
        protected final Collection<T> items;

        public ItemSetChangeEvent(ChartItems<T> source, DataChangeOperation operation,
                                  List<T> items) {
            super(source);

            this.operation = operation;
            this.items = items;
        }

        @SuppressWarnings("unchecked")
        @Override
        public ChartItems<T> getSource() {
            return (ChartItems<T>) super.getSource();
        }

        /**
         * @return operation which caused the data provider change
         */
        public DataChangeOperation getOperation() {
            return operation;
        }

        /**
         * @return items which used in operation
         */
        public Collection<T> getItems() {
            return items;
        }
    }

    enum DataChangeOperation implements HasEnumId {
        REFRESH("refresh"),
        UPDATE("update"),
        ADD("add"),
        REMOVE("remove");

        private final String id;

        DataChangeOperation(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static DataChangeOperation fromId(String id) {
            for (DataChangeOperation at : DataChangeOperation.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }

            return null;
        }
    }
}
