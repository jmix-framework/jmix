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

package io.jmix.search.index.mapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.core.common.util.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contains configuration of index field.
 */
public class FieldConfiguration {

    protected ObjectNode config;
    protected List<String> virtualFields;

    protected FieldConfiguration(ObjectNode config) {
        this(config, Collections.emptyList());
    }

    protected FieldConfiguration(ObjectNode config, List<String> virtualFields) {
        this.config = config;
        this.virtualFields = virtualFields;
    }

    /**
     * Provides field configuration as native json.
     *
     * @return json with field configuration
     */
    public ObjectNode asJson() {
        return config.deepCopy();
    }

    /**
     * Provides inner "virtual" fields within current field.
     * @return list of fields names
     */
    public List<String> getVirtualFields() {
        return new ArrayList<>(virtualFields);
    }

    public static FieldConfiguration create(ObjectNode config) {
        Preconditions.checkNotNullArgument(config);

        List<String> virtualFieldsNames;
        JsonNode fieldsNode = config.path("fields");
        if(fieldsNode.isObject()) {
            ObjectNode fieldsObjectNode = (ObjectNode) fieldsNode;
            virtualFieldsNames = new ArrayList<>();
            fieldsObjectNode.fieldNames().forEachRemaining(virtualFieldsNames::add);
        } else {
            virtualFieldsNames = Collections.emptyList();
        }
        return new FieldConfiguration(config, virtualFieldsNames);
    }

    @Override
    public String toString() {
        return "FieldConfiguration{" +
                "config=" + config +
                ", virtualFields=" + virtualFields +
                '}';
    }
}
