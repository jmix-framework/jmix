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

package serialization;

import com.vaadin.flow.data.provider.KeyMapper;
import io.jmix.fullcalendarflowui.component.data.CalendarEvent;
import io.jmix.fullcalendarflowui.component.data.SimpleCalendarEvent;
import io.jmix.fullcalendarflowui.component.serialization.FullCalendarSerializer;
import io.jmix.fullcalendarflowui.component.serialization.FullCalendarSerializer.FullCalendarDataSerializer;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Checks how calendar event dates are sent to the client.
 */
class CalendarEventSerializationTest {

    static final LocalDateTime START = LocalDateTime.of(2026, 10, 20, 0, 0);
    static final LocalDateTime END = LocalDateTime.of(2026, 10, 22, 0, 0);

    @Test
    void serialize_allDayEvent_sendsDatesWithoutTimeAndOffset() {
        JsonNode json = serialize(createEvent(true), false);

        assertEquals("2026-10-20", json.get("start").asString());
        assertEquals("2026-10-22", json.get("end").asString());
    }

    @Test
    void serialize_allDayEventWithTime_sendsDateOfSystemTimeZone() {
        CalendarEvent event = SimpleCalendarEvent.create()
                .withAllDay(true)
                .withStartDateTime(LocalDateTime.of(2026, 10, 20, 23, 30))
                .build();

        JsonNode json = serialize(event, false);

        assertEquals("2026-10-20", json.get("start").asString());
    }

    @Test
    void serialize_eventWithoutAllDayAndDefaultAllDay_sendsDatesWithoutTimeAndOffset() {
        JsonNode json = serialize(createEvent(null), true);

        assertEquals("2026-10-20", json.get("start").asString());
        assertEquals("2026-10-22", json.get("end").asString());
    }

    @Test
    void serialize_timedEvent_sendsDatesWithSystemOffset() {
        JsonNode json = serialize(createEvent(false), true);

        assertEquals(withSystemOffset(START), json.get("start").asString());
        assertEquals(withSystemOffset(END), json.get("end").asString());
    }

    @Test
    void serialize_eventWithoutAllDay_sendsDatesWithSystemOffset() {
        JsonNode json = serialize(createEvent(null), false);

        assertEquals(withSystemOffset(START), json.get("start").asString());
    }

    CalendarEvent createEvent(@Nullable Boolean allDay) {
        return SimpleCalendarEvent.create()
                .withAllDay(allDay)
                .withStartDateTime(START)
                .withEndDateTime(END)
                .build();
    }

    JsonNode serialize(CalendarEvent event, boolean defaultAllDay) {
        FullCalendarDataSerializer serializer = new FullCalendarSerializer()
                .createDataSerializer("source", new KeyMapper<>());
        serializer.setDefaultAllDaySupplier(() -> defaultAllDay);

        return serializer.serializeData(List.of(event)).get(0);
    }

    String withSystemOffset(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
