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

import java.lang.annotation.*;

/**
 * One of the field-mapping annotation. Should be set on methods in index definition interfaces.
 * <p>Entity properties covered by this annotation will be indexed the most common way depends on their datatype.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(AutoMappedField.Container.class)
public @interface AutoMappedField {

    /**
     * Provides entity properties that should be covered by this annotation.
     * <p>Properties should be defined in a full-name format started from the root entity ("localPropertyName", "refPropertyName.propertyName").
     * <p>Wildcard is allowed at the last level of multilevel properties ("*", "refPropertyName.*").
     *
     * @return properties should be processed
     */
    String[] includeProperties() default "";

    /**
     * Provides entity properties that should NOT be covered by this annotation.
     * <p>Properties should be defined in a full-name format started from the root entity ("localPropertyName", "refPropertyName.propertyName").
     * <p>Wildcard is allowed at the last level of multilevel properties ("*", "refPropertyName.*").
     *
     * @return properties should not be processed
     */
    String[] excludeProperties() default "";

    /**
     * Provides analyzer that should be applied to all textual fields covered by this annotation.
     *
     * @return name of analyzer
     */
    String analyzer() default "";

    /**
     * Defines if content of file found in matched File properties should be indexed.
     *
     * @return True if both file name and content are indexed (default) or false if only file name is indexed
     */
    boolean indexFileContent() default true;

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Container {

        AutoMappedField[] value();
    }
}
