/*
 * Copyright 2021 Haulmont.
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

package io.jmix.search.index.mapping.analysis.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static io.jmix.search.index.mapping.analysis.AnalysisConfigurationStages.*;
import static io.jmix.search.index.mapping.analysis.impl.AnalysisElementConfigurationMode.MODIFIED;
import static io.jmix.search.index.mapping.analysis.impl.AnalysisElementConfigurationMode.NATIVE;

abstract class AnalysisElementConfigurer implements
        SetupNativeConfiguration,
        ConfigureBuiltIn,
        SetupParameters {

    protected static final ObjectMapper mapper = new ObjectMapper();

    protected String name;
    protected Map<String, Object> parameters;
    protected String nativeConfiguration;
    protected AnalysisElementConfigurationMode mode;
    protected String typeName;

    protected AnalysisElementConfigurer(String name) {
        this.name = name;
        this.parameters = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public String getNativeConfiguration() {
        return nativeConfiguration;
    }

    @Override
    public void withNativeConfiguration(String nativeConfiguration) {
        this.mode = NATIVE;
        this.nativeConfiguration = nativeConfiguration;
    }

    @Override
    public SetupParameters withParameter(String key, Object value) {
        this.parameters.put(key, value);
        return this;
    }

    @Override
    public SetupParameters configureBuiltIn(String builtInTypeName) {
        this.mode = MODIFIED;
        this.typeName = builtInTypeName;
        return this;
    }

    protected abstract AnalysisElementType getType();

    protected AnalysisElementConfiguration build() {
        AnalysisElementConfiguration config;
        switch (mode) {
            case CUSTOM:
                config = buildCustom();
                break;
            case MODIFIED:
                config = buildModified();
                break;
            case NATIVE:
                config = buildNative();
                break;
            default:
                throw new RuntimeException("Unsupported configuration mode: " + mode);
        }
        return config;
    }

    protected abstract ObjectNode createCustomConfig();

    protected AnalysisElementConfiguration buildCustom() {
        ObjectNode config = createCustomConfig();
        AnalysisElementType type = getType();
        return AnalysisElementConfiguration.create(type, name, config);
    }

    protected AnalysisElementConfiguration buildModified() {
        ObjectNode config = JsonNodeFactory.instance.objectNode();
        if (StringUtils.isBlank(typeName)) {
            throw new RuntimeException("Built-in type is not specified");
        }
        config.put("type", typeName);

        parameters.forEach((key, value) -> config.set(key, mapper.convertValue(value, JsonNode.class)));

        return AnalysisElementConfiguration.create(getType(), name, config);
    }

    protected AnalysisElementConfiguration buildNative() {
        JsonNode config;
        try {
            config = mapper.readTree(nativeConfiguration);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to read native configuration", e);
        }
        if (!config.isObject()) {
            throw new RuntimeException("Native configuration should be represented as Json object");
        }
        return AnalysisElementConfiguration.create(getType(), name, (ObjectNode) config);
    }
}
