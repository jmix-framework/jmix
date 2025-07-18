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

package io.jmix.search.index.mapping.processor.impl;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.search.index.annotation.AutoMappedField;
import io.jmix.search.index.mapping.MappingDefinition.MappingDefinitionBuilder;
import io.jmix.search.index.mapping.ParameterKeys;
import io.jmix.search.index.mapping.StaticAttributesConfigurationGroup;
import io.jmix.search.index.mapping.processor.AbstractFieldAnnotationProcessor;
import io.jmix.search.index.mapping.strategy.FieldMappingStrategy;
import io.jmix.search.index.mapping.strategy.impl.AutoMappingStrategy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component("search_AutoMappedFieldAnnotationProcessor")
public class AutoMappedFieldAnnotationProcessor extends AbstractFieldAnnotationProcessor<AutoMappedField> {

    @Override
    public Class<AutoMappedField> getAnnotationClass() {
        return AutoMappedField.class;
    }

    @Override
    protected void processSpecificAnnotation(MappingDefinitionBuilder builder,
                                             MetaClass rootEntityMetaClass,
                                             AutoMappedField annotation) {
        builder.addElement(
                StaticAttributesConfigurationGroup.builder()
                        .includeProperties(annotation.includeProperties())
                        .excludeProperties(annotation.excludeProperties())
                        .withFieldMappingStrategyClass(getFieldMappingStrategyClass())
                        .withParameters(createParameters(annotation))
                        .build()
        );
    }

    @Override
    protected Map<String, Object> createParameters(AutoMappedField specificAnnotation) {
        HashMap<String, Object> parameters = new HashMap<>();
        if (StringUtils.isNotBlank(specificAnnotation.analyzer())) {
            parameters.put(ParameterKeys.ANALYZER, specificAnnotation.analyzer());
        }
        parameters.put(ParameterKeys.INDEX_FILE_CONTENT, specificAnnotation.indexFileContent());
        return parameters;
    }

    @Override
    protected Class<? extends FieldMappingStrategy> getFieldMappingStrategyClass() {
        return AutoMappingStrategy.class;
    }
}
