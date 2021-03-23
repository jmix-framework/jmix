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

package io.jmix.search.impl;

import io.jmix.core.Metadata;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.search.IndexProcessManager;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.impl.IndexManagementServiceImpl;
import io.jmix.search.index.mapping.IndexConfigurationProvider;
import io.jmix.search.index.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class IndexProcessManagerImpl implements IndexProcessManager {

    private static final Logger log = LoggerFactory.getLogger(IndexProcessManagerImpl.class);

    @Autowired
    protected IndexManagementServiceImpl indexManagementServiceImpl;
    @Autowired
    protected QueueService queueService;
    @Autowired
    protected IndexConfigurationProvider indexDefinitionsProvider;
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
        Collection<IndexConfiguration> indexConfigurations = indexDefinitionsProvider.getIndexConfigurations();
        indexConfigurations.forEach(indexDefinition -> reindexEntity(indexDefinition.getEntityName()));
    }

    @Override
    public void reindexEntity(@Nullable String entityName) {
        Preconditions.checkNotNullArgument(entityName);

        log.info("Reindex entity '{}'", entityName);
        IndexConfiguration indexConfiguration = indexDefinitionsProvider.getIndexDefinitionByEntityName(entityName);
        if (indexConfiguration == null) {
            throw new RuntimeException("Index definition not found for entity '" + entityName + "'");
        }

        boolean reindexLocked = reindexLock.tryLock();
        if (!reindexLocked) {
            log.info("Unable to perform reindex entity '{}': reindex is currently in progress", entityName);
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
                    boolean recreated = indexManagementServiceImpl.recreateIndex(indexConfiguration);
                    if (!recreated) {
                        throw new RuntimeException("Failed to recreate index '" + indexConfiguration.getIndexName() + "'");
                    }
                    writeLock.unlock();
                    writeLocked = false;
                    queueService.enqueueAll(entityName, searchProperties.getReindexEntityEnqueueBatchSize());
                } catch (IOException e) {
                    throw new RuntimeException("Unable to recreate index '" + indexConfiguration.getIndexName() + "'", e);
                } finally {
                    authenticator.end();
                    if (writeLocked) {
                        writeLock.unlock();
                    }
                }
            } else {
                log.info("Unable to start reindex entity '{}': indexing process in progress", entityName);
            }
        } finally {
            reindexLock.unlock();
        }
    }

    @Override
    public void scheduleReindexAll() {
        indexDefinitionsProvider.getIndexConfigurations().stream()
                .map(IndexConfiguration::getEntityName)
                .forEach(this::scheduleReindexEntity);
    }

    @Override
    public void scheduleReindexEntity(@Nullable String entityName) {
        Preconditions.checkNotNullArgument(entityName);

        MetaClass metaClass = metadata.getClass(entityName);
        Preconditions.checkNotNullArgument(indexDefinitionsProvider.getIndexDefinitionByEntityName(metaClass.getName()),
                "Entity '%s' doesn't have index configuration", entityName);
        if (!reindexEntitiesQueue.contains(entityName)) {
            log.info("Schedule reindexing of entity '{}'", entityName);
            reindexEntitiesQueue.add(entityName);
        }
    }

    @Override
    public void processNextReindexingEntity() {
        String entityName = reindexEntitiesQueue.peek();
        if (entityName != null) {
            reindexEntity(entityName);
            reindexEntitiesQueue.remove();
        }
    }

    @Override
    public void processNextReindexingBatch() {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public int processQueue() {
        //todo check global conditions to proceed (ES availability, etc)

        log.debug("Start processing queue");
        int count = 0;
        boolean locked = writeLock.tryLock();
        if (!locked) {
            log.debug("Unable to process queue: queue is being processed at the moment");
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

        log.debug("{} queue items have been successfully processed", count);
        return count;
    }
}
