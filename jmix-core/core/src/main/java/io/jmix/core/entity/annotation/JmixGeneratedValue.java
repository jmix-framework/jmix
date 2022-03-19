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

package io.jmix.core.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates an entity attribute that must be assigned by the framework right after creating an entity instance.
 * <p>
 * The annotated attribute must be of {@code Long}, {@code Integer} or {@code UUID} type.
 * <p>
 * Entity cannot have more than one {@code UUID} attribute marked with this annotation.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@MetaAnnotation
public @interface JmixGeneratedValue {

    /**
     * Defines an existing database sequence name to use for number generation.
     */
    String sequenceName() default "";

    /**
     * If this attribute is true and the {@link #sequenceName()} is set, the sequence will be incremented
     * by {@code jmix.data.numberIdCacheSize} to cache intermediate values in memory.
     */
    boolean sequenceCache() default false;
}
