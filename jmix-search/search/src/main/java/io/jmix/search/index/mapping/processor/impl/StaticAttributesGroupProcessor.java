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
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.search.index.mapping.ExtendedSearchSettings;
import io.jmix.search.index.mapping.FieldConfiguration;
import io.jmix.search.index.mapping.MappingDefinitionElement;
import io.jmix.search.index.mapping.MappingFieldDescriptor;
import io.jmix.search.index.mapping.propertyvalue.PropertyValueExtractor;
import io.jmix.search.index.mapping.strategy.FieldMappingStrategy;
import io.jmix.search.index.mapping.strategy.FieldMappingStrategyProvider;
import io.jmix.search.utils.PropertyTools;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class StaticAttributesGroupProcessor extends AbstractAttributesGroupProcessor<MappingDefinitionElement> {

    private static final Logger log = LoggerFactory.getLogger(StaticAttributesGroupProcessor.class);

    protected final MetadataTools metadataTools;
    protected final FieldMappingStrategyProvider fieldMappingStrategyProvider;
    protected final InstanceNameRelatedPropertiesResolver instanceNameRelatedPropertiesResolver;

    StaticAttributesGroupProcessor(PropertyTools propertyTools, MetadataTools metadataTools, FieldMappingStrategyProvider fieldMappingStrategyProvider, InstanceNameRelatedPropertiesResolver instanceNameRelatedPropertiesResolver) {
        super(propertyTools);
        this.metadataTools = metadataTools;
        this.fieldMappingStrategyProvider = fieldMappingStrategyProvider;
        this.instanceNameRelatedPropertiesResolver = instanceNameRelatedPropertiesResolver;
    }


    @Override
    public List<MappingFieldDescriptor> processAttributesGroup(MetaClass metaClass, MappingDefinitionElement group, ExtendedSearchSettings extendedSearchSettings) {
        Map<String, MetaPropertyPath> effectiveProperties = resolveEffectiveProperties(
                metaClass, group.getIncludedProperties(), group.getExcludedProperties()
        );

        return effectiveProperties.values().stream()
                .map(propertyPath -> createMappingFieldDescriptor(propertyPath, group, extendedSearchSettings))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    protected Map<String, MetaPropertyPath> resolveEffectiveProperties(MetaClass rootEntityMetaClass,
                                                                       String[] includes,
                                                                       String[] excludes) {
        Map<String, MetaPropertyPath> effectiveProperties = new HashMap<>();
        Arrays.stream(includes)
                .filter(StringUtils::isNotBlank)
                .forEach(included -> {
                    Map<String, MetaPropertyPath> propertyPaths = propertyTools.findPropertiesByPath(rootEntityMetaClass, included);
                    Map<String, MetaPropertyPath> expandedPropertyPaths = expandEmbeddedProperties(rootEntityMetaClass, propertyPaths);
                    effectiveProperties.putAll(expandedPropertyPaths);
                });

        Arrays.stream(excludes)
                .filter(StringUtils::isNotBlank)
                .flatMap(excluded -> {
                    Map<String, MetaPropertyPath> propertyPaths = propertyTools.findPropertiesByPath(rootEntityMetaClass, excluded);
                    Map<String, MetaPropertyPath> expandedPropertyPaths = expandEmbeddedProperties(rootEntityMetaClass, propertyPaths);
                    return expandedPropertyPaths.keySet().stream();
                })
                .forEach(effectiveProperties::remove);

        return effectiveProperties;
    }

    protected Map<String, MetaPropertyPath> expandEmbeddedProperties(MetaClass rootEntityMetaClass, Map<String, MetaPropertyPath> propertyPaths) {
        return propertyPaths.entrySet().stream()
                .flatMap(entry -> {
                    String propertyFullName = entry.getKey();
                    MetaPropertyPath propertyPath = entry.getValue();
                    if (metadataTools.isEmbedded(propertyPath.getMetaProperty())) {
                        log.trace("Property '{}' is embedded. Expand", propertyFullName);
                        Map<String, MetaPropertyPath> expandedEmbeddedProperties = propertyTools.findPropertiesByPath(rootEntityMetaClass, propertyFullName + ".*");
                        log.trace("Property '{}' was expanded to {}", propertyFullName, expandedEmbeddedProperties.values());
                        Map<String, MetaPropertyPath> result = expandEmbeddedProperties(rootEntityMetaClass, expandedEmbeddedProperties);
                        return result.entrySet().stream();
                    } else {
                        log.trace("Property '{}' is not embedded", propertyFullName);
                        return Stream.of(entry);
                    }
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    protected Optional<MappingFieldDescriptor> createMappingFieldDescriptor(MetaPropertyPath propertyPath, MappingDefinitionElement element, ExtendedSearchSettings extendedSearchSettings) {
        Optional<FieldMappingStrategy> fieldMappingStrategyOpt = resolveFieldMappingStrategy(element);
        FieldConfiguration explicitFieldConfiguration = element.getFieldConfiguration();
        PropertyValueExtractor explicitPropertyValueExtractor = element.getPropertyValueExtractor();

        if (!fieldMappingStrategyOpt.isPresent() && explicitFieldConfiguration == null) {
            log.error("Unable to create mapping field descriptor for property '{}': neither field mapping strategy nor explicit field configuration is specified", propertyPath);
            return Optional.empty();
        }

        FieldConfiguration strategyFieldConfiguration = null;
        PropertyValueExtractor strategyPropertyValueExtractor = null;
        if (fieldMappingStrategyOpt.isPresent()) {
            FieldMappingStrategy fieldMappingStrategy = fieldMappingStrategyOpt.get();
            if (fieldMappingStrategy.isSupported(propertyPath)) {
                strategyFieldConfiguration = fieldMappingStrategy.createFieldConfiguration(propertyPath, element.getParameters(), extendedSearchSettings);
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

        int effectiveOrder = element.getOrder() == null
                ? fieldMappingStrategyOpt.map(FieldMappingStrategy::getOrder).orElse(Integer.MIN_VALUE)
                : element.getOrder();

        MappingFieldDescriptor fieldDescriptor = new MappingFieldDescriptor();
        fieldDescriptor.setEntityPropertyFullName(propertyPath.toPathString());
        fieldDescriptor.setIndexPropertyFullName(propertyPath.toPathString());
        fieldDescriptor.setMetaPropertyPath(propertyPath);
        fieldDescriptor.setFieldConfiguration(effectiveFieldConfiguration);
        fieldDescriptor.setOrder(effectiveOrder);
        fieldDescriptor.setPropertyValueExtractor(effectivePropertyValueExtractor);
        fieldDescriptor.setInstanceNameRelatedProperties(instanceNameRelatedProperties);
        fieldDescriptor.setParameters(element.getParameters());
        fieldDescriptor.setStandalone(false);

        return Optional.of(fieldDescriptor);
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

    protected Optional<FieldMappingStrategy> resolveFieldMappingStrategy(MappingDefinitionElement element) {
        FieldMappingStrategy fieldMappingStrategy = element.getFieldMappingStrategy();
        if (fieldMappingStrategy != null) {
            return Optional.of(fieldMappingStrategy);
        }

        Class<? extends FieldMappingStrategy> fieldMappingStrategyClass = element.getFieldMappingStrategyClass();
        return fieldMappingStrategyProvider.getFieldMappingStrategyByClass(fieldMappingStrategyClass);
    }


}
