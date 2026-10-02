/*
 * Copyright 2025 Haulmont.
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

package io.jmix.search.index.impl;

import io.jmix.core.Id;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.IndexManipulationResult;
import io.jmix.search.index.IndexOperationResult;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.index.queue.IndexingQueueManager;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;

import static java.util.Objects.requireNonNull;

@NullMarked
public class NoopIndexingQueueManager implements IndexingQueueManager {

    protected final IndexConfigurationManager indexConfigurationManager;

    @Autowired
    protected IndexLayout indexLayout;

    public NoopIndexingQueueManager(IndexConfigurationManager indexConfigurationManager) {
        this.indexConfigurationManager = indexConfigurationManager;
    }

    @Override
    public int emptyQueue() {
        return 0;
    }

    @Override
    public int emptyQueue(@Nullable String entityName, @Nullable String tenantId) {
        return 0;
    }

    @Override
    public int enqueueIndex(Object entityInstance) {
        return 0;
    }

    @Override
    public int enqueueIndexCollection(Collection<Object> entityInstances) {
        return 0;
    }

    @Override
    public int enqueueIndexByEntityId(Id<?> entityId) {
        return 0;
    }

    @Override
    public int enqueueIndexByEntityId(Id<?> entityId, @Nullable String tenantId) {
        return 0;
    }

    @Override
    public int enqueueIndexCollectionByEntityIds(Collection<Id<?>> entityIds) {
        return 0;
    }

    @Override
    public int enqueueIndexAll() {
        return 0;
    }

    @Override
    public int enqueueIndexAll(@Nullable String entityName, @Nullable String tenantId) {
        return 0;
    }

    @Override
    public List<String> getEntityNamesOfEnqueueingSessions() {
        return List.of();
    }

    @Override
    public List<String> getEntityNamesOfEnqueueingSessions(@Nullable String tenantId) {
        return List.of();
    }

    @Override
    public List<IndexOperationResult<IndexManipulationResult>> initAsyncEnqueueIndexAll() {
        return List.of();
    }

    @Override
    public List<IndexOperationResult<IndexManipulationResult>> initAsyncEnqueueIndexAll(String entityName) {
        return createResult(entityName, null);
    }

    @Override
    public List<IndexOperationResult<IndexManipulationResult>> initAsyncEnqueueIndexAll(@Nullable String entityName, @Nullable String tenantId) {
        return createResult(entityName, tenantId);
    }

    @Override
    public List<IndexOperationResult<IndexManipulationResult>> suspendAsyncEnqueueIndexAll() {
        return List.of();
    }

    @Override
    public List<IndexOperationResult<IndexManipulationResult>> suspendAsyncEnqueueIndexAll(String entityName) {
        return createResult(entityName, null);
    }

    @Override
    public List<IndexOperationResult<IndexManipulationResult>> suspendAsyncEnqueueIndexAll(@Nullable String entityName, @Nullable String tenantId) {
        return createResult(entityName, tenantId);
    }

    @Override
    public List<IndexOperationResult<IndexManipulationResult>> resumeAsyncEnqueueIndexAll() {
        return List.of();
    }

    @Override
    public List<IndexOperationResult<IndexManipulationResult>> resumeAsyncEnqueueIndexAll(String entityName) {
        return createResult(entityName, null);
    }

    @Override
    public List<IndexOperationResult<IndexManipulationResult>> resumeAsyncEnqueueIndexAll(@Nullable String entityName, @Nullable String tenantId) {
        return createResult(entityName, tenantId);
    }

    @Override
    public List<IndexOperationResult<IndexManipulationResult>> terminateAsyncEnqueueIndexAll() {
        return List.of();
    }

    @Override
    public List<IndexOperationResult<IndexManipulationResult>> terminateAsyncEnqueueIndexAll(String entityName) {
        return createResult(entityName, null);
    }

    @Override
    public List<IndexOperationResult<IndexManipulationResult>> terminateAsyncEnqueueIndexAll(@Nullable String entityName, @Nullable String tenantId) {
        return createResult(entityName, tenantId);
    }

    @Override
    public int processNextEnqueueingSession() {
        return 0;
    }

    @Override
    public int processNextEnqueueingSession(@Nullable String tenantId) {
        return 0;
    }

    @Override
    public int processNextEnqueueingSession(int batchSize) {
        return 0;
    }

    @Override
    public int processNextEnqueueingSession(@Nullable String tenantId, int batchSize) {
        return 0;
    }

    @Override
    public int processEnqueueingSession(String entityName) {
        return 0;
    }

    @Override
    public int processEnqueueingSession(String entityName, @Nullable String tenantId) {
        return 0;
    }

    @Override
    public int processEnqueueingSession(String entityName, int batchSize) {
        return 0;
    }

    @Override
    public int processEnqueueingSession(String entityName, @Nullable String tenantId, int batchSize) {
        return 0;
    }

    @Override
    public int enqueueDelete(Object entityInstance) {
        return 0;
    }

    @Override
    public int enqueueDeleteCollection(Collection<Object> entityInstances) {
        return 0;
    }

    @Override
    public int enqueueDeleteByEntityId(Id<?> entityId) {
        return 0;
    }

    @Override
    public int enqueueDeleteByEntityId(Id<?> entityId, @Nullable String tenantId) {
        return 0;
    }

    @Override
    public int enqueueDeleteCollectionByEntityIds(Collection<Id<?>> entityIds) {
        return 0;
    }

    @Override
    public int processNextBatch() {
        return 0;
    }

    @Override
    public int processNextBatch(int batchSize) {
        return 0;
    }

    @Override
    public int processEntireQueue() {
        return 0;
    }

    @Override
    public int processEntireQueue(int batchSize) {
        return 0;
    }

    protected List<IndexOperationResult<IndexManipulationResult>> createResult(String entityName, @Nullable String tenantId) {
        IndexConfiguration config = indexConfigurationManager.getIndexConfigurationByEntityName(entityName);
        if (tenantId == null) {
            return indexLayout.allIndexes(config).stream()
                    .map(index -> new IndexOperationResult<>(
                            entityName, index.indexName(), index.tenantId(), IndexManipulationResult.FAILURE))
                    .toList();
        }
        String indexName = indexLayout.indexName(config, tenantId);
        return List.of(
                new IndexOperationResult<>(entityName, requireNonNull(indexName), tenantId, IndexManipulationResult.FAILURE));
    }
}
