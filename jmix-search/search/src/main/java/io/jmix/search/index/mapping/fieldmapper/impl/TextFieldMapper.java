/*
 * Copyright 2020 Haulmont.
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

package io.jmix.search.index.mapping.fieldmapper.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;
import io.jmix.search.index.mapping.AdvancedSearchSettings;
import io.jmix.search.index.mapping.ParameterKeys;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

/**
 * Maps field as analyzed text
 */
@Component("search_TextFieldMapper")
public class TextFieldMapper extends SimpleFieldMapper {

    protected static final Set<String> supportedParameters = Collections.unmodifiableSet(
            Sets.newHashSet(ParameterKeys.ANALYZER)
    );

    @Override
    public Set<String> getSupportedMappingParameters() {
        return supportedParameters;
    }

    @Override
    boolean isAdvancedSearchSupported() {
        return true;
    }

    @Override
    protected ObjectNode applyAdvancedSearch(ObjectNode config, AdvancedSearchSettings advancedSearchSettings) {
        JsonNode currentFieldsNode = config.path("fields");
        ObjectNode fieldsNode;
        if (currentFieldsNode.isObject()) {
            fieldsNode = (ObjectNode) currentFieldsNode;
        } else {
            fieldsNode = config.objectNode();
        }

        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

        ObjectNode edgeSubFieldNode = nodeFactory.objectNode();
        edgeSubFieldNode.put("type", "text");
        edgeSubFieldNode.put("analyzer", "_jmix_edge_analyzer"); //todo
        edgeSubFieldNode.put("search_analyzer", "_jmix_edge_search_analyzer"); //todo

        fieldsNode.set("edge", edgeSubFieldNode); //todo name

        return config;
    }

    @Override
    public String getSearchPlatformDatatype() {
        return "text";
    }
}
