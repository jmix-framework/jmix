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

    public void addItem(CalendarEvent item) {
        items.add(item);

        fireItemSetChangeEvent(ItemChangeOperation.ADD, Collections.singletonList(item));
    }

    @SafeVarargs
    public final void addItems(CalendarEvent... items) {
        addItems(List.of(items));
    }

    public void addItems(List<CalendarEvent> items) {
        this.items.addAll(items);

        fireItemSetChangeEvent(ItemChangeOperation.ADD, items);
    }

    public void updateItem(CalendarEvent item) {
        if (items.contains(item)) {
            items.set(items.indexOf(item), item);
            fireItemSetChangeEvent(ItemChangeOperation.UPDATE, Collections.singletonList(item));
        } else {
            throw new IllegalArgumentException("No such element");
        }
    }

    public void removeItem(CalendarEvent item) {
        items.remove(item);

        fireItemSetChangeEvent(ItemChangeOperation.REMOVE, Collections.singletonList(item));
    }

    public void removeAllItems() {
        // todo rp discuss
        List<CalendarEvent> removedItems = new ArrayList<>(items);
        items.clear();

        fireItemSetChangeEvent(ItemChangeOperation.REMOVE, removedItems);
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

    protected void fireItemSetChangeEvent(ItemChangeOperation operation, List<CalendarEvent> items) {
        eventBus.fireEvent(new ItemSetChangeEvent(this, operation, items));
    }
}
