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

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.EntityOp;

import java.util.Collection;

/**
 * Provides functionality for enqueuing entity instances and processing queue.
 */
public interface IndexingQueueManager {

    /**
     * Sends provided entity instance to queue.
     *
     * @param entityInstance instance
     * @param operation      type of performed operation
     */
    void enqueue(Object entityInstance, EntityOp operation);

    /**
     * Sends provided entity instances to queue
     *
     * @param entityInstances instances
     * @param operation       type of performed operation
     */
    void enqueue(Collection<Object> entityInstances, EntityOp operation);

    /**
     * Sends entity instances to queue by provided primary keys.
     * Every PK should belong to instances of the same entity.
     *
     * @param metaClass MetaClass of enqueuing entity
     * @param entityPks primary keys of entities
     * @param operation type of performed operation
     */
    void enqueue(MetaClass metaClass, Collection<String> entityPks, EntityOp operation);

    /**
     * Sends all instances of provided entity to queue.
     *
     * @param entityName entity name
     * @param batchSize  amount of instances enqueued in single batch
     */
    void enqueueAll(String entityName, int batchSize);

    /**
     * Retrieves items from queue and save them to index.
     *
     * @param batchSize                amount of items processed in single batch
     * @param maxProcessedPerExecution max amount of items can be processed during single execution
     * @return amount of processed items
     */
    int processQueue(int batchSize, int maxProcessedPerExecution);

    /**
     * Removes all queue items related to provided entity.
     *
     * @param entityName entity
     */
    void emptyQueue(String entityName);
}
