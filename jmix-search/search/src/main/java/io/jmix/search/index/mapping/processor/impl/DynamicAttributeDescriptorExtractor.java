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

package io.jmix.search.index.mapping.processor.impl;

import io.jmix.search.index.DynamicAttributesIndexingDescriptor;
import io.jmix.search.index.annotation.DynamicAttributes;
import io.jmix.search.index.annotation.ReferenceFieldsIndexingMode;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static io.jmix.search.index.mapping.processor.impl.DynamicAttributesIndexingConfigurationException.ConflictType.CATEGORIES;
import static io.jmix.search.index.mapping.processor.impl.DynamicAttributesIndexingConfigurationException.ConflictType.FIELDS;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toSet;

@Component
public class DynamicAttributeDescriptorExtractor {
    public DynamicAttributesIndexingDescriptor extract(Class<?> indexDefClass)
            throws DynamicAttributesIndexingConfigurationException{
        if(indexDefClass.isAnnotationPresent(DynamicAttributes.class)){
            DynamicAttributes dynamicAttributes = indexDefClass.getAnnotation(DynamicAttributes.class);

            if (hasCategoriesConflict(dynamicAttributes)){
                throw new DynamicAttributesIndexingConfigurationException(
                        CATEGORIES,
                        asList(dynamicAttributes.includeCategories()),
                        asList(dynamicAttributes.excludeCategories()));
            }

            if (hasFieldsConflict(dynamicAttributes)){
                throw new DynamicAttributesIndexingConfigurationException(
                        FIELDS,
                        asList(dynamicAttributes.includeFields()),
                        asList(dynamicAttributes.excludeFields()));
            }


            return new DynamicAttributesIndexingDescriptor(
                    true,
                    dynamicAttributes.referenceFieldsIndexingMode(),
                    getListWithoutDuplicates(dynamicAttributes.includeCategories()),
                    getListWithoutDuplicates(dynamicAttributes.excludeCategories()),
                    getListWithoutDuplicates(dynamicAttributes.includeFields()),
                    getListWithoutDuplicates(dynamicAttributes.excludeFields())
            );
        }

        return new DynamicAttributesIndexingDescriptor(
                false,
                ReferenceFieldsIndexingMode.NONE,
                emptyList(),
                emptyList(),
                emptyList(),
                emptyList());
    }

    private static List<String> getListWithoutDuplicates(String[] elements) {
        return Arrays.stream(elements).distinct().toList();
    }

    protected boolean hasCategoriesConflict(DynamicAttributes dynamicAttributes) {
        return hasConflict(dynamicAttributes.includeCategories(), dynamicAttributes.excludeCategories());
    }

    private boolean hasFieldsConflict(DynamicAttributes dynamicAttributes) {
        return hasConflict(dynamicAttributes.includeFields(), dynamicAttributes.excludeFields());
    }

    protected boolean hasConflict(String[] includedArray, String[] excludedArray) {
        Set<String> included = new HashSet<>(asList(includedArray));
        Set<String> excluded = new HashSet<>(asList(excludedArray));
        Set<String> combined = Stream.of(included, excluded).flatMap(Set::stream).collect(toSet());
        return combined.size() != included.size() + excluded.size();
    }
}
