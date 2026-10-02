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

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Manages the search indexes of the application.
 * <p>
 * The scope of an operation is a set of index configurations and, optionally, a tenant, where {@code null} stands
 * for every tenant. An entity that is not split by tenants has no index of a single tenant - its index is shared by
 * everyone - so an operation asked for one tenant leaves such an entity alone.
 * <p>
 * Every operation reports a row per index it reached, because one configuration corresponds to as many indexes as
 * the application has tenants. An operation that reached no index at all reports nothing.
 */
@NullMarked
public interface IndexManager {

    /**
     * Creates the indexes of the given scope that do not exist yet.
     *
     * @param indexConfigurations entities whose indexes to create
     * @param tenantId            tenant to create the indexes of, or null for every tenant
     * @return a row per index the creation reached
     */
    List<IndexOperationResult<IndexManipulationResult>> createIndexes(
            Collection<IndexConfiguration> indexConfigurations, @Nullable String tenantId);

    /**
     * Deletes the indexes of the given scope. All data they hold is lost, and the items the indexing queue was
     * holding for them are discarded with them: there is nowhere left to write those items, and a dropped index is
     * marked unavailable, which would keep them out of every batch while they sat in the queue table for good.
     *
     * @param indexConfigurations entities whose indexes to delete
     * @param tenantId            tenant to delete the indexes of, or null for every tenant
     * @return a row per index the deletion reached
     */
    List<IndexOperationResult<IndexManipulationResult>> deleteIndexes(
            Collection<IndexConfiguration> indexConfigurations, @Nullable String tenantId);

    /**
     * Drops index by name.
     *
     * @param indexName index name
     * @return true if index was successfully dropped, false otherwise
     */
    boolean dropIndex(String indexName);

    /**
     * Drops and creates all search indexes.
     *
     * @return operation result per every index
     */
    List<IndexOperationResult<IndexRecreationStatus>> recreateIndexes();

    /**
     * Drops and creates search indexes using provided collection of {@link IndexConfiguration}.
     *
     * @param indexConfigurations index configurations
     * @param tenantId            tenant to recreate the indexes of, or null for every tenant
     * @return operation result per every index
     */
    List<IndexOperationResult<IndexRecreationStatus>> recreateIndexes(
            Collection<IndexConfiguration> indexConfigurations, @Nullable String tenantId);

    /**
     * Checks if index exists.
     *
     * @param indexName index name
     * @return true if index exists, false otherwise
     */
    boolean isIndexExist(String indexName);

    /**
     * Validates current state of schema of all search indexes defined in application.
     *
     * @return {@link IndexValidationStatus} per every index
     */
    List<IndexOperationResult<IndexValidationStatus>> validateIndexes();

    /**
     * Validates current state of index schema related to provided collection of {@link IndexConfiguration}.
     *
     * @param indexConfigurations actual configurations
     * @param tenantId            tenant to validate the indexes of, or null for every tenant
     * @return {@link IndexValidationStatus} per every index
     */
    List<IndexOperationResult<IndexValidationStatus>> validateIndexes(
            Collection<IndexConfiguration> indexConfigurations, @Nullable String tenantId);

    /**
     * Requests info about index from server.
     *
     * @param indexName index name
     * @return response as Json
     */
    ObjectNode getIndexMetadata(String indexName);

    /**
     * Synchronizes schemas of all search indexes defined in application.
     * <p>
     * See {@link #synchronizeIndexSchemas(Collection, String)}.
     *
     * @return {@link IndexSynchronizationStatus} per every index
     */
    List<IndexOperationResult<IndexSynchronizationStatus>> synchronizeIndexSchemas();

    /**
     * Synchronizes schemas of search indexes for provided collection of {@link IndexConfiguration}.
     * <p>
     * The schema is brought to the actual state according to the {@link IndexSchemaManagementStrategy} defined by
     * the 'jmix.search.index-schema-management-strategy' application property.
     *
     * @param indexConfigurations actual index configurations
     * @param tenantId            tenant to synchronize the indexes of, or null for every tenant
     * @return {@link IndexSynchronizationStatus} per every index
     */
    List<IndexOperationResult<IndexSynchronizationStatus>> synchronizeIndexSchemas(
            Collection<IndexConfiguration> indexConfigurations, @Nullable String tenantId);

}
