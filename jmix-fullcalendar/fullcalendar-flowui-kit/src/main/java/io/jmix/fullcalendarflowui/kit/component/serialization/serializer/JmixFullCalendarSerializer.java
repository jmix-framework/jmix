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

package io.jmix.fullcalendarflowui.kit.component.serialization.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import io.jmix.fullcalendarflowui.kit.component.model.CalendarDuration;
import io.jmix.fullcalendarflowui.kit.component.model.option.CalendarOption;
import io.jmix.fullcalendarflowui.kit.component.model.option.OptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.jmix.fullcalendarflowui.kit.component.CalendarDateTimeTransformations.transformToZDT;

public class JmixFullCalendarSerializer extends AbstractFullCalendarSerializer {
    private static final Logger log = LoggerFactory.getLogger(JmixFullCalendarSerializer.class);

    public JsonObject serializeObject(Object value) {
        log.debug("Starting object: {}", value.getClass());

        String rawJson;
        try {
            rawJson = objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize object", e);
        }

        JsonObject json = jsonFactory.parse(rawJson);

        log.debug("Serialized object: {}", json);

        return json;
    }

    public JsonValue serializeValue(Object value) {
        log.debug("Starting value: {}", value.getClass());

        String rawJson;
        try {
            rawJson = objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize object", e);
        }

        JsonValue json = jsonFactory.parse(rawJson);

        log.debug("Serialized value: {}", json);

        return json;
    }

    public JsonObject serializeOptions(List<CalendarOption> options) {
        Map<String, Object> optionsMap = new HashMap<>(options.size());
        for (CalendarOption option : options) {
            optionsMap.put(
                    OptionUtils.getName(option),
                    OptionUtils.getValueToSerialize(option));
        }

        log.debug("Starting serialize {} calendar options", optionsMap.size());

        String rawJson;
        try {
            rawJson = objectMapper.writeValueAsString(optionsMap);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize calendar options", e);
        }

        JsonObject json = jsonFactory.parse(rawJson);

        log.debug("Serialized options {}", json);

        return json;
    }

    public JsonObject serializeCalendarDuration(CalendarDuration duration) {
        log.debug("Starting serialize calendar duration");

        String rawJson;
        try {
            rawJson = objectMapper.writeValueAsString(duration);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize calendar duration", e);
        }

        JsonObject json = jsonFactory.parse(rawJson);

        log.debug("Serialized calendar duration {}", json);

        return json;
    }

    public JsonArray toJsonArrayFromString(List<String> sourceIds) {
        JsonArray jsonArray = jsonFactory.createArray();
        for (int i = 0; i < sourceIds.size(); i++) {
            jsonArray.set(i, sourceIds.get(i));
        }
        return jsonArray;
    }

    public JsonArray toJsonArrayFromJsonValue(List<JsonValue> sourceIds) {
        JsonArray jsonArray = jsonFactory.createArray();
        for (int i = 0; i < sourceIds.size(); i++) {
            jsonArray.set(i, sourceIds.get(i));
        }
        return jsonArray;
    }

    public String serializeDateTime(LocalDateTime dateTime) {
        String rawDateTime;
        try {
            rawDateTime = objectMapper.writeValueAsString(transformToZDT(dateTime, null));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize date time value: " + dateTime, e);
        }
        return rawDateTime.replaceAll("\"", "");
    }
}
