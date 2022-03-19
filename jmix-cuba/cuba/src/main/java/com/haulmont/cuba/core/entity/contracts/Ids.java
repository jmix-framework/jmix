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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * Helper class which represent a collection of {@link io.jmix.core.Id}
 *
 * @param <T> type of entity
 *
 * @deprecated use only in legacy CUBA code. In new code, use {@link io.jmix.core.Ids}.
 */
@Deprecated
public final class Ids<T extends Entity, K> extends ArrayList<Id<T, K>> {

    /**
     * @param entities entity instances
     * @param <K>      type of entity key
     * @param <T>      entity type
     * @return list of ids of the passed entities
     */
    public static <T extends Entity, K> Ids<T, K> of(Collection<T> entities) {
        Ids<T, K> ids = new Ids<>();

        for (T entity : entities) {
            checkNotNullArgument(entity);
            checkNotNullArgument(EntityValues.getId(entity));

            @SuppressWarnings("unchecked")
            Class<T> entityClass = (Class<T>) entity.getClass();
            ids.add(Id.of((K) EntityValues.getId(entity), entityClass));
        }

        return ids;
    }

    /**
     * @param entityClass entity class
     * @param values      id values
     * @param <T>         entity type
     * @return list of ids of the passed entities
     */
    public static <T extends Entity, K> Ids<T, K> of(Class<T> entityClass, Collection<K> values) {
        Ids<T, K> ids = new Ids<>();

        for (K value : values) {
            ids.add(Id.of(value, entityClass));
        }

        return ids;
    }

    /**
     * Extract ids of entities from {@code List<Id<T, K>>}.
     *
     * @return list of id values
     */
    public List<K> getValues() {
        return stream()
                .map(Id::getValue)
                .collect(Collectors.toList());
    }

    /**
     * Extract ids of entities from {@code List<Id<T, K>>}.
     *
     * @param ids list of ids
     * @param <T> type of entity
     * @return list of entity keys
     */
    public static <T extends Entity, K> List<K> getValues(List<Id<T, K>> ids) {
        return ids.stream()
                .map(Id::getValue)
                .collect(Collectors.toList());
    }
}
