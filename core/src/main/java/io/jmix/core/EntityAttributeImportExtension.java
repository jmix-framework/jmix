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

import io.jmix.core.metamodel.model.MetaProperty;

/**
 * An extension point for the entity import mechanism. The mechanism is implemented in
 * the {@link EntityImportExport} bean.
 * <p>
 * Such beans can be useful for defining specific logic for importing value of {@link MetaProperty}.
 * The supported {@link MetaProperty} is determined by the {@link #supports(MetaProperty)} method.
 */
public interface EntityAttributeImportExtension {

    /**
     * Checks whether the extension supports the given meta property
     *
     * @param property a meta property
     * @return true if the extension supports the given meta property, or false otherwise
     */
    boolean supports(MetaProperty property);

    /**
     * Imports the entity attribute.
     *
     * @param property  a meta property
     * @param srcEntity entity that came to the {@link EntityImportExport} bean
     * @param dstEntity reloaded srcEntity or new entity instance if srcEntity doesn't exist in the database
     */
    void importEntityAttribute(MetaProperty property, Object srcEntity, Object dstEntity);
}
