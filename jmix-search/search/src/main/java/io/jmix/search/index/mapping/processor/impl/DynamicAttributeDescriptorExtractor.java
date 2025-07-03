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
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;


@Component("search_DynamicAttributeDescriptorExtractor")
public class DynamicAttributeDescriptorExtractor {

    @Nullable
    public DynamicAttributesIndexingDescriptor extract(Class<?> indexDefClass){
        if(indexDefClass.isAnnotationPresent(DynamicAttributes.class)){
            DynamicAttributes dynamicAttributes = indexDefClass.getAnnotation(DynamicAttributes.class);

            return new DynamicAttributesIndexingDescriptor(
                    true,
                    dynamicAttributes.referenceFieldsIndexingMode(),
                    getListWithoutDuplicates(dynamicAttributes.excludeCategories()),
                    getListWithoutDuplicates(dynamicAttributes.excludeFields()),
                    dynamicAttributes.analyzer(),
                    dynamicAttributes.indexFileContent()
                    );
        }

        return null;
    }

    private static List<String> getListWithoutDuplicates(String[] elements) {
        return Arrays.stream(elements).distinct().toList();
    }
}
