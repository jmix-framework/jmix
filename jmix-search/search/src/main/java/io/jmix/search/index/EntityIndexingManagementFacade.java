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
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.index.queue.IndexingQueueManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

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
    protected ESIndexManager esIndexManager;
    @Autowired
    protected IndexConfigurationManager indexConfigurationManager;
    @Autowired
    protected SearchProperties searchProperties;

    @ManagedAttribute(description = "Strategy of index synchronization")
    public String getIndexSchemaManagementStrategy() {
        return searchProperties.getIndexSchemaManagementStrategy().toString();
    }

    @ManagedAttribute(description = "List of entities to be asynchronously enqueued")
    public List<String> getEntityNamesOfAsyncEnqueueingSessions() {
        return indexingQueueManager.getEntityNamesOfEnqueueingSessions();
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
        InputValidationResult inputValidationResult = validateInputEntity(entityName);
        if (!inputValidationResult.isValid()) {
            return inputValidationResult.getMessage();
        }

        int amount = indexingQueueManager.enqueueIndexAll(entityName);
        return String.format("%d instances of entity '%s' have been enqueued", amount, entityName);
    }

    @Authenticated
    @ManagedOperation(description = "Init async enqueueing process for all indexed entities")
    public String initAsyncEnqueueing() {
        indexingQueueManager.initAsyncEnqueueIndexAll();
        return "Async enqueueing process has been initialized for all indexed entities";
    }

    @Authenticated
    @ManagedOperation(description = "Init async enqueueing process for provided entity")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order")
    })
    public String initAsyncEnqueueing(String entityName) {
        InputValidationResult inputValidationResult = validateInputEntity(entityName);
        if (!inputValidationResult.isValid()) {
            return inputValidationResult.getMessage();
        }

        indexingQueueManager.initAsyncEnqueueIndexAll(entityName);
        return String.format("Async enqueueing process has been initialized for entity '%s'", entityName);
    }

    @Authenticated
    @ManagedOperation(description = "Suspend async enqueueing process")
    public String suspendAsyncEnqueueing() {
        indexingQueueManager.suspendAsyncEnqueueIndexAll();
        return "All async enqueueing processed has been suspended";
    }

    @Authenticated
    @ManagedOperation(description = "Suspend async enqueueing process for provided entity")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order")
    })
    public String suspendAsyncEnqueueing(String entityName) {
        InputValidationResult inputValidationResult = validateInputEntity(entityName);
        if (!inputValidationResult.isValid()) {
            return inputValidationResult.getMessage();
        }

        indexingQueueManager.suspendAsyncEnqueueIndexAll(entityName);
        return String.format("Async enqueueing process has been suspended for entity '%s'", entityName);
    }

    @Authenticated
    @ManagedOperation(description = "Resume all previously suspended async enqueueing processes")
    public String resumeAsyncEnqueueing() {
        indexingQueueManager.resumeAsyncEnqueueIndexAll();
        return "All async enqueueing processed has been resumed";
    }

    @Authenticated
    @ManagedOperation(description = "Resume previously suspended async enqueueing process for provided entity")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order")
    })
    public String resumeAsyncEnqueueing(String entityName) {
        InputValidationResult inputValidationResult = validateInputEntity(entityName);
        if (!inputValidationResult.isValid()) {
            return inputValidationResult.getMessage();
        }

        indexingQueueManager.resumeAsyncEnqueueIndexAll(entityName);
        return String.format("Async enqueueing process has been resumed for entity '%s'", entityName);
    }

    @Authenticated
    @ManagedOperation(description = "Terminate async enqueueing process")
    public String terminateAsyncEnqueueing() {
        indexingQueueManager.terminateAsyncEnqueueIndexAll();
        return "All async enqueueing processed has been terminated";
    }

    @Authenticated
    @ManagedOperation(description = "Terminate async enqueueing process for provided entity")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order")
    })
    public String terminateAsyncEnqueueing(String entityName) {
        InputValidationResult inputValidationResult = validateInputEntity(entityName);
        if (!inputValidationResult.isValid()) {
            return inputValidationResult.getMessage();
        }

        indexingQueueManager.terminateAsyncEnqueueIndexAll(entityName);
        return String.format("Async enqueueing process has been stopped for entity '%s'", entityName);
    }

    @Authenticated
    @ManagedOperation(description = "Async enqueue next batch")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order")
    })
    public String enqueueNextBatch(String entityName) {
        InputValidationResult inputValidationResult = validateInputEntity(entityName);
        if (!inputValidationResult.isValid()) {
            return inputValidationResult.getMessage();
        }

        int processed = indexingQueueManager.processEnqueueingSession(entityName);
        return String.format("Enqueued %d instances of entity '%s'", processed, entityName);
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
        String serializedId = entityName + "." + id;
        Id<?> entityId = idSerialization.stringToId(serializedId);
        IndexResult indexResult = entityIndexer.indexByEntityId(entityId);

        String message;
        if (indexResult.getTotalSize() == 0) {
            message = "Entity instance not found or can't be processed";
        } else if (indexResult.hasFailures()) {
            IndexResult.Failure failure = indexResult.getFailures().iterator().next();
            String errorMessage = failure.getCause().getLocalizedMessage();
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
        String serializedId = entityName + "." + id;
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
        String serializedId = entityName + "." + id;
        Id<?> entityId = idSerialization.stringToId(serializedId);
        IndexResult indexResult = entityIndexer.deleteByEntityId(entityId);

        String message;
        if (indexResult.getTotalSize() == 0) {
            message = "Entity instance not found or can't be processed";
        } else if (indexResult.hasFailures()) {
            IndexResult.Failure failure = indexResult.getFailures().iterator().next();
            String errorMessage = failure.getCause().getLocalizedMessage();
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
        String serializedId = entityName + "." + id;
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
    @ManagedOperation(description = "Validates schemas of all search indexes defined in application.")
    public String validateIndexes() {
        Map<IndexConfiguration, IndexValidationStatus> validationResult = esIndexManager.validateIndexes();
        StringBuilder sb = new StringBuilder("Validation result:");
        validationResult.forEach((config, status) -> sb.append(System.lineSeparator()).append("\t")
                .append(
                        formatSingleStatusString(
                                config.getEntityName(),
                                config.getIndexName(),
                                status.name()
                        )
                )
        );
        return sb.toString();
    }

    @Authenticated
    @ManagedOperation(description = "Validates schema of search index related to provided entity.")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order")
    })
    public String validateIndex(String entityName) {
        InputValidationResult inputValidationResult = validateInputEntity(entityName);
        if (!inputValidationResult.isValid()) {
            return inputValidationResult.getMessage();
        }

        IndexConfiguration indexConfiguration = indexConfigurationManager.getIndexConfigurationByEntityName(entityName);
        IndexValidationStatus status = esIndexManager.validateIndex(indexConfiguration);
        return "Validation result: " + formatSingleStatusString(
                indexConfiguration.getEntityName(),
                indexConfiguration.getIndexName(),
                status.name()
        );
    }

    @Authenticated
    @ManagedOperation(description = "Synchronizes schemas of all search indexes defined in application. " +
            "This may cause deletion of indexes with all their data - depends on schema management strategy")
    public String synchronizeIndexSchemas() {
        Map<IndexConfiguration, IndexSynchronizationStatus> synchronizationResult = esIndexManager.synchronizeIndexSchemas();
        StringBuilder sb = new StringBuilder("Synchronization result:");
        synchronizationResult.forEach((config, status) -> sb.append(System.lineSeparator()).append("\t")
                .append(
                        formatSingleStatusString(
                                config.getEntityName(),
                                config.getIndexName(),
                                status.name()
                        )
                )
        );
        return sb.toString();
    }

    @Authenticated
    @ManagedOperation(description = "Synchronizes schema of index related to provided entity. " +
            "This may cause deletion of this index with all data - depends on schema management strategy")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order")
    })
    public String synchronizeIndexSchema(String entityName) {
        InputValidationResult inputValidationResult = validateInputEntity(entityName);
        if (!inputValidationResult.isValid()) {
            return inputValidationResult.getMessage();
        }

        IndexConfiguration indexConfiguration = indexConfigurationManager.getIndexConfigurationByEntityName(entityName);
        IndexSynchronizationStatus status = esIndexManager.synchronizeIndexSchema(indexConfiguration);
        return "Synchronization result: " + formatSingleStatusString(
                indexConfiguration.getEntityName(),
                indexConfiguration.getIndexName(),
                status.name()
        );
    }

    @Authenticated
    @ManagedOperation(description = "Drops and creates all search indexes defined in application. All data will be lost.")
    public String recreateIndexes() {
        Map<IndexConfiguration, Boolean> recreationResult = esIndexManager.recreateIndexes();
        StringBuilder sb = new StringBuilder("Recreation result:");
        recreationResult.forEach((config, created) -> sb.append(System.lineSeparator()).append("\t")
                .append(
                        formatSingleStatusString(
                                config.getEntityName(),
                                config.getIndexName(),
                                created ? "SUCCESS" : "FAILURE"
                        )
                )
        );
        return sb.toString();
    }

    @Authenticated
    @ManagedOperation(description = "Drops and creates index related to provided entity. All data will be lost.")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order")
    })
    public String recreateIndex(String entityName) {
        InputValidationResult inputValidationResult = validateInputEntity(entityName);
        if (!inputValidationResult.isValid()) {
            return inputValidationResult.getMessage();
        }

        IndexConfiguration indexConfiguration = indexConfigurationManager.getIndexConfigurationByEntityName(entityName);
        boolean created = esIndexManager.recreateIndex(indexConfiguration);
        return "Recreation result: " + formatSingleStatusString(
                indexConfiguration.getEntityName(),
                indexConfiguration.getIndexName(),
                created ? "SUCCESS" : "FAILURE"
        );
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
        InputValidationResult inputValidationResult = validateInputEntity(entityName);
        if (!inputValidationResult.isValid()) {
            return inputValidationResult.getMessage();
        }

        int deleted = indexingQueueManager.emptyQueue(entityName);
        return String.format("%d items for entity '%s' have been removed from Indexing Queue", deleted, entityName);
    }

    protected String formatSingleStatusString(String entityName, String indexName, String status) {
        return String.format("Entity=%s, Index=%s, Status=%s", entityName, indexName, status);
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
