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

package io.jmix.search.index.mapping.processor.impl.dynattr;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.search.index.annotation.DynamicAttributes;
import io.jmix.search.index.mapping.DynamicAttributesConfigurationGroup;
import io.jmix.search.index.mapping.DynamicAttributesParameterKeys;
import io.jmix.search.index.mapping.MappingDefinition.MappingDefinitionBuilder;
import io.jmix.search.index.mapping.ParameterKeys;
import io.jmix.search.index.mapping.processor.AbstractFieldAnnotationProcessor;
import io.jmix.search.index.mapping.strategy.FieldMappingStrategy;
import io.jmix.search.index.mapping.strategy.impl.AutoMappingStrategy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * TODO javadoc
 */
@Component("search_AutoMappedDynamicFieldAnnotationProcessor")
public class AutoMappedDynamicFieldAnnotationProcessor extends AbstractFieldAnnotationProcessor<DynamicAttributes> {

    @Override
    public Class<DynamicAttributes> getAnnotationClass() {
        return DynamicAttributes.class;
    }

    @Override
    protected void processSpecificAnnotation(MappingDefinitionBuilder builder,
                                             MetaClass rootEntityMetaClass,
                                             DynamicAttributes annotation) {
        builder.addDynamicAttributesGroup(createDefinition(annotation));
    }


    public DynamicAttributesConfigurationGroup createDefinition(DynamicAttributes annotation) {
        //TODO think about
        Objects.requireNonNull(annotation, "Annotation can't be null.");

        return DynamicAttributesConfigurationGroup
                .builder()
                .excludeCategories(annotation.excludeCategories())
                .excludeProperties(annotation.excludeFields())
                .withParameters(createParameters(annotation))
                .withFieldMappingStrategyClass(AutoMappingStrategy.class)
                .build();
    }


    @Override
    protected Map<String, Object> createParameters(DynamicAttributes specificAnnotation) {
        HashMap<String, Object> parameters = new HashMap<>();
        if (StringUtils.isNotBlank(specificAnnotation.analyzer())) {
            parameters.put(ParameterKeys.ANALYZER, specificAnnotation.analyzer());
        }
        parameters.put(DynamicAttributesParameterKeys.REFERENCE_FIELD_INDEXING_MODE, specificAnnotation.referenceFieldsIndexingMode());
        parameters.put(ParameterKeys.INDEX_FILE_CONTENT, specificAnnotation.indexFileContent());
        return parameters;
    }

    @Override
    protected Class<? extends FieldMappingStrategy> getFieldMappingStrategyClass() {
        return AutoMappingStrategy.class;
    }
}
