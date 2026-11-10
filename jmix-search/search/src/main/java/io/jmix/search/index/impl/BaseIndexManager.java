/*
 * Copyright 2026 Haulmont.
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
import io.jmix.core.common.util.Preconditions;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.*;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.index.mapping.IndexMappingConfiguration;
import io.jmix.search.index.queue.IndexingQueueManager;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Contains non-platform-specific operations.
 * Interaction with indexes is performed in platform-specific implementations.
 */
@NullMarked
public abstract class BaseIndexManager<TState, TSettings, TJsonp> implements IndexManager {

    private static final Logger log = LoggerFactory.getLogger(BaseIndexManager.class);

    protected final IndexConfigurationManager indexConfigurationManager;
    protected final IndexStateRegistry indexStateRegistry;
    protected final SearchProperties searchProperties;

    protected final ObjectMapper objectMapper;

    @Autowired
    protected IndexLayout indexLayout;

    @Autowired
    protected IndexingQueueManager indexingQueueManager;
    protected final IndexConfigurationComparator<TState, TSettings, TJsonp> indexConfigurationComparator;
    protected final IndexStateResolver<TState, TJsonp> indexStateResolver;

    protected BaseIndexManager(IndexConfigurationManager indexConfigurationManager,
                               IndexStateRegistry indexStateRegistry,
                               SearchProperties searchProperties,
                               IndexConfigurationComparator<TState, TSettings, TJsonp> indexConfigurationComparator,
                               IndexStateResolver<TState, TJsonp> indexStateResolver) {
        this.indexConfigurationManager = indexConfigurationManager;
        this.indexStateRegistry = indexStateRegistry;
        this.searchProperties = searchProperties;
        this.indexConfigurationComparator = indexConfigurationComparator;
        this.indexStateResolver = indexStateResolver;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<IndexOperationResult<IndexManipulationResult>> createIndexes(
            Collection<IndexConfiguration> indexConfigurations, @Nullable String tenantId) {
        Preconditions.checkNotNullArgument(indexConfigurations);
        return performOnEachIndex(indexConfigurations, tenantId, this::createIndex, IndexManipulationResult.FAILURE);
    }

    @Override
    public List<IndexOperationResult<IndexManipulationResult>> deleteIndexes(
            Collection<IndexConfiguration> indexConfigurations, @Nullable String tenantId) {
        Preconditions.checkNotNullArgument(indexConfigurations);
        List<IndexOperationResult<IndexManipulationResult>> results = performOnEachIndex(
                indexConfigurations, tenantId,
                (configuration, indexName) -> IndexManipulationResult.of(dropIndex(indexName)),
                IndexManipulationResult.FAILURE);
        discardQueuedWork(results);
        return results;
    }

    /**
     * Removes what the indexing queue was holding for the indexes that were actually deleted.
     * <p>
     * There is nowhere left to write those items, and dropping an index marks it unavailable, which keeps its
     * items out of every batch from now on. Left in place they would sit in the queue table for good, so deleting
     * the index removes them too.
     * <p>
     * An index whose deletion failed still holds its documents, so its queued items are kept: discarding them
     * would leave a record that was deleted or edited in the index for good, with nothing left to correct it.
     */
    protected void discardQueuedWork(List<IndexOperationResult<IndexManipulationResult>> deletions) {
        for (IndexOperationResult<IndexManipulationResult> deletion : deletions) {
            if (!deletion.isSuccess()) {
                log.info("Queue items of entity '{}' are kept: index '{}' has not been deleted",
                        deletion.entityName(), deletion.indexName());
                continue;
            }
            int discarded = indexingQueueManager.emptyQueue(deletion.entityName(), deletion.tenantId());
            if (discarded > 0) {
                log.info("{} queue items of entity '{}' are discarded with the deleted index '{}'",
                        discarded, deletion.entityName(), deletion.indexName());
            }
        }
    }

    protected abstract IndexManipulationResult createIndex(IndexConfiguration indexConfiguration, String indexName);

    /**
     * Creates the index, treating "it is already there" as the same outcome as having created it.
     * <p>
     * Two nodes of a cluster are told about a new tenant at the same moment and both go to create its indexes.
     * One of them wins; the engine answers the other with an error, which the client raises as a runtime
     * exception - the loser would otherwise leave the index unmarked, and an unmarked index is an unavailable
     * one. The index is there, the node just did not put it there.
     * <p>
     * Asking the engine again costs a round trip, and only on the path where creation has already failed.
     */
    protected boolean createdOrAlreadyThere(IndexConfiguration indexConfiguration, String indexName) {
        try {
            if (createIndex(indexConfiguration, indexName).isSuccess()) {
                return true;
            }
        } catch (RuntimeException e) {
            log.info("Index '{}' of entity '{}' was not created by this node, checking whether it is there anyway",
                    indexName, indexConfiguration.getEntityName(), e);
            return isIndexExist(indexName);
        }
        return isIndexExist(indexName);
    }

    @Override
    public List<IndexOperationResult<IndexRecreationStatus>> recreateIndexes() {
        return recreateIndexes(indexConfigurationManager.getAllIndexConfigurations(), null);
    }

    @Override
    public List<IndexOperationResult<IndexRecreationStatus>> recreateIndexes(
            Collection<IndexConfiguration> indexConfigurations, @Nullable String tenantId) {
        Preconditions.checkNotNullArgument(indexConfigurations);
        return performOnEachIndex(indexConfigurations, tenantId, this::recreateIndex,
                IndexRecreationStatus.PROBLEM_WITH_INDEX_CREATING);
    }

    /**
     * Recreates the index, creating it when it is not there at all.
     * <p>
     * The engines answer a deletion of a missing index with an error, so dropping unconditionally would turn
     * "recreate an index that does not exist" - a new tenant, an index removed by hand, a fresh engine - into a
     * failure instead of a creation.
     */
    protected IndexRecreationStatus recreateIndex(IndexConfiguration indexConfiguration, String indexName) {
        if (isIndexExist(indexName) && !dropIndex(indexName)) {
            return IndexRecreationStatus.PROBLEM_WITH_INDEX_DELETING;
        }

        IndexManipulationResult creationResult = createIndex(indexConfiguration, indexName);

        if (creationResult == IndexManipulationResult.FAILURE) {
            return IndexRecreationStatus.PROBLEM_WITH_INDEX_CREATING;
        }
        return IndexRecreationStatus.SUCCESS;
    }

    @Override
    public List<IndexOperationResult<IndexValidationStatus>> validateIndexes() {
        return validateIndexes(indexConfigurationManager.getAllIndexConfigurations(), null);
    }

    @Override
    public List<IndexOperationResult<IndexValidationStatus>> validateIndexes(
            Collection<IndexConfiguration> indexConfigurations, @Nullable String tenantId) {
        Preconditions.checkNotNullArgument(indexConfigurations);
        return performOnEachIndex(indexConfigurations, tenantId, this::validateIndex,
                IndexValidationStatus.IRRELEVANT);
    }

    protected IndexValidationStatus validateIndex(IndexConfiguration indexConfiguration, String indexName) {
        boolean indexExist = isIndexExist(indexName);

        IndexValidationStatus status = getIndexValidationStatus(indexConfiguration, indexName, indexExist);

        if (status == IndexValidationStatus.ACTUAL) {
            indexStateRegistry.markIndexAsAvailable(indexName);
        } else {
            indexStateRegistry.markIndexAsUnavailable(indexName);
        }

        log.info("Validation status of search index '{}' (entity '{}'): {}",
                indexName, indexConfiguration.getEntityName(), status);
        return status;
    }

    protected IndexValidationStatus getIndexValidationStatus(IndexConfiguration indexConfiguration,
                                                             String indexName,
                                                             boolean indexExist) {
        if (indexExist) {
            ConfigurationComparingResult result = indexConfigurationComparator.compareConfigurations(indexConfiguration, indexName);
            if (result.isEqual()) {
                return IndexValidationStatus.ACTUAL;
            } else {
                return IndexValidationStatus.IRRELEVANT;
            }
        } else {
            return IndexValidationStatus.MISSING;
        }
    }

    @Override
    public List<IndexOperationResult<IndexSynchronizationStatus>> synchronizeIndexSchemas() {
        return synchronizeIndexSchemas(indexConfigurationManager.getAllIndexConfigurations(), null);
    }

    @Override
    public List<IndexOperationResult<IndexSynchronizationStatus>> synchronizeIndexSchemas(
            Collection<IndexConfiguration> indexConfigurations, @Nullable String tenantId) {
        Preconditions.checkNotNullArgument(indexConfigurations);

        IndexSchemaManagementStrategy strategy = searchProperties.getIndexSchemaManagementStrategy();
        return performOnEachIndex(indexConfigurations, tenantId,
                (configuration, indexName) -> synchronizeIndexSchema(configuration, strategy, indexName),
                IndexSynchronizationStatus.IRRELEVANT);
    }

    protected IndexSynchronizationStatus synchronizeIndexSchema(
            IndexConfiguration indexConfiguration,
            IndexSchemaManagementStrategy strategy,
            String indexName) {
        log.info("Synchronize search index '{}' (entity '{}') according to strategy '{}'",
                indexName, indexConfiguration.getEntityName(), strategy);
        IndexSynchronizationStatus status;
        boolean indexExist = isIndexExist(indexName);
        if (indexExist) {
            ConfigurationComparingResult result = indexConfigurationComparator.compareConfigurations(indexConfiguration, indexName);
            if (result.isIndexRecreatingRequired()) {
                status = recreateIrrelevantIndex(indexConfiguration, strategy, indexName);
            } else if (result.isConfigurationUpdateRequired()) {
                status = updateIndexConfiguration(indexConfiguration, strategy, result, indexName);
            } else {
                status = IndexSynchronizationStatus.ACTUAL;
                indexStateRegistry.markIndexAsAvailable(indexName);
            }
        } else {
            status = handleMissingIndex(indexConfiguration, strategy, indexName);
        }
        log.info("Synchronization status of search index '{}' (entity '{}'): {}",
                indexName, indexConfiguration.getEntityName(), status);
        return status;
    }

    protected IndexSynchronizationStatus recreateIrrelevantIndex(
            IndexConfiguration indexConfiguration,
            IndexSchemaManagementStrategy strategy,
            String indexName) {
        IndexSynchronizationStatus status;
        if (strategy.isIndexRecreationSupported()) {
            boolean created = recreateIndex(indexConfiguration, indexName).isSuccess();
            if (created) {
                status = IndexSynchronizationStatus.RECREATED;
                indexStateRegistry.markIndexAsAvailable(indexName);
            } else {
                status = IndexSynchronizationStatus.IRRELEVANT;
                indexStateRegistry.markIndexAsUnavailable(indexName);
            }
        } else {
            status = IndexSynchronizationStatus.IRRELEVANT;
            indexStateRegistry.markIndexAsUnavailable(indexName);
        }
        return status;
    }

    protected IndexSynchronizationStatus handleMissingIndex(
            IndexConfiguration indexConfiguration,
            IndexSchemaManagementStrategy strategy,
            String indexName) {
        IndexSynchronizationStatus status;

        if (!strategy.isIndexCreationSupported()) {
            status = IndexSynchronizationStatus.MISSING;
            indexStateRegistry.markIndexAsUnavailable(indexName);
        } else {
            if (createdOrAlreadyThere(indexConfiguration, indexName)) {
                status = IndexSynchronizationStatus.CREATED;
                indexStateRegistry.markIndexAsAvailable(indexName);
            } else {
                status = IndexSynchronizationStatus.MISSING;
                indexStateRegistry.markIndexAsUnavailable(indexName);
            }
        }
        return status;
    }

    protected IndexSynchronizationStatus updateIndexConfiguration(
            IndexConfiguration indexConfiguration,
            IndexSchemaManagementStrategy strategy,
            ConfigurationComparingResult result,
            String indexName) {
        if (strategy.isConfigurationUpdateSupported()) {
            if (result.isMappingUpdateRequired()) {
                boolean mappingSavingResult = putMapping(indexName, indexConfiguration.getMapping());
                if (mappingSavingResult) {
                    indexStateRegistry.markIndexAsAvailable(indexName);
                    return IndexSynchronizationStatus.UPDATED;
                } else {
                    log.error("Problem with index mapping saving.");
                    indexStateRegistry.markIndexAsUnavailable(indexName);
                    return IndexSynchronizationStatus.IRRELEVANT;
                }
            }
            //Such exception throwing is because we have a potential possibility when the
            //strategy.isConfigurationUpdateSupported()==true and the result.isMappingUpdateRequired()==false.
            //But actually it is an impossible situation because actually the result.isSettingsUpdateRequired() method
            //always returns false. The ability to update settings will be implemented later.
            throw new UnsupportedOperationException("An index settings update is not supported yet. Only index recreating is supported.");
        } else {
            indexStateRegistry.markIndexAsUnavailable(indexName);
            return IndexSynchronizationStatus.IRRELEVANT;
        }
    }

    protected abstract boolean putMapping(String indexName, IndexMappingConfiguration mapping);

    /**
     * Performs the operation on every physical index of the configuration: on the index of each known tenant if
     * the configuration is split by tenants, on a single index otherwise.
     * <p>
     * A failure on one index doesn't stop the others: it is reported as {@code resultOnError} for that index, so
     * that one broken tenant cannot leave an administrative operation without results for the rest.
     *
     * @param resultOnError result reported for an index whose operation has thrown
     */
    protected <RT extends AtomicIndexOperationResult> List<IndexOperationResult<RT>> performOnEachIndex(
            Collection<IndexConfiguration> configurations, @Nullable String tenantId,
            AtomicIndexOperation<RT> atomicIndexOperation, RT resultOnError) {
        List<IndexOperationResult<RT>> results = new ArrayList<>();
        for (IndexConfiguration configuration : configurations) {
            results.addAll(IndexOperationResults.perIndex(configuration, indexesInScope(configuration, tenantId),
                    index -> performSafely(configuration, index, atomicIndexOperation, resultOnError)));
        }
        return results;
    }

    /**
     * @return indexes of the configuration the operation applies to: every one of them when no tenant is named,
     * and the index of that tenant otherwise. An entity that is not split by tenants has no index of a single
     * tenant, because its index is shared by everyone, so an operation limited to one tenant skips it.
     */
    protected List<IndexLayout.TenantIndex> indexesInScope(IndexConfiguration configuration,
                                                           @Nullable String tenantId) {
        if (tenantId == null) {
            return indexLayout.allIndexes(configuration);
        }
        if (!indexLayout.isSplitByTenants(configuration)) {
            return List.of();
        }
        String indexName = indexLayout.indexName(configuration, tenantId);
        return indexName == null ? List.of() : List.of(new IndexLayout.TenantIndex(tenantId, indexName));
    }

    protected <RT extends AtomicIndexOperationResult> RT performSafely(IndexConfiguration configuration,
                                                                       IndexLayout.TenantIndex index,
                                                                       AtomicIndexOperation<RT> atomicIndexOperation,
                                                                       RT resultOnError) {
        try {
            return atomicIndexOperation.perform(configuration, index.indexName());
        } catch (UnsupportedOperationException e) {
            // The operation is impossible for every index, so reporting it per index would only hide the reason.
            throw e;
        } catch (RuntimeException e) {
            log.error("Operation on index '{}' of entity '{}' and tenant '{}' failed",
                    index.indexName(), configuration.getEntityName(), index.tenantId(), e);
            return resultOnError;
        }
    }

    @FunctionalInterface
    protected interface AtomicIndexOperation<RT extends AtomicIndexOperationResult> {

        RT perform(IndexConfiguration configuration, String indexName);
    }
}
