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

package com.haulmont.cuba.core.entity.contracts;

import io.jmix.core.Entity;
import io.jmix.core.entity.EntityValues;

import javax.annotation.Nullable;

import java.io.Serializable;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * Convenient class for methods that receive Id of an entity as a parameter.
 *
 * @param <T> entity type
 *
 * @deprecated use only in legacy CUBA code. In new code, use {@link io.jmix.core.Id}.
 */
@Deprecated
public final class Id<T extends Entity, K> implements Serializable {
    private final K id;
    private final Class<T> entityClass;

    private Id(K id, Class<T> entityClass) {
        this.id = id;
        this.entityClass = entityClass;
    }

    /**
     * @return value of entity id
     */
    public K getValue() {
        return id;
    }

    /**
     * @return class of entity
     */
    public Class<T> getEntityClass() {
        return entityClass;
    }

    /**
     * @param entity entity instance
     * @param <K>    type of entity key
     * @param <T>    entity type
     * @return Id of the passed entity
     */
    public static <T extends Entity, K> Id<T, K> of(T entity) {
        checkNotNullArgument(entity);
        checkNotNullArgument(EntityValues.getId(entity));

        @SuppressWarnings("unchecked")
        Class<T> entityClass = (Class<T>) entity.getClass();
        return new Id<>((K) EntityValues.getId(entity), entityClass);
    }

    /**
     * @param entity entity instance, can be null
     * @param <K>    type of entity key
     * @param <T>    entity type
     * @return Id of the passed entity or null
     */
    @Nullable
    public static <T extends Entity, K> Id<T, K> ofNullable(@Nullable T entity) {
        return entity == null ? null : Id.of(entity);
    }

    /**
     * @param id          entity id
     * @param entityClass entity class
     * @param <K>         type of entity key
     * @param <T>         entity type
     * @return Id of the passed entity
     */
    public static <T extends Entity, K> Id<T, K> of(K id, Class<T> entityClass) {
        checkNotNullArgument(id);
        checkNotNullArgument(entityClass);

        return new Id<>(id, entityClass);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Id<?, ?> that = (Id<?, ?>) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return entityClass != null ? entityClass.equals(that.entityClass) : that.entityClass == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (entityClass != null ? entityClass.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Id{" + entityClass.getName() + ", " + id + '}';
    }
}
