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

package io.jmix.search.index.annotation;

import io.jmix.search.index.mapping.processor.MappingDefinition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark index definition interfaces.
 * <p>Methods in such interfaces can be annotated via field-mapping annotations to map some entity properties to index fields.
 * <p>
 * Also there can be one method with implementation body - it allows to build {@link MappingDefinition} directly.
 * Such method should fulfil the following requirements:
 * <ul>
 *     <li>default</li>
 *     <li>With return type - {@link MappingDefinition}</li>
 *     <li>Without parameters</li>
 * </ul>
 * <p><b>Note:</b> if there is definition method with implementation any field-mapping annotations will be ignored
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JmixEntitySearchIndex {

    /**
     * Provides entity that should be indexed using this index definition interface.
     * <p>All properties defined in further field-mapping annotation related to this entity.
     *
     * @return entity class
     */
    Class<?> entity();

    /**
     * Provides explicitly defined name of the search index.
     * <p>If it's not set index name will be based on 'searchIndexNamePrefix' property and entity name.
     *
     * @return custom index name
     */
    String indexName() default "";
}
