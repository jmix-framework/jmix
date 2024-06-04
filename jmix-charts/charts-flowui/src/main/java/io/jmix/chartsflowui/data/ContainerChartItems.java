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

package io.jmix.chartsflowui.data;

import com.vaadin.flow.data.provider.AbstractDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.shared.Registration;
import io.jmix.chartsflowui.component.Chart;
import io.jmix.chartsflowui.data.item.EntityDataItem;
import io.jmix.chartsflowui.kit.data.chart.ChartItems;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.data.BindingState;
import io.jmix.flowui.data.ContainerDataUnit;
import io.jmix.flowui.kit.event.EventBus;
import io.jmix.flowui.model.CollectionChangeType;
import io.jmix.flowui.model.CollectionContainer;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Data provider bound to the {@link CollectionContainer} for a {@link Chart} component.
 */
public class ContainerChartItems<T> extends AbstractDataProvider<EntityDataItem, Void>
        implements ContainerDataUnit<T>, ChartItems<EntityDataItem> {

    protected CollectionContainer<T> container;

    private EventBus eventBus;

    public ContainerChartItems(CollectionContainer<T> container) {
        Preconditions.checkNotNullArgument(container);

        this.container = container;
        initContainer(container);
    }

    protected void initContainer(CollectionContainer<T> container) {
        container.addCollectionChangeListener(this::onContainerCollectionChanged);
    }

    protected void onContainerCollectionChanged(CollectionContainer.CollectionChangeEvent<T> event) {
        DataChangeOperation operation = mapToChangeOperation(event.getChangeType());
        List<EntityDataItem> changes = mapToEntityDataItem(event.getChanges());

        getEventBus().fireEvent(new ItemSetChangeEvent<>(this, operation, changes));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Registration addItemSetChangeListener(Consumer<ItemSetChangeEvent<EntityDataItem>> listener) {
        return getEventBus().addListener(ItemSetChangeEvent.class, ((Consumer) listener));
    }

    @Override
    public List<EntityDataItem> getItems() {
        return mapToEntityDataItem(container.getItems());
    }

    @Nullable
    @Override
    public EntityDataItem getItem(Object id) {
        Object entity = container.getItemOrNull(id);
        return entity == null ? null : new EntityDataItem(entity);
    }

    @Override
    public MetaClass getEntityMetaClass() {
        return container.getEntityMetaClass();
    }

    @Override
    public CollectionContainer<T> getContainer() {
        return container;
    }

    @Override
    public boolean isInMemory() {
        return true;
    }

    @Override
    public int size(Query<EntityDataItem, Void> query) {
        if (getState() == BindingState.INACTIVE) {
            return 0;
        }
        return Math.toIntExact(fetch(query).count());
    }

    @Override
    public Stream<EntityDataItem> fetch(Query<EntityDataItem, Void> query) {
        if (getState() == BindingState.INACTIVE) {
            return Stream.empty();
        }

        return getItems()
                .stream()
                .skip(query.getOffset())
                .limit(query.getLimit());
    }

    @Override
    public BindingState getState() {
        return BindingState.ACTIVE;
    }

    @Override
    public Registration addStateChangeListener(Consumer<StateChangeEvent> listener) {
        return getEventBus().addListener(StateChangeEvent.class, listener);
    }

    protected List<EntityDataItem> mapToEntityDataItem(Collection<? extends T> collection) {
        return collection.stream()
                .map(EntityDataItem::new)
                .toList();
    }

    protected DataChangeOperation mapToChangeOperation(CollectionChangeType changeType) {
        return switch (changeType) {
            case REFRESH -> DataChangeOperation.REFRESH;
            case SET_ITEM -> DataChangeOperation.UPDATE;
            case ADD_ITEMS -> DataChangeOperation.ADD;
            case REMOVE_ITEMS -> DataChangeOperation.REMOVE;
        };
    }

    protected EventBus getEventBus() {
        if (eventBus == null) {
            eventBus = new EventBus();
        }

        return eventBus;
    }
}
