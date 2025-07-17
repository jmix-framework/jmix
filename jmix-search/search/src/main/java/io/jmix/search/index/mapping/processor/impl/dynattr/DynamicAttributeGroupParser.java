/*
 * Copyright 2024 Haulmont.
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
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.search.index.mapping.DynamicAttributesConfigurationGroup;
import io.jmix.search.index.mapping.ExtendedSearchSettings;
import io.jmix.search.index.mapping.MappingFieldDescriptor;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DynamicAttributeGroupParser {

    public DynamicAttributeGroupParser(DynAttrMetadata dynAttrMetadata) {
        this.dynAttrMetadata = dynAttrMetadata;
    }

    private final DynAttrMetadata dynAttrMetadata;

    public Map<String, MappingFieldDescriptor> parseGroup(
            MetaClass metaClass,
            DynamicAttributesConfigurationGroup group,
            ExtendedSearchSettings extendedSearchSettings) {
        Collection<AttributeDefinition> attributes = getAttributeDefinitions(metaClass, group);
        return attributes
                .stream()
                .map(attribute->createDescriptor(attribute, group, extendedSearchSettings))
                .collect(Collectors.toMap(DescriptorPair::path, DescriptorPair::descriptor));
    }

    private Collection<AttributeDefinition> getAttributeDefinitions(MetaClass metaClass, DynamicAttributesConfigurationGroup group) {
        return dynAttrMetadata.getAttributes(metaClass);
    }

    private DescriptorPair createDescriptor(AttributeDefinition attribute,
                                            DynamicAttributesConfigurationGroup group,
                                            ExtendedSearchSettings extendedSettings) {
//        MappingFieldDescriptor fieldDescriptor = new MappingFieldDescriptor();
//        fieldDescriptor.setEntityPropertyFullName(propertyPath.toPathString());
//        fieldDescriptor.setIndexPropertyFullName(propertyPath.toPathString());
//        fieldDescriptor.setMetaPropertyPath(propertyPath);
//        fieldDescriptor.setFieldConfiguration(effectiveFieldConfiguration);
//        fieldDescriptor.setOrder(0);
//        fieldDescriptor.setPropertyValueExtractor(effectivePropertyValueExtractor);
//        fieldDescriptor.setInstanceNameRelatedProperties(instanceNameRelatedProperties);
//        fieldDescriptor.setParameters(group.getParameters());
//        fieldDescriptor.setStandalone(false);
        return null;
    }

    private record DescriptorPair(@NotNull String path, @NotNull MappingFieldDescriptor descriptor){

    }
}