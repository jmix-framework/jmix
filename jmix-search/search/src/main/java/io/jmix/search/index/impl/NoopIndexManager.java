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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.search.index.AtomicIndexOperationResult;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.IndexManager;
import io.jmix.search.index.IndexManipulationResult;
import io.jmix.search.index.IndexOperationResult;
import io.jmix.search.index.IndexRecreationStatus;
import io.jmix.search.index.IndexSynchronizationStatus;
import io.jmix.search.index.IndexValidationStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;

@NullMarked
public class NoopIndexManager implements IndexManager {

    protected final ObjectMapper objectMapper;

    @Autowired
    protected IndexLayout indexLayout;

    public NoopIndexManager() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<IndexOperationResult<IndexManipulationResult>> createIndexes(
            Collection<IndexConfiguration> indexConfigurations, @Nullable String tenantId) {
        return createResult(indexConfigurations, IndexManipulationResult.FAILURE);
    }

    @Override
    public List<IndexOperationResult<IndexManipulationResult>> deleteIndexes(
            Collection<IndexConfiguration> indexConfigurations, @Nullable String tenantId) {
        return createResult(indexConfigurations, IndexManipulationResult.FAILURE);
    }

    @Override
    public boolean dropIndex(String indexName) {
        return false;
    }

    @Override
    public List<IndexOperationResult<IndexRecreationStatus>> recreateIndexes() {
        return List.of();
    }

    @Override
    public List<IndexOperationResult<IndexRecreationStatus>> recreateIndexes(
            Collection<IndexConfiguration> indexConfigurations, @Nullable String tenantId) {
        return createResult(indexConfigurations, IndexRecreationStatus.PROBLEM_WITH_INDEX_CREATING);
    }

    @Override
    public boolean isIndexExist(String indexName) {
        return false;
    }

    @Override
    public List<IndexOperationResult<IndexValidationStatus>> validateIndexes() {
        return List.of();
    }

    @Override
    public List<IndexOperationResult<IndexValidationStatus>> validateIndexes(
            Collection<IndexConfiguration> indexConfigurations, @Nullable String tenantId) {
        return createResult(indexConfigurations, IndexValidationStatus.IRRELEVANT);
    }

    @Override
    public ObjectNode getIndexMetadata(@NonNull String indexName) {
        return objectMapper.createObjectNode();
    }

    @Override
    public List<IndexOperationResult<IndexSynchronizationStatus>> synchronizeIndexSchemas() {
        return List.of();
    }

    @Override
    public List<IndexOperationResult<IndexSynchronizationStatus>> synchronizeIndexSchemas(
            Collection<IndexConfiguration> indexConfigurations, @Nullable String tenantId) {
        return createResult(indexConfigurations, IndexSynchronizationStatus.IRRELEVANT);
    }

    protected <RT extends AtomicIndexOperationResult> List<IndexOperationResult<RT>> createResult(
            Collection<IndexConfiguration> indexConfigurations, RT result) {
        return indexConfigurations.stream()
                .flatMap(configuration -> IndexOperationResults
                        .perIndex(configuration, indexLayout.allIndexes(configuration), index -> result)
                        .stream())
                .toList();
    }
}
