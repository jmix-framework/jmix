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
import io.jmix.pivottableflowui.kit.component.model.AggregationMode;
import io.jmix.pivottableflowui.kit.component.model.Order;
import io.jmix.pivottableflowui.kit.component.model.PivotTableOptions;
import io.jmix.pivottableflowui.kit.component.model.Renderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.ser.std.SimpleFilterProvider;
import tools.jackson.databind.ser.std.StdSerializer;

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

    protected final DateTimeFormatter temporalDateFormatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
    protected final DateTimeFormatter temporalDateTimeFormatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT);

    protected ObjectMapper optionsObjectMapper = new ObjectMapper();
    protected ObjectMapper itemsObjectMapper = new ObjectMapper();

    public JmixPivotTableSerializer() {
        initSerializer();
    }

    protected void initSerializer() {
        initOptionsMapper();
        initItemsMapper();
    }

    protected void initOptionsMapper() {
        SimpleModule module = createModule();
        getOptionsSerializers().forEach(module::addSerializer);

        module.addDeserializer(Renderer.class, new EnumIdDeserializer<>(Renderer.class));
        module.addDeserializer(AggregationMode.class, new EnumIdDeserializer<>(AggregationMode.class));
        module.addDeserializer(Order.class, new EnumIdDeserializer<>(Order.class));

        optionsObjectMapper = JsonMapper.builder()
                .changeDefaultPropertyInclusion(incl ->
                        incl.withValueInclusion(JsonInclude.Include.NON_NULL))
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .addModule(module)
                .filterProvider(createFilterProvider())
                .build();
    }

    private void initItemsMapper() {
        SimpleModule module = createModule();
        getItemsSerializers().forEach(module::addSerializer);

        itemsObjectMapper = JsonMapper.builder()
                .changeDefaultPropertyInclusion(incl ->
                        incl.withValueInclusion(JsonInclude.Include.NON_NULL))
                .addModule(module)
                .filterProvider(createFilterProvider())
                .build();
    }

    protected SimpleModule createModule() {
        return new SimpleModule();
    }

    protected List<StdSerializer<?>> getOptionsSerializers() {
        return Stream.of(
                new EnumIdSerializer(),
                new JsFunctionSerializer(),
                new LocalDateSerializer(temporalDateFormatter),
                new LocalDateTimeSerializer(temporalDateTimeFormatter),
                new DefaultDateSerializer()
        ).collect(Collectors.toList());
    }

    protected List<StdSerializer<?>> getItemsSerializers() {
        return Stream.of(
                new EnumIdSerializer(),
                new JsFunctionSerializer(),
                new LocalDateSerializer(temporalDateFormatter),
                new LocalDateTimeSerializer(temporalDateTimeFormatter),
                new DefaultDateSerializer()
        ).collect(Collectors.toList());
    }

    protected SimpleFilterProvider createFilterProvider() {
        return new SimpleFilterProvider();
    }

    public JsonNode parseRawJson(String rawJson, ObjectMapper objectMapper) {
        return objectMapper.readTree(rawJson);
    }

    public JsonNode serializeOptions(PivotTableOptions options) {
        return serialize(options, PivotTableOptions.class, optionsObjectMapper);
    }

    public JsonNode serializeItems(List<Map<String, Object>> dataItems) {
        return serialize(dataItems, dataItems.getClass(), itemsObjectMapper);
    }

    public <T> Object deserialize(ObjectNode jsonObject, Class<T> objectClass) {
        return deserialize(jsonObject.toString(), objectClass);
    }

    public <T> Object deserialize(String jsonContent, Class<T> objectClass) {
        T deserializedObject;

        log.debug("Starting deserialize {}", objectClass.getSimpleName());

        try {
            deserializedObject = optionsObjectMapper.readValue(jsonContent, objectClass);
        } catch (JacksonException e) {
            throw new IllegalStateException(String.format("Cannot deserialize %s to %s",
                    jsonContent, objectClass.getSimpleName()), e);
        }

        return deserializedObject;
    }

    protected JsonNode serialize(Object object, Class<?> objectClass, ObjectMapper objectMapper) {
        String rawJson;

        log.debug("Starting serialize {}", objectClass.getSimpleName());

        try {
            rawJson = objectMapper.writeValueAsString(object);
        } catch (JacksonException e) {
            throw new IllegalStateException(String.format("Cannot serialize %s", objectClass.getSimpleName()), e);
        }

        log.debug("Serialized {}: {}", objectClass.getSimpleName(), rawJson);

        return parseRawJson(rawJson, objectMapper);
    }
}
