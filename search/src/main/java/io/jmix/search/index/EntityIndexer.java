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

package io.jmix.search.index;

import io.jmix.core.Id;

import java.util.Collection;

/**
 * Provides functionality for direct documents indexing.
 */
public interface EntityIndexer {

    /**
     * Stores provided entity instance to index.
     *
     * @param entityInstance instance
     * @return {@link IndexResult}
     */
    IndexResult index(Object entityInstance);

    /**
     * Stores provided entity instances to index.
     *
     * @param entityInstances instances
     * @return {@link IndexResult}
     */
    IndexResult indexCollection(Collection<Object> entityInstances);

    /**
     * Stores entity instance to index by provided ID.
     *
     * @param entityId ID of entity instance
     * @return {@link IndexResult}
     */
    IndexResult indexByEntityId(Id<?> entityId);

    /**
     * Stores entity instances to index by provided IDs.
     *
     * @param entityIds IDs of entity instances
     * @return {@link IndexResult}
     */
    IndexResult indexCollectionByEntityIds(Collection<Id<?>> entityIds);

    /**
     * Deletes provided entity instance from index.
     *
     * @param entityInstance instance
     * @return {@link IndexResult}
     */
    IndexResult delete(Object entityInstance);

    /**
     * Deletes provided entity instances from index.
     *
     * @param entityInstances instances
     * @return {@link IndexResult}
     */
    IndexResult deleteCollection(Collection<Object> entityInstances);

    /**
     * Deletes entity instance from index by provided ID.
     *
     * @param entityId ID of entity instance
     * @return {@link IndexResult}
     */
    IndexResult deleteByEntityId(Id<?> entityId);

    /**
     * Deletes entity instances from index by provided IDs.
     *
     * @param entityIds IDs of entity instances
     * @return {@link IndexResult}
     */
    IndexResult deleteCollectionByEntityIds(Collection<Id<?>> entityIds);
}
