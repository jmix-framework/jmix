/*
 * Copyright 2022 Haulmont.
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

package io.jmix.core;

import com.google.gson.JsonElement;
import io.jmix.core.metamodel.model.MetaProperty;

import jakarta.annotation.Nullable;

/**
 * An extension point for a mechanism to serialize and deserialize entity to JSON.
 * The mechanism is implemented in the {@link EntitySerialization} bean.
 * <p>
 * Such beans can be useful for defining specific serialization and deserialization logic for
 * a {@link MetaProperty} value. The supported {@link MetaProperty} is determined by the
 * {@link #supports(MetaProperty)} method.
 */
public interface EntityAttributeSerializationExtension {

    /**
     * Checks whether the extension supports the given meta property
     *
     * @param property a meta property
     * @return true if the extension supports the given meta property, or false otherwise
     */
    boolean supports(MetaProperty property);

    /**
     * Serializes a meta property value to JSON element
     *
     * @param property      a meta property
     * @param propertyValue a meta property value
     * @return a JSON element
     */
    @Nullable
    JsonElement toJson(MetaProperty property, @Nullable Object propertyValue);

    /**
     * Deserializes a JSON element to meta property value.
     *
     * @param property a meta property
     * @param element  a JSON element
     * @return a meta property value
     */
    @Nullable
    Object fromJson(MetaProperty property, JsonElement element);
}
