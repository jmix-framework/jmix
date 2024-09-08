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

package io.jmix.search.index.mapping.fieldmapper;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.search.index.mapping.AdvancedSearchSettings;

import java.util.HashMap;
import java.util.Map;

public class FieldMappingBuilder {

    protected String type;
    protected Map<String, Object> nativeParameters;
    protected AdvancedSearchSettings advancedSearchSettings;

    public FieldMappingBuilder withRootType(String type) {
        this.type = type;
        return this;
    }

    public FieldMappingBuilder withNativeParameters(Map<String, Object> nativeParameters) {
        this.nativeParameters = new HashMap<>(nativeParameters);
        return this;
    }

    public FieldMappingBuilder withAdvancedSearchSettings(AdvancedSearchSettings advancedSearchSettings) {
        this.advancedSearchSettings = advancedSearchSettings;
        return this;
    }

}
