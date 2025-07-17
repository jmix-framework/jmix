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

package io.jmix.search.index.mapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.search.index.mapping.fieldmapper.FieldMapper;
import io.jmix.search.index.mapping.propertyvalue.PropertyValueExtractor;
import io.jmix.search.index.mapping.strategy.FieldMappingStrategy;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Describes details of mapping for entity property or group of properties.
 * Equivalent of single field-mapping annotation.
 */
public class DynamicAttributesConfigurationGroup extends AbstractAttributesConfigurationGroup {
    protected final String[] excludedCategories;
    protected final String[] excludedProperties;

    protected DynamicAttributesConfigurationGroup(DynamicAttributeGroupDefinitionBuilder builder) {
        super(builder.fieldMappingStrategyClass,
                builder.fieldMappingStrategy,
                builder.fieldConfiguration,
                builder.propertyValueExtractor,
                builder.parameters == null ? Collections.emptyMap() : builder.parameters,
                builder.order);
        this.excludedCategories = builder.excludedCategories;
        this.excludedProperties = builder.excludedProperties;
    }

    /**
     * Provides full names of properties that should NOT be indexed.
     *
     * @return property names
     */
    public String[] getExcludedProperties() {
        return excludedProperties;
    }

    public static DynamicAttributeGroupDefinitionBuilder builder() {
        return new DynamicAttributeGroupDefinitionBuilder();
    }

    //TODO

    /**
     * @return
     */
    public String[] getExcludedCategories() {
        return excludedCategories;
    }

    public static class DynamicAttributeGroupDefinitionBuilder
            extends AbstractAttributeGroupDefinitionBuilder<DynamicAttributesConfigurationGroup> {

        private static final ObjectMapper mapper = new ObjectMapper();

        private String[] excludedCategories = new String[0];
        private String[] excludedProperties = new String[0];

        private DynamicAttributeGroupDefinitionBuilder() {
        }

        public DynamicAttributeGroupDefinitionBuilder excludeCategories(String... categories) {
            this.excludedCategories = categories;
            return this;
        }

        /**
         * Defines entity properties that should NOT be indexed.
         * <p>
         * Properties should be defined in a full-name format started from the root entity ("localPropertyName", "refPropertyName.propertyName").
         * <p>
         * Wildcard is allowed at the last level of multilevel properties ("*", "refPropertyName.*").
         *
         * @param properties property names
         * @return builder
         */
        public DynamicAttributeGroupDefinitionBuilder excludeProperties(String... properties) {
            this.excludedProperties = properties;
            return this;
        }

        @Override
        public DynamicAttributesConfigurationGroup build() {
            return new DynamicAttributesConfigurationGroup(this);
        }
    }
}
