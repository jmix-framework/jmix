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

package io.jmix.search.index.impl;

import io.jmix.search.SearchProperties;
import io.jmix.search.index.ESIndexManager;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.IndexSynchronizationStatus;
import io.jmix.search.index.queue.IndexingQueueManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Synchronizes search indexes on application startup.
 */
@Component("search_StartupIndexSynchronizer")
public class StartupIndexSynchronizer {

    private static final Logger log = LoggerFactory.getLogger(StartupIndexSynchronizer.class);

    @Autowired
    protected ESIndexManager esIndexManager;
    @Autowired
    protected IndexingQueueManager indexingQueueManager;
    @Autowired
    protected SearchProperties searchProperties;
    @Autowired
    protected IndexStateRegistry indexStateRegistry;

    protected final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @PostConstruct
    protected void postConstruct() {
        try {
            log.info("Start initial index synchronization");
            Map<IndexConfiguration, IndexSynchronizationStatus> indexSynchronizationResults = esIndexManager.synchronizeIndexSchemas();

            List<IndexConfiguration> enqueueAllCandidates = new ArrayList<>();
            List<IndexConfiguration> available = new ArrayList<>();
            List<IndexConfiguration> unavailable = new ArrayList<>();
            indexSynchronizationResults.forEach((config, status) -> {
                log.info("Synchronization Result: Entity={}, Index={}, Status={}",
                        config.getEntityName(),
                        config.getIndexName(),
                        status);
                switch (status) {
                    case CREATED:
                    case RECREATED:
                        enqueueAllCandidates.add(config);
                        available.add(config);
                        break;
                    case IRRELEVANT:
                    case MISSING:
                        unavailable.add(config);
                        break;
                    default:
                        available.add(config);
                }
            });

            available.forEach(config -> indexStateRegistry.markIndexAsAvailable(config.getEntityName()));
            unavailable.forEach(config -> indexStateRegistry.markIndexAsUnavailable(config.getEntityName()));

            if (searchProperties.isEnqueueIndexAllOnStartupIndexRecreationEnabled()) {
                List<IndexConfiguration> indexConfigurationsToEnqueueAll = enqueueAllCandidates.stream()
                        .filter(config -> {
                            List<String> entitiesAllowedToEnqueue = searchProperties.getEnqueueIndexAllOnStartupIndexRecreationEntities();
                            return entitiesAllowedToEnqueue.isEmpty() || entitiesAllowedToEnqueue.contains(config.getEntityName());
                        })
                        .collect(Collectors.toList());
                executorService.submit(() -> indexConfigurationsToEnqueueAll.forEach(config -> enqueueEntity(config.getEntityName())));
            }

            log.info("Finish initial index synchronization");
        } catch (Exception e) {
            log.error("Failed to synchronize indexes", e);
        }
    }

    protected void enqueueEntity(String entityName) {
        log.info("Start initial enqueueing instances of entity '{}'", entityName);
        indexingQueueManager.enqueueIndexAll(entityName);
        log.info("Finish initial enqueueing instances of entity '{}'", entityName);
    }
}
