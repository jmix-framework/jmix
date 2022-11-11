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

import com.vaadin.flow.data.provider.AbstractDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.Sort;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.data.BindingState;
import io.jmix.flowui.data.ContainerDataUnit;
import io.jmix.flowui.kit.event.EventBus;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.HasLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ContainerDataGridItems<T> extends AbstractDataProvider<T, Void>
        implements ContainerDataUnit<T>, EntityDataGridItems<T>, DataGridItems.Sortable<T> {

    private static final Logger log = LoggerFactory.getLogger(ContainerDataGridItems.class);

    protected CollectionContainer<T> container;

    protected boolean suppressSorting;

    private EventBus eventBus;

    public ContainerDataGridItems(CollectionContainer<T> container) {
        Preconditions.checkNotNullArgument(container);

        this.container = container;
        initContainer(container);
    }

    protected void initContainer(CollectionContainer<T> container) {
        container.addItemChangeListener(this::containerItemChanged);
        container.addCollectionChangeListener(this::containerCollectionChanged);
        container.addItemPropertyChangeListener(this::containerItemPropertyChanged);
    }

    protected void containerItemChanged(CollectionContainer.ItemChangeEvent<T> event) {
        getEventBus().fireEvent(new SelectedItemChangeEvent<>(this, event.getItem()));
    }

    protected void containerCollectionChanged(@SuppressWarnings("unused") CollectionContainer.CollectionChangeEvent<T> event) {
        refreshAll();

        getEventBus().fireEvent(new ItemSetChangeEvent<>(this));
    }

    protected void containerItemPropertyChanged(CollectionContainer.ItemPropertyChangeEvent<T> event) {
        refreshItem(event.getItem());

        getEventBus().fireEvent(new ValueChangeEvent<>(this, event.getItem(),
                event.getProperty(), event.getPrevValue(), event.getValue()));
    }

    @Nullable
    @Override
    public T getSelectedItem() {
        return container.getItemOrNull();
    }

    @Override
    public void setSelectedItem(@Nullable T item) {
        container.setItem(item);
    }

    @Override
    public boolean containsItem(T item) {
        return container.containsItem(item);
    }

    @Override
    public void sort(Object[] propertyId, boolean[] ascending) {
        if (container.getSorter() != null) {
            if (suppressSorting
                    && container instanceof HasLoader
                    && ((HasLoader) container).getLoader() instanceof CollectionLoader) {
                ((CollectionLoader<?>) ((HasLoader) container).getLoader())
                        .setSort(createSort(propertyId, ascending));
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
            if (suppressSorting
                    && container instanceof HasLoader
                    && ((HasLoader) container).getLoader() instanceof CollectionLoader) {
                ((CollectionLoader<?>) ((HasLoader) container).getLoader()).setSort(Sort.UNSORTED);
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Registration addValueChangeListener(Consumer<ValueChangeEvent<T>> listener) {
        return getEventBus().addListener(ValueChangeEvent.class, ((Consumer) listener));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Registration addItemSetChangeListener(Consumer<ItemSetChangeEvent<T>> listener) {
        return getEventBus().addListener(ItemSetChangeEvent.class, ((Consumer) listener));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Registration addSelectedItemChangeListener(Consumer<SelectedItemChangeEvent<T>> listener) {
        return getEventBus().addListener(SelectedItemChangeEvent.class, ((Consumer) listener));
    }

    @Override
    public boolean isInMemory() {
        return true;
    }

    @Override
    public int size(Query<T, Void> query) {
        if (getState() == BindingState.INACTIVE) {
            return 0;
        }
        return Math.toIntExact(fetch(query).count());
    }

    @Override
    public Stream<T> fetch(Query<T, Void> query) {
        if (getState() == BindingState.INACTIVE) {
            return Stream.empty();
        }

        return getItems()
                .skip(query.getOffset())
                .limit(query.getLimit());
    }

    protected Stream<T> getItems() {
        return container.getItems().stream();
    }

    @Override
    public CollectionContainer<T> getContainer() {
        return container;
    }

    @Override
    public BindingState getState() {
        return BindingState.ACTIVE;
    }

    @Override
    public MetaClass getEntityMetaClass() {
        return container.getEntityMetaClass();
    }

    @Override
    public Registration addStateChangeListener(Consumer<StateChangeEvent> listener) {
        return getEventBus().addListener(StateChangeEvent.class, listener);
    }

    @Override
    public Class<T> getType() {
        return getEntityMetaClass().getJavaClass();
    }

    protected EventBus getEventBus() {
        if (eventBus == null) {
            eventBus = new EventBus();
        }

        return eventBus;
    }
}
