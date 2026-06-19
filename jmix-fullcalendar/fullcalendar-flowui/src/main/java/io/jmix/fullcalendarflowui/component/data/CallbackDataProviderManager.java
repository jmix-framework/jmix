/*
 * Copyright 2026 Haulmont.
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

import io.jmix.core.annotation.Internal;
import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.component.serialization.FullCalendarSerializer;
import tools.jackson.databind.node.ArrayNode;

import java.util.List;

/**
 * INTERNAL.
 * <p>
 * Data provider manager that works with {@link CallbackCalendarDataProvider}.
 */
@Internal
public class CallbackDataProviderManager extends AbstractDataProviderManager {

    protected List<CalendarEvent> lastFetchedEvents;

    public CallbackDataProviderManager(CallbackCalendarDataProvider dataProvider,
                                       FullCalendarSerializer serializer,
                                       FullCalendar fullCalendar) {
        super(dataProvider, serializer, fullCalendar, "_addLazyEventSource");
    }

    @Override
    public CallbackCalendarDataProvider getDataProvider() {
        return (CallbackCalendarDataProvider) super.getDataProvider();
    }

    public ArrayNode fetchAndSerialize(CallbackCalendarDataProvider.ItemsFetchContext context) {
        lastFetchedEvents = getDataProvider().onItemsFetch(context);

        return serializeData(lastFetchedEvents);
    }

    public ArrayNode serializeData(List<CalendarEvent> calendarEvents) {
        return dataSerializer.serializeData(calendarEvents);
    }

    @Override
    public CalendarEvent getCalendarEvent(String clientId) {
        if (lastFetchedEvents == null || lastFetchedEvents.isEmpty()) {
            return null;
        }
        Object itemId = eventKeyMapper.get(clientId);
        if (itemId != null) {
            return lastFetchedEvents.stream()
                    .filter(ce -> itemId.equals(ce.getId()))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
}
