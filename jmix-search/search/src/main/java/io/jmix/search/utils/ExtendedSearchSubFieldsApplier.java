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

package io.jmix.search.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.search.index.mapping.ExtendedSearchSettings;

import static io.jmix.search.index.impl.ExtendedSearchConstants.PREFIX_SUBFIELD_NAME;

public class ExtendedSearchSubFieldsApplier {

    public static final String FIELDS_FIELD_NAME = "fields";
    public static final String TYPE_FIELD_NAME = "type";
    public static final String ANALYZER_FIELD_NAME = "analyzer";
    public static final String SEARCH_ANALYZER_FIELD_NAME = "search_analyzer";

    public static ObjectNode applyPrefixSubField(ObjectNode mainFieldConfig, ExtendedSearchSettings extendedSearchSettings) {
        JsonNode currentFieldsNode = mainFieldConfig.path(FIELDS_FIELD_NAME);
        ObjectNode fieldsNode;
        if (currentFieldsNode.isObject()) {
            fieldsNode = (ObjectNode) currentFieldsNode;
        } else {
            fieldsNode = mainFieldConfig.putObject("fields");
        }

        ObjectNode prefixSubFieldNode = fieldsNode.putObject(PREFIX_SUBFIELD_NAME);
        prefixSubFieldNode.put(TYPE_FIELD_NAME, "text");
        prefixSubFieldNode.put(ANALYZER_FIELD_NAME, extendedSearchSettings.getPrefixAnalyzer());
        prefixSubFieldNode.put(SEARCH_ANALYZER_FIELD_NAME, extendedSearchSettings.getPrefixSearchAnalyzer());

        return mainFieldConfig;
    }
}
