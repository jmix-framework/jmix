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

import com.vaadin.flow.data.provider.AbstractDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.event.EventBus;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Data provider for a Chart component in which all items are stored in {@link List}.
 */
public class ListChartItems<T extends DataItem> extends AbstractDataProvider<T, Void>
        implements ChartItems<T> {

    private EventBus eventBus;

    protected final List<T> items = new ArrayList<>();

    public ListChartItems() {
    }

    @SafeVarargs
    public ListChartItems(T... items) {
        this.items.addAll(List.of(items));
    }

    public ListChartItems(Collection<T> items) {
        this.items.addAll(items);
    }

    @Override
    public List<T> getItems() {
        return items;
    }

    @Override
    public T getItem(Object id) {
        return items.stream()
                .filter(dataItem -> Objects.equals(id, dataItem.getId()))
                .findAny()
                .orElse(null);
    }

    public void addItem(T item) {
        items.add(item);
        fireChangedEvent(DataChangeOperation.ADD, Collections.singletonList(item));
    }

    @SafeVarargs
    public final void addItems(T... items) {
        this.items.addAll(List.of(items));
        fireChangedEvent(DataChangeOperation.ADD, Arrays.stream(items).toList());
    }

    public void addItems(Collection<T> items) {
        this.items.addAll(items);
        fireChangedEvent(DataChangeOperation.ADD, List.copyOf(items));
    }

    /**
     * Update an item in the data provider if it is already there.
     *
     * @param item an item to be updated
     * @throws IllegalArgumentException if no such element found
     */
    public void updateItem(T item) {
        if (items.contains(item)) {
            items.set(items.indexOf(item), item);
            fireChangedEvent(DataChangeOperation.UPDATE, Collections.singletonList(item));
        } else {
            throw new IllegalArgumentException("No such element");
        }
    }

    public void removeItem(T item) {
        items.remove(item);
        fireChangedEvent(DataChangeOperation.REMOVE, Collections.singletonList(item));
    }

    public void removeAll() {
        items.clear();
        fireChangedEvent(DataChangeOperation.REFRESH, Collections.emptyList());
    }

    protected void fireChangedEvent(DataChangeOperation operation, List<T> items) {
        getEventBus().fireEvent(new ItemSetChangeEvent<>(this, operation, items));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Registration addItemSetChangeListener(Consumer<ItemSetChangeEvent<T>> listener) {
        return getEventBus().addListener(ItemSetChangeEvent.class, ((Consumer) listener));
    }

    @Override
    public boolean isInMemory() {
        return false;
    }

    @Override
    public int size(Query<T, Void> query) {
        return items.size();
    }

    @Override
    public Stream<T> fetch(Query<T, Void> query) {
        return items.stream()
                .skip(query.getOffset())
                .limit(query.getLimit());
    }

    protected EventBus getEventBus() {
        if (eventBus == null) {
            eventBus = new EventBus();
        }

        return eventBus;
    }
}
