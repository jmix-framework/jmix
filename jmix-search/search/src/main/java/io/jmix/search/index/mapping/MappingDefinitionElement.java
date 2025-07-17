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

import java.util.Collections;

/**
 * Describes details of mapping for entity property or group of properties.
 * Equivalent of single field-mapping annotation.
 */
@Deprecated
public class MappingDefinitionElement extends AbstractAttributesConfigurationGroup {
    protected final String[] includedProperties;
    protected final String[] excludedProperties;

    protected MappingDefinitionElement(StaticAttributeGroupBuilder builder) {
        super(builder.fieldMappingStrategyClass,
                builder.fieldMappingStrategy,
                builder.fieldConfiguration,
                builder.propertyValueExtractor,
                builder.parameters == null ? Collections.emptyMap(): builder.parameters,
                builder.order);
        this.includedProperties = builder.includedProperties;
        this.excludedProperties = builder.excludedProperties;
    }

    /**
     * Provides full names of properties that should be indexed.
     *
     * @return property names
     */
    public String[] getIncludedProperties() {
        return includedProperties;
    }

    /**
     * Provides full names of properties that should NOT be indexed.
     *
     * @return property names
     */
    public String[] getExcludedProperties() {
        return excludedProperties;
    }

    public static StaticAttributeGroupBuilder builder() {
        return new StaticAttributeGroupBuilder();
    }

    public static class StaticAttributeGroupBuilder  extends AbstractAttributeGroupDefinitionBuilder<MappingDefinitionElement>{

        private String[] includedProperties = new String[0];
        private String[] excludedProperties = new String[0];


        private StaticAttributeGroupBuilder() {
        }

        /**
         * Defines entity properties that should be indexed.
         * <p>
         * Properties should be defined in a full-name format started from the root entity ("localPropertyName", "refPropertyName.propertyName").
         * <p>
         * Wildcard is allowed at the last level of multilevel properties ("*", "refPropertyName.*").
         *
         * @param properties property names
         * @return builder
         */
        public StaticAttributeGroupBuilder includeProperties(String... properties) {
            this.includedProperties = properties;
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
        public StaticAttributeGroupBuilder excludeProperties(String... properties) {
            this.excludedProperties = properties;
            return this;
        }

        @Override
        public MappingDefinitionElement build() {
            return new MappingDefinitionElement(this);
        }
    }
}
