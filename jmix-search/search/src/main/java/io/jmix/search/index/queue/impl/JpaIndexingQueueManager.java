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

package io.jmix.search.index.queue.impl;

import io.jmix.core.*;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.data.StoreAwareLocator;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.EntityDeletionTarget;
import io.jmix.search.index.EntityIndexer;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.IndexManipulationResult;
import io.jmix.search.index.IndexResult;
import io.jmix.search.index.IndexOperationResult;
import io.jmix.search.index.impl.IndexLayout;
import io.jmix.search.index.impl.IndexStateRegistry;
import io.jmix.search.index.impl.IndexingLocker;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.index.queue.EntityIdsLoader;
import io.jmix.search.index.queue.EntityIdsLoader.ResultHolder;
import io.jmix.search.index.queue.IndexingQueueManager;
import io.jmix.search.index.queue.entity.EnqueueingSession;
import io.jmix.search.index.queue.entity.IndexingQueueItem;
import io.jmix.search.index.impl.MultitenancyAdapter;
import org.apache.commons.collections4.MapUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.lang.String.format;

@NullMarked
public class JpaIndexingQueueManager implements IndexingQueueManager {

    private static final Logger log = LoggerFactory.getLogger(JpaIndexingQueueManager.class);

    protected final UnconstrainedDataManager dataManager;
    protected final Metadata metadata;
    protected final MetadataTools metadataTools;
    protected final EntityIndexer entityIndexer;
    protected final StoreAwareLocator storeAwareLocator;
    protected final IndexConfigurationManager indexConfigurationManager;
    protected final IdSerialization idSerialization;
    protected final SystemAuthenticator authenticator;
    protected final IndexingLocker locker;
    protected final SearchProperties searchProperties;
    protected final IndexStateRegistry indexStateRegistry;

    @Autowired
    protected IndexLayout indexLayout;
    protected final EnqueueingSessionManager enqueueingSessionManager;
    protected final EntityIdsLoaderProvider entityIdsLoaderProvider;
    protected final MultitenancyAdapter multitenancyAdapter;

    public JpaIndexingQueueManager(SearchProperties searchProperties,
                                   UnconstrainedDataManager dataManager,
                                   Metadata metadata,
                                   MetadataTools metadataTools,
                                   EntityIndexer entityIndexer,
                                   StoreAwareLocator storeAwareLocator,
                                   IndexConfigurationManager indexConfigurationManager,
                                   IdSerialization idSerialization,
                                   SystemAuthenticator authenticator,
                                   IndexingLocker locker,
                                   IndexStateRegistry indexStateRegistry,
                                   EnqueueingSessionManager enqueueingSessionManager,
                                   EntityIdsLoaderProvider entityIdsLoaderProvider,
                                   MultitenancyAdapter multitenancyAdapter) {
        this.searchProperties = searchProperties;
        this.dataManager = dataManager;
        this.metadata = metadata;
        this.metadataTools = metadataTools;
        this.entityIndexer = entityIndexer;
        this.storeAwareLocator = storeAwareLocator;
        this.indexConfigurationManager = indexConfigurationManager;
        this.idSerialization = idSerialization;
        this.authenticator = authenticator;
        this.locker = locker;
        this.indexStateRegistry = indexStateRegistry;
        this.enqueueingSessionManager = enqueueingSessionManager;
        this.entityIdsLoaderProvider = entityIdsLoaderProvider;
        this.multitenancyAdapter = multitenancyAdapter;
    }

    @Override
    public int emptyQueue() {
        TransactionTemplate transactionTemplate = storeAwareLocator.getTransactionTemplate(Stores.MAIN);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        Integer result = transactionTemplate.execute(status -> {
            log.debug("Empty Indexing queue");
            EntityManager entityManager = storeAwareLocator.getEntityManager(Stores.MAIN);
            Query query = entityManager.createQuery("delete from search_IndexingQueue q");
            int deleted = query.executeUpdate();
            log.debug("{} records have been deleted from queue", deleted);
            return deleted;
        });
        return result == null ? 0 : result;
    }

    @Override
    public int emptyQueue(@Nullable String entityName, @Nullable String tenantId) {
        TransactionTemplate transactionTemplate = storeAwareLocator.getTransactionTemplate(Stores.MAIN);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        Integer result = transactionTemplate.execute(status -> {
            log.debug("Empty queue for entity '{}' of tenant '{}'", entityName, tenantId);
            EntityManager entityManager = storeAwareLocator.getEntityManager(Stores.MAIN);

            StringBuilder queryString = new StringBuilder("delete from search_IndexingQueue q");
            List<String> conditions = new ArrayList<>();
            if (entityName != null) {
                conditions.add("q.entityName = :entityName");
            }
            if (tenantId != null) {
                conditions.add("q.tenantId = :tenantId");
            }
            if (!conditions.isEmpty()) {
                queryString.append(" where ").append(String.join(" and ", conditions));
            }

            Query query = entityManager.createQuery(queryString.toString());
            if (entityName != null) {
                query.setParameter("entityName", entityName);
            }
            if (tenantId != null) {
                query.setParameter("tenantId", tenantId);
            }
            int deleted = query.executeUpdate();
            log.debug("{} records have been deleted from queue", deleted);
            return deleted;
        });
        return result == null ? 0 : result;
    }

