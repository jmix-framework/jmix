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

import elemental.json.JsonArray;

import java.util.List;

public abstract class AbstractLazyEventProviderManager extends AbstractEventProviderManager {

    protected List<CalendarEvent> lastFetchedEvents;

    public AbstractLazyEventProviderManager(LazyCalendarEventProvider eventProvider) {
        super(eventProvider, "_addLazyEventSource");
    }

    @Override
    public LazyCalendarEventProvider getEventProvider() {
        return (LazyCalendarEventProvider) super.getEventProvider();
    }

    public JsonArray fetchAndSerialize(LazyCalendarEventProvider.ItemsFetchContext context) {
        lastFetchedEvents = getEventProvider().onItemsFetch(context);

        return serializeData(lastFetchedEvents);
    }

    public JsonArray serializeData(List<CalendarEvent> calendarEvents) {
        return dataSerializer.serializeData(calendarEvents);
    }

    @Override
    public CalendarEvent getCalendarEvent(String clientId) {
        if (lastFetchedEvents == null || lastFetchedEvents.isEmpty()) {
            return null;
        }
        Object itemId = keyMapper.get(clientId);
        if (itemId != null) {
            return lastFetchedEvents.stream()
                    .filter(ce -> itemId.equals(ce.getId()))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
}
