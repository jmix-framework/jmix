/*
 * Copyright 2025 Haulmont.
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

package io.jmix.search.index.mapping.processor.impl;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.search.index.mapping.*;
import io.jmix.search.index.mapping.propertyvalue.PropertyValueExtractor;
import io.jmix.search.index.mapping.strategy.FieldMappingStrategy;
import io.jmix.search.index.mapping.strategy.FieldMappingStrategyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class FieldMappingCreator {

    private static final Logger log = LoggerFactory.getLogger(FieldMappingCreator.class);

    protected final FieldMappingStrategyProvider fieldMappingStrategyProvider;
    protected final InstanceNameRelatedPropertiesResolver instanceNameRelatedPropertiesResolver;


    FieldMappingCreator(FieldMappingStrategyProvider fieldMappingStrategyProvider,
                        InstanceNameRelatedPropertiesResolver instanceNameRelatedPropertiesResolver) {
        this.fieldMappingStrategyProvider = fieldMappingStrategyProvider;
        this.instanceNameRelatedPropertiesResolver = instanceNameRelatedPropertiesResolver;
    }

    public Optional<MappingFieldDescriptor> createMappingFieldDescriptor(MetaPropertyPath propertyPath, AttributesConfigurationGroup group, ExtendedSearchSettings extendedSearchSettings) {
        Optional<FieldMappingStrategy> fieldMappingStrategyOpt = resolveFieldMappingStrategy(group);
        FieldConfiguration explicitFieldConfiguration = group.getFieldConfiguration();
        PropertyValueExtractor explicitPropertyValueExtractor = group.getPropertyValueExtractor();

        if (!fieldMappingStrategyOpt.isPresent() && explicitFieldConfiguration == null) {
            log.error("Unable to create mapping field descriptor for property '{}': neither field mapping strategy nor explicit field configuration is specified", propertyPath);
            return Optional.empty();
        }

        FieldConfiguration strategyFieldConfiguration = null;
        PropertyValueExtractor strategyPropertyValueExtractor = null;
        if (fieldMappingStrategyOpt.isPresent()) {
            FieldMappingStrategy fieldMappingStrategy = fieldMappingStrategyOpt.get();
            if (fieldMappingStrategy.isSupported(propertyPath)) {
                strategyFieldConfiguration = fieldMappingStrategy.createFieldConfiguration(propertyPath, group.getParameters(), extendedSearchSettings);
                strategyPropertyValueExtractor = fieldMappingStrategy.getPropertyValueExtractor(propertyPath);
            } else {
                log.debug("Property '{}' ('{}') is not supported by field mapping strategy '{}'", propertyPath, propertyPath.getMetaClass(), fieldMappingStrategy);
            }
        }

        if (strategyFieldConfiguration == null && explicitFieldConfiguration == null) {
            log.debug("Property '{}' doesn't have any field mapping configuration", propertyPath);
            return Optional.empty();
        }

        if (strategyPropertyValueExtractor == null & explicitPropertyValueExtractor == null) {
            log.debug("Property '{}' doesn't have any property value extractor", propertyPath);
            return Optional.empty();
        }

        FieldConfiguration effectiveFieldConfiguration = resolveEffectiveFieldConfiguration(
                strategyFieldConfiguration, explicitFieldConfiguration
        );

        PropertyValueExtractor effectivePropertyValueExtractor = explicitPropertyValueExtractor == null
                ? strategyPropertyValueExtractor
                : explicitPropertyValueExtractor;

        List<MetaPropertyPath> instanceNameRelatedProperties = instanceNameRelatedPropertiesResolver.resolveInstanceNameRelatedProperties(propertyPath);

        int effectiveOrder = group.getOrder() == null
                ? fieldMappingStrategyOpt.map(FieldMappingStrategy::getOrder).orElse(Integer.MIN_VALUE)
                : group.getOrder();

        MappingFieldDescriptor fieldDescriptor = new MappingFieldDescriptor();
        fieldDescriptor.setEntityPropertyFullName(propertyPath.toPathString());
        fieldDescriptor.setIndexPropertyFullName(propertyPath.toPathString());
        fieldDescriptor.setMetaPropertyPath(propertyPath);
        fieldDescriptor.setFieldConfiguration(effectiveFieldConfiguration);
        fieldDescriptor.setOrder(effectiveOrder);
        fieldDescriptor.setPropertyValueExtractor(effectivePropertyValueExtractor);
        fieldDescriptor.setInstanceNameRelatedProperties(instanceNameRelatedProperties);
        fieldDescriptor.setParameters(group.getParameters());
        fieldDescriptor.setStandalone(false);

        return Optional.of(fieldDescriptor);
    }

    protected Optional<FieldMappingStrategy> resolveFieldMappingStrategy(AttributesConfigurationGroup element) {
        FieldMappingStrategy fieldMappingStrategy = element.getFieldMappingStrategy();
        if (fieldMappingStrategy != null) {
            return Optional.of(fieldMappingStrategy);
        }

        Class<? extends FieldMappingStrategy> fieldMappingStrategyClass = element.getFieldMappingStrategyClass();
        return fieldMappingStrategyProvider.getFieldMappingStrategyByClass(fieldMappingStrategyClass);
    }

    protected FieldConfiguration resolveEffectiveFieldConfiguration(@Nullable FieldConfiguration strategyFieldConfiguration,
                                                                    @Nullable FieldConfiguration explicitFieldConfiguration) {
        FieldConfiguration effectiveFieldConfiguration;
        if (strategyFieldConfiguration == null) {
            if (explicitFieldConfiguration == null) {
                throw new IllegalArgumentException("Strategy and explicit configurations are null");
            } else {
                effectiveFieldConfiguration = explicitFieldConfiguration;
            }
        } else {
            if (explicitFieldConfiguration == null) {
                effectiveFieldConfiguration = strategyFieldConfiguration;
            } else {
                ObjectNode strategyRoot = strategyFieldConfiguration.asJson().deepCopy();
                ObjectNode explicitRoot = explicitFieldConfiguration.asJson().deepCopy();
                strategyRoot.setAll(explicitRoot);

                effectiveFieldConfiguration = FieldConfiguration.create(strategyRoot);
            }
        }
        return effectiveFieldConfiguration;
    }
}