    @Override
    public int enqueueIndex(Object entityInstance) {
        Preconditions.checkNotNullArgument(entityInstance);
        return enqueueIndexCollection(Collections.singletonList(entityInstance));
    }

    @Override
    public int enqueueIndexCollection(Collection<Object> entityInstances) {
        Preconditions.checkNotNullArgument(entityInstances);
        return enqueue(entityInstances, IndexingOperation.INDEX);
    }

    @Override
    public int enqueueIndexByEntityId(Id<?> entityId) {
        Preconditions.checkNotNullArgument(entityId);
        return enqueueIndexCollectionByEntityIds(Collections.singletonList(entityId));
    }

    @Override
    public int enqueueIndexByEntityId(Id<?> entityId, @Nullable String tenantId) {
        Preconditions.checkNotNullArgument(entityId);
        if (tenantId == null) {
            return enqueueIndexByEntityId(entityId);
        }
        MetaClass metaClass = metadata.getClass(entityId.getEntityClass());
        if (indexConfigurationManager.getIndexConfigurationByEntityNameOpt(metaClass.getName()).isEmpty()) {
            return 0;
        }
        IndexingQueueItem item = createQueueItem(metaClass.getName(), idSerialization.idToString(entityId),
                IndexingOperation.INDEX, tenantId);
        return enqueue(Collections.singletonList(item));
    }

    @Override
    public int enqueueIndexCollectionByEntityIds(Collection<Id<?>> entityIds) {
        Preconditions.checkNotNullArgument(entityIds);
        return enqueueByIds(entityIds, IndexingOperation.INDEX);
    }

    @Override
    public int enqueueIndexAll() {
        return enqueueIndexAll(null, null);
    }

    @Override
    public int enqueueIndexAll(@Nullable String entityName, @Nullable String tenantId) {
        Collection<IndexConfiguration> configurations = entityName == null
                ? indexConfigurationManager.getAllIndexConfigurations()
                : List.of(indexConfigurationManager.getIndexConfigurationByEntityName(entityName));

        int batchSize = searchProperties.getReindexEntityEnqueueBatchSize();
        int enqueued = 0;
        for (IndexConfiguration configuration : configurations) {
            if (tenantId != null && !indexLayout.isSplitByTenants(configuration)) {
                // The data of this entity is shared by every tenant, so none of it belongs to the named one.
                log.debug("Entity '{}' is not stored per tenant: nothing of tenant '{}' to enqueue",
                        configuration.getEntityName(), tenantId);
                continue;
            }
            if (tenantId == null && indexLayout.isSplitByTenants(configuration)) {
                // One pass per tenant: the records of a tenant are what one index holds, and an item that
                // remembers no tenant cannot be told apart from an item addressed to an unavailable index.
                for (IndexLayout.TenantIndex index : indexLayout.allIndexes(configuration)) {
                    enqueued += enqueueIndexAll(configuration.getEntityName(), index.tenantId(), batchSize);
                }
                continue;
            }
            enqueued += enqueueIndexAll(configuration.getEntityName(), tenantId, batchSize);
        }
        return enqueued;
    }

    @Override
    public List<String> getEntityNamesOfEnqueueingSessions() {
        return enqueueingSessionManager.loadEntityNamesOfSessions();
    }

    @Override
    public List<String> getEntityNamesOfEnqueueingSessions(@Nullable String tenantId) {
        if (tenantId == null) {
            return getEntityNamesOfEnqueueingSessions();
        }
        checkMultitenancyModulePresent();
        return enqueueingSessionManager.loadEntityNamesOfSessions(tenantId);
    }

    @Override
    public List<IndexOperationResult<IndexManipulationResult>> initAsyncEnqueueIndexAll() {
        List<IndexOperationResult<IndexManipulationResult>> results = new ArrayList<>();
        indexConfigurationManager.getAllIndexConfigurations().stream()
                .map(IndexConfiguration::getEntityName)
                .forEach(entityName -> results.addAll(initAsyncEnqueueIndexAll(entityName)));
        return results;
    }

    @Override
    public List<IndexOperationResult<IndexManipulationResult>> initAsyncEnqueueIndexAll(String entityName) {
        return enqueueingSessionManager.initSession(entityName);
    }

    @Override
    public List<IndexOperationResult<IndexManipulationResult>> initAsyncEnqueueIndexAll(@Nullable String entityName, @Nullable String tenantId) {
        if (tenantId != null) {
            checkMultitenancyModulePresent();
        }
        if (entityName == null) {
            return everyEntityOfTenant(tenantId, entity -> initAsyncEnqueueIndexAll(entity, tenantId));
        }
        return tenantId == null
                ? initAsyncEnqueueIndexAll(entityName)
                : enqueueingSessionManager.initSession(entityName, tenantId);
    }

    @Override
    public List<IndexOperationResult<IndexManipulationResult>> suspendAsyncEnqueueIndexAll() {
        List<IndexOperationResult<IndexManipulationResult>> results = new ArrayList<>();
        indexConfigurationManager.getAllIndexConfigurations().stream()
                .map(IndexConfiguration::getEntityName)
                .forEach(entityName -> results.addAll(suspendAsyncEnqueueIndexAll(entityName)));
        return results;
    }

