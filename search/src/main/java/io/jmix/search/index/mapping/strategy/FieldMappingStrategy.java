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

package io.jmix.search.index.mapping.strategy;

import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.search.index.mapping.FieldConfiguration;
import io.jmix.search.index.mapping.propertyvalue.PropertyValueExtractor;

import java.util.Map;

/**
 * Base interface for mapping strategy.
 * <p>Mapping strategy defines the way of transformation of property metadata and content into index mapping and value.
 */
public interface FieldMappingStrategy {

    /**
     * Defines the order of mapping strategy.
     * If several strategies match the same property strategy with the latest order will be used
     *
     * @return order
     */
    int getOrder();

    /**
     * Checks is provided {@link MetaPropertyPath} is supported by this mapping strategy
     *
     * @param propertyPath property to check
     * @return true if provided {@link MetaPropertyPath} is supported, false otherwise
     */
    boolean isSupported(MetaPropertyPath propertyPath);

    /**
     * Creates field configuration as Elasticsearch-native json.
     *
     * @param propertyPath property to generate configuration for
     * @param parameters   input parameters
     * @return {@link FieldConfiguration}
     */
    FieldConfiguration createFieldConfiguration(MetaPropertyPath propertyPath, Map<String, Object> parameters);

    /**
     * Provides {@link PropertyValueExtractor} to extract property value from entity instances
     *
     * @param propertyPath property
     * @return {@link PropertyValueExtractor}
     */
    PropertyValueExtractor getPropertyValueExtractor(MetaPropertyPath propertyPath);
}
