/*
 * Copyright 2019 Haulmont.
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

package io.jmix.data.impl;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.model.MetaProperty;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface NumberIdSource {

    /**
     * @deprecated takes the sequence parameters from the first attribute annotated with {@link JmixGeneratedValue},
     * use {@link #createLongId(String, MetaProperty)} instead
     */
    @Deprecated(since = "3.2", forRemoval = true)
    Long createLongId(String entityName);

    /**
     * @deprecated takes the sequence parameters from the first attribute annotated with {@link JmixGeneratedValue},
     * use {@link #createIntegerId(String, MetaProperty)} instead
     */
    @Deprecated(since = "3.2", forRemoval = true)
    Integer createIntegerId(String entityName);

    /**
     * Generates next value for an attribute annotated with {@link JmixGeneratedValue}, using the sequence
     * parameters of the annotation of this attribute.
     *
     * @param entityName entity name that defines the default sequence name and the data store
     * @param property   attribute annotated with {@link JmixGeneratedValue}
     * @return next value
     */
    default Long createLongId(String entityName, MetaProperty property) {
        return createLongId(entityName);
    }

    /**
     * Generates next value for an attribute annotated with {@link JmixGeneratedValue}, using the sequence
     * parameters of the annotation of this attribute.
     *
     * @param entityName entity name that defines the default sequence name and the data store
     * @param property   attribute annotated with {@link JmixGeneratedValue}
     * @return next value
     */
    default Integer createIntegerId(String entityName, MetaProperty property) {
        return createIntegerId(entityName);
    }
}
