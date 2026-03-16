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

import com.vaadin.flow.data.provider.KeyMapper;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.datatype.EnumClass;
import io.jmix.fullcalendarflowui.component.data.CalendarEvent;
import io.jmix.fullcalendarflowui.component.model.IncrementalData;
import io.jmix.fullcalendarflowui.kit.component.model.HasEnumId;
import io.jmix.fullcalendarflowui.kit.component.serialization.JmixFullCalendarSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.node.ArrayNode;

import java.util.List;
import java.util.TimeZone;
import java.util.function.Supplier;

public class FullCalendarSerializer extends JmixFullCalendarSerializer {

    protected KeyMapper<Object> crossDataProviderKeyMapper = new KeyMapper<>();

    @Override
    protected List<ValueSerializer<?>> getSerializers() {
        List<ValueSerializer<?>> serializers = super.getSerializers();
        serializers.add(new EnumClassSerializer());
        serializers.add(new DaysOfWeekSerializer());
        return serializers;
    }

    @Nullable
    public String serializeGroupIdOrConstraint(@Nullable Object value) {
        if (value == null) {
            return null;
        }

        String rawValue = getRawGroupIdOrConstraint(value);

        return rawValue != null ? rawValue : crossDataProviderKeyMapper.key(value);
    }

    public FullCalendarDataSerializer createDataSerializer(String sourceId,
                                                           KeyMapper<Object> eventKeyMapper) {
        return new FullCalendarDataSerializer(sourceId, eventKeyMapper);
    }

    @Nullable
    public static String getRawGroupIdOrConstraint(Object value) {
        Preconditions.checkNotNullArgument(value);

        if (value instanceof String stringValue) {
            return stringValue;
        } else if (value instanceof EnumClass<?> enumClass) {
            return enumClass.getId().toString();
        } else if (value instanceof HasEnumId<?> enumId) {
            return enumId.getId().toString();
        } else if (value instanceof Enum<?> enumValue) {
            return enumValue.name();
        } else {
            return null;
        }
    }

    /**
     * Data serializer is used per data provider manager.
     */
    public class FullCalendarDataSerializer extends FullCalendarSerializer {
        private static final Logger log = LoggerFactory.getLogger(FullCalendarDataSerializer.class);

        protected CalendarEventSerializer eventSerializer;
        protected KeyMapper<Object> eventKeyMapper;
        protected String sourceId;
        protected ObjectMapper dataObjectMapper; // TODO: pinyazhin, check that correctly works

        public FullCalendarDataSerializer(String sourceId, KeyMapper<Object> eventKeyMapper) {
            this.eventKeyMapper = eventKeyMapper;
            this.sourceId = sourceId;

            eventSerializer = createCalendarEventSerializer();
            dataObjectMapper = addCalendarEventSerializer(FullCalendarSerializer.this.objectMapper, eventSerializer);
        }

        public void setTimeZoneSupplier(Supplier<TimeZone> timeZoneSupplier) {
            Preconditions.checkNotNullArgument(timeZoneSupplier);
            eventSerializer.setTimeZoneSupplier(timeZoneSupplier);
        }

        public JsonNode serializeIncrementalData(IncrementalData incrementalData) {
            JsonNode json;
            try {
                json = dataObjectMapper.valueToTree(incrementalData);
            } catch (JacksonException e) {
                throw new IllegalStateException("Cannot serialize calendar's incremental data", e);
            }

            log.debug("Serialized incremental data: {}", json);

            return json;
        }

        public ArrayNode serializeData(List<? extends CalendarEvent> items) {

            log.debug("Starting serialize calendar's data: {} items", items.size());

            JsonNode json;
            try {
                json = dataObjectMapper.valueToTree(items);
            } catch (JacksonException e) {
                throw new IllegalStateException("Cannot serialize calendar's data", e);
            }

            if (!json.isArray()) {
                throw new IllegalStateException("Serialized data is not an array");
            }

            log.debug("Serialized data: {} items", json);

            return (ArrayNode) json;
        }

        protected CalendarEventSerializer createCalendarEventSerializer() {
            return new CalendarEventSerializer(sourceId, eventKeyMapper,
                    FullCalendarSerializer.this.crossDataProviderKeyMapper);
        }

        protected ObjectMapper addCalendarEventSerializer(ObjectMapper objectMapper,
                                                          CalendarEventSerializer eventSerializer) {
            SimpleModule module = new SimpleModule();
            module.addSerializer(eventSerializer);

            return objectMapper.rebuild()
                    .addModule(module)
                    .build();
        }
    }
}
