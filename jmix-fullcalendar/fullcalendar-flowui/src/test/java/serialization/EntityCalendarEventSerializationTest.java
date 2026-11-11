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
import io.jmix.core.Metadata;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataComponents;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.fullcalendarflowui.component.data.ContainerCalendarDataProvider;
import io.jmix.fullcalendarflowui.component.data.EntityCalendarDataRetriever;
import io.jmix.fullcalendarflowui.component.data.EntityCalendarEvent;
import io.jmix.fullcalendarflowui.component.serialization.FullCalendarSerializer;
import io.jmix.fullcalendarflowui.component.serialization.FullCalendarSerializer.FullCalendarDataSerializer;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import test_support.FullCalendarFlowuiTestConfiguration;
import test_support.entity.DateTypesEvent;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Checks how the dates of an all-day entity event are sent to the client, depending on the attribute type.
 */
@SpringBootTest(classes = {FullCalendarFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
class EntityCalendarEventSerializationTest {

    static final LocalDateTime START = LocalDateTime.of(2026, 10, 20, 0, 0);
    static final ZonedDateTime START_IN_SYSTEM_ZONE = START.atZone(ZoneId.systemDefault());

    @Autowired
    Metadata metadata;
    @Autowired
    DataComponents dataComponents;
    @Autowired
    ApplicationContext applicationContext;

    @Test
    void serialize_allDayLocalDate_sendsDate() {
        JsonNode json = serializeAllDay("localDate", e -> e.setLocalDate(START.toLocalDate()));

        assertEquals("2026-10-20", json.get("start").asString());
    }

    @Test
    void serialize_allDayLocalDateTime_sendsDate() {
        JsonNode json = serializeAllDay("localDateTime", e -> e.setLocalDateTime(START));

        assertEquals("2026-10-20", json.get("start").asString());
    }

    @Test
    void serialize_allDayDateWithTemporalDate_sendsDate() {
        JsonNode json = serializeAllDay("date", e -> e.setDate(java.sql.Date.valueOf(START.toLocalDate())));

        assertEquals("2026-10-20", json.get("start").asString());
    }

    @Test
    void serialize_allDayDateWithIgnoreUserTimeZone_sendsDate() {
        JsonNode json = serializeAllDay("dateTimeIgnoringUserTimeZone",
                e -> e.setDateTimeIgnoringUserTimeZone(Date.from(START_IN_SYSTEM_ZONE.toInstant())));

        assertEquals("2026-10-20", json.get("start").asString());
    }

    @Test
    void serialize_allDayDateWithTemporalTimestamp_sendsMomentWithSystemOffset() {
        JsonNode json = serializeAllDay("dateTime",
                e -> e.setDateTime(Date.from(START_IN_SYSTEM_ZONE.toInstant())));

        assertEquals(START_IN_SYSTEM_ZONE.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                json.get("start").asString());
    }

    @Test
    void serialize_allDayOffsetDateTime_sendsMomentWithSystemOffset() {
        JsonNode json = serializeAllDay("offsetDateTime",
                e -> e.setOffsetDateTime(START_IN_SYSTEM_ZONE.toOffsetDateTime()));

        assertEquals(START_IN_SYSTEM_ZONE.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                json.get("start").asString());
    }

    @Test
    void serialize_allDayDateStartAndMomentEnd_sendsEachByItsType() {
        JsonNode json = serializeAllDay("localDate", "offsetDateTime", e -> {
            e.setLocalDate(START.toLocalDate());
            e.setOffsetDateTime(START_IN_SYSTEM_ZONE.plusDays(1).toOffsetDateTime());
        });

        assertEquals("2026-10-20", json.get("start").asString());
        assertEquals(START_IN_SYSTEM_ZONE.plusDays(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                json.get("end").asString());
    }

    @Test
    void serialize_allDayMomentStartAndDateEnd_sendsEachByItsType() {
        JsonNode json = serializeAllDay("offsetDateTime", "localDate", e -> {
            e.setOffsetDateTime(START_IN_SYSTEM_ZONE.toOffsetDateTime());
            e.setLocalDate(START.toLocalDate().plusDays(1));
        });

        assertEquals(START_IN_SYSTEM_ZONE.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                json.get("start").asString());
        assertEquals("2026-10-21", json.get("end").asString());
    }

    @Test
    void serialize_allDayEventOfRetrieverWithoutEntityClass_sendsDate() {
        DateTypesEvent entity = metadata.create(DateTypesEvent.class);
        entity.setAllDay(true);
        entity.setLocalDateTime(START);

        // Such a retriever loads events only through its load delegate
        EntityCalendarDataRetriever retriever = applicationContext.getBean(EntityCalendarDataRetriever.class);
        retriever.setLoadDelegate(context -> List.of());
        retriever.setAllDayProperty("allDay");
        retriever.setStartDateTimeProperty("localDateTime");

        FullCalendarDataSerializer serializer = new FullCalendarSerializer()
                .createDataSerializer("source", new KeyMapper<>());
        serializer.setDataProvider(retriever);

        JsonNode json = serializer.serializeData(List.of(new EntityCalendarEvent<>(entity, retriever))).get(0);

        assertEquals("2026-10-20", json.get("start").asString());
    }

    JsonNode serializeAllDay(String startProperty, Consumer<DateTypesEvent> startSetter) {
        return serializeAllDay(startProperty, null, startSetter);
    }

    JsonNode serializeAllDay(String startProperty, @Nullable String endProperty, Consumer<DateTypesEvent> setter) {
        DateTypesEvent entity = metadata.create(DateTypesEvent.class);
        entity.setAllDay(true);
        setter.accept(entity);

        CollectionContainer<DateTypesEvent> container = dataComponents.createCollectionContainer(DateTypesEvent.class);
        container.setItems(List.of(entity));

        ContainerCalendarDataProvider<DateTypesEvent> dataProvider = new ContainerCalendarDataProvider<>(container);
        dataProvider.setAllDayProperty("allDay");
        dataProvider.setStartDateTimeProperty(startProperty);
        dataProvider.setEndDateTimeProperty(endProperty);

        FullCalendarDataSerializer serializer = new FullCalendarSerializer()
                .createDataSerializer("source", new KeyMapper<>());
        serializer.setDataProvider(dataProvider);

        return serializer.serializeData(List.of(new EntityCalendarEvent<>(entity, dataProvider))).get(0);
    }
}
