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

package io.jmix.fullcalendarflowui.kit.component.data;

import elemental.json.JsonValue;
import io.jmix.fullcalendarflowui.kit.component.serialization.model.IncrementalData;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractItemEventProviderManager extends AbstractEventProviderManager {

    protected Consumer<ItemCalendarEventProvider.ItemSetChangeEvent> itemSetChangeListener;
    protected List<Pair<ItemChangeOperation, Collection<?>>> pendingIncrementalChanges = new ArrayList<>();

    public AbstractItemEventProviderManager(ItemCalendarEventProvider eventProvider) {
        super(eventProvider, "_addItemEventSource");

        eventProvider.addItemSetChangeListener(this::onItemSetChangeListener);
    }

    @Override
    public ItemCalendarEventProvider getEventProvider() {
        return (ItemCalendarEventProvider) super.getEventProvider();
    }

    @Override
    public CalendarEvent getCalendarEvent(String clientId) {
        Object itemId = keyMapper.get(clientId);
        return itemId == null ? null : getEventProvider().getItem(itemId);
    }

    public JsonValue serializeData() {
        return dataSerializer.serializeData(((ItemCalendarEventProvider) eventProvider).getItems());
    }

    public List<JsonValue> serializeIncrementalData() {
        if (pendingIncrementalChanges== null || pendingIncrementalChanges.isEmpty()) {
            return Collections.emptyList();
        }
        return pendingIncrementalChanges.stream()
                .map(change -> dataSerializer.serializeIncrementalData(
                        new IncrementalData(sourceId, change.getFirst(), change.getSecond())))
                .toList();
    }

    public void addIncrementalChange(ItemChangeOperation operation, Collection<?> items) {
        pendingIncrementalChanges.add(new Pair<>(operation, items));
    }

    public void clearIncrementalData() {
        pendingIncrementalChanges.clear();
    }

    @Nullable
    public Consumer<ItemCalendarEventProvider.ItemSetChangeEvent> getItemSetChangeListener() {
        return itemSetChangeListener;
    }

    public void setItemSetChangeListener(@Nullable Consumer<ItemCalendarEventProvider.ItemSetChangeEvent> itemSetChangeListener) {
        this.itemSetChangeListener = itemSetChangeListener;
    }

    protected void onItemSetChangeListener(ItemCalendarEventProvider.ItemSetChangeEvent event) {
        if (itemSetChangeListener != null) {
            itemSetChangeListener.accept(event);
        }
    }

    // todo rp rework
    protected static class Pair<F, S> {
        protected F first;
        protected S second;

        public Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }

        public F getFirst() {
            return first;
        }

        public S getSecond() {
            return second;
        }
    }
}
