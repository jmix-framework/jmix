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

import io.jmix.core.Id;
import io.jmix.core.IdSerialization;
import io.jmix.core.security.Authenticated;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.impl.MultitenancyAdapter;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.index.queue.IndexingQueueManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.jmx.export.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@ManagedResource(description = "Manages entity indexing for full text search", objectName = "jmix.search:type=EntityIndexing")
@Component("search_EntityIndexingManagementFacade")
public class EntityIndexingManagementFacade {

    @Autowired
    protected IndexingQueueManager indexingQueueManager;
    @Autowired
    protected EntityIndexer entityIndexer;
    @Autowired
    protected IdSerialization idSerialization;
    @Autowired
    protected IndexManager indexManager;
    @Autowired
    protected IndexConfigurationManager indexConfigurationManager;
    @Autowired
    protected SearchProperties searchProperties;
    @Autowired
    protected MultitenancyAdapter multitenancyAdapter;

    @ManagedAttribute(description = "Strategy of index synchronization")
    public String getIndexSchemaManagementStrategy() {
        return searchProperties.getIndexSchemaManagementStrategy().toString();
    }

    @ManagedAttribute(description = "List of entities to be asynchronously enqueued")
    public List<String> getEntityNamesOfAsyncEnqueueingSessions() {
        return indexingQueueManager.getEntityNamesOfEnqueueingSessions();
    }

    @ManagedAttribute(description = "Search status")
    public String searchStatus() {
        return searchProperties.isEnabled() ? "Enabled" : "Disabled";
    }

    @Authenticated
    @ManagedOperation(description = "Synchronously enqueues all instances of all indexed entities. Don't use it on a huge amount of data")
    public String enqueueIndexAll() {
        int amount = indexingQueueManager.enqueueIndexAll();
        return String.format("%d instances within all indexed entities have been enqueued", amount);
    }

