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

import java.lang.annotation.Annotation;

/**
 * Base interface for processors of field-mapping annotations.
 *
 * @param <T> specific annotation class
 */
public interface FieldAnnotationProcessor<T extends Annotation> {

    /**
     * Processes field-mapping annotation and adds new Mapping Definition element to builder.
     *
     * @param builder             Mapping Definition builder
     * @param rootEntityMetaClass entity holds indexed properties
     * @param annotation          processed annotation
     */
    void process(MappingDefinitionBuilder builder, MetaClass rootEntityMetaClass, Annotation annotation);

    /**
     * Gets specific annotation class
     *
     * @return annotation class
     */
    Class<T> getAnnotationClass();
}
