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

package io.jmix.flowui.data.items;

import com.vaadin.flow.data.provider.AbstractDataProvider;
import com.vaadin.flow.data.provider.InMemoryDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.data.BindingState;
import io.jmix.flowui.data.ContainerDataUnit;
import io.jmix.flowui.data.EntityItems;
import io.jmix.flowui.kit.event.EventBus;
import io.jmix.flowui.model.CollectionChangeType;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.model.HasLoader;

import jakarta.annotation.Nullable;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ContainerDataProvider<E> extends AbstractDataProvider<E, SerializablePredicate<E>>
        implements InMemoryDataProvider<E>, ContainerDataUnit<E>, EntityItems<E> {

    protected CollectionContainer<E> container;
    protected DataLoader loader;

    protected E deferredSelectedItem;

    protected SerializableComparator<E> sortOrder;
    protected SerializablePredicate<E> filter;

    private EventBus eventBus;

    public ContainerDataProvider(CollectionContainer<E> container) {
        Preconditions.checkNotNullArgument(container);

        this.container = container;
        initContainer(container);
    }

    private void initContainer(CollectionContainer<E> container) {
        if (container instanceof HasLoader) {
            loader = ((HasLoader) container).getLoader();
        }

        container.addCollectionChangeListener(this::containerCollectionChanged);
        container.addItemPropertyChangeListener(this::containerItemPropertyChanged);
    }

    protected void containerCollectionChanged(@SuppressWarnings("unused") CollectionContainer.CollectionChangeEvent<E> e) {
        if (deferredSelectedItem != null) {
            // UI components (e.g. ComboBox) can have value that does not exist in container
            if (container.containsItem(deferredSelectedItem)) {
                container.setItem(deferredSelectedItem);
            }
            deferredSelectedItem = null;
        }

        if (e.getChangeType() == CollectionChangeType.SET_ITEM) {
            e.getChanges().forEach(this::refreshItem);
        } else {
            getEventBus().fireEvent(new ItemsChangeEvent<>(this, container.getItems()));
        }
    }

    protected void containerItemPropertyChanged(CollectionContainer.ItemPropertyChangeEvent<E> e) {
        refreshItem(e.getItem());
    }

    @Override
    public MetaClass getEntityMetaClass() {
        return container.getEntityMetaClass();
    }

    @Override
    public CollectionContainer<E> getContainer() {
        return container;
    }

    @Override
    public void setSelectedItem(@Nullable E item) {
        if (item == null) {
            container.setItem(null);

        } else if (!container.getItems().isEmpty()) {
            // UI components (e.g. ComboBox) can have value that does not exist in container
            if (container.containsItem(item)) {
                container.setItem(item);
            } else {
                this.deferredSelectedItem = item;
            }
        } else {
            this.deferredSelectedItem = item;
        }
    }

    @Override
    public boolean containsItem(@Nullable E item) {
        return item != null && container.containsItem(item);
    }

    @Override
    public void updateItem(E item) {
        container.replaceItem(item);
    }

    @Override
    public void refresh() {
        if (loader != null) {
            loader.load();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Registration addItemsChangeListener(Consumer<ItemsChangeEvent<E>> listener) {
        return getEventBus().addListener(ItemsChangeEvent.class, ((Consumer) listener));
    }

    @Override
    public int size(Query<E, SerializablePredicate<E>> query) {
        return (int) getFilteredItems(query).count();
    }

    @Override
    public Stream<E> fetch(Query<E, SerializablePredicate<E>> query) {
        Stream<E> items = getFilteredItems(query);

        // TODO: gg, needs testing
        Optional<Comparator<E>> comparing = Stream
                .of(query.getInMemorySorting(), sortOrder)
                .filter(Objects::nonNull)
                .reduce(Comparator::thenComparing);

        if (comparing.isPresent()) {
            items = items.sorted(comparing.get());
        }

        return items
                .skip(query.getOffset())
                .limit(query.getLimit());
    }

    protected Stream<E> getFilteredItems(Query<E, SerializablePredicate<E>> query) {
        Stream<E> items = getItems();

        // Apply our own filters first so that query filters never see the items
        // that would already have been filtered out
        if (filter != null) {
            items = items.filter(filter);
        }

        return query.getFilter()
                .map(items::filter)
                .orElse(items);
    }

    protected Stream<E> getItems() {
        return container.getItems().stream();
    }

    @Override
    public SerializablePredicate<E> getFilter() {
        return filter;
    }

    @Override
    public void setFilter(SerializablePredicate<E> filter) {
        if (!Objects.equals(this.filter, filter)) {
            this.filter = filter;
            refreshAll();
        }
    }

    @Override
    public SerializableComparator<E> getSortComparator() {
        return sortOrder;
    }

    @Override
    public void setSortComparator(SerializableComparator<E> comparator) {
        if (!Objects.equals(this.sortOrder, comparator)) {
            this.sortOrder = comparator;
            refreshAll();
        }
    }

    @Override
    public BindingState getState() {
        return BindingState.ACTIVE;
    }

    @Override
    public Registration addStateChangeListener(Consumer<StateChangeEvent> listener) {
        return getEventBus().addListener(StateChangeEvent.class, listener);
    }

    protected EventBus getEventBus() {
        if (eventBus == null) {
            eventBus = new EventBus();
        }

        return eventBus;
    }
}
