/*
 * Copyright 2026 Haulmont.
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

package io.jmix.aitools.dataload.introspection.model;

import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Read-only description of a single entity property, exposed for domain-model discovery.
 */
public interface EntityPropertyDescriptor {

    /**
     * Returns the property name.
     *
     * @return property name
     */
    String getName();

    /**
     * Returns the property captions for the configured locales.
     *
     * @return localized property names
     */
    List<String> getLocalizedNames();

    /**
     * Returns the simple Java type name of the property.
     *
     * @return Java type name
     */
    String getJavaType();

    /**
     * Returns the Jmix property type (for example {@code "datatype"} or {@code "enum"}).
     *
     * @return property type name
     */
    String getPropertyType();

    /**
     * Returns whether the property is the entity identifier.
     *
     * @return {@code true} if the property is the primary key, or {@code null} otherwise
     */
    @Nullable
    Boolean getIdentifier();

    /**
     * Returns whether the property is persistent (stored in the database).
     *
     * @return {@code true} if the property is persistent
     */
    Boolean getPersistent();

    /**
     * Returns whether the property is mandatory.
     *
     * @return {@code true} if the property is mandatory
     */
    Boolean getMandatory();

    /**
     * Returns the property comment.
     *
     * @return comment text, or {@code null} if none
     */
    @Nullable
    String getComment();
}
