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

package io.jmix.ui.components.data.table;

import io.jmix.core.Sort;
import io.jmix.core.commons.events.EventHub;
import io.jmix.core.commons.events.Subscription;
import io.jmix.core.Entity;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.components.AggregationInfo;
import io.jmix.ui.components.data.AggregatableTableItems;
import io.jmix.ui.components.data.BindingState;
import io.jmix.ui.components.data.TableItems;
import io.jmix.ui.components.data.meta.ContainerDataUnit;
import io.jmix.ui.components.data.meta.EntityTableItems;
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
import java.util.stream.Collectors;

import static io.jmix.core.commons.util.Preconditions.checkNotNullArgument;

public class ContainerTableItems<E extends Entity> implements EntityTableItems<E>, TableItems.Sortable<E>,
        ContainerDataUnit<E>, AggregatableTableItems<E> {

    private static final Logger log = LoggerFactory.getLogger(ContainerTableItems.class);

    protected CollectionContainer<E> container;

    protected AggregatableDelegate aggregatableDelegate;

    protected boolean suppressSorting;

    protected EventHub events = new EventHub();

    public ContainerTableItems(CollectionContainer<E> container) {
        this.container = container;
        this.container.addItemChangeListener(this::containerItemChanged);
        this.container.addCollectionChangeListener(this::containerCollectionChanged);
        this.container.addItemPropertyChangeListener(this::containerItemPropertyChanged);

        this.aggregatableDelegate = createAggregatableDelegate();
    }

    public AggregatableDelegate createAggregatableDelegate() {
        return new AggregatableDelegate() {
            @Override
            public Object getItem(Object itemId) {
                return ContainerTableItems.this.getItem(itemId);
            }

            @Override
            public Object getItemValue(MetaPropertyPath property, Object itemId) {
                return ContainerTableItems.this.getItemValue(itemId, property);
            }
        };
    }

    public CollectionContainer<E> getContainer() {
        return container;
    }

    protected void containerItemChanged(CollectionContainer.ItemChangeEvent<E> event) {
        events.publish(SelectedItemChangeEvent.class, new SelectedItemChangeEvent<>(this, event.getItem()));
    }

    protected void containerCollectionChanged(@SuppressWarnings("unused") CollectionContainer.CollectionChangeEvent<E> e) {
        events.publish(ItemSetChangeEvent.class, new ItemSetChangeEvent<>(this));
    }

    @SuppressWarnings("unchecked")
    protected void containerItemPropertyChanged(CollectionContainer.ItemPropertyChangeEvent<E> e) {
        events.publish(ValueChangeEvent.class, new ValueChangeEvent(this,
                e.getItem(), e.getProperty(), e.getPrevValue(), e.getValue()));
    }

    @Override
    public Collection<?> getItemIds() {
        return container.getItems().stream().map(e -> EntityValues.getId(e)).collect(Collectors.toList());
    }

    @Override
    public Collection<E> getItems() {
        return container.getItems();
    }

    @Nullable
    @Override
    public E getItem(Object itemId) {
        return container.getItemOrNull(itemId);
    }

    @Override
    public E getItemNN(Object itemId) {
        return container.getItem(itemId);
    }

    @Override
    public void updateItem(E item) {
        checkNotNullArgument(item, "item is null");

        if (container.containsItem(EntityValues.getId(item))) {
            container.replaceItem(item);
        }
    }

    @Override
    public Object getItemValue(Object itemId, Object propertyId) {
        MetaPropertyPath propertyPath = (MetaPropertyPath) propertyId;
        return EntityValues.getValueEx(container.getItem(itemId), propertyPath);
    }

    @Override
    public int size() {
        return container.getItems().size();
    }

    @Override
    public boolean containsId(Object itemId) {
        return container.getItems().stream().anyMatch(e -> EntityValues.getId(e).equals(itemId));
    }

    @Override
    public BindingState getState() {
        return BindingState.ACTIVE;
    }

    @Override
    public Class<?> getType(Object propertyId) {
        MetaPropertyPath propertyPath = (MetaPropertyPath) propertyId;
        return propertyPath.getRangeJavaClass();
    }

    @Override
    public boolean supportsProperty(Object propertyId) {
        return propertyId instanceof MetaPropertyPath;
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
    public MetaClass getEntityMetaClass() {
        return container.getEntityMetaClass();
    }

    @Override
    public Object nextItemId(Object itemId) {
        List<E> items = container.getItems();
        int index = container.getItemIndex(itemId);
        return index == items.size() - 1 ? null : EntityValues.getId(items.get(index + 1));
    }

    @Override
    public Object prevItemId(Object itemId) {
        int index = container.getItemIndex(itemId);
        return index <= 0 ? null : EntityValues.getId(container.getItems().get(index - 1));
    }

    @Override
    public Object firstItemId() {
        List<E> items = container.getItems();
        return items.isEmpty() ? null : EntityValues.getId(items.get(0));
    }

    @Override
    public Object lastItemId() {
        List<E> items = container.getItems();
        if (items.isEmpty()) {
            return null;
        }
        return EntityValues.getId(items.get(items.size() - 1));
    }

    @Override
    public boolean isFirstId(Object itemId) {
        int index = container.getItemIndex(itemId);
        return index == 0;
    }

    @Override
    public boolean isLastId(Object itemId) {
        int index = container.getItemIndex(itemId);
        return index == container.getItems().size() - 1;
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


    @SuppressWarnings("unchecked")
    @Override
    public Map<AggregationInfo, String> aggregate(AggregationInfo[] aggregationInfos, Collection<?> itemIds) {
        return aggregatableDelegate.aggregate(aggregationInfos, itemIds);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<AggregationInfo, Object> aggregateValues(AggregationInfo[] aggregationInfos, Collection<?> itemIds) {
        return aggregatableDelegate.aggregateValues(aggregationInfos, itemIds);
    }

    @Override
    public void suppressSorting() {
        suppressSorting = true;
    }

    @Override
    public void enableSorting() {
        suppressSorting = false;
    }
}
