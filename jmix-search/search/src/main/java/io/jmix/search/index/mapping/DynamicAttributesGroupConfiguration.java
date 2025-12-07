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
import io.jmix.search.index.annotation.ReferenceAttributesIndexingMode;
import io.jmix.search.index.mapping.propertyvalue.PropertyValueExtractor;
import io.jmix.search.index.mapping.strategy.FieldMappingStrategy;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.jmix.search.index.annotation.ReferenceAttributesIndexingMode.INSTANCE_NAME_ONLY;

/**
 * The {@code DynamicAttributesGroupConfiguration} class represents a search configuration
 * for a group of the dynamic attributes.
 * <p>
 * It allows specifying which properties should be included or excluded for indexing
 * and provides functionality to define additional configurations like field mapping
 * strategies and parameterization.
 * <p>
 * Equivalent of a single field-mapping annotation {@link io.jmix.search.index.annotation.DynamicAttributes}.
 * <p>
 * Instances of this class should be built using the {@link DynamicAttributesGroupConfiguration.DynamicAttributeGroupDefinitionBuilder}.
 */
public class DynamicAttributesGroupConfiguration extends AbstractAttributesGroupConfiguration {

    protected final String[] excludedCategories;
    protected final String[] excludedProperties;
    protected final ReferenceAttributesIndexingMode referenceAttributesIndexingMode;

