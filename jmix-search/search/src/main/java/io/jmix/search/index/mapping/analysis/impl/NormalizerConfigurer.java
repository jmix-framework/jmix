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

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.jmix.search.index.mapping.analysis.AnalysisConfigurationStages.SetupFilters;
import static io.jmix.search.index.mapping.analysis.impl.AnalysisElementConfigurationMode.CUSTOM;
import static io.jmix.search.index.mapping.analysis.impl.AnalysisElementType.NORMALIZER;

public class NormalizerConfigurer extends AnalysisElementConfigurer implements NormalizerConfigurationStages {

    protected List<String> charFilters;
    protected List<String> tokenFilters;

    protected NormalizerConfigurer(String name) {
        super(name);
        this.charFilters = Collections.emptyList();
        this.tokenFilters = Collections.emptyList();
    }

    @Override
    protected AnalysisElementType getType() {
        return NORMALIZER;
    }

    @Override
    public SetupFilters createCustom() {
        this.mode = CUSTOM;
        this.typeName = "custom";
        return this;
    }

    @Override
    public SetupFilters withCharacterFilters(String... charFilterNames) {
        this.charFilters = Arrays.asList(charFilterNames);
        return this;
    }

    @Override
    public SetupFilters withTokenFilters(String... tokenFilterNames) {
        this.tokenFilters = Arrays.asList(tokenFilterNames);
        return this;
    }

    @Override
    protected ObjectNode createCustomConfig() {
        ObjectNode config = JsonNodeFactory.instance.objectNode();
        config.put("type", typeName);

        ArrayNode charFiltersNode = mapper.convertValue(charFilters, ArrayNode.class);
        config.set("char_filter", charFiltersNode);

        ArrayNode tokenFiltersNode = mapper.convertValue(tokenFilters, ArrayNode.class);
        config.set("filter", tokenFiltersNode);

        return config;
    }
}
