/*
 * Copyright 2020 Haulmont.
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

package io.jmix.search.index.queue;

import io.jmix.core.Id;

import java.util.Collection;

/**
 * Provides functionality for enqueuing entity instances and processing queue.
 */
public interface IndexingQueueManager {

    /**
     * Removes all queue items.
     *
     * @return amount of deleted items
     */
    int emptyQueue();

    /**
     * Removes all queue items related to provided entity.
     *
     * @param entityName entity
     * @return amount of deleted items
     */
    int emptyQueue(String entityName);

    /**
     * Sends provided entity instance to indexing queue in order to store it to index.
     *
     * @param entityInstance instance
     * @return amount of enqueued instances
     */
    int enqueueIndex(Object entityInstance);

    /**
     * Sends provided entity instances to indexing queue in order to store them to index.
     *
     * @param entityInstances instances
     * @return amount of enqueued instances
     */
    int enqueueIndexCollection(Collection<Object> entityInstances);

    /**
     * Sends entity instance to indexing queue by provided ID in order to store it to index.
     *
     * @param entityId ID of entity instance
     * @return amount of enqueued instances
     */
    int enqueueIndexByEntityId(Id<?> entityId);

    /**
     * Sends entity instances to indexing queue by provided IDs in order to store them to index.
     *
     * @param entityIds IDs of entity instances
     * @return amount of enqueued instances
     */
    int enqueueIndexCollectionByEntityIds(Collection<Id<?>> entityIds);

    /**
     * Sends all instances of all index-configured entities to indexing queue.
     *
     * @return amount of enqueued instances
     */
    int enqueueIndexAll();

    /**
     * Sends all instances of provided entity to indexing queue.
     *
     * @param entityName entity name
     * @return amount of enqueued instances
     */
    int enqueueIndexAll(String entityName);

    /**
     * Sends provided entity instance to indexing queue in order to delete it from index.
     *
     * @param entityInstance instance
     * @return amount of enqueued instances
     */
    int enqueueDelete(Object entityInstance);

    /**
     * Sends provided entity instances to indexing queue in order to delete them from index.
     *
     * @param entityInstances instances
     * @return amount of enqueued instances
     */
    int enqueueDeleteCollection(Collection<Object> entityInstances);

    /**
     * Sends entity instance to indexing queue by provided ID in order to delete it from index.
     *
     * @param entityId ID of entity instance
     * @return amount of enqueued instances
     */
    int enqueueDeleteByEntityId(Id<?> entityId);

    /**
     * Sends entity instances to indexing queue by provided IDs in order to delete them from index.
     *
     * @param entityIds IDs of entity instances
     * @return amount of enqueued instances
     */
    int enqueueDeleteCollectionByEntityIds(Collection<Id<?>> entityIds);

    /**
     * Retrieves next batch of items from indexing queue and processes them - store/remove related documents in index.
     *
     * @return amount of processed queue items
     */
    int processNextBatch();

    /**
     * Retrieves next batch of items from indexing queue and processes them - store/remove related documents in index.
     *
     * @param batchSize amount of queue items to process
     * @return amount of processed queue items
     */
    int processNextBatch(int batchSize);

    /**
     * Retrieves items from indexing queue and processes them - store/remove related documents in index.
     *
     * @return amount of processed queue items
     */
    int processEntireQueue();

    /**
     * Retrieves items from indexing queue and processes them - store/remove related documents in index.
     *
     * @param batchSize amount of queue items to process within single batch
     * @return amount of processed queue items
     */
    int processEntireQueue(int batchSize);
}
