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

import org.elasticsearch.client.indices.GetIndexResponse;

import java.util.Collection;

/**
 * Provides functionality for index management.
 */
public interface ESIndexManager {

    /**
     * Creates index if not exists using provided {@link IndexConfiguration}.
     *
     * @param indexConfiguration index configuration
     * @return true if index was successfully created, false otherwise
     */
    boolean createIndex(IndexConfiguration indexConfiguration);

    /**
     * Drops index by name.
     *
     * @param indexName index name
     * @return true if index was successfully dropped, false otherwise
     */
    boolean dropIndex(String indexName);

    /**
     * Drops (if exists) and creates index using provided {@link IndexConfiguration}.
     *
     * @param indexConfiguration index configuration
     * @return true if index was successfully recreated, false otherwise
     */
    boolean recreateIndex(IndexConfiguration indexConfiguration);

    /**
     * Checks if index exists.
     *
     * @param indexName index name
     * @return true if index exists, false otherwise
     */
    boolean isIndexExist(String indexName);

    /**
     * Checks if index has actual configuration.
     *
     * @param indexConfiguration actual configuration
     * @return true if existing index has the same configuration as the provided one, false otherwise
     */
    boolean isIndexActual(IndexConfiguration indexConfiguration);

    Collection<IndexValidationResult> validateIndexes();

    Collection<IndexValidationResult> validateIndexes(Collection<IndexConfiguration> indexConfigurations);

    IndexValidationResult validateIndex(IndexConfiguration indexConfiguration);

    /**
     * Requests info about index from ES cluster.
     *
     * @param indexName index name
     * @return response
     */
    GetIndexResponse getIndex(String indexName);

    /**
     * Synchronizes schemas of all search indices defined in application.
     * <p>See {@link ESIndexManager#synchronizeIndexSchemas(Collection)}
     * <p>See {@link ESIndexManager#synchronizeIndexSchema(IndexConfiguration)}
     *
     * @return Collection of {@link IndexSynchronizationResult} with details of synchronization
     */
    Collection<IndexSynchronizationResult> synchronizeIndexSchemas();

    /**
     * Synchronizes schemas of search indexes for provided collection of {@link IndexConfiguration}.
     * <p>
     * See {@link ESIndexManager#synchronizeIndexSchema(IndexConfiguration)}
     *
     * @param indexConfigurations actual index configurations
     * @return Collection of {@link IndexSynchronizationResult} with details of synchronization
     */
    Collection<IndexSynchronizationResult> synchronizeIndexSchemas(Collection<IndexConfiguration> indexConfigurations);

    /**
     * Synchronizes schema of search index for provided {@link IndexConfiguration}.
     * <p>
     * It tries to update schema to the actual state according to {@link IndexSchemaManagementStrategy}
     * defined by 'jmix.search.indexSchemaManagementStrategy' application property.
     *
     * @param indexConfiguration actual index configuration
     * @return {@link IndexSynchronizationResult} with details of synchronization
     */
    IndexSynchronizationResult synchronizeIndexSchema(IndexConfiguration indexConfiguration);
}
