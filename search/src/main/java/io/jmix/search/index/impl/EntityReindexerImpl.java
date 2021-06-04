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

import io.jmix.core.Metadata;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.search.index.ESIndexManager;
import io.jmix.search.index.EntityReindexer;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.index.queue.IndexingQueueManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component("search_EntityReindexer")
public class EntityReindexerImpl implements EntityReindexer {

    private static final Logger log = LoggerFactory.getLogger(EntityReindexerImpl.class);

    @Autowired
    protected IndexConfigurationManager indexConfigurationManager;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected IndexingLocker locker;
    @Autowired
    protected SystemAuthenticator authenticator;
    @Autowired
    protected IndexingQueueManager indexingQueueManager;
    @Autowired
    protected ESIndexManager indexManager;

    @Override
    public int enqueueReindexAll() {
        return indexConfigurationManager.getAllIndexConfigurations().stream()
                .map(IndexConfiguration::getEntityName)
                .map(this::enqueueReindexAll)
                .reduce(Integer::sum)
                .orElse(0);
    }

    @Override
    public int enqueueReindexAll(String entityName) {
        Preconditions.checkNotNullArgument(entityName);

        log.info("Reindex entity '{}'", entityName);
        IndexConfiguration indexConfiguration = indexConfigurationManager.getIndexConfigurationByEntityName(entityName);

        boolean reindexLocked = locker.tryLockReindexing();
        if (!reindexLocked) {
            log.info("Unable to start reindex of entity '{}': reindex is currently in progress", entityName);
            return 0;
        }

        try {
            boolean queueProcessingLocked;
            try {
                queueProcessingLocked = locker.tryLockQueueProcessing(10, TimeUnit.SECONDS); //wait a little to let current indexing process finish. //todo property
            } catch (InterruptedException e) {
                log.info("Unable to start reindex of entity '{}': indexing queue is being processed", entityName);
                return 0;
            }

            if (queueProcessingLocked) {
                try {
                    authenticator.begin();
                    indexingQueueManager.emptyQueue(entityName);
                    boolean recreated = indexManager.recreateIndex(indexConfiguration);
                    if (!recreated) {
                        throw new RuntimeException("Failed to recreate index '" + indexConfiguration.getIndexName() + "'");
                    }
                    locker.unlockQueueProcessing();
                    queueProcessingLocked = false;
                    return indexingQueueManager.enqueueIndexAll(entityName);
                } finally {
                    authenticator.end();
                    if (queueProcessingLocked) {
                        locker.unlockQueueProcessing();
                    }
                }
            } else {
                log.info("Unable to start reindex entity '{}': indexing process in progress", entityName);
                return 0;
            }
        } finally {
            locker.unlockReindexing();
        }
    }
}
