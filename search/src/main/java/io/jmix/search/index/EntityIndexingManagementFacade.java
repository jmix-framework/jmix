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

import io.jmix.core.common.util.Preconditions;
import io.jmix.core.security.Authenticated;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.index.queue.IndexingQueueManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@ManagedResource(description = "Manages entity indexing for full text search", objectName = "jmix.search:type=EntityIndexing")
@Component("search_EntityIndexingManagementFacade")
public class EntityIndexingManagementFacade {

    @Autowired
    protected EntityReindexer entityReindexer;
    @Autowired
    protected IndexingQueueManager indexingQueueManager;
    @Autowired
    protected ESIndexManager esIndexManager;
    @Autowired
    protected IndexConfigurationManager indexConfigurationManager;
    @Autowired
    protected SearchProperties searchProperties;

    @ManagedAttribute(description = "Defines the way of index synchronization")
    public String getIndexSchemaManagementStrategy() {
        return searchProperties.getIndexSchemaManagementStrategy().toString();
    }

    @Authenticated
    @ManagedOperation(description = "Recreate indexes and enqueue all instances of all indexed entities")
    public String reindexAll() {
        int amount = entityReindexer.enqueueReindexAll();
        return String.format("%d instances of all indexed entities have been enqueued", amount);
    }

    @Authenticated
    @ManagedOperation(description = "Recreate index and enqueue all instances of provided indexed entity")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order")
    })
    public String reindexAll(String entityName) {
        Preconditions.checkNotEmptyString(entityName);
        int amount = entityReindexer.enqueueReindexAll(entityName);
        return String.format("%d instances of entity '%s' have been enqueued", amount, entityName);
    }

    @Authenticated
    @ManagedOperation(description = "Enqueue all instances of all indexed entities")
    public String enqueueIndexAll() {
        int amount = indexingQueueManager.enqueueIndexAll();
        return String.format("%d instances of all indexed entities have been enqueued", amount);
    }

    @Authenticated
    @ManagedOperation(description = "Enqueue all instances of provided indexed entity")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order")
    })
    public String enqueueIndexAll(String entityName) {
        Preconditions.checkNotEmptyString(entityName);
        int amount = indexingQueueManager.enqueueIndexAll(entityName);
        return String.format("%d instances of entity '%s' have been enqueued", amount, entityName);
    }

    @Authenticated
    @ManagedOperation(description = "Validates schemas of all search indexes defined in application.")
    public String validateIndexes() {
        Collection<IndexValidationResult> indexValidationResults = esIndexManager.validateIndexes();
        StringBuilder sb = new StringBuilder("Validation result:");
        indexValidationResults.forEach(result -> sb.append(System.lineSeparator()).append("\t")
                .append(formatValidationResult(result))
        );
        return sb.toString();
    }

    @Authenticated
    @ManagedOperation(description = "Validates schema of search index related to provided entity.")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order")
    })
    public String validateIndex(String entityName) {
        Optional<IndexConfiguration> indexConfigurationOpt = indexConfigurationManager.getIndexConfigurationByEntityNameOpt(entityName);
        String result;
        if (indexConfigurationOpt.isPresent()) {
            IndexConfiguration indexConfiguration = indexConfigurationOpt.get();
            IndexValidationResult indexValidationResult = esIndexManager.validateIndex(indexConfiguration);
            result = "Validation result: " + formatValidationResult(indexValidationResult);
        } else {
            result = String.format("Entity '%s' is not indexed", entityName);
        }
        return result;
    }

    @Authenticated
    @ManagedOperation(description = "Synchronize schemas of all search indexes defined in application. " +
            "This may cause deletion of indexes with all their data - depends on schema management strategy")
    public String synchronizeIndexSchemas() {
        Collection<IndexSynchronizationResult> results = esIndexManager.synchronizeIndexSchemas();
        StringBuilder sb = new StringBuilder("Synchronization result:");
        results.forEach(result -> sb.append(System.lineSeparator()).append("\t")
                .append(formatSynchronizationResult(result)));
        return sb.toString();
    }

    @Authenticated
    @ManagedOperation(description = "Synchronize schema of index related to provided entity. " +
            "This may cause deletion of this index with all its data - depends on schema management strategy")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order")
    })
    public String synchronizeIndexSchema(String entityName) {
        Preconditions.checkNotEmptyString(entityName);
        Optional<IndexConfiguration> indexConfigurationOpt = indexConfigurationManager.getIndexConfigurationByEntityNameOpt(entityName);
        if (!indexConfigurationOpt.isPresent()) {
            return String.format("Entity '%s' is not configured for indexing", entityName);
        }

        IndexSynchronizationResult result = esIndexManager.synchronizeIndexSchema(indexConfigurationOpt.get());
        return String.format(
                "Synchronization result: Entity=%s Index=%s Status=%s",
                entityName, result.getIndexConfiguration().getIndexName(), result.getIndexSynchronizationStatus()
        );
    }

    protected String formatValidationResult(IndexValidationResult result) {
        return formatSingleStatusString(
                result.getIndexConfiguration().getEntityName(),
                result.getIndexConfiguration().getIndexName(),
                result.getIndexValidationStatus().name()
        );
    }

    protected String formatSynchronizationResult(IndexSynchronizationResult result) {
        return formatSingleStatusString(
                result.getIndexConfiguration().getEntityName(),
                result.getIndexConfiguration().getIndexName(),
                result.getIndexSynchronizationStatus().name()
        );
    }

    protected String formatSingleStatusString(String entityName, String indexName, String status) {
        return String.format("Entity=%s, Index=%s, Status=%s", entityName, indexName, status);
    }
}
