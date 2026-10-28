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

import io.jmix.core.JmixOrder;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.IndexManager;
import io.jmix.search.index.IndexOperationResult;
import io.jmix.search.index.IndexSynchronizationStatus;
import io.jmix.search.index.queue.IndexingQueueManager;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Synchronizes search indexes on application startup.
 * <p>
 * Runs at {@link JmixOrder#LOWEST_PRECEDENCE} so that add-ons contributing metadata have published it first: an index
 * mapping is built from the metadata, and an incomplete one makes the default {@code create-or-recreate} strategy drop
 * the index.
 */
@Component("search_StartupIndexSynchronizer")
public class StartupIndexSynchronizer {

    private static final Logger log = LoggerFactory.getLogger(StartupIndexSynchronizer.class);

    @Autowired
    protected IndexManager indexManager;
    @Autowired
    protected IndexingQueueManager indexingQueueManager;
    @Autowired
    protected SearchProperties searchProperties;
    @Autowired
    protected SystemAuthenticator authenticator;

    /**
     * Runs once the application context is up, not while the beans are being created.
     * <p>
     * Working out which indexes an entity has reads the tenants from the database, and the queue of a recreated
     * index is filled through the database as well. Both are only possible after the schema is there: a
     * {@code @PostConstruct} of a plain bean runs before Liquibase, and on an empty database the synchronization
     * failed as a whole, leaving the application running with no indexes at all and one line in the log.
     * <p>
     * The order is the one the framework gives its own listeners and says nothing about the cluster event
     * channel, which subscribes to the same event with no order of its own and therefore after this sweep. A
     * tenant announced by another node while the sweep is running is missed by this node until something reads
     * the tenants again - synchronizing the schemas from the console does it.
     */
    @EventListener(ApplicationStartedEvent.class)
    @Order(JmixOrder.LOWEST_PRECEDENCE)
    protected void synchronizeOnStartup() {
        authenticator.runWithSystem(this::synchronize);
    }

    /**
     * Runs as the system user: both the tenants an entity has indexes for and the queue of a recreated index are
     * read and written through {@code DataManager}, which applies security constraints, and a startup thread has no
     * authentication of its own.
     */
    protected void synchronize() {
        try {
            if (!searchProperties.isEnabled()) {
                log.info("Unable to start index synchronization: Search add-on is disabled");
                return;
            }

            log.info("Start initial index synchronization");
            List<IndexOperationResult<IndexSynchronizationStatus>> indexSynchronizationResults
                    = indexManager.synchronizeIndexSchemas();

            Set<EnqueueingTarget> enqueueAllCandidates = new LinkedHashSet<>();
            indexSynchronizationResults.forEach(result -> handleSynchronizationResult(result, enqueueAllCandidates));

            if (searchProperties.isEnqueueIndexAllOnStartupIndexRecreationEnabled()) {
                List<String> entitiesAllowedToEnqueue
                        = searchProperties.getEnqueueIndexAllOnStartupIndexRecreationEntities();
                enqueueAllCandidates.stream()
                        .filter(target -> entitiesAllowedToEnqueue.isEmpty()
                                || entitiesAllowedToEnqueue.contains(target.entityName()))
                        .forEach(this::initAsyncEnqueueIndexAll);
            }
            log.info("Finish initial index synchronization");
        } catch (Exception e) {
            log.error("Failed to synchronize indexes", e);
        }
    }

    /**
     * Availability is not recorded here: the index manager has already marked every index it touched, and a second
     * owner of the same registry is how the two start disagreeing. What is left is deciding what to reindex - an
     * index that has just been created or recreated holds nothing yet.
     */
    protected void handleSynchronizationResult(IndexOperationResult<IndexSynchronizationStatus> result,
                                               Set<EnqueueingTarget> enqueueAllCandidates) {
        log.info("Synchronization Result: Entity={}, Index={}, Status={}",
                result.entityName(),
                result.indexName(),
                result.result());
        switch (result.result()) {
            case CREATED:
            case RECREATED:
                enqueueAllCandidates.add(new EnqueueingTarget(result.entityName(), result.tenantId()));
                break;
            default:
        }
    }

    /**
     * Requests a reindex of the data that the recreated index holds.
     * <p>
     * An index of a tenant holds the data of that tenant only, so recreating it must not make the application reread
     * the data of all the other tenants.
     */
    protected void initAsyncEnqueueIndexAll(EnqueueingTarget target) {
        if (target.tenantId() == null) {
            indexingQueueManager.initAsyncEnqueueIndexAll(target.entityName());
        } else {
            indexingQueueManager.initAsyncEnqueueIndexAll(target.entityName(), target.tenantId());
        }
    }

    /**
     * Data to reread after its index has been created anew.
     *
     * @param tenantId tenant whose data is to be reread, or null if the index is not tenant-specific
     */
    protected record EnqueueingTarget(String entityName, @Nullable String tenantId) {
    }
}
