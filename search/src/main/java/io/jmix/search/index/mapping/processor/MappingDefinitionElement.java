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

package io.jmix.search.index.mapping.processor;

import io.jmix.search.index.annotation.JmixEntitySearchIndex;
import io.jmix.search.index.mapping.strategy.FieldMappingStrategy;

import java.util.Map;

/**
 * Contains information about indexed properties defined by single field-mapping annotation within index definition interface
 * marked with {@link JmixEntitySearchIndex}.
 */
public class MappingDefinitionElement {

    protected String[] includedProperties;
    protected String[] excludedProperties;
    protected Class<? extends FieldMappingStrategy> fieldMappingStrategyClass;
    protected Map<String, Object> parameters;
    //todo attributes for standalone fields

    /**
     * Provides full name of properties that should be indexed.
     * @return property names
     */
    public String[] getIncludedProperties() {
        return includedProperties;
    }

    public void setIncludedProperties(String[] includedProperties) {
        this.includedProperties = includedProperties;
    }

    /**
     * Provides full name of properties that should NOT be indexed.
     * @return property names
     */
    public String[] getExcludedProperties() {
        return excludedProperties;
    }

    public void setExcludedProperties(String[] excludedProperties) {
        this.excludedProperties = excludedProperties;
    }

    /**
     * Provides {@link FieldMappingStrategy} implementation class that should be used to map properties.
     * @return {@link FieldMappingStrategy} implementation class
     */
    public Class<? extends FieldMappingStrategy> getFieldMappingStrategyClass() {
        return fieldMappingStrategyClass;
    }

    public void setFieldMappingStrategyClass(Class<? extends FieldMappingStrategy> fieldMappingStrategyClass) {
        this.fieldMappingStrategyClass = fieldMappingStrategyClass;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}
