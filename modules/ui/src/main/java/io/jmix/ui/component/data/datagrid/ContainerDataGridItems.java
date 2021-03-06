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

package io.jmix.ui.component.data.datagrid;

import io.jmix.core.Sort;
import io.jmix.core.common.event.EventHub;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.JmixEntity;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.PropertyPath;
import io.jmix.ui.component.AggregationInfo;
import io.jmix.ui.component.data.AggregatableDataGridItems;
import io.jmix.ui.component.data.BindingState;
import io.jmix.ui.component.data.meta.ContainerDataUnit;
import io.jmix.ui.component.data.DataGridItems;
import io.jmix.ui.component.data.meta.EntityDataGridItems;
import io.jmix.ui.gui.data.impl.AggregatableDelegate;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.model.HasLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ContainerDataGridItems<E extends JmixEntity>
        implements EntityDataGridItems<E>, AggregatableDataGridItems<E>, DataGridItems.Sortable<E>, ContainerDataUnit<E> {

    private static final Logger log = LoggerFactory.getLogger(ContainerDataGridItems.class);

    protected CollectionContainer<E> container;

    protected boolean suppressSorting;

    protected AggregatableDelegate aggregatableDelegate;

    protected EventHub events = new EventHub();

    public ContainerDataGridItems(CollectionContainer<E> container, AggregatableDelegate aggregatableDelegate) {
        this.container = container;
        this.container.addItemChangeListener(this::containerItemChanged);
        this.container.addCollectionChangeListener(this::containerCollectionChanged);
        this.container.addItemPropertyChangeListener(this::containerItemPropertyChanged);

        this.aggregatableDelegate = aggregatableDelegate;
        initAggregatableDelegate();
    }

    @Override
    public CollectionContainer<E> getContainer() {
        return container;
    }

    protected void containerItemChanged(CollectionContainer.ItemChangeEvent<E> event) {
        events.publish(DataGridItems.SelectedItemChangeEvent.class, new DataGridItems.SelectedItemChangeEvent<>(this, event.getItem()));
    }

    protected void containerCollectionChanged(@SuppressWarnings("unused") CollectionContainer.CollectionChangeEvent<E> e) {
        events.publish(DataGridItems.ItemSetChangeEvent.class, new DataGridItems.ItemSetChangeEvent<>(this));
    }

    @SuppressWarnings("unchecked")
    protected void containerItemPropertyChanged(CollectionContainer.ItemPropertyChangeEvent<E> e) {
        events.publish(DataGridItems.ValueChangeEvent.class, new DataGridItems.ValueChangeEvent(this,
                e.getItem(), e.getProperty(), e.getPrevValue(), e.getValue()));
    }

    @Nullable
    @Override
    public MetaClass getEntityMetaClass() {
        return container.getEntityMetaClass();
    }

    @Override
    public BindingState getState() {
        return BindingState.ACTIVE;
    }

    @Nullable
    @Override
    public Object getItemId(E item) {
        return EntityValues.getId(item);
    }

    @Nullable
    @Override
    public E getItem(@Nullable Object itemId) {
        return itemId == null ? null : container.getItemOrNull(itemId);
    }

    @Override
    public int indexOfItem(E item) {
        return container.getItemIndex(EntityValues.getId(item));
    }

    @Nullable
    @Override
    public E getItemByIndex(int index) {
        return container.getItems().get(index);
    }

    @Override
    public Stream<E> getItems() {
        return container.getItems().stream();
    }

    @Override
    public List<E> getItems(int startIndex, int numberOfItems) {
        return container.getItems().subList(startIndex, startIndex + numberOfItems);
    }

    @Override
    public boolean containsItem(E item) {
        return container.getItemOrNull(EntityValues.getId(item)) != null;
    }

    @Override
    public int size() {
        return container.getItems().size();
    }

    @Nullable
    @Override
    public E getSelectedItem() {
        return container.getItemOrNull();
    }

    @Override
    public void setSelectedItem(@Nullable E item) {
        container.setItem(item);
    }

    @Override
    public Subscription addStateChangeListener(Consumer<StateChangeEvent> listener) {
        return events.subscribe(StateChangeEvent.class, listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Subscription addValueChangeListener(Consumer<ValueChangeEvent<E>> listener) {
        return events.subscribe(ValueChangeEvent.class, (Consumer) listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Subscription addItemSetChangeListener(Consumer<ItemSetChangeEvent<E>> listener) {
        return events.subscribe(ItemSetChangeEvent.class, (Consumer) listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Subscription addSelectedItemChangeListener(Consumer<SelectedItemChangeEvent<E>> listener) {
        return events.subscribe(SelectedItemChangeEvent.class, (Consumer) listener);
    }

    @Override
    public void sort(Object[] propertyId, boolean[] ascending) {
        if (container.getSorter() != null) {
            if (suppressSorting && container instanceof HasLoader && ((HasLoader) container).getLoader() instanceof CollectionLoader) {
                ((CollectionLoader) ((HasLoader) container).getLoader()).setSort(createSort(propertyId, ascending));
            } else {
                container.getSorter().sort(createSort(propertyId, ascending));
            }
        } else {
            log.debug("Container {} sorter is null", container);
        }
    }

    protected Sort createSort(Object[] propertyId, boolean[] ascending) {
        List<Sort.Order> orders = new ArrayList<>();
        for (int i = 0; i < propertyId.length; i++) {
            String property;
            if (propertyId[i] instanceof MetaPropertyPath) {
                property = ((MetaPropertyPath) propertyId[i]).toPathString();
            } else {
                property = (String) propertyId[i];
            }
            Sort.Order order = ascending[i] ? Sort.Order.asc(property) : Sort.Order.desc(property);
            orders.add(order);
        }
        return Sort.by(orders);
    }

    @Override
    public void resetSortOrder() {
        if (container.getSorter() != null) {
            if (suppressSorting && container instanceof HasLoader && ((HasLoader) container).getLoader() instanceof CollectionLoader) {
                ((CollectionLoader) ((HasLoader) container).getLoader()).setSort(Sort.UNSORTED);
            } else {
                container.getSorter().sort(Sort.UNSORTED);
            }
        } else {
            log.debug("Container {} sorter is null", container);
        }
    }

    @Override
    public void suppressSorting() {
        suppressSorting = true;
    }

    @Override
    public void enableSorting() {
        suppressSorting = false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<AggregationInfo, String> aggregate(@Nullable AggregationInfo[] aggregationInfos, Collection<?> itemIds) {
        return aggregatableDelegate.aggregate(aggregationInfos, itemIds);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<AggregationInfo, Object> aggregateValues(@Nullable AggregationInfo[] aggregationInfos, Collection<?> itemIds) {
        return aggregatableDelegate.aggregateValues(aggregationInfos, itemIds);
    }

    @SuppressWarnings("rawtypes")
    protected void initAggregatableDelegate() {
        aggregatableDelegate.setItemProvider(container::getItem);
        aggregatableDelegate.setItemValueProvider((property, itemId) -> EntityValues.getValueEx(container.getItem(itemId), (PropertyPath) property));
    }
}
