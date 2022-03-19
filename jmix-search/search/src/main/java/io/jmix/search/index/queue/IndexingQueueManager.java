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
import java.util.List;

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
     * Synchronously sends all instances of all index-configured entities to indexing queue.
     * <p>
     * Don't use it on a huge amount of data - all ids (per entity) will be kept in memory during this process.
     * Use {@link #initAsyncEnqueueIndexAll} methods instead.
     *
     * @return amount of enqueued instances
     */
    int enqueueIndexAll();

    /**
     * Synchronously sends all instances of provided entity to indexing queue.
     * <p>
     * Don't use it on a huge amount of data - all ids will be kept in memory during this process.
     * Use {@link #initAsyncEnqueueIndexAll} methods instead.
     *
     * @param entityName entity name
     * @return amount of enqueued instances
     */
    int enqueueIndexAll(String entityName);

    /**
     * Gets entity names of all existing enqueueing sessions.
     *
     * @return list of entity names
     */
    List<String> getEntityNamesOfEnqueueingSessions();

    /**
     * Initializes async enqueueing session for all indexed entities.
     */
    void initAsyncEnqueueIndexAll();

    /**
     * Initializes async enqueueing session for provided entity.
     * Existing session will be removed and created again.
     *
     * @param entityName entity name
     * @return true if operation was successfully performed, false otherwise
     */
    boolean initAsyncEnqueueIndexAll(String entityName);

    /**
     * Suspends all enqueueing sessions.
     * Suspended sessions are ignored during session processing.
     * Session can be resumed by {@link #resumeAsyncEnqueueIndexAll}
     */
    void suspendAsyncEnqueueIndexAll();

    /**
     * Suspends enqueueing session for provided entity.
     * Suspended sessions are ignored during session processing.
     * Session can be resumed by {@link #resumeAsyncEnqueueIndexAll}
     *
     * @param entityName entity name
     * @return true if operation was successfully performed, false otherwise
     */
    boolean suspendAsyncEnqueueIndexAll(String entityName);

    /**
     * Resumes all previously suspended enqueueing sessions.
     */
    void resumeAsyncEnqueueIndexAll();

    /**
     * Resumes previously suspended enqueueing session for provided entity.
     *
     * @param entityName entity name
     * @return true if operation was successfully performed, false otherwise
     */
    boolean resumeAsyncEnqueueIndexAll(String entityName);

    /**
     * Terminates all enqueueing sessions.
     */
    void terminateAsyncEnqueueIndexAll();

    /**
     * Terminates enqueueing session for provided entity.
     *
     * @param entityName entity name
     * @return true if operation was successfully performed, false otherwise
     */
    boolean terminateAsyncEnqueueIndexAll(String entityName);

    /**
     * Processes next available enqueueing session - one batch (with default size) of entity instances
     * will be enqueued.
     *
     * @return amount of processed entity instances
     */
    int processNextEnqueueingSession();

    /**
     * Processes next available enqueueing session - one batch (with provided size) of entity instances
     * will be enqueued.
     *
     * @param batchSize batch size
     * @return amount of processed entity instances
     */
    int processNextEnqueueingSession(int batchSize);

    /**
     * Processes enqueueing session for provided entity - one batch (with default size) of entity instances
     * will be enqueued.
     *
     * @param entityName entity name
     * @return amount of processed entity instances
     */
    int processEnqueueingSession(String entityName);

    /**
     * Processes enqueueing session for provided entity - one batch (with provided size) of entity instances
     * will be enqueued.
     *
     * @param entityName entity name
     * @param batchSize  batch size
     * @return amount of processed entity instances
     */
    int processEnqueueingSession(String entityName, int batchSize);

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
