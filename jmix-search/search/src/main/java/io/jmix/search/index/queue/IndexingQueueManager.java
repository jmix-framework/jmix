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
import io.jmix.search.index.IndexManipulationResult;
import io.jmix.search.index.IndexOperationResult;
import org.jspecify.annotations.NullMarked;

import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Provides functionality for enqueuing entity instances and processing the queue.
 */
@NullMarked
public interface IndexingQueueManager {

    /**
     * Removes all queue items.
     *
     * @return amount of deleted items
     */
    int emptyQueue();

    /**
     * Removes all queue items related to the provided entity.
     *
     * @param entityName entity
     * @return amount of deleted items
     */
    default int emptyQueue(String entityName) {
        return emptyQueue(entityName, null);
    }

    /**
     * Removes the queue items of the given scope.
     *
     * @param entityName entity, or null for every indexed entity
     * @param tenantId   tenant whose items to remove, or null for every tenant
     * @return amount of deleted items
     */
    int emptyQueue(@Nullable String entityName, @Nullable String tenantId);

    /**
     * Sends the provided entity instance to indexing queue to store it to index.
     *
     * @param entityInstance instance
     * @return amount of enqueued instances
     */
    int enqueueIndex(Object entityInstance);

    /**
     * Sends the provided entity instances to indexing queue to store them to index.
     *
     * @param entityInstances instances
     * @return amount of enqueued instances
     */
    int enqueueIndexCollection(Collection<Object> entityInstances);

    /**
     * Sends an entity instance to indexing queue by the provided ID to store it to index.
     *
     * @param entityId ID of entity instance
     * @return amount of enqueued instances
     */
    int enqueueIndexByEntityId(Id<?> entityId);

    /**
     * Sends an entity instance to indexing queue by the provided ID, remembering the tenant it belongs to.
     * <p>
     * The tenant is an optimization, not a requirement: an indexing item is turned into a document by loading
     * the record, so the tenant can always be worked out then. Recorded here, it lets the queue tell which items
     * are addressed to an index that is currently unavailable and leave only those behind, instead of holding
     * back every item of the entity.
     *
     * @param entityId ID of entity instance
     * @param tenantId tenant the record belongs to, or {@code null} if it is unknown
     * @return amount of enqueued instances
     */
    int enqueueIndexByEntityId(Id<?> entityId, @Nullable String tenantId);

    /**
     * Sends entity instances to indexing queue by the provided IDs to store them to index.
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
     * Synchronously sends all instances of the provided entity to indexing queue.
     * <p>
     * Don't use it on a huge amount of data - all ids will be kept in memory during this process.
     * Use {@link #initAsyncEnqueueIndexAll} methods instead.
     *
     * @param entityName entity name
     * @return amount of enqueued instances
     */
    default int enqueueIndexAll(String entityName) {
        return enqueueIndexAll(entityName, null);
    }

    /**
     * Synchronously sends the instances of the given scope to the indexing queue.
     * <p>
     * Don't use it on a huge amount of data - all ids will be kept in memory during this process.
     * Use {@link #initAsyncEnqueueIndexAll} methods instead. Data of a single tenant is the size this is meant for.
     *
     * @param entityName entity name, or null for every indexed entity
     * @param tenantId   tenant whose instances to enqueue, or null for every tenant
     * @return amount of enqueued instances
     */
    int enqueueIndexAll(@Nullable String entityName, @Nullable String tenantId);

    /**
     * Gets entity names of all existing enqueueing sessions.
     *
     * @return list of entity names
     */
    List<String> getEntityNamesOfEnqueueingSessions();

    /**
     * Retrieves a list of entity names that currently have enqueueing sessions in the provided scope.
     * <p>
     * Sessions of entities that are not split by tenants are not returned when a tenant is named.
     *
     * @param tenantId tenant id, or null for every tenant
     * @return a list of entity names associated with enqueueing sessions in the requested scope
     * @throws IllegalStateException if a tenant is named and multitenancy is not available
     */
    List<String> getEntityNamesOfEnqueueingSessions(@Nullable String tenantId);

    /**
     * Initializes async enqueueing session for all indexed entities.
     *
     * @return one row per affected index
     */
    List<IndexOperationResult<IndexManipulationResult>> initAsyncEnqueueIndexAll();

