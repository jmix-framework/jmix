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

package io.jmix.messagetemplatesflowui.kit.component.serialization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import elemental.json.JsonValue;
import elemental.json.impl.JreJsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JmixGrapesJsSerializer {

    private static final Logger log = LoggerFactory.getLogger(JmixGrapesJsSerializer.class);

    protected ObjectMapper objectMapper = new ObjectMapper();
    protected JreJsonFactory jsonFactory = new JreJsonFactory();

    public JmixGrapesJsSerializer() {
        initSerializer();
    }

    protected void initSerializer() {
        initMapper();
    }

    protected void initMapper() {
        SimpleModule module = createModule();
        getSerializers().forEach(module::addSerializer);

        objectMapper.registerModule(module);
        objectMapper.setFilterProvider(createFilterProvider());

        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    protected SimpleModule createModule() {
        return new SimpleModule();
    }

    protected SimpleFilterProvider createFilterProvider() {
        return new SimpleFilterProvider();
    }

    protected List<StdSerializer<?>> getSerializers() {
        return Stream.<StdSerializer<?>>empty()
                .collect(Collectors.toList());
    }

    public JsonValue serialize(Object object) {
        String rawJson;

        try {
            rawJson = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize", e);
        }

        log.debug("Serialized {}", rawJson);

        return parseRawJson(rawJson);
    }

    public JsonValue parseRawJson(String rawJson) {
        return jsonFactory.parse(rawJson);
    }
}