    @Override
    public List<IndexOperationResult<IndexManipulationResult>> suspendAsyncEnqueueIndexAll(String entityName) {
        return enqueueingSessionManager.suspendSession(entityName);
    }

    @Override
    public List<IndexOperationResult<IndexManipulationResult>> suspendAsyncEnqueueIndexAll(@Nullable String entityName, @Nullable String tenantId) {
        if (tenantId != null) {
            checkMultitenancyModulePresent();
        }
        if (entityName == null) {
            return everyEntityOfTenant(tenantId, entity -> suspendAsyncEnqueueIndexAll(entity, tenantId));
        }
        return tenantId == null
                ? suspendAsyncEnqueueIndexAll(entityName)
                : enqueueingSessionManager.suspendSession(entityName, tenantId);
    }

    @Override
    public List<IndexOperationResult<IndexManipulationResult>> resumeAsyncEnqueueIndexAll() {
        List<IndexOperationResult<IndexManipulationResult>> results = new ArrayList<>();
        indexConfigurationManager.getAllIndexConfigurations().stream()
                .map(IndexConfiguration::getEntityName)
                .forEach(entityName -> results.addAll(resumeAsyncEnqueueIndexAll(entityName)));
        return results;
    }

    @Override
    public List<IndexOperationResult<IndexManipulationResult>> resumeAsyncEnqueueIndexAll(String entityName) {
        return enqueueingSessionManager.resumeSession(entityName);
    }

    @Override
    public List<IndexOperationResult<IndexManipulationResult>> resumeAsyncEnqueueIndexAll(@Nullable String entityName, @Nullable String tenantId) {
        if (tenantId != null) {
            checkMultitenancyModulePresent();
        }
        if (entityName == null) {
            return everyEntityOfTenant(tenantId, entity -> resumeAsyncEnqueueIndexAll(entity, tenantId));
        }
        return tenantId == null
                ? resumeAsyncEnqueueIndexAll(entityName)
                : enqueueingSessionManager.resumeSession(entityName, tenantId);
    }

    @Override
    public List<IndexOperationResult<IndexManipulationResult>> terminateAsyncEnqueueIndexAll() {
        List<IndexOperationResult<IndexManipulationResult>> results = new ArrayList<>();
        indexConfigurationManager.getAllIndexConfigurations().stream()
                .map(IndexConfiguration::getEntityName)
                .forEach(entityName -> results.addAll(terminateAsyncEnqueueIndexAll(entityName)));
        return results;
    }

    @Override
    public List<IndexOperationResult<IndexManipulationResult>> terminateAsyncEnqueueIndexAll(String entityName) {
        return enqueueingSessionManager.removeSession(entityName);
    }

    @Override
    public List<IndexOperationResult<IndexManipulationResult>> terminateAsyncEnqueueIndexAll(@Nullable String entityName, @Nullable String tenantId) {
        if (tenantId != null) {
            checkMultitenancyModulePresent();
        }
        if (entityName == null) {
            return everyEntityOfTenant(tenantId, entity -> terminateAsyncEnqueueIndexAll(entity, tenantId));
        }
        return tenantId == null
                ? terminateAsyncEnqueueIndexAll(entityName)
                : enqueueingSessionManager.removeSession(entityName, tenantId);
    }

    @Override
    public int processNextEnqueueingSession() {
        return processNextEnqueueingSession(searchProperties.getReindexEntityEnqueueBatchSize());
    }

    @Override
    public int processNextEnqueueingSession(@Nullable String tenantId) {
        return processNextEnqueueingSession(tenantId, searchProperties.getReindexEntityEnqueueBatchSize());
    }

    @Override
    public int processNextEnqueueingSession(int batchSize) {
        return processSession(enqueueingSessionManager::getNextActiveSession, batchSize);
    }

    @Override
    public int processNextEnqueueingSession(@Nullable String tenantId, int batchSize) {
        if (tenantId == null) {
            return processNextEnqueueingSession(batchSize);
        }
        checkMultitenancyModulePresent();
        return processSession(() -> enqueueingSessionManager.getNextActiveSession(tenantId), batchSize);
    }

    /**
     * Advances one session by a batch, whichever session the caller points at.
     * <p>
     * Every caller supplies its own way of choosing the session - the next active one of any tenant, the next
     * active one of a named entity, or the session of a named entity and tenant. Choosing is the only thing that
     * differs between them; locking, authentication and the batch itself are the same.
     */
    protected int processSession(Supplier<EnqueueingSession> nextSession, int batchSize) {
        try {
            authenticator.begin();
            log.debug("Get next active enqueueing session");
            EnqueueingSession session = nextSession.get();
            if (session == null) {
                log.trace("Active enqueueing session not found");
                return 0;
            }

            if (!locker.tryLockEntityForEnqueueIndexAll(session.getEntityName(), session.getTenantId())) {
                log.info("Unable to process enqueueing session for entity '{}' of tenant '{}': currently in progress",
                        session.getEntityName(), session.getTenantId());
                return 0;
            }
            try {
                return processEnqueueingSession(session, batchSize);
            } finally {
                locker.unlockEntityForEnqueueIndexAll(session.getEntityName(), session.getTenantId());
            }
        } finally {
            authenticator.end();
        }
    }

