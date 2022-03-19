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

import io.jmix.core.Entity;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.data.*;
import io.jmix.ui.model.CollectionContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Data provider which contains {@link CollectionContainer} with items.
 */
public class ContainerDataProvider implements DataProvider, HasMetaClass {

    protected final CollectionContainer dataContainer;

    protected final Consumer<CollectionContainer.CollectionChangeEvent> collectionChangeListener;
    protected final List<DataChangeListener> changeListeners = new ArrayList<>();

    public ContainerDataProvider(CollectionContainer dataContainer) {
        this.dataContainer = dataContainer;

        collectionChangeListener = e -> {
            DataChangeOperation operation = null;
            switch (e.getChangeType()) {
                case REFRESH:
                    operation = DataChangeOperation.REFRESH;
                    break;
                case SET_ITEM:
                    operation = DataChangeOperation.UPDATE;
                    break;
                case ADD_ITEMS:
                    operation = DataChangeOperation.ADD;
                    break;
                case REMOVE_ITEMS:
                    operation = DataChangeOperation.REMOVE;
                    break;
            }

            fireCollectionChangeListener(operation, e.getChanges());
        };

        //noinspection unchecked
        this.dataContainer.addCollectionChangeListener(collectionChangeListener);
    }

    protected void fireCollectionChangeListener(DataChangeOperation operation, Collection changedItems) {
        List<DataItem> dataItems;

        if (!changedItems.isEmpty()) {
            dataItems = new ArrayList<>();
            for (Object object : changedItems) {
                Entity entity = (Entity) object;

                dataItems.add(new EntityDataItem(entity));
            }
        } else {
            dataItems = Collections.emptyList();
        }

        DataItemsChangeEvent dataItemsChangeEvent = new DataItemsChangeEvent(operation, dataItems);
        for (DataChangeListener listener : new ArrayList<>(changeListeners)) {
            listener.dataItemsChanged(dataItemsChangeEvent);
        }
    }

    @Override
    public List<DataItem> getItems() {
        List<DataItem> dataItems = new ArrayList<>(dataContainer.getItems().size());

        for (Object object : dataContainer.getItems()) {
            Entity entity = (Entity) object;

            dataItems.add(new EntityDataItem(entity));
        }

        return dataItems;
    }

    @Override
    public DataItem getItem(Object id) {
        Entity entity = (Entity) dataContainer.getItemOrNull(id);
        return entity == null ? null : new EntityDataItem(entity);
    }

    /**
     * Unsupported. Always throws an {@link UnsupportedOperationException}.
     * Use CollectionContainer for changing data items of ContainerDataProvider
     *
     * @throws UnsupportedOperationException use CollectionContainer for changing data items of ContainerDataProvider
     */
    @Override
    public void addItem(DataItem item) {
        throw new UnsupportedOperationException("Use CollectionContainer for changing data items of ContainerDataProvider");
    }

    /**
     * Unsupported. Always throws an {@link UnsupportedOperationException}.
     * Use CollectionContainer for changing data items of ContainerDataProvider
     *
     * @throws UnsupportedOperationException use CollectionContainer for changing data items of ContainerDataProvider
     */
    @Override
    public void addItems(Collection<? extends DataItem> items) {
        throw new UnsupportedOperationException("Use CollectionContainer for changing data items of ContainerDataProvider");
    }

    /**
     * Unsupported. Always throws an {@link UnsupportedOperationException}.
     * Use CollectionContainer for changing data items of ContainerDataProvider
     *
     * @throws UnsupportedOperationException use CollectionContainer for changing data items of ContainerDataProvider
     */
    @Override
    public void updateItem(DataItem item) {
        throw new UnsupportedOperationException("Use CollectionContainer for changing data items of ContainerDataProvider");
    }

    /**
     * Unsupported. Always throws an {@link UnsupportedOperationException}.
     * Use CollectionContainer for changing data items of ContainerDataProvider
     *
     * @throws UnsupportedOperationException use CollectionContainer for changing data items of ContainerDataProvider
     */
    @Override
    public void removeItem(DataItem item) {
        throw new UnsupportedOperationException("Use CollectionContainer for changing data items of ContainerDataProvider");
    }

    @Override
    public void removeAll() {
        throw new UnsupportedOperationException("Use CollectionContainer for changing data items of ContainerDataProvider");
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

    @Override
    public MetaClass getMetaClass() {
        return dataContainer.getEntityMetaClass();
    }

    /**
     * @return CollectionContainer instance
     */
    public CollectionContainer getCollectionContainer() {
        return dataContainer;
    }
}
