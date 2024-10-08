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

package io.jmix.pivottableflowui.kit.component.serialization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import elemental.json.impl.JreJsonFactory;
import io.jmix.pivottableflowui.kit.component.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JmixPivotTableSerializer {

    private static final Logger log = LoggerFactory.getLogger(JmixPivotTableSerializer.class);

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss.SSS";
    public static final String DEFAULT_DATE_TIME_FORMAT = DEFAULT_DATE_FORMAT + " " + DEFAULT_TIME_FORMAT;

    protected final DateFormat dateFormatter = new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT);
    protected final DateTimeFormatter temporalDateFormatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
    protected final DateTimeFormatter temporalDateTimeFormatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT);

    protected ObjectMapper objectMapper = new ObjectMapper();
    protected JreJsonFactory jsonFactory = new JreJsonFactory();

    public JmixPivotTableSerializer() {
        initSerializer();
    }

    protected void initSerializer() {
        initMapper();
    }

    protected void initMapper() {
        SimpleModule module = createModule();
        getSerializers().forEach(module::addSerializer);

        module.addDeserializer(Renderer.class, new EnumIdDeserializer<>(Renderer.class));
        module.addDeserializer(AggregationMode.class, new EnumIdDeserializer<>(AggregationMode.class));
        module.addDeserializer(Order.class, new EnumIdDeserializer<>(Order.class));

        objectMapper.registerModule(module);
        objectMapper.setFilterProvider(createFilterProvider());

        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    protected SimpleModule createModule() {
        return new SimpleModule();
    }

    protected List<StdSerializer<?>> getSerializers() {
        return Stream.of(
                new EnumIdSerializer(),
                new JsFunctionSerializer(),
                new LocalDateSerializer(temporalDateFormatter),
                new LocalDateTimeSerializer(temporalDateTimeFormatter),
                new DateSerializer(false, dateFormatter)
        ).collect(Collectors.toList());
    }

    protected SimpleFilterProvider createFilterProvider() {
        return new SimpleFilterProvider();
    }

    public JsonValue parseRawJson(String rawJson) {
        return jsonFactory.parse(rawJson);
    }

    public JsonValue serializeOptions(PivotTableOptions options) {
        return serialize(options, PivotTableOptions.class);
    }

    public JsonValue serializeItems(List<Map<String, Object>> dataItems) {
        return serialize(dataItems, dataItems.getClass());
    }

    public <T> Object deserialize(JsonObject jsonObject, Class<T> objectClass) {
        return deserialize(jsonObject.toJson(), objectClass);
    }

    public <T> Object deserialize(String jsonContent, Class<T> objectClass) {
        T deserializedObject;

        log.debug("Starting deserialize {}", objectClass.getSimpleName());

        try {
            deserializedObject = objectMapper.readValue(jsonContent, objectClass);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(String.format("Cannot deserialize %s to %s",
                    jsonContent, objectClass.getSimpleName()), e);
        }

        return deserializedObject;
    }

    protected JsonValue serialize(Object object, Class<?> objectClass) {
        String rawJson;

        log.debug("Starting serialize {}", objectClass.getSimpleName());

        try {
            rawJson = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(String.format("Cannot serialize %s", objectClass.getSimpleName()), e);
        }

        log.debug("Serialized {}: {}", objectClass.getSimpleName(), rawJson);

        return parseRawJson(rawJson);
    }
}
