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

package io.jmix.fullcalendarflowui.kit.component.serialization.deserializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import elemental.json.JsonFactory;
import elemental.json.JsonObject;
import elemental.json.impl.JreJsonFactory;

public class JmixFullCalendarDeserializer {

    protected JsonFactory jsonFactory;
    protected ObjectMapper objectMapper;

    public JmixFullCalendarDeserializer() {
        jsonFactory = createJsonFactory();
        objectMapper = createObjectMapper();

        initObjectMapper(objectMapper);
    }

    public <T> T deserialize(JsonObject json, Class<T> objectType) {
        String rawJson = json.toJson();

        T context;
        try {
            context = objectMapper.readValue(rawJson, objectType);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot deserialize JSON", e);
        }

        return context;
    }

    protected JsonFactory createJsonFactory() {
        return new JreJsonFactory();
    }

    protected ObjectMapper createObjectMapper() {
        return new ObjectMapper();
    }

    protected void initObjectMapper(ObjectMapper objectMapper) {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
