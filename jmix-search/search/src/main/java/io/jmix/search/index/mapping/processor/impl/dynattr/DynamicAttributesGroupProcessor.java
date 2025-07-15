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
import io.jmix.search.index.annotation.ReferenceFieldsIndexingMode;
import io.jmix.search.index.mapping.DynamicAttributesConfigurationGroup;
import io.jmix.search.index.mapping.ExtendedSearchSettings;
import io.jmix.search.index.mapping.MappingFieldDescriptor;
import io.jmix.search.index.mapping.processor.impl.AbstractAttributesGroupProcessor;
import io.jmix.search.utils.PropertyTools;

import java.util.*;
import java.util.stream.Collectors;

import static io.jmix.search.index.annotation.ReferenceFieldsIndexingMode.*;
import static io.jmix.search.index.mapping.DynamicAttributesParameterKeys.REFERENCE_FIELD_INDEXING_MODE;

public class DynamicAttributesGroupProcessor extends AbstractAttributesGroupProcessor<DynamicAttributesConfigurationGroup> {

    private final DynamicAttributesResolver dynamicAttributesResolver;
    private final DynamicAttributesMappingCreator dynamicAttributesMappingCreator;

    protected DynamicAttributesGroupProcessor(PropertyTools propertyTools, DynamicAttributesResolver dynamicAttributesResolver, DynamicAttributesMappingCreator dynamicAttributesMappingCreator) {
        super(propertyTools);
        this.dynamicAttributesResolver = dynamicAttributesResolver;
        this.dynamicAttributesMappingCreator = dynamicAttributesMappingCreator;
    }

    @Override
    public List<MappingFieldDescriptor> processAttributesGroup(MetaClass metaClass,
                                                               DynamicAttributesConfigurationGroup group,
                                                               ExtendedSearchSettings extendedSearchSettings) {
        Map<String, MetaPropertyPath> effectiveProperties = dynamicAttributesResolver.resolveEffectiveProperties(
                metaClass,
                group.getExcludedCategories(),
                group.getExcludedProperties(),
                extractReferenceFieldsIndexingMode(group)
        );

        return effectiveProperties.values().stream()
                .map(propertyPath -> dynamicAttributesMappingCreator.createMappingFieldDescriptor(propertyPath, group, extendedSearchSettings))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private ReferenceFieldsIndexingMode extractReferenceFieldsIndexingMode(DynamicAttributesConfigurationGroup group) {
        if (group.getParameters() != null){
            Object mode = group.getParameters().get(REFERENCE_FIELD_INDEXING_MODE);
            if(mode != null){
                return (ReferenceFieldsIndexingMode) mode;
            }
        }
        return NONE;
    }
}