    @Authenticated
    @ManagedOperation(description = "Synchronously enqueues all instances of provided indexed entity. " +
                                    "Don't use it on a huge amount of data")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order")
    })
    public String enqueueIndexAll(String entityName) {
        String entity = StringUtils.trimToNull(entityName);
        InputValidationResult inputValidationResult = validateInputEntity(entity);
        if (!inputValidationResult.isValid()) {
            return inputValidationResult.getMessage();
        }

        int amount = indexingQueueManager.enqueueIndexAll(entity);
        return String.format("%d instances of entity '%s' have been enqueued", amount, entity);
    }

    @Authenticated
    @ManagedOperation(description = "Init async enqueueing process for all indexed entities")
    public String initAsyncEnqueueing() {
        List<IndexOperationResult<IndexManipulationResult>> results =
                indexingQueueManager.initAsyncEnqueueIndexAll();
        return formatResults("Init async enqueueing", results);
    }

    @Authenticated
    @ManagedOperation(description = "Init async enqueueing process for provided entity")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order")
    })
    public String initAsyncEnqueueing(String entityName) {
        String entity = StringUtils.trimToNull(entityName);
        InputValidationResult inputValidationResult = validateInputEntity(entity);
        if (!inputValidationResult.isValid()) {
            return inputValidationResult.getMessage();
        }

        indexingQueueManager.initAsyncEnqueueIndexAll(entity);
        return String.format("Async enqueueing process has been initialized for entity '%s'", entity);
    }

    @Authenticated
    @ManagedOperation(description = "Suspend async enqueueing process")
    public String suspendAsyncEnqueueing() {
        List<IndexOperationResult<IndexManipulationResult>> results =
                indexingQueueManager.suspendAsyncEnqueueIndexAll();
        return formatResults("Suspend async enqueueing", results);
    }

    @Authenticated
    @ManagedOperation(description = "Suspend async enqueueing process for provided entity")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order")
    })
    public String suspendAsyncEnqueueing(String entityName) {
        String entity = StringUtils.trimToNull(entityName);
        InputValidationResult inputValidationResult = validateInputEntity(entity);
        if (!inputValidationResult.isValid()) {
            return inputValidationResult.getMessage();
        }

        indexingQueueManager.suspendAsyncEnqueueIndexAll(entity);
        return String.format("Async enqueueing process has been suspended for entity '%s'", entity);
    }

    @Authenticated
    @ManagedOperation(description = "Resume all previously suspended async enqueueing processes")
    public String resumeAsyncEnqueueing() {
        List<IndexOperationResult<IndexManipulationResult>> results =
                indexingQueueManager.resumeAsyncEnqueueIndexAll();
        return formatResults("Resume async enqueueing", results);
    }

    @Authenticated
    @ManagedOperation(description = "Resume previously suspended async enqueueing process for provided entity")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order")
    })
    public String resumeAsyncEnqueueing(String entityName) {
        String entity = StringUtils.trimToNull(entityName);
        InputValidationResult inputValidationResult = validateInputEntity(entity);
        if (!inputValidationResult.isValid()) {
            return inputValidationResult.getMessage();
        }

        indexingQueueManager.resumeAsyncEnqueueIndexAll(entity);
        return String.format("Async enqueueing process has been resumed for entity '%s'", entity);
    }

    @Authenticated
    @ManagedOperation(description = "Terminate async enqueueing process")
    public String terminateAsyncEnqueueing() {
        List<IndexOperationResult<IndexManipulationResult>> results =
                indexingQueueManager.terminateAsyncEnqueueIndexAll();
        return formatResults("Terminate async enqueueing", results);
    }

    @Authenticated
    @ManagedOperation(description = "Terminate async enqueueing process for provided entity")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order")
    })
    public String terminateAsyncEnqueueing(String entityName) {
        String entity = StringUtils.trimToNull(entityName);
        InputValidationResult inputValidationResult = validateInputEntity(entity);
        if (!inputValidationResult.isValid()) {
            return inputValidationResult.getMessage();
        }

        indexingQueueManager.terminateAsyncEnqueueIndexAll(entity);
        return String.format("Async enqueueing process has been stopped for entity '%s'", entity);
    }

    @Authenticated
    @ManagedOperation(description = "Async enqueue next batch")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order")
    })
    public String enqueueNextBatch(String entityName) {
        String entity = StringUtils.trimToNull(entityName);
        InputValidationResult inputValidationResult = validateInputEntity(entity);
        if (!inputValidationResult.isValid()) {
            return inputValidationResult.getMessage();
        }

        int processed = indexingQueueManager.processEnqueueingSession(entity);
        return String.format("Enqueued %d instances of entity '%s'", processed, entity);
    }

    @Authenticated
    @ManagedOperation(description = "Async enqueue next batch of next available session")
    public String enqueueNextBatch() {
        int processed = indexingQueueManager.processNextEnqueueingSession();
        return String.format("Enqueued %d instances", processed);
    }

    @Authenticated
    @ManagedOperation(description = "Index specific entity instance by its id")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order"),
            @ManagedOperationParameter(name = "id", description = "Id of target entity instance")
    })
    public String indexEntityInstance(String entityName, String id) {
        String serializedId = StringUtils.trimToNull(entityName) + "." + StringUtils.trimToNull(id);
        Id<?> entityId = idSerialization.stringToId(serializedId);
        IndexResult indexResult = entityIndexer.indexByEntityId(entityId);

        String message;
        if (indexResult.getTotalSize() == 0) {
            message = "Entity instance not found or can't be processed";
        } else if (indexResult.hasFailures()) {
            IndexResult.Failure failure = indexResult.getFailures().iterator().next();
            String errorMessage = failure.getCause();
            message = String.format("Failed to index instance of entity '%s' with id '%s': %s",
                    entityName, serializedId, errorMessage);
        } else {
            message = "Entity instance was successfully indexed";
        }
        return message;
    }

    @Authenticated
    @ManagedOperation(description = "Enqueue indexing of specific entity instance by its id")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order"),
            @ManagedOperationParameter(name = "id", description = "Id of target entity instance")
    })
    public String enqueueIndexEntityInstance(String entityName, String id) {
        String serializedId = StringUtils.trimToNull(entityName) + "." + StringUtils.trimToNull(id);
        Id<?> entityId = idSerialization.stringToId(serializedId);
        int enqueued = indexingQueueManager.enqueueIndexByEntityId(entityId);

        String message;
        if (enqueued == 0) {
            message = "Entity instance was not enqueued for indexing";
        } else {
            message = "Entity instance was successfully enqueued for indexing";
        }
        return message;
    }

    @Authenticated
    @ManagedOperation(description = "Delete index document related to specific entity instance by its id")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order"),
            @ManagedOperationParameter(name = "id", description = "Idd of target entity instance")
    })
    public String deleteEntityInstanceFromIndex(String entityName, String id) {
        String serializedId = StringUtils.trimToNull(entityName) + "." + StringUtils.trimToNull(id);
        Id<?> entityId = idSerialization.stringToId(serializedId);
        IndexResult indexResult = entityIndexer.deleteByEntityId(entityId);

        String message;
        if (indexResult.getTotalSize() == 0) {
            message = "Entity instance not found or can't be processed";
        } else if (indexResult.hasFailures()) {
            IndexResult.Failure failure = indexResult.getFailures().iterator().next();
            String errorMessage = failure.getCause();
            message = String.format("Failed to delete document related to instance of entity '%s' with id '%s': %s",
                    entityName, serializedId, errorMessage);
        } else {
            message = "Index document was successfully deleted";
        }
        return message;
    }

    @Authenticated
    @ManagedOperation(description = "Enqueue deletion of index document related to specific entity instance by its id")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order"),
            @ManagedOperationParameter(name = "id", description = "Id of target entity instance")
    })
    public String enqueueDeleteEntityInstanceFromIndex(String entityName, String id) {
        String serializedId = StringUtils.trimToNull(entityName) + "." + StringUtils.trimToNull(id);
        Id<?> entityId = idSerialization.stringToId(serializedId);
        int enqueued = indexingQueueManager.enqueueDeleteByEntityId(entityId);

        String message;
        if (enqueued == 0) {
            message = "Entity instance was not enqueued for deletion";
        } else {
            message = "Entity instance was successfully enqueued for deletion";
        }
        return message;
    }

    @Authenticated
    @ManagedOperation(description = "Init async enqueueing process. Leave a field empty to cover everything: no entity means every "
                                    + "indexed entity, no tenant means every tenant")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order; empty for all"),
            @ManagedOperationParameter(name = "tenantId", description = "Tenant id; empty for all")
    })
    public String initAsyncEnqueueing(String entityName, String tenantId) {
        return withScope(entityName, tenantId, true,
                scope -> formatResults("Init async enqueueing", indexingQueueManager.initAsyncEnqueueIndexAll(scope.entityName(), scope.tenantId())));
    }

    @Authenticated
    @ManagedOperation(description = "Suspend async enqueueing process. Leave a field empty to cover everything: no entity means every "
                                    + "indexed entity, no tenant means every tenant")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order; empty for all"),
            @ManagedOperationParameter(name = "tenantId", description = "Tenant id; empty for all")
    })
    public String suspendAsyncEnqueueing(String entityName, String tenantId) {
        return withScope(entityName, tenantId, false,
                scope -> formatResults("Suspend async enqueueing", indexingQueueManager.suspendAsyncEnqueueIndexAll(scope.entityName(), scope.tenantId())));
    }

    @Authenticated
    @ManagedOperation(description = "Resume async enqueueing process. Leave a field empty to cover everything: no entity means every "
                                    + "indexed entity, no tenant means every tenant")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order; empty for all"),
            @ManagedOperationParameter(name = "tenantId", description = "Tenant id; empty for all")
    })
    public String resumeAsyncEnqueueing(String entityName, String tenantId) {
        return withScope(entityName, tenantId, true,
                scope -> formatResults("Resume async enqueueing", indexingQueueManager.resumeAsyncEnqueueIndexAll(scope.entityName(), scope.tenantId())));
    }

    @Authenticated
    @ManagedOperation(description = "Terminate async enqueueing process. Leave a field empty to cover everything: no entity means every "
                                    + "indexed entity, no tenant means every tenant")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order; empty for all"),
            @ManagedOperationParameter(name = "tenantId", description = "Tenant id; empty for all")
    })
    public String terminateAsyncEnqueueing(String entityName, String tenantId) {
        return withScope(entityName, tenantId, false,
                scope -> formatResults("Terminate async enqueueing", indexingQueueManager.terminateAsyncEnqueueIndexAll(scope.entityName(), scope.tenantId())));
    }

    @Authenticated
    @ManagedOperation(description = "Async enqueue next batch. Leave a field empty to cover everything: no entity "
                                    + "means the next available session, no tenant means every tenant")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order; empty for the next available session"),
            @ManagedOperationParameter(name = "tenantId", description = "Tenant id; empty for all")
    })
    public String enqueueNextBatch(String entityName, String tenantId) {
        return withScope(entityName, tenantId, true, scope -> {
            int processed = scope.entityName() == null
                    ? indexingQueueManager.processNextEnqueueingSession(scope.tenantId())
                    : indexingQueueManager.processEnqueueingSession(scope.entityName(), scope.tenantId());
            // Worded as the operations of the same name are: an administrator moving from the one-field form to
            // this one is doing the same thing and should read the same sentence.
            return String.format("Enqueued %d instances", processed);
        });
    }

    @Authenticated
    @ManagedOperation(description = "Entities to be asynchronously enqueued. Leave the field empty for every tenant")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "tenantId", description = "Tenant id; empty for all")
    })
    public List<String> getEntityNamesOfAsyncEnqueueingSessions(String tenantId) {
        String tenant = StringUtils.trimToNull(tenantId);
        return tenant == null
                ? indexingQueueManager.getEntityNamesOfEnqueueingSessions()
                : indexingQueueManager.getEntityNamesOfEnqueueingSessions(tenant);
    }

    @Authenticated
    @ManagedOperation(description = "Validates index schemas. Leave a field empty to cover everything: no entity "
                                    + "means every indexed entity, no tenant means every tenant")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order; empty for all"),
            @ManagedOperationParameter(name = "tenantId", description = "Tenant id; empty for all")
    })
    public String validateIndexes(String entityName, String tenantId) {
        return withScope(entityName, tenantId, false,
                scope -> formatResults("Validation", indexManager.validateIndexes(scope.configurations(), scope.tenantId())));
    }

    @Authenticated
    @ManagedOperation(description = "Synchronizes index schemas. Leave a field empty to cover everything. "
                                    + "This may cause deletion of indexes with all their data - depends on schema management strategy")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order; empty for all"),
            @ManagedOperationParameter(name = "tenantId", description = "Tenant id; empty for all")
    })
    public String synchronizeIndexSchemas(String entityName, String tenantId) {
        return withScope(entityName, tenantId, true,
                scope -> formatResults("Synchronization", indexManager.synchronizeIndexSchemas(scope.configurations(), scope.tenantId())));
    }

    @Authenticated
    @ManagedOperation(description = "Drops and creates indexes. Leave a field empty to cover everything. All data of "
                                    + "the indexes it reaches will be lost")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order; empty for all"),
            @ManagedOperationParameter(name = "tenantId", description = "Tenant id; empty for all")
    })
    public String recreateIndexes(String entityName, String tenantId) {
        return withScope(entityName, tenantId, true,
                scope -> formatResults("Recreation", indexManager.recreateIndexes(scope.configurations(), scope.tenantId())));
    }

    @Authenticated
    @ManagedOperation(description = "Deletes indexes. Leave a field empty to cover everything. All data of the indexes "
                                    + "it reaches will be lost, and the items the indexing queue held for them are discarded")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order; empty for all"),
            @ManagedOperationParameter(name = "tenantId", description = "Tenant id; empty for all")
    })
    public String deleteIndexes(String entityName, String tenantId) {
        return withScope(entityName, tenantId, false,
                scope -> formatResults("Deletion", indexManager.deleteIndexes(scope.configurations(), scope.tenantId())));
    }

    @Authenticated
    @ManagedOperation(description = "Synchronously enqueues instances for indexing. Leave a field empty to cover "
                                    + "everything. Don't use it on a huge amount of data")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order; empty for all"),
            @ManagedOperationParameter(name = "tenantId", description = "Tenant id; empty for all")
    })
    public String enqueueIndexAll(String entityName, String tenantId) {
        return withScope(entityName, tenantId, true, scope -> {
            int amount = indexingQueueManager.enqueueIndexAll(scope.entityName(), scope.tenantId());
            return String.format("%d instances have been enqueued", amount);
        });
    }

    @Authenticated
    @ManagedOperation(description = "Removes items from Indexing Queue. Leave a field empty to cover everything")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order; empty for all"),
            @ManagedOperationParameter(name = "tenantId", description = "Tenant id; empty for all")
    })
    public String emptyIndexingQueue(String entityName, String tenantId) {
        return withScope(entityName, tenantId, false, scope -> {
            int deleted = indexingQueueManager.emptyQueue(scope.entityName(), scope.tenantId());
            return String.format("%d items have been removed from Indexing Queue", deleted);
        });
    }

    @Authenticated
    @ManagedOperation(description = "Validates schemas of all search indexes defined in application.")
    public String validateIndexes() {
        return formatResults("Validation", indexManager.validateIndexes());
    }

    @Authenticated
    @ManagedOperation(description = "Validates schema of search index related to provided entity.")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order")
    })
    public String validateIndex(String entityName) {
        String entity = StringUtils.trimToNull(entityName);
        InputValidationResult inputValidationResult = validateInputEntity(entity);
        if (!inputValidationResult.isValid()) {
            return inputValidationResult.getMessage();
        }

        IndexConfiguration indexConfiguration = indexConfigurationManager.getIndexConfigurationByEntityName(entity);
        return formatResults("Validation", indexManager.validateIndexes(List.of(indexConfiguration), null));
    }

    @Authenticated
    @ManagedOperation(description = "Synchronizes schemas of all search indexes defined in application. " +
                                    "This may cause deletion of indexes with all their data - depends on schema management strategy")
    public String synchronizeIndexSchemas() {
        return formatResults("Synchronization", indexManager.synchronizeIndexSchemas());
    }

    @Authenticated
    @ManagedOperation(description = "Synchronizes schema of index related to provided entity. " +
                                    "This may cause deletion of this index with all data - depends on schema management strategy")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order")
    })
    public String synchronizeIndexSchema(String entityName) {
        String entity = StringUtils.trimToNull(entityName);
        InputValidationResult inputValidationResult = validateInputEntity(entity);
        if (!inputValidationResult.isValid()) {
            return inputValidationResult.getMessage();
        }

        IndexConfiguration indexConfiguration = indexConfigurationManager.getIndexConfigurationByEntityName(entity);
        return formatResults("Synchronization", indexManager.synchronizeIndexSchemas(List.of(indexConfiguration), null));
    }

    @Authenticated
    @ManagedOperation(description = "Drops and creates all search indexes defined in application. All data will be lost.")
    public String recreateIndexes() {
        return formatResults("Recreation", indexManager.recreateIndexes());
    }

    @Authenticated
    @ManagedOperation(description = "Drops and creates index related to provided entity. All data will be lost.")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order")
    })
    public String recreateIndex(String entityName) {
        String entity = StringUtils.trimToNull(entityName);
        InputValidationResult inputValidationResult = validateInputEntity(entity);
        if (!inputValidationResult.isValid()) {
            return inputValidationResult.getMessage();
        }

        IndexConfiguration indexConfiguration = indexConfigurationManager.getIndexConfigurationByEntityName(entity);
        return formatResults("Recreation", indexManager.recreateIndexes(List.of(indexConfiguration), null));
    }

    @Authenticated
    @ManagedOperation(description = "Processes all items in Indexing Queue")
    public String processEntireIndexingQueue() {
        int processed = indexingQueueManager.processEntireQueue();
        return String.format("Processed %d queue items", processed);
    }

    @Authenticated
    @ManagedOperation(description = "Processes next batch of items in Indexing Queue")
    public String processIndexingQueueNextBatch() {
        int processed = indexingQueueManager.processNextBatch();
        return String.format("Processed %d queue items", processed);
    }

    @Authenticated
    @ManagedOperation(description = "Removes all items from Indexing Queue")
    public String emptyIndexingQueue() {
        int deleted = indexingQueueManager.emptyQueue();
        return String.format("%d items have been removed from Indexing Queue", deleted);
    }

    @Authenticated
    @ManagedOperation(description = "Removes all items related to provided entity from Indexing Queue")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order")
    })
    public String emptyIndexingQueue(String entityName) {
        String entity = StringUtils.trimToNull(entityName);
        InputValidationResult inputValidationResult = validateInputEntity(entity);
        if (!inputValidationResult.isValid()) {
            return inputValidationResult.getMessage();
        }

        int deleted = indexingQueueManager.emptyQueue(entity);
        return String.format("%d items for entity '%s' have been removed from Indexing Queue", deleted, entity);
    }

    @Authenticated
    @ManagedOperation(description = "Recalculates all index configurations, including the dynamic attributes analysis, and synchronizes index schemas." +
                                    "Synchronizes schemas of all search indexes defined in application. " +
                                    "This may cause deletion of indexes with all their data - depends on schema management strategy")
    public String synchronizeIndexSchemasWithIndexDefinitionsRefresh() {
        indexConfigurationManager.refreshIndexDefinitions();
        return synchronizeIndexSchemas();
    }

    /**
     * Resolves what the two text fields of a scoped operation mean and runs the operation, or reports why it cannot
     * run. An empty field is "everything": no entity means every indexed entity, no tenant means every tenant.
     *
     * @param mustExist whether the operation creates or schedules something, and so needs a tenant that exists.
     *                  Operations that clean up or report take a tenant that is already gone: removing what a
     *                  deleted tenant left behind is one of their jobs
     */
    protected String withScope(@Nullable String entityName, @Nullable String tenantId, boolean mustExist,
                               Function<Scope, String> operation) {
        String entity = StringUtils.trimToNull(entityName);
        String tenant = StringUtils.trimToNull(tenantId);

        if (entity != null) {
            InputValidationResult entityValidation = validateInputEntity(entity);
            if (!entityValidation.isValid()) {
                return entityValidation.getMessage();
            }
        }
        if (tenant != null && mustExist && !multitenancyAdapter.getAvailableTenants().contains(tenant)) {
            return String.format("Tenant '%s' does not exist", tenant);
        }

        Collection<IndexConfiguration> configurations = entity == null
                ? indexConfigurationManager.getAllIndexConfigurations()
                : List.of(indexConfigurationManager.getIndexConfigurationByEntityName(entity));
        return operation.apply(new Scope(entity, tenant, configurations));
    }

    /**
     * The scope of a management operation: the entities it covers and the tenant it is limited to, both already
     * resolved from what was typed into the console.
     */
    protected record Scope(@Nullable String entityName,
                           @Nullable String tenantId,
                           Collection<IndexConfiguration> configurations) {
    }

    protected static <RT extends AtomicIndexOperationResult> String formatResults(
            String operationName,
            List<IndexOperationResult<RT>> results) {
        if (!results.iterator().hasNext()) {
            return operationName + " result: no indexes to work with";
        }
        return results.stream()
                .map(r -> String.format("Entity=%s, Index=%s, Tenant=%s, Status=%s",
                        r.entityName(),
                        r.indexName(),
                        r.tenantId() == null ? "-" : r.tenantId(),
                        r.result()))
                .collect(Collectors.joining(System.lineSeparator() + "\t",
                        operationName + " result:" + System.lineSeparator() + "\t", ""));
    }

    protected InputValidationResult validateInputEntity(String entityName) {
        if (StringUtils.isBlank(entityName)) {
            return InputValidationResult.failWithMessage("Entity name is not specified");
        }

        if (!indexConfigurationManager.isDirectlyIndexed(entityName)) {
            return InputValidationResult.failWithMessage(String.format("Entity '%s' is not configured for indexing", entityName));
        }

        return InputValidationResult.pass();
    }

    private static class InputValidationResult {
        private final boolean valid;
        private final String message;

        private InputValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        private boolean isValid() {
            return valid;
        }

        private String getMessage() {
            return message;
        }

        private static InputValidationResult failWithMessage(String message) {
            return new InputValidationResult(false, message);
        }

        private static InputValidationResult pass() {
            return new InputValidationResult(true, "");
        }
    }
}
