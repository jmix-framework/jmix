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

package io.jmix.fullcalendarflowui.component.serialization;

import io.jmix.core.common.util.Preconditions;
import io.jmix.fullcalendarflowui.component.model.DayOfWeek;
import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.component.FullCalendarUtils;
import io.jmix.fullcalendarflowui.component.contextmenu.event.DayCell;
import io.jmix.fullcalendarflowui.component.contextmenu.event.FullCalendarCellContext;
import io.jmix.fullcalendarflowui.component.data.AbstractDataProviderManager;
import io.jmix.fullcalendarflowui.component.data.CalendarDataProvider;
import io.jmix.fullcalendarflowui.component.data.CalendarEvent;
import io.jmix.fullcalendarflowui.kit.component.event.MouseEventDetails;
import io.jmix.fullcalendarflowui.kit.component.serialization.JmixFullCalendarDeserializer;
import io.jmix.fullcalendarflowui.kit.component.model.dom.DomCalendarEvent;
import io.jmix.fullcalendarflowui.kit.component.model.dom.DomMouseEventDetails;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.Objects;

import static io.jmix.fullcalendarflowui.kit.component.CalendarDateTimeUtils.parseIsoDate;

public class FullCalendarDeserializer extends JmixFullCalendarDeserializer {

    public FullCalendarCellContext deserializeCalendarCellContext(ObjectNode json, FullCalendar calendar) {
        Preconditions.checkNotNullArgument(json);
        Preconditions.checkNotNullArgument(calendar);

        DayCell dayCell = json.has("dayCell")
                ? deserializeDayCell(json.get("dayCell"))
                : null;

        CalendarEvent event = null;
        CalendarDataProvider dataProvider = null;

        if (json.has("event")) {
            DomCalendarEvent domCalendarEvent = deserialize((ObjectNode) json.get("event"), DomCalendarEvent.class);
            AbstractDataProviderManager dataProviderManager = getDataProviderManager(domCalendarEvent, calendar);

            event = dataProviderManager.getCalendarEvent(domCalendarEvent.getId());
            if (event == null) {
                throw new IllegalStateException("Unable to find calendar event for client id: "
                        + domCalendarEvent.getId());
            }
            dataProvider = dataProviderManager.getDataProvider();
        }

        DomMouseEventDetails domMouseEventDetails =
                deserialize((ObjectNode) json.get("mouseDetails"), DomMouseEventDetails.class);

        return new FullCalendarCellContext(dayCell, event, dataProvider, new MouseEventDetails(domMouseEventDetails));
    }

    public DayCell deserializeDayCell(JsonNode json) {
        Preconditions.checkNotNullArgument(json);

        DayOfWeek dayOfWeek = DayOfWeek.fromId(json.get("dow").asInt());
        return new DayCell(parseIsoDate(json.get("date").asString()),
                json.get("isDisabled").asBoolean(),
                json.get("isFuture").asBoolean(),
                json.get("isOther").asBoolean(),
                json.get("isPast").asBoolean(),
                json.get("isToday").asBoolean(),
                Objects.requireNonNull(dayOfWeek));
    }

    public AbstractDataProviderManager getDataProviderManager(DomCalendarEvent domCalendarEvent,
                                                              FullCalendar calendar) {
        AbstractDataProviderManager dataProviderManager =
                FullCalendarUtils.getDataProviderManager(calendar, domCalendarEvent.getSourceId());
        if (dataProviderManager == null) {
            throw new IllegalStateException("Unable to find data provider for sourceId: "
                    + domCalendarEvent.getSourceId());
        }
        return dataProviderManager;
    }
}