    /**
     * Initializes async enqueueing session for the provided entity.
     * Existing session will be removed and created again.
     *
     * @param entityName entity name
     * @return result of operation
     */
    List<IndexOperationResult<IndexManipulationResult>> initAsyncEnqueueIndexAll(String entityName);

    /**
     * Initializes async enqueueing session for the provided entity and tenant.
     * Existing session will be removed and created again.
     *
     * @param entityName entity name, or null for every indexed entity
     * @param tenantId   tenant id, or null for every tenant
     * @return result of operation
     * @throws IllegalArgumentException if a named entity is not tenant-aware; an entity without a tenant
     *                                  attribute is left out when no entity is named
     * @throws IllegalStateException if a tenant is named and multitenancy is not available
     */
    List<IndexOperationResult<IndexManipulationResult>> initAsyncEnqueueIndexAll(@Nullable String entityName, @Nullable String tenantId);

    /**
     * Suspends all enqueueing sessions.
     * Suspended sessions are ignored during session processing.
     * Session can be resumed by {@link #resumeAsyncEnqueueIndexAll}
     *
     * @return one row per affected index
     */
    List<IndexOperationResult<IndexManipulationResult>> suspendAsyncEnqueueIndexAll();

    /**
     * Suspends enqueueing session for the provided entity.
     * Suspended sessions are ignored during session processing.
     * Session can be resumed by {@link #resumeAsyncEnqueueIndexAll}
     *
     * @param entityName entity name
     * @return result of operation
     */
    List<IndexOperationResult<IndexManipulationResult>> suspendAsyncEnqueueIndexAll(String entityName);

    /**
     * Suspends enqueueing session for the provided entity and tenant.
     *
     * @param entityName entity name, or null for every indexed entity
     * @param tenantId   tenant id, or null for every tenant
     * @return result of operation
     * @throws IllegalArgumentException if a named entity is not tenant-aware; an entity without a tenant
     *                                  attribute is left out when no entity is named
     * @throws IllegalStateException if a tenant is named and multitenancy is not available
     */
    List<IndexOperationResult<IndexManipulationResult>> suspendAsyncEnqueueIndexAll(@Nullable String entityName, @Nullable String tenantId);

    /**
     * Resumes all previously suspended enqueueing sessions.
     *
     * @return one row per affected index
     */
    List<IndexOperationResult<IndexManipulationResult>> resumeAsyncEnqueueIndexAll();

    /**
     * Resumes previously suspended enqueueing session for the provided entity.
     *
     * @param entityName entity name
     * @return result of operation
     */
    List<IndexOperationResult<IndexManipulationResult>> resumeAsyncEnqueueIndexAll(String entityName);

    /**
     * Resumes previously suspended enqueueing session for the provided entity and tenant.
     *
     * @param entityName entity name, or null for every indexed entity
     * @param tenantId   tenant id, or null for every tenant
     * @return result of operation
     * @throws IllegalArgumentException if a named entity is not tenant-aware; an entity without a tenant
     *                                  attribute is left out when no entity is named
     * @throws IllegalStateException if a tenant is named and multitenancy is not available
     */
    List<IndexOperationResult<IndexManipulationResult>> resumeAsyncEnqueueIndexAll(@Nullable String entityName, @Nullable String tenantId);

    /**
     * Terminates all enqueueing sessions.
     *
     * @return one row per affected index
     */
    List<IndexOperationResult<IndexManipulationResult>> terminateAsyncEnqueueIndexAll();

    /**
     * Terminates enqueueing session for the provided entity.
     *
     * @param entityName entity name
     * @return result of operation
     */
    List<IndexOperationResult<IndexManipulationResult>> terminateAsyncEnqueueIndexAll(String entityName);

    /**
     * Terminates enqueueing session for the provided entity and tenant.
     *
     * @param entityName entity name, or null for every indexed entity
     * @param tenantId   tenant id, or null for every tenant
     * @return result of operation
     * @throws IllegalArgumentException if a named entity is not tenant-aware; an entity without a tenant
     *                                  attribute is left out when no entity is named
     * @throws IllegalStateException if a tenant is named and multitenancy is not available
     */
    List<IndexOperationResult<IndexManipulationResult>> terminateAsyncEnqueueIndexAll(@Nullable String entityName, @Nullable String tenantId);

