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

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import elemental.json.JsonFactory;
import elemental.json.impl.JreJsonFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractFullCalendarSerializer {

    protected JsonFactory jsonFactory;
    protected ObjectMapper objectMapper;

    public AbstractFullCalendarSerializer() {
        jsonFactory = createJsonFactory();
        objectMapper = createObjectMapper();

        initObjectMapper(objectMapper);
    }

    protected JsonFactory createJsonFactory() {
        return new JreJsonFactory();
    }

    protected ObjectMapper createObjectMapper() {
        return new ObjectMapper();
    }

    protected void initObjectMapper(ObjectMapper objectMapper) {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        SimpleModule module = new SimpleModule();
        getSerializers().forEach(module::addSerializer);
        objectMapper.registerModule(module);
    }

    protected List<JsonSerializer<?>> getSerializers() {
        List<JsonSerializer<?>> serializers = new ArrayList<>(11);
        serializers.add(new EnumIdSerializer());
        serializers.add(new DayMaxEventRowsSerializer());
        serializers.add(new DayMaxEventsSerializer());
        serializers.add(new JsFunctionSerializer());
        serializers.add(new MoreLinkClassNamesSerializer());
        serializers.add(new EventOverlapSerializer());
        serializers.add(new SelectOverlapSerializer());
        return serializers;
    }
}
