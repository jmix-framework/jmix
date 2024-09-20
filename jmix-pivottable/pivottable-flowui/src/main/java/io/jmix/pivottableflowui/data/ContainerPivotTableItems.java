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

package io.jmix.pivottableflowui.data;

import com.vaadin.flow.shared.Registration;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.data.BindingState;
import io.jmix.flowui.data.ContainerDataUnit;
import io.jmix.flowui.kit.event.EventBus;
import io.jmix.flowui.model.CollectionChangeType;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.pivottableflowui.component.PivotTable;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Data provider bound to the {@link CollectionContainer} for a {@link PivotTable} component.
 *
 * @param <T> type of items contained
 */
public class ContainerPivotTableItems<T> implements ContainerDataUnit<T>, PivotTableItems<T> {

    protected CollectionContainer<T> container;
    protected Class<?> idType;

    private EventBus eventBus;

    public ContainerPivotTableItems(CollectionContainer<T> container, Class<?> idType) {
        Preconditions.checkNotNullArgument(container);

        this.container = container;
        this.idType = idType;

        initContainer(container);
    }

    protected void initContainer(CollectionContainer<T> container) {
        container.addCollectionChangeListener(this::containerCollectionChanged);
        container.addItemPropertyChangeListener(this::containerItemPropertyChanged);
    }

    protected void containerCollectionChanged(CollectionContainer.CollectionChangeEvent<T> event) {
        ItemsChangeType itemsChangeType = convertToItemsChangeType(event.getChangeType());

        getEventBus().fireEvent(new ItemsChangeEvent<>(this, itemsChangeType, event.getChanges()));
    }

    protected void containerItemPropertyChanged(CollectionContainer.ItemPropertyChangeEvent<T> event) {
        getEventBus().fireEvent(new ItemsChangeEvent<>(this,
                ItemsChangeType.UPDATE,
                Collections.singleton(event.getItem())
        ));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Registration addItemsChangeListener(Consumer<ItemsChangeEvent<T>> listener) {
        return getEventBus().addListener(ItemsChangeEvent.class, ((Consumer) listener));
    }

    @Override
    public Collection<T> getItems() {
        return container.getItems();
    }

    @Nullable
    @Override
    public <V> V getItemValue(T item, String propertyId) {
        return EntityValues.getValueEx(item, propertyId);
    }

    @Override
    public void setItemValue(T item, String propertyPath, @Nullable Object value) {
        EntityValues.setValueEx(item, propertyPath, value);
    }

    @Nullable
    public Object getItemId(T item) {
        return EntityValues.getId(item);
    }

    @Override
    public T getItem(Object itemId) {
        return container.getItem(itemId);
    }

    @Override
    public T getItem(String stringId) {
        Object id = deserializeId(stringId);
        return getItem(id);
    }

    @Override
    public void updateItem(T item) {
        container.replaceItem(item);
    }

    @Override
    public boolean containsItem(T item) {
        return container.containsItem(item);
    }

    @Override
    public Class<T> getType() {
        return container.getEntityMetaClass().getJavaClass();
    }

    @Override
    public CollectionContainer<T> getContainer() {
        return container;
    }

    @Override
    public MetaClass getEntityMetaClass() {
        return container.getEntityMetaClass();
    }

    @Override
    public BindingState getState() {
        return BindingState.ACTIVE;
    }

    @Override
    public Registration addStateChangeListener(Consumer<StateChangeEvent> listener) {
        return getEventBus().addListener(StateChangeEvent.class, listener);
    }

    protected Object deserializeId(String stringValue) {
        if (String.class == idType) {
            return stringValue;
        } else if (Integer.class == idType) {
            return Integer.valueOf(stringValue);
        } else if (Long.class == idType) {
            return Long.valueOf(stringValue);
        } else if (UUID.class == idType) {
            return UUID.fromString(stringValue);
        } else {
            throw new IllegalArgumentException(
                    String.format("Unable to deserialize id '%s' of type '%s'", stringValue, idType));
        }
    }

    protected ItemsChangeType convertToItemsChangeType(CollectionChangeType changeType) {
        return switch (changeType) {
            case REFRESH -> ItemsChangeType.REFRESH;
            case ADD_ITEMS -> ItemsChangeType.ADD;
            case REMOVE_ITEMS -> ItemsChangeType.REMOVE;
            case SET_ITEM -> ItemsChangeType.UPDATE;
        };
    }

    protected EventBus getEventBus() {
        if (eventBus == null) {
            eventBus = new EventBus();
        }

        return eventBus;
    }
}
