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

package io.jmix.dataimport;

import io.jmix.core.FetchPlan;
import io.jmix.dataimport.configuration.UniqueEntityConfiguration;
import io.jmix.dataimport.extractor.data.ImportedData;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

/**
 * Executes import for a given import configuration and {@link ImportedData}.
 */
public interface DuplicateEntityManager {
    /**
     * Searches in the database the duplicate for a specified entity by properties from the given {@link UniqueEntityConfiguration}.
     *
     * @param entity        entity for which duplicated is searched
     * @param configuration unique entity configuration
     * @param fetchPlan     fetch plan with which the duplicate entity is loaded
     * @return found duplicate
     */
    @Nullable
    Object load(Object entity, UniqueEntityConfiguration configuration, FetchPlan fetchPlan);

    /**
     * Checks whether specified entities are duplicates by properties from the given {@link UniqueEntityConfiguration}.
     *
     * @param firstEntity   first entity
     * @param secondEntity  second entity
     * @param configuration unique entity configuration
     * @return true if entities are duplicates
     */
    boolean isDuplicated(Object firstEntity, Object secondEntity, UniqueEntityConfiguration configuration);

    /**
     * Searches an entity in the specified list that has the same property values as in specified map.
     *
     * @param existingEntities entities to search the entity by property values
     * @param propertyValues   property values
     * @return found entity
     */
    @Nullable
    Object find(Collection<Object> existingEntities, Map<String, Object> propertyValues);

    /**
     * Loads an entity by given property values.
     *
     * @param entityClass    class of entity to load
     * @param propertyValues property values
     * @param fetchPlan      fetch plan
     * @return loaded entity
     */
    @Nullable
    Object load(Class entityClass, Map<String, Object> propertyValues, @Nullable FetchPlan fetchPlan);
}
