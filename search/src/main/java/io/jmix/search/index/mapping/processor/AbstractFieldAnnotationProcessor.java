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
import io.jmix.search.index.mapping.MappingDefinition.MappingDefinitionBuilder;
import io.jmix.search.index.mapping.strategy.FieldMappingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Class with basic functionality for processor of some field-mapping annotation.
 *
 * @param <T> specific annotation class
 */
public abstract class AbstractFieldAnnotationProcessor<T extends Annotation> implements FieldAnnotationProcessor<T> {

    private static final Logger log = LoggerFactory.getLogger(AbstractFieldAnnotationProcessor.class);

    @Override
    public void process(MappingDefinitionBuilder builder, MetaClass rootEntityMetaClass, Annotation annotation) {
        log.debug("Start process annotation '{}' for entity class '{}'", annotation, rootEntityMetaClass);
        T specificAnnotation = getAnnotationClass().cast(annotation);
        processSpecificAnnotation(builder, rootEntityMetaClass, specificAnnotation);
    }

    /**
     * Extracts parameters from annotation.
     *
     * @param specificAnnotation processed annotation
     * @return map with parameters
     */
    protected abstract Map<String, Object> createParameters(T specificAnnotation);

    /**
     * Processes specific field-mapping annotation and adds new Mapping definition element to builder.
     *
     * @param builder             Mapping Definition builder
     * @param rootEntityMetaClass entity holds indexed properties
     * @param annotation          processed annotation
     */
    protected abstract void processSpecificAnnotation(MappingDefinitionBuilder builder, MetaClass rootEntityMetaClass, T annotation);

    /**
     * Provides class of {@link FieldMappingStrategy} specific for this annotation.
     *
     * @return mapping strategy
     */
    protected abstract Class<? extends FieldMappingStrategy> getFieldMappingStrategyClass();

}
