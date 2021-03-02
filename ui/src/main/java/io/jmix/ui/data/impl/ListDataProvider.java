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

package io.jmix.ui.data.impl;

import io.jmix.ui.data.*;

import java.util.*;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * Data provider in which all items are stored in {@link List}.
 */
public class ListDataProvider implements DataProvider {

    private static final long serialVersionUID = -6810317342142985329L;

    protected final List<DataItem> items = new ArrayList<>();
    protected final List<DataChangeListener> changeListeners = new ArrayList<>();

    public ListDataProvider() {
    }

    public ListDataProvider(DataItem... items) {
        if (items != null) {
            this.items.addAll(Arrays.asList(items));
        }
    }

    public ListDataProvider(List<? extends DataItem> items) {
        checkNotNull(items);

        this.items.addAll(items);
    }

    @Override
    public List<DataItem> getItems() {
        return items;
    }

    @Override
    public DataItem getItem(Object id) {
        return items.stream()
                .filter(dataItem -> (dataItem instanceof DataItem.HasId)
                        && Objects.equals(id, ((DataItem.HasId) dataItem).getId()))
                .findFirst().orElse(null);
    }

    @Override
    public void addItem(DataItem item) {
        items.add(item);
        fireDataChanged(DataChangeOperation.ADD, Collections.singletonList(item));
    }

    @Override
    public void addItems(Collection<? extends DataItem> items) {
        this.items.addAll(items);
        fireDataChanged(DataChangeOperation.ADD, new ArrayList<>(items));
    }

    /**
     * Update an item in the data provider if it is already there.
     *
     * @param item an item to be updated
     * @throws IllegalArgumentException if no such element found
     */
    @Override
    public void updateItem(DataItem item) {
        int i = items.indexOf(item);
        if (i >= 0) {
            items.set(i, item);
            fireDataChanged(DataChangeOperation.UPDATE, Collections.singletonList(item));
        } else {
            throw new IllegalArgumentException("No such element");
        }
    }

    @Override
    public void removeItem(DataItem item) {
        items.remove(item);
        fireDataChanged(DataChangeOperation.REMOVE, Collections.singletonList(item));
    }

    @Override
    public void removeAll() {
        items.clear();
        fireDataChanged(DataChangeOperation.REFRESH, Collections.emptyList());
    }

    protected void fireDataChanged(DataChangeOperation operation, List<DataItem> items) {
        DataItemsChangeEvent event = new DataItemsChangeEvent(operation, items);
        List<DataChangeListener> changeListeners = new ArrayList<>(this.changeListeners);
        for (DataChangeListener listener : changeListeners) {
            listener.dataItemsChanged(event);
        }
    }

    @Override
    public void addChangeListener(DataChangeListener listener) {
        if (!changeListeners.contains(listener)) {
            changeListeners.add(listener);
        }
    }

    @Override
    public void removeChangeListener(DataChangeListener listener) {
        changeListeners.remove(listener);
    }
}