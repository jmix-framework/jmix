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

package io.jmix.search.index.impl;

import io.jmix.core.common.util.Preconditions;
import io.jmix.core.security.Authenticated;
import io.jmix.search.index.ESIndexManager;
import io.jmix.search.index.EntityReindexer;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.index.queue.IndexingQueueManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

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
    public String reindexEntity(String entityName) {
        Preconditions.checkNotEmptyString(entityName);
        int amount = entityReindexer.enqueueReindexAll(entityName);
        return String.format("%d instances of entity '%s' have been enqueued", amount, entityName);
    }

    @Authenticated
    @ManagedOperation(description = "Enqueue all instances of all indexed entities")
    public String enqueueAll() {
        int amount = indexingQueueManager.enqueueIndexAll();
        return String.format("%d instances of all indexed entities have been enqueued", amount);
    }

    @Authenticated
    @ManagedOperation(description = "Enqueue all instances of provided indexed entity")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order")
    })
    public String enqueueAll(String entityName) {
        Preconditions.checkNotEmptyString(entityName);
        int amount = indexingQueueManager.enqueueIndexAll(entityName);
        return String.format("%d instances of entity '%s' have been enqueued", amount, entityName);
    }

    @Authenticated
    @ManagedOperation(description = "Update all search indexes defined in application to the actual state")
    public String synchronizeIndexes() {
        esIndexManager.synchronizeIndexes();
        return "All indexes have been synchronized";
    }

    @Authenticated
    @ManagedOperation(description = "Update search index related to provided entity to the actual state")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Name of entity configured for indexing, e.g. demo_Order")
    })
    public String synchronizeIndex(String entityName) {
        Preconditions.checkNotEmptyString(entityName);
        Optional<IndexConfiguration> indexConfigurationOpt = indexConfigurationManager.getIndexConfigurationByEntityNameOpt(entityName);
        if (!indexConfigurationOpt.isPresent()) {
            return String.format("Entity '%s' is not configured for indexing", entityName);
        }

        esIndexManager.synchronizeIndex(indexConfigurationOpt.get());
        return String.format("Index for entity '%s' has been synchronized", entityName);
    }
}
