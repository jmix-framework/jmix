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
import io.jmix.search.index.mapping.propertyvalue.PropertyValueExtractor;
import io.jmix.search.index.mapping.strategy.FieldMappingStrategy;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
                builder.parameters == null ? Collections.emptyMap() : builder.parameters,
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

    public static class StaticAttributeGroupBuilder {

        private String[] includedProperties = new String[0];
        private String[] excludedProperties = new String[0];
        protected static final ObjectMapper mapper = new ObjectMapper();

        protected Class<? extends FieldMappingStrategy> fieldMappingStrategyClass;
        protected FieldMappingStrategy fieldMappingStrategy;
        protected FieldConfiguration fieldConfiguration;
        protected PropertyValueExtractor propertyValueExtractor;
        protected Map<String, Object> parameters = null;
        protected Integer order = null;

        protected StaticAttributeGroupBuilder() {
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
        public StaticAttributeGroupBuilder withFieldMappingStrategyClass(Class<? extends FieldMappingStrategy> fieldMappingStrategyClass) {
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
        public StaticAttributeGroupBuilder withFieldMappingStrategy(FieldMappingStrategy fieldMappingStrategy) {
            this.fieldMappingStrategy = fieldMappingStrategy;
            return this;
        }

        /**
         * Defines parameters map.
         * <p>
         * See {@link MappingDefinitionElement#getParameters()}.
         *
         * @param parameters parameters
         * @return builder
         */
        public StaticAttributeGroupBuilder withParameters(Map<String, Object> parameters) {
            this.parameters = new HashMap<>(parameters);
            return this;
        }

        /**
         * Adds new parameter to parameters map.
         * <p>
         * See {@link StaticAttributeGroupBuilder#getParameters()}.
         *
         * @param parameterName  parameter name
         * @param parameterValue parameter value
         * @return builder
         */
        public StaticAttributeGroupBuilder addParameter(String parameterName, Object parameterValue) {
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
        public StaticAttributeGroupBuilder withFieldConfiguration(String configuration) {
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
        public StaticAttributeGroupBuilder withFieldConfiguration(ObjectNode configuration) {
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
        public StaticAttributeGroupBuilder withPropertyValueExtractor(PropertyValueExtractor propertyValueExtractor) {
            this.propertyValueExtractor = propertyValueExtractor;
            return this;
        }

        /**
         * Defines explicit order.
         * It overrides order on strategy - {@link FieldMappingStrategy#getOrder()}.
         *
         * @param order order
         * @return builder
         */
        public StaticAttributeGroupBuilder withOrder(int order) {
            this.order = order;
            return this;
        }

        public MappingDefinitionElement build() {
            return new MappingDefinitionElement(this);
        }
    }
}
