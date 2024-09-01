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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.vaadin.flow.data.provider.KeyMapper;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import io.jmix.core.common.util.Preconditions;
import io.jmix.fullcalendarflowui.component.data.CalendarEvent;
import io.jmix.fullcalendarflowui.component.model.IncrementalData;
import io.jmix.fullcalendarflowui.kit.component.serialization.JmixFullCalendarSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.TimeZone;
import java.util.function.Supplier;

public class FullCalendarSerializer extends JmixFullCalendarSerializer {

    protected KeyMapper<Object> crossEventProviderKeyMapper = new KeyMapper<>();

    @Override
    protected List<JsonSerializer<?>> getSerializers() {
        List<JsonSerializer<?>> serializers = super.getSerializers();
        serializers.add(new EnumClassSerializer());
        serializers.add(new DaysOfWeekSerializer());
        return serializers;
    }

    @Nullable
    public String serializeGroupIdOrConstraint(@Nullable Object value) {
        if (value == null) {
            return null;
        }

        String rawValue = CalendarEventSerializer.getRawGroupIdOrConstraint(value);

        return rawValue != null ? rawValue : crossEventProviderKeyMapper.key(value);
    }

    public void clearData() {
        crossEventProviderKeyMapper.removeAll();
    }

    public FullCalendarDataSerializer createDataSerializer(String sourceId,
                                                           KeyMapper<Object> eventKeyMapper) {
        return new FullCalendarDataSerializer(sourceId, eventKeyMapper);
    }

    public class FullCalendarDataSerializer extends FullCalendarSerializer {
        private static final Logger log = LoggerFactory.getLogger(FullCalendarDataSerializer.class);

        protected CalendarEventSerializer eventSerializer;
        protected KeyMapper<Object> eventKeyMapper;
        protected String sourceId;

        public FullCalendarDataSerializer(String sourceId, KeyMapper<Object> eventKeyMapper) {
            this.eventKeyMapper = eventKeyMapper;
            this.sourceId = sourceId;

            eventSerializer = createCalendarEventSerializer();
            setupCalendarEventSerializer(objectMapper, eventSerializer);
        }

        public void setTimeZoneSupplier(Supplier<TimeZone> timeZoneSupplier) {
            Preconditions.checkNotNullArgument(timeZoneSupplier);
            eventSerializer.setTimeZoneSupplier(timeZoneSupplier);
        }

        public JsonValue serializeIncrementalData(IncrementalData incrementalData) {
            String dataJson;
            try {
                dataJson = objectMapper.writeValueAsString(incrementalData);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Cannot serialize calendar's incremental data", e);
            }

            JsonObject json = jsonFactory.parse(dataJson);

            log.debug("Serialized incremental data: {}", json.toJson());

            return json;
        }

        public JsonArray serializeData(List<? extends CalendarEvent> items) {

            log.debug("Starting serialize calendar's data: {} items", items.size());

            String rawJson;
            try {
                rawJson = objectMapper.writeValueAsString(items);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Cannot serialize calendar's data", e);
            }

            JsonArray json = jsonFactory.parse(rawJson);

            log.debug("Serialized data: {} items", json.toJson());

            return json;
        }

        protected CalendarEventSerializer createCalendarEventSerializer() {
            return new CalendarEventSerializer(sourceId, eventKeyMapper,
                    FullCalendarSerializer.this.crossEventProviderKeyMapper);
        }

        protected void setupCalendarEventSerializer(ObjectMapper objectMapper,
                                                    CalendarEventSerializer eventSerializer) {
            SimpleModule module = new SimpleModule();
            module.addSerializer(eventSerializer);
            objectMapper.registerModule(module);
        }
    }
}
