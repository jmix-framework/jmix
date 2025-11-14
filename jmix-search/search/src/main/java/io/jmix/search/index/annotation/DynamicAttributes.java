/*
 * Copyright 2024 Haulmont.
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

import com.drew.lang.annotations.Nullable;

import java.lang.annotation.*;

import static io.jmix.search.index.annotation.ReferenceAttributesIndexingMode.*;

/**
 * Annotation for marking methods in index definition interfaces that define dynamic attributes to be indexed.
 * This annotation is used to configure various indexing behavior for dynamic attributes.
 *
 * <h3>Container Annotation:</h3>
 * `DynamicAttributes.Container` is a repeatable container that allows multiple `DynamicAttributes` annotations
 * to be used on a single method.
 *
 * <h3>Usage:</h3>
 * This annotation, along with its attributes, provides flexibility for indexing dynamic attributes while ensuring
 * exclusion and indexing modes can be explicitly defined when needed.
 *
 * @see ReferenceAttributesIndexingMode
 * @see FieldMappingAnnotation
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(DynamicAttributes.Container.class)
@FieldMappingAnnotation
public @interface DynamicAttributes {

    /**
     * Specifies dynamic attribute categories to be excluded from indexing for the annotated method.
     */
    @Nullable
    String[] excludeCategories() default {};

    /**
     * Specifies dynamic attributes to be excluded from indexing for the annotated method.
     */
    @Nullable
    String[] excludeAttributes() default {};

    /**
     * Determines the indexing mode for reference attributes associated with the annotated method.
     * Allows configuration of how reference attributes are handled during indexing, such as whether
     * reference values are indexed based on instance names or excluded completely.
     * Defaults to {@code INSTANCE_NAME_ONLY}.
     */
    @Nullable
    ReferenceAttributesIndexingMode referenceAttributesIndexingMode() default INSTANCE_NAME_ONLY;

    /**
     * Specifies the name of the analyzer to be used for processing textual content associated
     * with the annotated method. An analyzer is responsible for breaking down text into tokens
     * and applying text normalization rules to make the text searchable in a specific manner.
     */
    String analyzer() default "";

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Container {

        DynamicAttributes[] value();
    }
}
