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

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.core.common.util.Preconditions;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;

public class AnalysisElementConfiguration {

    private final String name;
    private final ObjectNode config;
    private final AnalysisElementType type;
    private final Settings settings;

    private AnalysisElementConfiguration(AnalysisElementType type, String name, ObjectNode config, Settings settings) {
        this.name = name;
        this.config = config;
        this.type = type;
        this.settings = settings;
    }

    public String getName() {
        return name;
    }

    public ObjectNode getConfig() {
        return config;
    }

    public Settings getSettings() {
        return settings;
    }

    public AnalysisElementType getType() {
        return type;
    }

    protected static AnalysisElementConfiguration create(AnalysisElementType type, String name, ObjectNode config) {
        Preconditions.checkNotNullArgument(type);
        Preconditions.checkNotEmptyString(name);
        Preconditions.checkNotNullArgument(config);

        ObjectNode resultConfig = JsonNodeFactory.instance.objectNode();
        resultConfig.putObject("index").putObject("analysis").putObject(type.getName()).set(name, config);
        String stringConfig = resultConfig.toString();
        Settings settings = Settings.builder()
                .loadFromSource(stringConfig, XContentType.JSON)
                .build();

        return new AnalysisElementConfiguration(type, name, config, settings);
    }
}
