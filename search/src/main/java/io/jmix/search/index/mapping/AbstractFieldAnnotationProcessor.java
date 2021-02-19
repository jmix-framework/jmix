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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.Map;

public abstract class AbstractFieldAnnotationProcessor<T extends Annotation> implements FieldAnnotationProcessor<T> {

    private static final Logger log = LoggerFactory.getLogger(AbstractFieldAnnotationProcessor.class);

    @Override
    public IndexMappingConfigTemplateItem process(MetaClass rootEntityMetaClass, Annotation annotation) {
        log.info("[IVGA] Start process annotation '{}' for entity class '{}'", annotation, rootEntityMetaClass);
        T specificAnnotation = getAnnotationClass().cast(annotation);
        return createIndexMappingConfigTemplateItem(rootEntityMetaClass, specificAnnotation);
    }

    protected abstract Map<String, Object> createParameters(T specificAnnotation);

    protected abstract IndexMappingConfigTemplateItem createIndexMappingConfigTemplateItem(MetaClass rootEntityMetaClass, T specificAnnotation);

    protected abstract Class<? extends FieldMappingStrategy> getFieldMappingStrategyClass();

}
