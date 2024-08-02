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
import com.vaadin.flow.data.provider.KeyMapper;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import io.jmix.fullcalendarflowui.kit.component.data.CalendarEvent;
import io.jmix.fullcalendarflowui.kit.component.serialization.model.IncrementalData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class JmixEventProviderDataSerializer extends AbstractFullCalendarSerializer {
    private static final Logger log = LoggerFactory.getLogger(JmixEventProviderDataSerializer.class);

    protected KeyMapper<Object> keyMapper;
    protected String sourceId;

    public JmixEventProviderDataSerializer(KeyMapper<Object> keyMapper, String sourceId) {
        this.keyMapper = keyMapper;
        this.sourceId = sourceId;
    }

    public JsonValue serializeIncrementalData(IncrementalData incrementalData) {
        String dataJson;
        try {
            dataJson = objectMapper.writeValueAsString(incrementalData);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize calendar data", e);
        }

        JsonObject json =  jsonFactory.parse(dataJson);

        log.debug("Serialized data {}", json.toJson());

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
}
