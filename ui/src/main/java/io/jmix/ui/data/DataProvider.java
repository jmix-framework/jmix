/*
 * Copyright 2021 Haulmont.
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

package io.jmix.ui.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public interface DataProvider extends Serializable {

    /**
     * @return list of all items.
     */
    List<DataItem> getItems();

    /**
     * @param id id of data item
     * @return data item by id
     */
    default DataItem getItem(Object id) {
        return getItems().stream()
                .filter(dataItem -> (dataItem instanceof DataItem.HasId)
                        && Objects.equals(id, ((DataItem.HasId) dataItem).getId()))
                .findFirst().orElse(null);
    }

    /**
     * Adds an item to the data provider.
     *
     * @param item an item to be added
     */
    void addItem(DataItem item);

    /**
     * Adds a collection of data items to the data provider.
     *
     * @param items a collection of data items to be added
     */
    default void addItems(Collection<? extends DataItem> items) {
        for (DataItem item : items) {
            addItem(item);
        }
    }

    /**
     * Update an item in the data provider if it is already there.
     *
     * @param item an item to be updated
     */
    void updateItem(DataItem item);

    /**
     * Removes an item from the data provider.
     *
     * @param item an item to be removed
     */
    void removeItem(DataItem item);

    /**
     * Removes all items from the data provider.
     */
    void removeAll();

    /**
     * Adds listener to the data provider events.
     *
     * @param listener listener to be added
     */
    void addChangeListener(DataChangeListener listener);

    /**
     * Removes listener to data provider events
     *
     * @param listener listener to be removed
     */
    void removeChangeListener(DataChangeListener listener);
}