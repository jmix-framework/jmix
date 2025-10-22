/*
 * Copyright 2021 Haulmont.
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

package io.jmix.core.repository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify custom query for Jmix Query methods
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Query {

    /**
     * @return query that should be executed
     */
    String value();

    /**
     * For scalar queries only.
     * <p>
     * Specifies property names (keys) to store values in {@link io.jmix.core.entity.KeyValueEntity}.
     * Must be specified if {@link io.jmix.core.entity.KeyValueEntity} is used in return type.
     * @see io.jmix.core.entity.KeyValueEntity#getValue(String)
     */
    String[] properties() default {};
}
