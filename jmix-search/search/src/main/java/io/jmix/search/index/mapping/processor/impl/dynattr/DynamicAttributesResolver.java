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

package io.jmix.search.index.mapping.processor.impl.dynattr;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.AttributeType;
import io.jmix.dynattr.CategoryDefinition;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.search.index.annotation.ReferenceAttributesIndexingMode;
import io.jmix.search.utils.PropertyTools;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.jmix.dynattr.AttributeType.*;
import static io.jmix.dynattr.AttributeType.ENTITY;
import static io.jmix.search.index.annotation.ReferenceAttributesIndexingMode.INSTANCE_NAME_ONLY;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public class DynamicAttributesResolver {

    private final DynAttrMetadata dynAttrMetadata;
    private final PropertyTools propertyTools;
    private static final List<AttributeType> SUPPORTED_DATA_TYPES = List.of(STRING, ENTITY, ENUMERATION);
    private final WildcardPatternsMatcher wildcardPatternsMatcher;

    public DynamicAttributesResolver(DynAttrMetadata dynAttrMetadata,
                                     PropertyTools propertyTools,
                                     WildcardPatternsMatcher wildcardPatternsMatcher) {
        this.dynAttrMetadata = dynAttrMetadata;
        this.propertyTools = propertyTools;
        this.wildcardPatternsMatcher = wildcardPatternsMatcher;
    }

    public Map<String, MetaPropertyPath> resolveEffectivePropertyPaths(
            MetaClass metaClass,
            String[] excludedCategories,
            String[] excludedProperties,
            ReferenceAttributesIndexingMode mode) {
        Map<String, MetaPropertyPath> effectiveProperties = new HashMap<>();
        Collection<AttributeDefinition> attributes = getAttributes(metaClass, excludedCategories, excludedProperties, mode);

        attributes.forEach(attributeDefinition ->
                effectiveProperties.putAll(propertyTools.findPropertiesByPath(
                        metaClass,
                        "+" + attributeDefinition.getCode(),
                        true))
        );
        return effectiveProperties;
    }

    protected Collection<AttributeDefinition> getAttributes(
            MetaClass metaClass,
            String[] excludedCategories,
            String[] excludedProperties,
            ReferenceAttributesIndexingMode mode) {
        Map<String, AttributeDefinition> attributeDefinitionMap = dynAttrMetadata.getAttributes(metaClass).stream()
                .filter(
                        attributeDefinition -> {
                            AttributeType dataType = attributeDefinition.getDataType();
                            if (!SUPPORTED_DATA_TYPES.contains(dataType)) {
                                return false;
                            }
                            return dataType != ENTITY || mode == INSTANCE_NAME_ONLY;
                        })
                .collect(toMap(AttributeDefinition::getCode, identity()));
        if (excludedCategories.length > 0) {
            removeAttributesForExcludedCategories(metaClass, excludedCategories, attributeDefinitionMap);
        }
        if (excludedProperties.length > 0) {
            removeExcludedAttributes(excludedProperties, attributeDefinitionMap);
        }

        return attributeDefinitionMap.values();
    }

    protected void removeAttributesForExcludedCategories(MetaClass metaClass,
                                                         String[] excludedCategories,
                                                         Map<String, AttributeDefinition> attributeDefinitionMap) {
        Map<String, CategoryDefinition> categories = dynAttrMetadata
                .getCategories(metaClass)
                .stream()
                .collect(toMap(CategoryDefinition::getName, identity()));

        Collection<CategoryDefinition> categoriesToRemove =
                wildcardPatternsMatcher.getMatchingElements(categories, List.of(excludedCategories));

        List<String> excludedAttributeCodes = categoriesToRemove
                .stream()
                .map(CategoryDefinition::getAttributeDefinitions)
                .flatMap(Collection::stream)
                .map(AttributeDefinition::getCode)
                .toList();

        excludedAttributeCodes.forEach(attributeDefinitionMap::remove);
    }

    protected void removeExcludedAttributes(String[] excludedAttributes,
                                            Map<String, AttributeDefinition> attributeDefinitionMap) {
        Collection<AttributeDefinition> attributesToRemove =
                wildcardPatternsMatcher.getMatchingElements(attributeDefinitionMap, List.of(excludedAttributes));

        attributesToRemove.forEach(
                attributeDefinition -> attributeDefinitionMap.remove(attributeDefinition.getCode())
        );
    }
}
