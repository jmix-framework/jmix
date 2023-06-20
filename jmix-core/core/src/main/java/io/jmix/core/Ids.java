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

import io.jmix.core.entity.EntityValues;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * Helper class which represent a collection of {@link Id}
 *
 * @param <T> type of entity
 */
public final class Ids<T> extends ArrayList<Id<T>> {

    /**
     * @param entities entity instances
     * @param <T>      entity type
     * @return list of ids of the passed entities
     */
    public static <T> Ids<T> of(Collection<T> entities) {
        Ids<T> ids = new Ids<>();

        for (T entity : entities) {
            checkNotNullArgument(entity);
            Object entityId = EntityValues.getId(entity);
            checkNotNullArgument(entityId);

            @SuppressWarnings("unchecked")
            Class<T> entityClass = (Class<T>) entity.getClass();
            ids.add(Id.of(entityId, entityClass));
        }

        return ids;
    }

    /**
     * @param entityClass entity class
     * @param values      id values
     * @param <T>         entity type
     * @return list of ids of the passed entities
     */
    public static <T> Ids<T> of(Class<T> entityClass, Collection values) {
        Ids<T> ids = new Ids<>();

        for (Object value : values) {
            ids.add(Id.of(value, entityClass));
        }

        return ids;
    }

    /**
     * Extract ids of entities from {@code List<Id<T, K>>}.
     *
     * @return list of id values
     */
    public List getValues() {
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
    public static <T> List getValues(List<Id<T>> ids) {
        return ids.stream()
                .map(Id::getValue)
                .collect(Collectors.toList());
    }
}