    @Override
    public int processEnqueueingSession(String entityName) {
        return processEnqueueingSession(entityName, searchProperties.getReindexEntityEnqueueBatchSize());
    }

    @Override
    public int processEnqueueingSession(String entityName, @Nullable String tenantId) {
        return processEnqueueingSession(entityName, tenantId, searchProperties.getReindexEntityEnqueueBatchSize());
    }

    @Override
    public int processEnqueueingSession(String entityName, int batchSize) {
        return processEnqueueingSession(entityName, null, batchSize);
    }

    @Override
    public int processEnqueueingSession(String entityName, @Nullable String tenantId, int batchSize) {
        if (tenantId == null) {
            return processSession(() -> enqueueingSessionManager.getNextActiveSessionOfEntity(entityName), batchSize);
        }
        checkMultitenancyModulePresent();
        return processSession(() -> enqueueingSessionManager.getSession(entityName, tenantId), batchSize);
    }

    @Override
    public int enqueueDelete(Object entityInstance) {
        Preconditions.checkNotNullArgument(entityInstance);
        return enqueueDeleteCollection(Collections.singletonList(entityInstance));
    }

    @Override
    public int enqueueDeleteCollection(Collection<Object> entityInstances) {
        Preconditions.checkNotNullArgument(entityInstances);
        return enqueueDeleteTargets(entityInstances.stream()
                .map(instance -> new EntityDeletionTarget(Id.of(instance), tenantOfInstance(instance)))
                .toList());
    }

    @Override
    public int enqueueDeleteByEntityId(Id<?> entityId) {
        Preconditions.checkNotNullArgument(entityId);
        return enqueueDeleteCollectionByEntityIds(Collections.singletonList(entityId));
    }

    @Override
    public int enqueueDeleteByEntityId(Id<?> entityId, @Nullable String tenantId) {
        Preconditions.checkNotNullArgument(entityId);
        if (tenantId == null) {
            return enqueueDeleteByEntityId(entityId);
        }
        return enqueueDeleteTargets(List.of(new EntityDeletionTarget(entityId, tenantId)));
    }

    @Override
    public int enqueueDeleteCollectionByEntityIds(Collection<Id<?>> entityIds) {
        Preconditions.checkNotNullArgument(entityIds);
        return enqueueDeleteTargets(entityIds.stream().map(EntityDeletionTarget::tenantUnknown).toList());
    }

    /**
     * Puts deletions into the queue, each carrying the tenant of its record when the caller knows it.
     * <p>
     * The tenant is stored because it cannot be determined when the item is processed: the record is gone from the
     * database by then. An item without a tenant is deleted from every index of its entity.
     */
    protected int enqueueDeleteTargets(Collection<EntityDeletionTarget> targets) {
        List<IndexingQueueItem> queueItems = targets.stream()
                .map(target -> {
                    MetaClass metaClass = metadata.getClass(target.entityId().getEntityClass());
                    return indexConfigurationManager.getIndexConfigurationByEntityNameOpt(metaClass.getName())
                            .map(configuration -> createQueueItem(metaClass.getName(),
                                    idSerialization.idToString(target.entityId()),
                                    IndexingOperation.DELETE,
                                    target.tenantId()))
                            .orElse(null);
                })
                .filter(Objects::nonNull)
                .toList();
        return enqueue(queueItems);
    }

    /**
     * Reads the tenant off an instance the caller holds. An instance detached with the tenant attribute left
     * unfetched has no tenant to read; it is then left unknown and the deletion falls back to every index of the
     * entity.
     */
    @Nullable
    protected String tenantOfInstance(Object instance) {
        if (!multitenancyAdapter.isTenantIdReadable(instance)) {
            log.debug("The tenant of an instance of entity '{}' cannot be read: its deletion is enqueued without one",
                    metadata.getClass(instance).getName());
            return null;
        }
        return multitenancyAdapter.getTenantIdForInstance(instance);
    }

    @Override
    public int processNextBatch() {
        return processNextBatch(searchProperties.getProcessQueueBatchSize());
    }

    @Override
    public int processNextBatch(int batchSize) {
        return processQueue(batchSize, false);
    }

    @Override
    public int processEntireQueue() {
        return processEntireQueue(searchProperties.getProcessQueueBatchSize());
    }

    @Override
    public int processEntireQueue(int batchSize) {
        return processQueue(batchSize, true);
    }

    protected int processEnqueueingSession(EnqueueingSession session, int batchSize) {
        EnqueueingSessionStatus status = session.getStatus();
        switch (status) {
            case ACTIVE:
                return enqueueNextBatchInternal(session, batchSize);
            case SUSPENDED:
                log.debug("Skip session for entity '{}'", session.getEntityName());
            default:
                return 0;
        }
    }

