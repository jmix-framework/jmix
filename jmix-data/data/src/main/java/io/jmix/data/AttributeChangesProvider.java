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

package io.jmix.data;

import io.jmix.core.event.AttributeChanges;

import jakarta.annotation.Nullable;
import java.util.Set;

/**
 * Provides information about changes in entity attributes.
 */
public interface AttributeChangesProvider {

    /**
     * Returns an object describing changes in entity attributes.
     *
     * @param entity entity instance
     * @return dirty attribute names
     */
    AttributeChanges getAttributeChanges(Object entity);

    /**
     * Returns the set of dirty attributes (changed since the last load from the database).
     * <p> If the entity is new, returns all its attributes.
     * <p> If the entity is not persistent or not in the Managed state, returns empty set.
     *
     * @param entity entity instance
     * @return dirty attribute names
     * @see #isChanged(Object, String...)
     */
    Set<String> getChangedAttributeNames(Object entity);

    /**
     * Returns true if the given entity has dirty attributes (changed since the last load from the database).
     * <br> If the entity is new, returns true.
     * <br> If the entity is not persistent or not in the Managed state, returns false.
     *
     * @param entity entity instance
     * @see #getChangedAttributeNames(Object)
     * @see #isChanged(Object, String...)
     */
    boolean isChanged(Object entity);

    /**
     * Returns true if at least one of the given attributes is dirty (i.e. changed since the last load from the database).
     * <p> If the entity is new, always returns true.
     * <p> If the entity is not persistent or not in the Managed state, always returns false.
     *
     * @param entity     entity instance
     * @param attributes attributes to check
     * @see #getChangedAttributeNames(Object)
     */
    boolean isChanged(Object entity, String... attributes);

    /**
     * Returns an old value of an attribute changed in the current transaction. The entity must be in the Managed state.
     * For enum attributes returns enum value. <br>
     * You can check if the value has been changed using {@link #isChanged(Object, String...)} method.
     *
     * @param entity    entity instance
     * @param attribute attribute name
     * @return an old value stored in the database. For a new entity returns null.
     * @throws IllegalArgumentException if the entity is not persistent or not in the Managed state
     * @see #isChanged(Object, String...)
     * @see #getChangedAttributeNames(Object)
     */
    @Nullable
    Object getOldValue(Object entity, String attribute);
}
