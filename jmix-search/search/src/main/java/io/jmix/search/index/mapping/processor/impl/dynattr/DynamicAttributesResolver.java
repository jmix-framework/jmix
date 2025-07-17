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
import io.jmix.search.index.annotation.ReferenceFieldsIndexingMode;
import io.jmix.search.utils.PropertyTools;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.jmix.search.index.annotation.ReferenceFieldsIndexingMode.INSTANCE_NAME_ONLY;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Component
public class DynamicAttributesResolver {

    private final DynAttrMetadata dynAttrMetadata;
    private final PropertyTools propertyTools;

    public DynamicAttributesResolver(DynAttrMetadata dynAttrMetadata,
                                     PropertyTools propertyTools) {
        this.dynAttrMetadata = dynAttrMetadata;
        this.propertyTools = propertyTools;
    }

    public Map<String, MetaPropertyPath> resolveEffectivePropertyPaths(
            MetaClass metaClass,
            String[] excludedCategories,
            String[] excludedProperties,
            ReferenceFieldsIndexingMode mode) {
        if (excludedCategories.length == 0 && excludedProperties.length == 0) {
            Map<String, MetaPropertyPath> effectiveProperties = new HashMap<>();
            Collection<AttributeDefinition> attributes = getAttributes(metaClass, excludedCategories, excludedProperties, mode);

            attributes.forEach(attributeDefinition ->
                    effectiveProperties.putAll(propertyTools.findPropertiesByPath(metaClass, attributeDefinition.getCode(), true))
            );
            return effectiveProperties;
        }
        //TODO
        return null;

    }

    protected Collection<AttributeDefinition> getAttributes(
            MetaClass metaClass,
            String[] excludedCategories,
            String[] excludedProperties,
            ReferenceFieldsIndexingMode mode) {
        Map<String, AttributeDefinition> attributeDefinitionMap = dynAttrMetadata.getAttributes(metaClass).stream()
                .filter(
                        attributeDefinition ->
                                attributeDefinition.getDataType() != AttributeType.ENTITY || mode == INSTANCE_NAME_ONLY)
                .collect(toMap(AttributeDefinition::getCode, identity()));
        if (excludedCategories.length > 0) {

            cleanAttributesForExcludedProperties(metaClass, excludedCategories, attributeDefinitionMap);
        }

        return attributeDefinitionMap.values();
    }

    private void cleanAttributesForExcludedProperties(MetaClass metaClass, String[] excludedCategories, Map<String, AttributeDefinition> attributeDefinitionMap) {
        Map<String, CategoryDefinition> categories = dynAttrMetadata
                .getCategories(metaClass)
                .stream()
                .collect(toMap(CategoryDefinition::getName, identity()));

        List<String> excludedAttributeCodes = Stream.of(excludedCategories)
                .map(categories::get)
                .filter(Objects::nonNull)
                .map(CategoryDefinition::getAttributeDefinitions)
                .flatMap(Collection::stream)
                .map(AttributeDefinition::getCode)
                .toList();

        excludedAttributeCodes.forEach(attributeDefinitionMap::remove);
    }
}