    protected int enqueueNextBatchInternal(EnqueueingSession session, int batchSize) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("Size of enqueueing batch must be positive");
        }

        String entityName = session.getEntityName();
        MetaClass entityClass = metadata.getClass(entityName);

        EntityIdsLoader loader = entityIdsLoaderProvider.getLoader(entityName);
        ResultHolder resultHolder = loader.loadNextIds(session, batchSize);
        List<?> ids = resultHolder.getIds();
        log.debug("Next {} enqueuing instances of entity '{}': {}", ids.size(), entityName, ids);
        int processed = processRawIds(ids, entityClass, session.getTenantId(), batchSize);
        log.debug("Processed {} instances of entity '{}'", processed, entityName);
        if (ids.size() < batchSize) {
            log.debug("All instances of entity '{}' have been processed", entityName);
            enqueueingSessionManager.removeSession(session);
        } else {
            Object lastOrderingValue = resultHolder.getLastOrderingValue();
            enqueueingSessionManager.updateOrderingValue(session, lastOrderingValue);
        }
        return processed;
    }

    protected int enqueueIndexAll(String entityName, @Nullable String tenantId, int batchSize) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("Size of enqueuing batch during reindex entity must be positive");
        }

        if (!indexConfigurationManager.isDirectlyIndexed(entityName)) {
            throw new IllegalArgumentException(String.format("Unable to enqueue instances of entity '%s' - entity is not configured for indexing", entityName));
        }

        if (!locker.tryLockEntityForEnqueueIndexAll(entityName, tenantId)) {
            log.info("Unable to enqueue all instances of entity '{}' of tenant '{}' for indexing:"
                    + " 'Enqueue all' process is active", entityName, tenantId);
            return 0;
        }

        try {
            MetaClass metaClass = metadata.getClass(entityName);
            List<?> rawIds = loadRawIds(metaClass, tenantId);
            return processRawIds(rawIds, metaClass, tenantId, batchSize);
        } finally {
            locker.unlockEntityForEnqueueIndexAll(entityName, tenantId);
        }
    }

    protected List<?> loadRawIds(MetaClass metaClass, @Nullable String tenantId) {
        String entityName = metaClass.getName();
        String primaryKeyName = metadataTools.getPrimaryKeyName(metaClass);
        log.debug("Primary key of entity '{}': '{}'", entityName, primaryKeyName);
        if (primaryKeyName == null) {
            throw new IllegalArgumentException(String.format("Unable to enqueue instances of entity '%s' - entity doesn't have primary key", entityName));
        }

        if (!metadataTools.isJpaEntity(metaClass)) {
            return dataManager.load(metaClass.getJavaClass())
                    .all()
                    .fetchPlanProperties(primaryKeyName)
                    .list()
                    .stream()
                    .map(EntityValues::getId)
                    .toList();
        }

        List<?> rawIds;
        TransactionTemplate transactionTemplate = storeAwareLocator.getTransactionTemplate(metaClass.getStore().getName());
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        String tenantProperty = tenantId == null ? null : tenantPropertyName(metaClass);
        rawIds = transactionTemplate.execute(status -> {
            EntityManager em = storeAwareLocator.getEntityManager(metaClass.getStore().getName());

            List<String> conditions = new ArrayList<>();
            if (!metaClass.getDescendants().isEmpty()) {
                conditions.add("TYPE(e) = " + entityName);
            }
            if (tenantProperty != null) {
                conditions.add("e." + tenantProperty + " = :tenantId");
            }
            String where = conditions.isEmpty() ? "" : "where " + String.join(" and ", conditions);

            Query query = em.createQuery(format("select e.%s from %s e %s", primaryKeyName, entityName, where));
            if (tenantProperty != null) {
                query.setParameter("tenantId", tenantId);
            }
            return query.getResultList();
        });
        if (rawIds == null) {
            rawIds = Collections.emptyList();
        }
        return rawIds;
    }

    /**
     * @return name of the attribute holding the tenant of the entity
     * @throws IllegalArgumentException if the entity has no such attribute, so that instances of one tenant cannot
     *                                  be asked for at all
     */
    protected String tenantPropertyName(MetaClass metaClass) {
        MetaProperty tenantProperty = metadataTools.findTenantIdProperty(metaClass);
        if (tenantProperty == null) {
            throw new IllegalArgumentException(String.format(
                    "Unable to enqueue instances of one tenant: entity '%s' is not tenant-aware", metaClass.getName()));
        }
        return tenantProperty.getName();
    }

    /**
     * @param tenantId tenant every one of these records belongs to, or null when the entity is not split by
     *                 tenants - the ids were loaded with that tenant as the condition, so recording it costs
     *                 nothing and lets the queue tell the items of one tenant from the items of another
     */
    protected int processRawIds(List<?> rawIds, MetaClass metaClass, @Nullable String tenantId, int batchSize) {
        Class<Object> entityClass = metaClass.getJavaClass();
        String entityName = metaClass.getName();
        int totalSize = rawIds.size();
        int processedBatchSize = 0;
        int totalEnqueued = 0;
        int start = 0;
        int end = batchSize;
        do {
            end = Math.min(end, totalSize);
            List<?> rawIdsBatch = rawIds.subList(start, end);
            log.trace("Start process raw ids sublist [{}:{}) of entity '{}'", start, end, entityName);

            if (rawIdsBatch.isEmpty()) {
                processedBatchSize = 0;
            } else {
                List<IndexingQueueItem> queueItems = rawIdsBatch.stream()
                        .map(id -> idSerialization.idToString(Id.of(id, entityClass)))
                        .map(id -> createQueueItem(entityName, id, IndexingOperation.INDEX, tenantId))
                        .collect(Collectors.toList());

                int enqueued = enqueue(queueItems);
                totalEnqueued += enqueued;

                log.debug("Enqueued next {} instances of entity '{}': Total enqueued = {}/{}", enqueued, entityName, totalEnqueued, totalSize);

                processedBatchSize = rawIdsBatch.size();
                start += processedBatchSize;
                end += processedBatchSize;
            }
        } while (processedBatchSize == batchSize);

        return totalEnqueued;
    }

    protected int processQueue(int batchSize, boolean processEntireQueue) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("Size of queue processing batch must be positive");
        }

        int count = 0;
        boolean locked = locker.tryLockQueueProcessing();
        if (!locked) {
            log.debug("Unable to process queue: queue is being processed at the moment");
            return count;
        }

        log.debug("Start processing queue");
        try {
            authenticator.begin();

            List<IndexingQueueItem> queueItems;
            do {
                LoadContext<IndexingQueueItem> loadContext = createDequeueLoadContext(batchSize);
                log.trace("Dequeue items by load context: {}", loadContext);
                queueItems = dataManager.loadList(loadContext);
                log.debug("Dequeued {} items: {}", queueItems.size(), queueItems);

                if (queueItems.isEmpty()) {
                    break;
                }
                List<IndexingQueueItem> successfullyProcessedQueueItems = processQueueItems(queueItems);

                SaveContext saveContext = new SaveContext();
                saveContext.removing(successfullyProcessedQueueItems);
                dataManager.save(saveContext);

                count += successfullyProcessedQueueItems.size();

                // A batch where nothing succeeded leaves the queue exactly as it was, so the next iteration would
                // load the same items and the loop would never end.
                if (successfullyProcessedQueueItems.isEmpty()) {
                    log.info("Stop processing the queue: none of the {} items of the batch could be processed",
                            queueItems.size());
                    break;
                }
            } while (processEntireQueue && queueItems.size() == batchSize);
        } finally {
            locker.unlockQueueProcessing();
            authenticator.end();
        }

        log.debug("{} queue items have been successfully processed", count);
        return count;
    }

    /**
     * Builds the query that takes the next batch, leaving out the items that cannot be processed right now.
     * <p>
     * An item whose index is unavailable cannot be processed and is not removed from the queue, so taking it
     * would put it back at the head of the next batch - the batch is the oldest items first - and the queue
     * would stop for everyone. The items are therefore excluded by the query rather than discovered late.
     * <p>
     * Exclusion is per tenant where the tenant is known: a record of a tenant whose index is missing waits,
     * while records of the other tenants of the same entity keep flowing. An item that does not carry a tenant
     * is excluded together with its entity, because there is no telling which index it is addressed to.
     * <p>
     * The query is rebuilt for every batch, so the indexes of all configurations are asked for in one call: the
     * layout reads the list of tenants from the database, and asking configuration by configuration would mean
     * one query per indexed entity several times a minute.
     */
    protected LoadContext<IndexingQueueItem> createDequeueLoadContext(int batchSize) {
        LoadContext.Query query = new LoadContext.Query("");
        StringBuilder queryString = new StringBuilder("select q from search_IndexingQueue q");

        List<String> conditions = new ArrayList<>();
        int index = 0;
        Map<IndexConfiguration, List<IndexLayout.TenantIndex>> indexesByConfiguration =
                indexLayout.allIndexes(indexConfigurationManager.getAllIndexConfigurations());
        for (Map.Entry<IndexConfiguration, List<IndexLayout.TenantIndex>> entry : indexesByConfiguration.entrySet()) {
            IndexConfiguration configuration = entry.getKey();
            String entityName = configuration.getEntityName();
            List<IndexLayout.TenantIndex> indexes = entry.getValue();

            if (indexes.isEmpty()) {
                // A tenant-aware entity of an application that has no tenants: nothing to write to at all.
                conditions.add(String.format("not (q.entityName = :e%d)", index));
                query.setParameter("e" + index, entityName);
                index++;
                continue;
            }

            boolean anyUnavailable = false;
            for (IndexLayout.TenantIndex tenantIndex : indexes) {
                if (indexStateRegistry.isIndexAvailable(tenantIndex.indexName())) {
                    continue;
                }
                anyUnavailable = true;
                if (tenantIndex.tenantId() == null) {
                    conditions.add(String.format("not (q.entityName = :e%d and q.tenantId is null)", index));
                } else {
                    conditions.add(String.format("not (q.entityName = :e%d and q.tenantId = :t%d)", index, index));
                    query.setParameter("t" + index, tenantIndex.tenantId());
                }
                query.setParameter("e" + index, entityName);
                index++;
            }

            if (anyUnavailable && indexLayout.isSplitByTenants(configuration)) {
                // Items that were enqueued without a tenant may be addressed to the unavailable index.
                conditions.add(String.format("not (q.entityName = :e%d and q.tenantId is null)", index));
                query.setParameter("e" + index, entityName);
                index++;
            }
        }

        if (!conditions.isEmpty()) {
            log.debug("Skip queue items addressed to an unavailable index: {}", conditions);
            queryString.append(" where ").append(String.join(" and ", conditions));
        }
        queryString.append(" order by q.createdDate asc");

        query.setQueryString(queryString.toString());
        query.setMaxResults(batchSize);

        return new LoadContext<IndexingQueueItem>(metadata.getClass(IndexingQueueItem.class)).setQuery(query);
    }

    protected List<IndexingQueueItem> processQueueItems(List<IndexingQueueItem> queueItems) {
        QueueItemsAggregator queueItemsAggregator = new QueueItemsAggregator(queueItems);

        Map<Id<?>, List<IndexingQueueItem>> itemsForIndex = queueItemsAggregator.getIndexItemsGroup();
        Map<Id<?>, List<IndexingQueueItem>> itemsForDelete = queueItemsAggregator.getDeleteItemsGroup();

        List<IndexingQueueItem> successfullyProcessedQueueItems = new ArrayList<>(queueItems.size());
        if (MapUtils.isNotEmpty(itemsForIndex)) {
            successfullyProcessedQueueItems.addAll(
                    processQueueItemsGroup(itemsForIndex, entityIndexer::indexCollectionByEntityIds)
            );
        }
        if (MapUtils.isNotEmpty(itemsForDelete)) {
            successfullyProcessedQueueItems.addAll(
                    processQueueItemsGroup(itemsForDelete,
                            entityIds -> entityIndexer.deleteCollectionByTargets(
                                    deletionTargets(entityIds, itemsForDelete)))
            );
        }

        return successfullyProcessedQueueItems;
    }

    /**
     * Pairs every record to delete with the tenant its item carries, so that the deletion goes to that tenant's index
     * alone. An item enqueued without a tenant leaves the pair unresolved, and the indexer deletes the document from
     * every index of the entity.
     */
    protected List<EntityDeletionTarget> deletionTargets(Collection<Id<?>> entityIds,
                                                         Map<Id<?>, List<IndexingQueueItem>> itemsForDelete) {
        return entityIds.stream()
                .map(entityId -> new EntityDeletionTarget(entityId, tenantOfItems(itemsForDelete.get(entityId))))
                .toList();
    }

    @Nullable
    protected String tenantOfItems(@Nullable List<IndexingQueueItem> items) {
        return items == null ? null : items.stream()
                .map(IndexingQueueItem::getTenantId)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    protected List<IndexingQueueItem> processQueueItemsGroup(Map<Id<?>, List<IndexingQueueItem>> itemsGroup,
                                                             Function<Collection<Id<?>>, IndexResult> processingFunction) {
        Set<Id<?>> entityIds = itemsGroup.keySet();
        IndexResult indexResult = processingFunction.apply(entityIds);
        return handleIndexResult(indexResult, itemsGroup);
    }

    protected List<IndexingQueueItem> handleIndexResult(IndexResult indexResult, Map<Id<?>, List<IndexingQueueItem>> itemsGroup) {
        List<Id<?>> failedIds = Collections.emptyList();
        if (indexResult.hasFailures()) {
            failedIds = indexResult.getFailedIndexIds().stream()
                    .map(idSerialization::stringToId)
                    .collect(Collectors.toList());
        }
        failedIds.forEach(itemsGroup.keySet()::remove);
        return itemsGroup.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    protected int enqueue(Collection<Object> entityInstances, IndexingOperation operation) {
        List<Id<?>> ids = entityInstances.stream().map(Id::of).collect(Collectors.toList());
        return enqueueByIds(ids, operation);
    }

    protected int enqueueByIds(Collection<Id<?>> entityIds, IndexingOperation operation) {
        List<IndexingQueueItem> queueItems = entityIds.stream()
                .map(id -> {
                    MetaClass metaClass = metadata.getClass(id.getEntityClass());
                    Optional<IndexConfiguration> indexConfigurationOpt = indexConfigurationManager.getIndexConfigurationByEntityNameOpt(metaClass.getName());
                    if (indexConfigurationOpt.isPresent()) {
                        String serializedEntityId = idSerialization.idToString(id);
                        return createQueueItem(metaClass, serializedEntityId, operation);
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return enqueue(queueItems);
    }

    protected int enqueue(Collection<IndexingQueueItem> queueItems) {
        log.trace("Enqueue items: {}", queueItems);
        TransactionTemplate transactionTemplate = storeAwareLocator.getTransactionTemplate(Stores.MAIN);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        transactionTemplate.executeWithoutResult(status -> {
            EntityManager entityManager = storeAwareLocator.getEntityManager(Stores.MAIN);
            queueItems.forEach(entityManager::persist);
        });
        return queueItems.size();
    }

    protected IndexingQueueItem createQueueItem(MetaClass metaClass, String entityId, IndexingOperation operation) {
        return createQueueItem(metaClass.getName(), entityId, operation);
    }

    protected IndexingQueueItem createQueueItem(String entityName, String entityId, IndexingOperation operation) {
        return createQueueItem(entityName, entityId, operation, null);
    }

    protected IndexingQueueItem createQueueItem(String entityName,
                                                String entityId,
                                                IndexingOperation operation,
                                                @Nullable String tenantId) {
        IndexingQueueItem queueItem = metadata.create(IndexingQueueItem.class);
        queueItem.setOperation(operation);
        queueItem.setEntityId(entityId);
        queueItem.setEntityName(entityName);
        queueItem.setTenantId(tenantId);
        return queueItem;
    }

    protected void checkMultitenancyModulePresent() {
        if (!multitenancyAdapter.isMultitenancyActive()) {
            throw new IllegalStateException("Multitenancy is not available");
        }
    }

    /**
     * Analyzes collection of {@link IndexingQueueItem}, determines unique entity ids
     * and splits them among two disjoint groups: for index and for delete.
     * <p>
     * Group for specific id is determined by 'effective queue item' - the latest one for that id.
     * <p>
     * In case of multiple queue items related to single entity id
     * it allows to perform only one actual operation on each id and remove all related queue items.
     */
    protected class QueueItemsAggregator {
        Map<Id<?>, IndexingQueueItem> effectiveItemsForIds = new HashMap<>();
        Map<Id<?>, List<IndexingQueueItem>> itemsForIds = new HashMap<>();
        Map<IndexingOperation, Set<Id<?>>> idsForOperations = new HashMap<>();

        protected QueueItemsAggregator(Collection<IndexingQueueItem> queueItems) {
            groupQueueItems(queueItems);
        }

        /**
         * Gets all entity ids that should be indexed.
         * <p>
         * Every entity id is mapped to all {@link IndexingQueueItem} related to this id.
         *
         * @return Map with entity ids as keys and lists of related queue items as values
         */
        protected Map<Id<?>, List<IndexingQueueItem>> getIndexItemsGroup() {
            return getOperationItemsGroup(IndexingOperation.INDEX);
        }

        /**
         * Gets all entity ids that should be deleted from index.
         * <p>
         * Every entity id is mapped to all {@link IndexingQueueItem} related to this id.
         *
         * @return Map with entity ids as keys and lists of related queue items as values
         */
        protected Map<Id<?>, List<IndexingQueueItem>> getDeleteItemsGroup() {
            return getOperationItemsGroup(IndexingOperation.DELETE);
        }

        protected Map<Id<?>, List<IndexingQueueItem>> getOperationItemsGroup(IndexingOperation operation) {
            return idsForOperations.getOrDefault(operation, Collections.emptySet())
                    .stream()
                    .collect(Collectors.toMap(
                            Function.identity(),
                            id -> itemsForIds.getOrDefault(id, Collections.emptyList())
                    ));
        }

        protected void groupQueueItems(Collection<IndexingQueueItem> queueItems) {
            queueItems.forEach(item -> {
                Id<?> entityId = idSerialization.stringToId(item.getEntityId());

                //Resolve effective queue item
                IndexingQueueItem currentEffectiveItem = effectiveItemsForIds.get(entityId);
                IndexingQueueItem previousEffectiveItem = null;
                if (currentEffectiveItem == null) {
                    currentEffectiveItem = item;
                } else {
                    if (currentEffectiveItem.getCreatedDate().before(item.getCreatedDate())) {
                        previousEffectiveItem = currentEffectiveItem;
                        currentEffectiveItem = item;
                    }
                }

                //Bind processed item to entity id
                List<IndexingQueueItem> itemsForId = itemsForIds.computeIfAbsent(entityId, k -> new ArrayList<>());
                itemsForId.add(item);

                //Bind effective item to entity id
                effectiveItemsForIds.put(entityId, currentEffectiveItem);

                //Add entity id to actual operation group
                IndexingOperation currentEffectiveOperation = currentEffectiveItem.getOperation();
                Set<Id<?>> idsForCurrentEffectiveOperation = idsForOperations.computeIfAbsent(currentEffectiveOperation, k -> new HashSet<>());
                idsForCurrentEffectiveOperation.add(entityId);

                //Remove entity id from irrelevant operation group (if necessary)
                if (previousEffectiveItem != null) {
                    IndexingOperation previousEffectiveOperation = previousEffectiveItem.getOperation();
                    if (!previousEffectiveOperation.equals(currentEffectiveOperation)) {
                        Set<Id<?>> idsForPreviousEffectiveOperation = idsForOperations.computeIfAbsent(previousEffectiveOperation, k -> new HashSet<>());
                        idsForPreviousEffectiveOperation.remove(entityId);
                    }
                }
            });
        }
    }

    /**
     * Repeats a session operation for every indexed entity, which is what an operation asked without an entity
     * means. When a tenant is named, entities without a tenant attribute are left out: none of their data belongs
     * to that tenant. The attribute is enough to ask about here - that multitenancy is on has been established by
     * the caller.
     */
    protected List<IndexOperationResult<IndexManipulationResult>> everyEntityOfTenant(
            @Nullable String tenantId,
            Function<String, List<IndexOperationResult<IndexManipulationResult>>> operation) {
        List<IndexOperationResult<IndexManipulationResult>> results = new ArrayList<>();
        indexConfigurationManager.getAllIndexConfigurations().stream()
                .filter(configuration -> tenantId == null || configuration.isTenantAware())
                .map(IndexConfiguration::getEntityName)
                .forEach(entityName -> results.addAll(operation.apply(entityName)));
        return results;
    }

}
