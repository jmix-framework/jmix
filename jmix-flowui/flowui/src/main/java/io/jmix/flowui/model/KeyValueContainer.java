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

package io.jmix.flowui.model;

import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.metamodel.datatype.Datatype;

import javax.annotation.Nullable;

/**
 * Container for a single {@code KeyValueEntity} instance.
 */
public interface KeyValueContainer extends InstanceContainer<KeyValueEntity> {

    /**
     * Sets the name of a property that represents the entity id.
     *
     * @return this instance for chaining
     */
    KeyValueContainer setIdName(String name);

    /**
     * Returns the name of a property that represents the entity id.
     */
    @Nullable
    String getIdName();

    /**
     * Adds a string property to the meta-class of this loader.
     *
     * @return this instance for chaining
     */
    KeyValueContainer addProperty(String name);

    /**
     * Adds a property of the given Java class to the meta-class of this loader.
     * The Java class can be an entity or a datatype.
     *
     * @return this instance for chaining
     */
    KeyValueContainer addProperty(String name, Class<?> aClass);

    /**
     * Adds a property of the given datatype to the meta-class of this loader.
     *
     * @return this instance for chaining
     */
    KeyValueContainer addProperty(String name, Datatype<?> datatype);
}