    protected DynamicAttributesGroupConfiguration(DynamicAttributeGroupDefinitionBuilder builder) {
        super(builder.fieldMappingStrategyClass,
                builder.fieldMappingStrategy,
                builder.fieldConfiguration,
                builder.propertyValueExtractor,
                builder.parameters == null ? Collections.emptyMap() : builder.parameters,
                builder.order);
        this.excludedCategories = builder.excludedCategories;
        this.excludedProperties = builder.excludedProperties;
        this.referenceAttributesIndexingMode = builder.referenceAttributesIndexingMode;
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

    /**
     * Retrieves the categories that are excluded from processing or indexing.
     *
     * @return an array of the excluded category names.
     */
    public String[] getExcludedCategories() {
        return excludedCategories;
    }

    /**
     * Gets the reference attributes indexing mode.
     *
     * @return the reference attributes indexing mode
     */
    public ReferenceAttributesIndexingMode getReferenceAttributesIndexingMode() {
        return referenceAttributesIndexingMode;
    }

    public static class DynamicAttributeGroupDefinitionBuilder {

        protected static final ObjectMapper mapper = new ObjectMapper();

        protected Class<? extends FieldMappingStrategy> fieldMappingStrategyClass;
        protected FieldMappingStrategy fieldMappingStrategy;
        protected FieldConfiguration fieldConfiguration;
        protected PropertyValueExtractor propertyValueExtractor;
        protected Map<String, Object> parameters = null;
        protected ReferenceAttributesIndexingMode referenceAttributesIndexingMode = INSTANCE_NAME_ONLY;
        protected Integer order = null;

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

        /**
         * Defines {@link FieldMappingStrategy} implementation class that should be used to map properties.
         * <p>
         * Optional - at least one of the followings should be defined:
         * <ul>
         *     <li>{@link FieldMappingStrategy} implementation class via this method</li>
         *     <li>{@link FieldMappingStrategy} instance via {@link #withFieldMappingStrategy} method</li>
         *     <li>Explicit native configuration via {@link #withFieldConfiguration} methods</li>
         * </ul>
         * <p>
         * If some of them are defined at the same time:
         * <ul>
         *     <li>Strategy instance takes precedence over strategy class</li>
         *     <li>Explicit configuration overrides identical parameters of configuration generated by strategy</li>
         * </ul>
         *
         * @param fieldMappingStrategyClass class implements {@link FieldMappingStrategy}
         * @return builder
         * @see #withFieldMappingStrategy
         * @see #withFieldConfiguration(String)
         * @see #withFieldConfiguration(ObjectNode)
         */
        public DynamicAttributeGroupDefinitionBuilder withFieldMappingStrategyClass(
                Class<? extends FieldMappingStrategy> fieldMappingStrategyClass) {
            this.fieldMappingStrategyClass = fieldMappingStrategyClass;
            return this;
        }

        /**
         * Defines {@link FieldMappingStrategy} instance that should be used to map properties.
         * <p>
         * Optional - at least one of the followings should be defined:
         * <ul>
         *     <li>{@link FieldMappingStrategy} implementation class via {@link #withFieldMappingStrategyClass} method</li>
         *     <li>{@link FieldMappingStrategy} instance via this method</li>
         *     <li>Explicit native configuration via {@link #withFieldConfiguration} methods</li>
         * </ul>
         * <p>
         * If some of them are defined at the same time:
         * <ul>
         *     <li>Strategy instance takes precedence over strategy class</li>
         *     <li>Explicit configuration overrides identical parameters of configuration generated by strategy</li>
         * </ul>
         *
         * @param fieldMappingStrategy {@link FieldMappingStrategy} instance
         * @return builder
         * @see #withFieldMappingStrategyClass
         * @see #withFieldConfiguration(String)
         * @see #withFieldConfiguration(ObjectNode)
         */
        public DynamicAttributeGroupDefinitionBuilder withFieldMappingStrategy(FieldMappingStrategy fieldMappingStrategy) {
            this.fieldMappingStrategy = fieldMappingStrategy;
            return this;
        }

        /**
         * Defines parameters map.
         * <p>
         * See {@link DynamicAttributesGroupConfiguration#getParameters()}.
         *
         * @param parameters parameters
         * @return builder
         */
        public DynamicAttributeGroupDefinitionBuilder withParameters(Map<String, Object> parameters) {
            this.parameters = new HashMap<>(parameters);
            return this;
        }

        /**
         * Adds new parameter to parameters map.
         * <p>
         * See {@link DynamicAttributeGroupDefinitionBuilder#getParameters()}.
         *
         * @param parameterName  parameter name
         * @param parameterValue parameter value
         * @return builder
         */
        public DynamicAttributeGroupDefinitionBuilder addParameter(String parameterName, Object parameterValue) {
            if (this.parameters == null) {
                this.parameters = new HashMap<>();
            }
            this.parameters.put(parameterName, parameterValue);
            return this;
        }

        /**
         * Defines field configuration as String json object with native configuration.
         * It should contain only configuration itself without field name:
         * <pre>
         * {@code
         * {
         *      "type": "text",
         *      "analyzer": "english",
         *      "boost": 2
         * }}
         * </pre>
         * <p>
         * Optional - at least one of the followings should be defined:
         * <ul>
         *     <li>{@link FieldMappingStrategy} implementation class via {@link #withFieldMappingStrategyClass} method</li>
         *     <li>{@link FieldMappingStrategy} instance via {@link #withFieldMappingStrategy} method</li>
         *     <li>Explicit native configuration via {@link #withFieldConfiguration} methods</li>
         * </ul>
         * <p>
         * If some of them are defined at the same time:
         * <ul>
         *     <li>Strategy instance takes precedence over strategy class</li>
         *     <li>Explicit configuration overrides identical parameters of configuration generated by strategy</li>
         * </ul>
         *
         * @param configuration configuration as json string
         * @return builder
         * @throws RuntimeException if provided string is not a well-formed json object
         * @see #withFieldMappingStrategyClass
         * @see #withFieldMappingStrategy
         * @see #withFieldConfiguration(ObjectNode)
         */
        public DynamicAttributeGroupDefinitionBuilder withFieldConfiguration(String configuration) {
            try {
                ObjectNode configNode = mapper.readValue(configuration, ObjectNode.class);
                return withFieldConfiguration(configNode);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Unable to parse native configuration", e);
            }
        }

        /**
         * Defines field configuration as json object with native configuration.
         * It should contain only configuration itself without field name:
         * <pre>
         * {@code
         * {
         *      "type": "text",
         *      "analyzer": "english",
         *      "boost": 2
         * }}
         * </pre>
         * <p>
         * Optional - at least one of the followings should be defined:
         * <ul>
         *     <li>{@link FieldMappingStrategy} implementation class via {@link #withFieldMappingStrategyClass} method</li>
         *     <li>{@link FieldMappingStrategy} instance via {@link #withFieldMappingStrategy} method</li>
         *     <li>Explicit native configuration via {@link #withFieldConfiguration} methods</li>
         * </ul>
         * <p>
         * If some of them are defined at the same time:
         * <ul>
         *     <li>Strategy instance takes precedence over strategy class</li>
         *     <li>Explicit configuration overrides identical parameters of configuration generated by strategy</li>
         * </ul>
         *
         * @param configuration configuration as json object
         * @return builder
         * @see #withFieldMappingStrategyClass
         * @see #withFieldMappingStrategy
         * @see #withFieldConfiguration(String)
         */
        public DynamicAttributeGroupDefinitionBuilder withFieldConfiguration(ObjectNode configuration) {
            this.fieldConfiguration = FieldConfiguration.create(configuration);
            return this;
        }

        /**
         * Defines explicit {@link PropertyValueExtractor} that should be used to extract values from indexed properties.
         * <p>
         * Required if only explicit field configuration is defined.
         * <p>
         * Optional if {@link FieldMappingStrategy} is defined (class or instance) - explicit extractor
         * takes precedence over extractor provided by strategy.
         *
         * @param propertyValueExtractor property value extractor
         * @return builder
         * @see #withFieldConfiguration(String)
         * @see #withFieldConfiguration(ObjectNode)
         */
        public DynamicAttributeGroupDefinitionBuilder withPropertyValueExtractor(PropertyValueExtractor propertyValueExtractor) {
            this.propertyValueExtractor = propertyValueExtractor;
            return this;
        }

        /**
         * Defines indexing mode for the reference dynamic attributes of the indexing entity
         * @param mode reference attributes indexing mode
         * @return builder
         */
        public DynamicAttributeGroupDefinitionBuilder withReferenceAttributesIndexingMode(ReferenceAttributesIndexingMode mode) {
            this.referenceAttributesIndexingMode = mode;
            return this;
        }

        /**
         * Defines explicit priority of the {@link MappingDefinition} for the field.
         * The higher the order number, the higher the priority.
         * If the order is not specified {@link Integer#MIN_VALUE} will be used.
         * It overrides order on strategy - {@link FieldMappingStrategy#getOrder()}.
         * If two {@link MappingDefinition} objects have the same order
         * the runtime error during the application initialization will occur.
         *
         * @param order the order value to be set for the fields of this descriptor
         * @return builder
         */
        public DynamicAttributeGroupDefinitionBuilder withOrder(int order) {
            this.order = order;
            return this;
        }

        public DynamicAttributesGroupConfiguration build() {
            return new DynamicAttributesGroupConfiguration(this);
        }
    }
}
