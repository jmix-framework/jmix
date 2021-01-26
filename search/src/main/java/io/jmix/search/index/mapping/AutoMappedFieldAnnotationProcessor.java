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

package io.jmix.search.index.mapping;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.search.index.annotation.AutoMappedField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component("search_AutoMappedFieldAnnotationProcessor")
public class AutoMappedFieldAnnotationProcessor extends AbstractFieldAnnotationProcessor<AutoMappedField> {

    private static final Logger log = LoggerFactory.getLogger(AutoMappedFieldAnnotationProcessor.class);

    @Override
    public Class<AutoMappedField> getAnnotationClass() {
        return AutoMappedField.class;
    }

    @Override
    public IndexMappingConfigTemplate createIndexMappingConfigTemplate(MetaClass rootEntityMetaClass, AutoMappedField annotation) {
        IndexMappingConfigTemplate template = new IndexMappingConfigTemplate(); //todo use builder
        template.setRootEntityMetaClass(rootEntityMetaClass);
        template.setIncludedProperties(annotation.includeProperty());
        template.setExcludedProperties(annotation.excludeProperty());
        template.setFieldMappingStrategyClass(getFieldMappingStrategyClass());
        template.setParameters(createParameters(annotation));

        return template;
    }

    @Override
    protected Map<String, Object> createParameters(AutoMappedField specificAnnotation) {
        //todo
        return new HashMap<>();
    }

    @Override
    protected Class<? extends FieldMappingStrategy> getFieldMappingStrategyClass() {
        return AutoMappingStrategy.class;
    }
}
