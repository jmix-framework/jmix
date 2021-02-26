/*
 * Copyright 2020 Haulmont.
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

package io.jmix.search;

import io.jmix.core.Metadata;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.search.index.IndexDefinition;
import io.jmix.search.index.IndexManager;
import io.jmix.search.index.mapping.AnnotatedIndexDefinitionsProvider;
import io.jmix.search.index.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class SearchManagerImpl implements SearchManager {

    private static final Logger log = LoggerFactory.getLogger(SearchManagerImpl.class);

    @Autowired
    protected IndexManager indexManager;
    @Autowired
    protected QueueService queueService;
    @Autowired
    protected AnnotatedIndexDefinitionsProvider indexDefinitionsProvider;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected SystemAuthenticator authenticator;
    @Autowired
    protected SearchProperties searchProperties;

    protected final ReentrantLock writeLock = new ReentrantLock();
    protected final ReentrantLock reindexLock = new ReentrantLock();
    protected volatile Queue<String> reindexEntitiesQueue = new ConcurrentLinkedQueue<>();

    @Override
    public void reindexAll() {
        Collection<IndexDefinition> indexDefinitions = indexDefinitionsProvider.getIndexDefinitions();
        indexDefinitions.forEach(indexDefinition -> reindexEntity(indexDefinition.getEntityName()));
    }

    @Override
    public void reindexEntity(String entityName) {
        log.info("[IVGA] Reindex entity '{}'", entityName);
        IndexDefinition indexDefinition = indexDefinitionsProvider.getIndexDefinitionByEntityName(entityName);
        if(indexDefinition == null) {
            throw new RuntimeException("Index definition not found for entity '" + entityName + "'");
        }

        boolean reindexLocked = reindexLock.tryLock();
        if(!reindexLocked) {
            log.info("[IVGA] Unable to perform reindex entity '{}': reindex is currently in progress", entityName);
            return;
        }

        try {
            boolean writeLocked;
            try {
                writeLocked = writeLock.tryLock(10, TimeUnit.SECONDS); //wait a little to let current indexing process finish. //todo property
            } catch (InterruptedException e) {
                throw new RuntimeException("Failed during lock acquiring", e);
            }

            if (writeLocked) {
                try {
                    authenticator.begin();
                    queueService.emptyQueue(entityName);
                    boolean recreated = indexManager.recreateIndex(indexDefinition.getIndexName());
                    if (!recreated) {
                        throw new RuntimeException("Failed to recreate index '" + indexDefinition.getIndexName() + "'");
                    }
                    writeLock.unlock();
                    writeLocked = false;
                    queueService.enqueue(entityName, searchProperties.getReindexEntityEnqueueBatchSize());
                } catch (IOException e) {
                    throw new RuntimeException("Unable to recreate index '" + indexDefinition.getIndexName() + "'", e);
                } finally {
                    authenticator.end();
                    if(writeLocked) {
                        writeLock.unlock();
                    }
                }
            } else {
                log.info("[IVGA] Unable to start reindex entity '{}': indexing process in progress", entityName);
            }
        } finally {
            reindexLock.unlock();
        }
    }

    @Override
    public void asyncReindexAll() {
        indexDefinitionsProvider.getIndexDefinitions().stream()
                .map(IndexDefinition::getEntityName)
                .forEach(this::asyncReindexEntity);
    }

    @Override
    public void asyncReindexEntity(String entityName) {
        log.info("[IVGA] asyncReindexEntity: Current reindex queue: {}", reindexEntitiesQueue);
        MetaClass metaClass = metadata.getClass(entityName);
        Preconditions.checkNotNullArgument(indexDefinitionsProvider.getIndexDefinitionByEntityName(metaClass.getName()),
                "Entity '%s' doesn't have index configuration", entityName);
        if(!reindexEntitiesQueue.contains(entityName)) {
            log.info("[IVGA] Queue entity '{}' for reindex", entityName);
            reindexEntitiesQueue.add(entityName);
        }
    }

    @Override
    public void reindexNextEntity() {
        String entityName = reindexEntitiesQueue.peek();
        if(entityName != null) {
            reindexEntity(entityName);
            reindexEntitiesQueue.remove();
        }
    }

    @Override
    public int processQueue() {
        //todo check global conditions to proceed (ES availability, etc)

        log.info("[IVGA] Start processing queue");
        int count = 0;
        boolean locked = writeLock.tryLock();
        if (!locked) {
            log.info("[IVGA] Unable to process queue: queue is being processed at the moment");
            return count;
        }

        try {
            authenticator.begin();
            count = queueService.processQueue(
                    searchProperties.getProcessQueueBatchSize(),
                    searchProperties.getMaxProcessedQueueItemsPerExecution()
            );
        } finally {
            writeLock.unlock();
            authenticator.end();
        }

        log.info("[IVGA] {} queue items have been successfully processed", count);
        return count;
    }
}
