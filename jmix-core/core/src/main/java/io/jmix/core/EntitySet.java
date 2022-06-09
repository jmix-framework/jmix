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

package io.jmix.core;

import com.google.common.collect.ForwardingSet;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityValues;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of {@code Set&lt;Entity&gt;} with convenient methods for getting entities by a prototype instance
 * or by a class and id.
 *
 * @see #get(Object)
 * @see #get(Class, Object)
 * @see #optional(Object)
 * @see #optional(Class, Object)
 * @see #getAll(Class)
 */
public class EntitySet extends ForwardingSet<Object> implements Serializable {
    private static final long serialVersionUID = 4239884277120360439L;

    private Set<?> entities;

    public EntitySet() {
        this.entities = new HashSet<>();
    }

    public EntitySet(Set<?> entities) {
        this.entities = entities;
    }

    public EntitySet(Collection<?> entities) {
        this.entities = new HashSet<>(entities);
    }

    /**
     * Creates the {@code EntitySet} wrapping an existing set.
     */
    public static EntitySet of(Set<?> entities) {
        return new EntitySet(entities);
    }

    /**
     * Creates the {@code EntitySet} by copying the given collection to the internal set.
     */
    public static EntitySet of(Collection<?> entities) {
        return new EntitySet(entities);
    }

    /**
     * Returns the entity wrapped in {@code Optional} if it exists in the set.
     *
     * @param entityClass class of entity
     * @param entityId    entity id
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> optional(Class<T> entityClass, Object entityId) {
        Preconditions.checkNotNullArgument(entityClass, "entityClass is null");
        Preconditions.checkNotNullArgument(entityId, "entityId is null");
        return (Optional<T>) entities.stream()
                .filter(entity -> entityClass.equals(entity.getClass()) && EntityValues.getId(entity).equals(entityId))
                .findFirst();
    }

    /**
     * Returns the entity wrapped in {@code Optional} if it exists in the set.
     *
     * @param prototype a prototype instance whose class and id are used to look up an entity in the set.
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> optional(T prototype) {
        Preconditions.checkNotNullArgument(prototype, "prototype entity is null");
        return (Optional<T>) optional(prototype.getClass(), EntityValues.getId(prototype));
    }

    /**
     * Returns the entity if it exists in the set.
     *
     * @param entityClass class of entity
     * @param entityId    entity id
     * @throws IllegalArgumentException if the entity not found
     */
    public <T> T get(Class<T> entityClass, Object entityId) {
        return optional(entityClass, entityId).orElseThrow(() -> new IllegalArgumentException("Entity not found"));
    }

    /**
     * Returns the entity if it exists in the set.
     *
     * @param prototype a prototype instance whose class and id are used to look up an entity in the set.
     * @throws IllegalArgumentException if the entity not found
     */
    @SuppressWarnings("unchecked")
    public <T> T get(T prototype) {
        Preconditions.checkNotNullArgument(prototype, "prototype entity is null");
        return (T) get(prototype.getClass(), EntityValues.getId(prototype));
    }

    /**
     * Returns a collection of entities of the specified class.
     *
     * @param entityClass class of entities
     */
    @SuppressWarnings("unchecked")
    public <T> Collection<T> getAll(Class<T> entityClass) {
        Preconditions.checkNotNullArgument(entityClass, "entityClass is null");
        return entities.stream()
                .filter(e -> entityClass.isAssignableFrom(e.getClass()))
                .map(e -> (T) e)
                .collect(Collectors.toList());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected Set delegate() {
        return entities;
    }
}
