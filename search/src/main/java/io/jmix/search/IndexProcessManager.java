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

package io.jmix.search;

import javax.annotation.Nullable;

/**
 * Provides functionality for requesting and processing entities reindex.
 */
public interface IndexProcessManager {

    /**
     * Performs reindex of all entities synchronously.
     */
    void reindexAll();

    /**
     * Performs reindex of provided entity synchronously.
     *
     * @param entityName entity
     */
    void reindexEntity(@Nullable String entityName);

    /**
     * Performs reindex of all entities asynchronously.
     * <p>Entity instances will be added to the queue by the invocation of {@link IndexProcessManager#processNextReindexingEntity}
     * or {@link IndexProcessManager#processNextReindexingBatch} from a scheduled task.
     */
    void scheduleReindexAll();

    /**
     * Performs reindex of provided entity asynchronously.
     * <p>Entity instances will be added to the queue by the invocation of {@link IndexProcessManager#processNextReindexingEntity}
     * or {@link IndexProcessManager#processNextReindexingBatch} from a scheduled task.
     *
     * @param entityName entity
     */
    void scheduleReindexEntity(@Nullable String entityName);

    /**
     * Adds all instances of next entity to queue.
     * <p>At least one entity should be previously scheduled for reindex by calling {@link IndexProcessManager#scheduleReindexAll()}
     * or {@link IndexProcessManager#scheduleReindexEntity(String)} ()}.
     */
    void processNextReindexingEntity();

    /**
     * Adds next batch of instances to queue
     * <p>At least one entity should be previously scheduled for reindex by calling {@link IndexProcessManager#scheduleReindexAll()}
     * or {@link IndexProcessManager#scheduleReindexEntity(String)} ()}.
     */
    void processNextReindexingBatch();

    /**
     * Process next portion of queue elements.
     *
     * @return amount of processed queue elements
     */
    int processQueue();
}
