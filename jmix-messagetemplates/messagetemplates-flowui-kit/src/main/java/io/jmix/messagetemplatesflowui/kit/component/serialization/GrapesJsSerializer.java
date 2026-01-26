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
import io.jmix.messagetemplatesflowui.kit.component.GrapesJs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.node.BaseJsonNode;
import tools.jackson.databind.ser.std.SimpleFilterProvider;
import tools.jackson.databind.ser.std.StdSerializer;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Serializes various parameters for the {@link GrapesJs} into JSON for subsequent sending it to the client-side.
 */
public class GrapesJsSerializer {

    private static final Logger log = LoggerFactory.getLogger(GrapesJsSerializer.class);

    protected ObjectMapper objectMapper;

    public GrapesJsSerializer() {
        initSerializer();
    }

    protected void initSerializer() {
        initMapper();
    }

    protected void initMapper() {
        SimpleModule module = createModule();
        getSerializers().forEach(module::addSerializer);

        objectMapper = JsonMapper.builder()
                .changeDefaultPropertyInclusion(incl ->
                        incl.withValueInclusion(JsonInclude.Include.NON_NULL))
                .addModule(module)
                .filterProvider(createFilterProvider())
                .build();
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

    protected BaseJsonNode parseRawJson(String rawJson) {
        return (BaseJsonNode) objectMapper.readTree(rawJson);
    }

    /**
     * Serializes the passed object into JSON.
     *
     * @param object object to serialize
     * @return {@link JsonNode} of the passed object
     */
    public BaseJsonNode serialize(Object object) {
        String rawJson;

        try {
            rawJson = objectMapper.writeValueAsString(object);
        } catch (JacksonException e) {
            throw new IllegalStateException("Cannot serialize", e);
        }

        log.debug("Serialized {}", rawJson);

        return parseRawJson(rawJson);
    }
}