    /**
     * Processes next available enqueueing session - one batch (with default size) of entity instances
     * will be enqueued.
     *
     * @return amount of processed entity instances
     */
    int processNextEnqueueingSession();

    /**
     * Processes the next available enqueueing session in the provided scope.
     * <p>
     * Sessions of entities that are not split by tenants are not processed when a tenant is named.
     *
     * @param tenantId tenant id, or null for the next available session whatever tenant it belongs to
     * @return amount of processed entity instances
     * @throws IllegalStateException if a tenant is named and multitenancy is not available
     */
    int processNextEnqueueingSession(@Nullable String tenantId);

    /**
     * Processes next available enqueueing session - one batch (with provided size) of entity instances
     * will be enqueued.
     *
     * @param batchSize batch size
     * @return amount of processed entity instances
     */
    int processNextEnqueueingSession(int batchSize);

    /**
     * Processes the next available enqueueing session in the provided scope.
     * <p>
     * Sessions of entities that are not split by tenants are not processed when a tenant is named.
     *
     * @param tenantId  tenant id, or null for the next available session whatever tenant it belongs to
     * @param batchSize batch size
     * @return amount of processed entity instances
     * @throws IllegalStateException if a tenant is named and multitenancy is not available
     */
    int processNextEnqueueingSession(@Nullable String tenantId, int batchSize);

    /**
     * Processes enqueueing session for the provided entity - one batch (with default size) of entity instances
     * will be enqueued.
     *
     * @param entityName entity name
     * @return amount of processed entity instances
     */
    int processEnqueueingSession(String entityName);

    /**
     * Processes enqueueing session for the provided entity and tenant.
     *
     * @param entityName entity name
     * @param tenantId   tenant id, or null for the session of that entity whatever tenant it belongs to
     * @return amount of processed entity instances
     * @throws IllegalArgumentException if a tenant is named and the entity is not tenant-aware
     * @throws IllegalStateException if a tenant is named and multitenancy is not available
     */
    int processEnqueueingSession(String entityName, @Nullable String tenantId);

    /**
     * Processes enqueueing session for the provided entity - one batch (with provided size) of entity instances
     * will be enqueued.
     *
     * @param entityName entity name
     * @param batchSize  batch size
     * @return amount of processed entity instances
     */
    int processEnqueueingSession(String entityName, int batchSize);

    /**
     * Processes enqueueing session for the provided entity and tenant.
     *
     * @param entityName entity name
     * @param tenantId   tenant id, or null for the session of that entity whatever tenant it belongs to
     * @param batchSize  batch size
     * @return amount of processed entity instances
     * @throws IllegalArgumentException if a tenant is named and the entity is not tenant-aware
     * @throws IllegalStateException if a tenant is named and multitenancy is not available
     */
    int processEnqueueingSession(String entityName, @Nullable String tenantId, int batchSize);

    /**
     * Sends the provided entity instance to indexing queue to delete it from index.
     *
     * @param entityInstance instance
     * @return amount of enqueued instances
     */
    int enqueueDelete(Object entityInstance);

    /**
     * Sends the provided entity instances to indexing queue to delete them from index.
     *
     * @param entityInstances instances
     * @return amount of enqueued instances
     */
    int enqueueDeleteCollection(Collection<Object> entityInstances);

    /**
     * Sends an entity instance to indexing queue by the provided ID to delete it from index.
     *
     * @param entityId ID of entity instance
     * @return amount of enqueued instances
     */
    int enqueueDeleteByEntityId(Id<?> entityId);

    /**
     * Sends an entity instance to indexing queue by the provided ID to delete it from the index of the provided
     * tenant.
     * <p>
     * The tenant of a deleted record cannot be determined later - by the time the queue is processed the record is
     * gone from the database - so a caller that still knows it passes it here. An item enqueued without a tenant is
     * deleted from every index of the entity instead, which is correct but sends one request per index.
     *
     * @param entityId ID of entity instance
     * @param tenantId tenant the record belongs to, or {@code null} if it is unknown
     * @return amount of enqueued instances
     */
    int enqueueDeleteByEntityId(Id<?> entityId, @Nullable String tenantId);

    /**
     * Sends entity instances to indexing queue by the provided IDs to delete them from index.
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
