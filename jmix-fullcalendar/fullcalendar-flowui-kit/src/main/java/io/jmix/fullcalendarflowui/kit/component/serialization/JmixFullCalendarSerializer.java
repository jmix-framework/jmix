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

package io.jmix.fullcalendarflowui.kit.component.serialization;

import io.jmix.fullcalendarflowui.kit.component.model.CalendarDuration;
import io.jmix.fullcalendarflowui.kit.component.model.option.CalendarOption;
import io.jmix.fullcalendarflowui.kit.component.model.option.OptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.ValueNode;

import java.time.LocalDateTime;
import java.util.*;

/**
 * INTERNAL.
 */
public class JmixFullCalendarSerializer extends AbstractFullCalendarSerializer {

    private static final Logger log = LoggerFactory.getLogger(JmixFullCalendarSerializer.class);

    public ObjectNode serializeObject(Object value) {
        log.debug("Starting object: {}", value.getClass());

        JsonNode json;
        try {
            json = objectMapper.valueToTree(value);
        } catch (JacksonException e) {
            throw new IllegalStateException("Cannot serialize object", e);
        }

        if (!json.isObject()) {
            throw new IllegalStateException("Serialized object is not an JSON object");
        }

        log.debug("Serialized object: {}", json);

        return (ObjectNode) json;
    }

    public ValueNode serializeValue(Object value) {
        log.debug("Starting value: {}", value.getClass());

        JsonNode json;
        try {
            json = objectMapper.valueToTree(value);
        } catch (JacksonException e) {
            throw new IllegalStateException("Cannot serialize object", e);
        }

        if (!json.isValueNode()) {
            throw new IllegalStateException("Serialized value is not a value node");
        }

        log.debug("Serialized value: {}", json);

        return (ValueNode) json;
    }

    public ObjectNode serializeOptions(Collection<CalendarOption> options) {
        Map<String, Object> optionsMap = new HashMap<>(options.size());
        for (CalendarOption option : options) {
            optionsMap.put(
                    OptionUtils.getName(option),
                    OptionUtils.getValueToSerialize(option));
        }

        log.debug("Starting serialize {} calendar options", optionsMap.size());

        ObjectNode json;
        try {
            json = objectMapper.valueToTree(optionsMap);
        } catch (JacksonException e) {
            throw new IllegalStateException("Cannot serialize calendar options", e);
        }

        log.debug("Serialized options {}", json);

        return json;
    }

    public ObjectNode serializeCalendarDuration(CalendarDuration duration) {
        log.debug("Starting serialize calendar duration");

        ObjectNode objectNode;
        try {
            objectNode = objectMapper.valueToTree(duration);
        } catch (JacksonException e) {
            throw new IllegalStateException("Cannot serialize calendar duration", e);
        }

        log.debug("Serialized calendar duration {}", objectNode);

        return objectNode;
    }

    public ArrayNode toJsonArray(List<String> list) {
        ArrayNode jsonArray = objectMapper.createArrayNode();
        for (int i = 0; i < list.size(); i++) {
            jsonArray.set(i, list.get(i));
        }
        return jsonArray;
    }

    public ArrayNode toJsonArrayJson(List<JsonNode> list) {
        ArrayNode jsonArray = objectMapper.createArrayNode();
        for (int i = 0; i < list.size(); i++) {
            jsonArray.set(i, list.get(i));
        }
        return jsonArray;
    }

    /**
     * Serializes local date-time without time zone. It is considered that local date-time instances
     * are already in component's time zone.
     *
     * @param dateTime date-time to serialize
     * @return ISO representation of date-time
     */
    public String serializeDateTime(LocalDateTime dateTime) {
        return Objects.requireNonNull(dateTime).toString();
    }
}
