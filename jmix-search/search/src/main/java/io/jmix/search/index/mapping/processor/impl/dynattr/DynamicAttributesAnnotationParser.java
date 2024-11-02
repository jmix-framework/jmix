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

import io.jmix.search.index.annotation.DynamicAttributes;
import io.jmix.search.index.mapping.DynamicAttributesGroup;
import io.jmix.search.index.mapping.DynamicAttributesParameterKeys;
import io.jmix.search.index.mapping.ParameterKeys;
import io.jmix.search.index.mapping.strategy.impl.AutoMappingStrategy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;


@Component("search_DynamicAttributesAnnotationParser")
public class DynamicAttributesAnnotationParser {
    public DynamicAttributesGroup createDefinitions(DynamicAttributes annotation) {
        Objects.requireNonNull(annotation, "Annotation can't be null.");

        return DynamicAttributesGroup
                .builder()
                .excludeCategories(annotation.excludeCategories())
                .excludeProperties(annotation.excludeFields())
                .withParameters(createParameters(annotation))
                .withFieldMappingStrategyClass(AutoMappingStrategy.class)
                .build();
    }

    protected Map<String, Object> createParameters(DynamicAttributes specificAnnotation) {
        HashMap<String, Object> parameters = new HashMap<>();
        if (StringUtils.isNotBlank(specificAnnotation.analyzer())) {
            parameters.put(ParameterKeys.ANALYZER, specificAnnotation.analyzer());
        }
        parameters.put(DynamicAttributesParameterKeys.REFERENCE_FIELD_INDEXING_MODE, specificAnnotation.referenceFieldsIndexingMode());
        parameters.put(ParameterKeys.INDEX_FILE_CONTENT, specificAnnotation.indexFileContent());
        return parameters;
    }

    public Set<DynamicAttributes> extractAnnotations(Class<?> indexDefinitionClass) {
        return stream(indexDefinitionClass.getMethods())
                .map(MergedAnnotations::from)
                .flatMap(MergedAnnotations::stream)
                .map(MergedAnnotation::synthesize)
                .filter(annotation -> annotation instanceof DynamicAttributes)
                .map(annotation -> (DynamicAttributes)annotation)
                .collect(Collectors.toSet());
    }
}