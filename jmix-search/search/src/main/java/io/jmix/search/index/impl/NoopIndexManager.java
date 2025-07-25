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
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.IndexManager;
import io.jmix.search.index.IndexSynchronizationStatus;
import io.jmix.search.index.IndexValidationStatus;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.Map;

public class NoopIndexManager implements IndexManager {

    protected final ObjectMapper objectMapper;

    public NoopIndexManager() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public boolean createIndex(IndexConfiguration indexConfiguration) {
        return false;
    }

    @Override
    public boolean dropIndex(String indexName) {
        return false;
    }

    @Override
    public Map<IndexConfiguration, Boolean> recreateIndexes() {
        return Map.of();
    }

    @Override
    public Map<IndexConfiguration, Boolean> recreateIndexes(Collection<IndexConfiguration> indexConfigurations) {
        return Map.of();
    }

    @Override
    public boolean recreateIndex(IndexConfiguration indexConfiguration) {
        return false;
    }

    @Override
    public boolean isIndexExist(String indexName) {
        return false;
    }

    @Override
    public Map<IndexConfiguration, IndexValidationStatus> validateIndexes() {
        return Map.of();
    }

    @Override
    public Map<IndexConfiguration, IndexValidationStatus> validateIndexes(Collection<IndexConfiguration> indexConfigurations) {
        return Map.of();
    }

    @Override
    public IndexValidationStatus validateIndex(IndexConfiguration indexConfiguration) {
        return IndexValidationStatus.IRRELEVANT;
    }

    @Override
    public ObjectNode getIndexMetadata(@NonNull String indexName) {
        return objectMapper.createObjectNode();
    }

    @Override
    public Map<IndexConfiguration, IndexSynchronizationStatus> synchronizeIndexSchemas() {
        return Map.of();
    }

    @Override
    public Map<IndexConfiguration, IndexSynchronizationStatus> synchronizeIndexSchemas(Collection<IndexConfiguration> indexConfigurations) {
        return Map.of();
    }

    @Override
    public IndexSynchronizationStatus synchronizeIndexSchema(IndexConfiguration indexConfiguration) {
        return IndexSynchronizationStatus.IRRELEVANT;
    }
}
