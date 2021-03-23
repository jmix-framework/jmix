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

package io.jmix.search.index.mapping.processor;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.search.index.annotation.AutoMappedField;
import io.jmix.search.index.mapping.strategy.AutoMappingStrategy;
import io.jmix.search.index.mapping.strategy.FieldMappingStrategy;
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
    public MappingDefinitionElement createMappingDefinitionElement(MetaClass rootEntityMetaClass, AutoMappedField annotation) {
        MappingDefinitionElement item = new MappingDefinitionElement(); //todo use builder
        item.setIncludedProperties(annotation.includeProperty());
        item.setExcludedProperties(annotation.excludeProperty());
        item.setFieldMappingStrategyClass(getFieldMappingStrategyClass());
        item.setParameters(createParameters(annotation));

        return item;
    }

    @Override
    protected Map<String, Object> createParameters(AutoMappedField specificAnnotation) {
        HashMap<String, Object> parameters = new HashMap<>();
        //todo move validation to Field Mappers?
        if (StringUtils.isNotBlank(specificAnnotation.analyzer())) {
            parameters.put("analyzer", specificAnnotation.analyzer());
        }
        if (StringUtils.isNotBlank(specificAnnotation.normalizer())) {
            parameters.put("normalizer", specificAnnotation.normalizer());
        }
        return parameters;
    }

    @Override
    protected Class<? extends FieldMappingStrategy> getFieldMappingStrategyClass() {
        return AutoMappingStrategy.class;
    }
}
