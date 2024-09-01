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

package io.jmix.fullcalendarflowui.component.data;

import com.vaadin.flow.data.provider.AbstractDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.event.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Event provider that set events from {@link List}.
 */
public class ListCalendarEventProvider extends AbstractDataProvider<CalendarEvent, Void>
        implements CalendarEventProvider {

    protected final String id;
    protected List<CalendarEvent> items = new ArrayList<>();

    protected EventBus eventBus = new EventBus();

    public ListCalendarEventProvider() {
        this.id = EventProviderUtils.generateId();
    }

    public ListCalendarEventProvider(String id) {
        this.id = id;
    }

    public ListCalendarEventProvider(List<CalendarEvent> items) {
        this(EventProviderUtils.generateId(), items);
    }

    public ListCalendarEventProvider(String id, List<CalendarEvent> items) {
        this.id = id;
        this.items = new ArrayList<>(items);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public List<CalendarEvent> getItems() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public CalendarEvent getItem(Object itemId) {
        return items.stream()
                .filter(e -> e.getId().equals(itemId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Adds calendar event.
     *
     * @param item item to add
     */
    public void addItem(CalendarEvent item) {
        items.add(item);

        fireItemSetChangeEvent(DataChangeOperation.ADD, Collections.singletonList(item));
    }

    /**
     * Adds calendar events.
     *
     * @param items items to add
     */
    public final void addItems(CalendarEvent... items) {
        addItems(List.of(items));
    }

    /**
     * Adds a list of calendar events.
     *
     * @param items items to add
     */
    public void addItems(List<CalendarEvent> items) {
        this.items.addAll(items);

        fireItemSetChangeEvent(DataChangeOperation.ADD, items);
    }

    /**
     * Replaces previous event by new one, if exists.
     *
     * @param item item to update
     * @throws IllegalArgumentException if event provider does not contain provided event
     */
    public void updateItem(CalendarEvent item) {
        if (items.contains(item)) {
            items.set(items.indexOf(item), item);
            fireItemSetChangeEvent(DataChangeOperation.UPDATE, Collections.singletonList(item));
        } else {
            throw new IllegalArgumentException("No such element");
        }
    }

    /**
     * Removes event.
     *
     * @param item item to remove
     */
    public void removeItem(CalendarEvent item) {
        items.remove(item);

        fireItemSetChangeEvent(DataChangeOperation.REMOVE, Collections.singletonList(item));
    }

    /**
     * Removes all events from event provider.
     */
    public void removeAllItems() {
        // todo rp discuss
        List<CalendarEvent> removedItems = new ArrayList<>(items);
        items.clear();

        fireItemSetChangeEvent(DataChangeOperation.REMOVE, removedItems);
    }

    @Override
    public Registration addItemSetChangeListener(Consumer<ItemSetChangeEvent> listener) {
        return eventBus.addListener(ItemSetChangeEvent.class, listener);
    }

    @Override
    public boolean isInMemory() {
        return true;
    }

    @Override
    public int size(Query<CalendarEvent, Void> query) {
        return items.size();
    }

    @Override
    public Stream<CalendarEvent> fetch(Query<CalendarEvent, Void> query) {
        return items.stream()
                .skip(query.getOffset())
                .limit(query.getLimit());
    }

    protected void fireItemSetChangeEvent(DataChangeOperation operation, List<CalendarEvent> items) {
        eventBus.fireEvent(new ItemSetChangeEvent(this, operation, items));
    }
}
