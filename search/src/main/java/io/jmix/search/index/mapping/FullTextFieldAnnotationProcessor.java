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
import io.jmix.search.index.annotation.FullTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component("search_FullTextFieldAnnotationProcessor")
public class FullTextFieldAnnotationProcessor extends AbstractFieldAnnotationProcessor<FullTextField> {

    private static final Logger log = LoggerFactory.getLogger(FullTextFieldAnnotationProcessor.class);

    @Override
    public Class<FullTextField> getAnnotationClass() {
        return FullTextField.class;
    }

    @Override
    public IndexMappingConfigTemplateItem createIndexMappingConfigTemplateItem(MetaClass rootEntityMetaClass, FullTextField annotation) {
        //todo
        return null;
    }

    @Override
    protected Map<String, Object> createParameters(FullTextField specificAnnotation) {
        //todo
        return Collections.emptyMap();
    }

    @Override
    protected Class<? extends FieldMappingStrategy> getFieldMappingStrategyClass() {
        //todo
        return null;
    }
}
