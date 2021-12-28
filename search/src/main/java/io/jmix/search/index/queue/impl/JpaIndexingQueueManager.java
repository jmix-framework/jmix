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
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.data.StoreAwareLocator;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.EntityIndexer;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.IndexResult;
import io.jmix.search.index.impl.IndexStateRegistry;
import io.jmix.search.index.impl.IndexingLocker;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.index.queue.IndexingQueueManager;
import io.jmix.search.index.queue.entity.IndexingQueueItem;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Component("search_JpaIndexingQueueManager")
public class JpaIndexingQueueManager implements IndexingQueueManager {

    private static final Logger log = LoggerFactory.getLogger(JpaIndexingQueueManager.class);

    @Autowired
    protected UnconstrainedDataManager dataManager;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected EntityIndexer entityIndexer;
    @Autowired
    protected StoreAwareLocator storeAwareLocator;
    @Autowired
    protected IndexConfigurationManager indexConfigurationManager;
    @Autowired
    protected IdSerialization idSerialization;
    @Autowired
    protected SystemAuthenticator authenticator;
    @Autowired
    protected IndexingLocker locker;
    @Autowired
    protected SearchProperties searchProperties;
    @Autowired
    protected IndexStateRegistry indexStateRegistry;

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
    public int emptyQueue(String entityName) {
        Preconditions.checkNotEmptyString(entityName);
        TransactionTemplate transactionTemplate = storeAwareLocator.getTransactionTemplate(Stores.MAIN);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        Integer result = transactionTemplate.execute(status -> {
            log.debug("Empty queue for entity '{}'", entityName);
            EntityManager entityManager = storeAwareLocator.getEntityManager(Stores.MAIN);
            Query query = entityManager.createQuery("delete from search_IndexingQueue q where q.entityName = ?1");
            query.setParameter(1, entityName);
            int deleted = query.executeUpdate();
            log.debug("{} records for entity '{}' have been deleted from queue", deleted, entityName);
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
    public int enqueueIndexCollectionByEntityIds(Collection<Id<?>> entityIds) {
        Preconditions.checkNotNullArgument(entityIds);
        return enqueueByIds(entityIds, IndexingOperation.INDEX);
    }

    @Override
    public int enqueueIndexAll() {
        return indexConfigurationManager.getAllIndexConfigurations().stream()
                .map(IndexConfiguration::getEntityName)
                .map(this::enqueueIndexAll)
                .reduce(Integer::sum)
                .orElse(0);
    }

    @Override
    public int enqueueIndexAll(String entityName) {
        Preconditions.checkNotEmptyString(entityName);
        return enqueueIndexAll(entityName, searchProperties.getReindexEntityEnqueueBatchSize());
    }

    @Override
    public int enqueueDelete(Object entityInstance) {
        Preconditions.checkNotNullArgument(entityInstance);
        return enqueueDeleteCollection(Collections.singletonList(entityInstance));
    }

    @Override
    public int enqueueDeleteCollection(Collection<Object> entityInstances) {
        Preconditions.checkNotNullArgument(entityInstances);
        return enqueue(entityInstances, IndexingOperation.DELETE);
    }

    @Override
    public int enqueueDeleteByEntityId(Id<?> entityId) {
        Preconditions.checkNotNullArgument(entityId);
        return enqueueDeleteCollectionByEntityIds(Collections.singletonList(entityId));
    }

    @Override
    public int enqueueDeleteCollectionByEntityIds(Collection<Id<?>> entityIds) {
        Preconditions.checkNotNullArgument(entityIds);
        return enqueueByIds(entityIds, IndexingOperation.DELETE);
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

    protected int enqueueIndexAll(String entityName, int batchSize) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("Size of enqueuing batch during reindex entity must be positive");
        }

        if (!indexConfigurationManager.isDirectlyIndexed(entityName)) {
            throw new IllegalArgumentException(String.format("Unable to enqueue instances of entity '%s' - entity is not configured for indexing", entityName));
        }

        if (!locker.tryLockEntityForEnqueueIndexAll(entityName)) {
            log.info("Unable to enqueue all instances of entity '{}' for indexing: 'Enqueue all' process is active", entityName);
            return 0;
        }

        try {
            MetaClass metaClass = metadata.getClass(entityName);
            List<?> rawIds = loadRawIds(metaClass);
            return processRawIds(rawIds, metaClass, batchSize);
        } finally {
            locker.unlockEntityForEnqueueIndexAll(entityName);
        }
    }

    protected List<?> loadRawIds(MetaClass metaClass) {
        String entityName = metaClass.getName();
        String primaryKeyName = metadataTools.getPrimaryKeyName(metaClass);
        log.debug("Primary key of entity '{}': '{}'", entityName, primaryKeyName);
        if (primaryKeyName == null) {
            throw new IllegalArgumentException(String.format("Unable to enqueue instances of entity '%s' - entity doesn't have primary key", entityName));
        }

        List<?> rawIds;
        TransactionTemplate transactionTemplate = storeAwareLocator.getTransactionTemplate(metaClass.getStore().getName());
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        rawIds = transactionTemplate.execute(status -> {
            EntityManager em = storeAwareLocator.getEntityManager(metaClass.getStore().getName());
            Query query = em.createQuery(format("select e.%s from %s e", primaryKeyName, entityName));
            return query.getResultList();
        });
        if (rawIds == null) {
            rawIds = Collections.emptyList();
        }
        return rawIds;
    }

    protected int processRawIds(List<?> rawIds, MetaClass metaClass, int batchSize) {
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
                        .map(id -> createQueueItem(entityName, id, IndexingOperation.INDEX))
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
                List<String> unavailableEntities = indexStateRegistry.getAllUnavailableIndexedEntities();
                LoadContext<IndexingQueueItem> loadContext = createDequeueLoadContext(unavailableEntities, batchSize);
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
            } while (processEntireQueue && queueItems.size() == batchSize);
        } finally {
            locker.unlockQueueProcessing();
            authenticator.end();
        }

        log.debug("{} queue items have been successfully processed", count);
        return count;
    }

    protected LoadContext<IndexingQueueItem> createDequeueLoadContext(List<String> unavailableEntities, int batchSize) {
        LoadContext.Query query = new LoadContext.Query("");
        StringBuilder sb = new StringBuilder("select q from search_IndexingQueue q");
        if (!unavailableEntities.isEmpty()) {
            sb.append(" where q.entityName not in :unavailableEntities");
            query.setParameter("unavailableEntities", unavailableEntities);
        }
        sb.append(" order by q.createdDate asc");
        query.setQueryString(sb.toString());
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
                    processQueueItemsGroup(itemsForDelete, entityIndexer::deleteCollectionByEntityIds)
            );
        }

        return successfullyProcessedQueueItems;
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

    protected Map<IndexingOperation, Map<Id<?>, List<IndexingQueueItem>>> groupQueueItems(Collection<IndexingQueueItem> queueItems) {
        Map<IndexingOperation, Map<Id<?>, List<IndexingQueueItem>>> result = new HashMap<>();
        queueItems.forEach(item -> {
            IndexingOperation operation = item.getOperation();
            Id<?> id = idSerialization.stringToId(item.getEntityId());
            Map<Id<?>, List<IndexingQueueItem>> itemsForOperation = result.computeIfAbsent(operation, k -> new HashMap<>());
            List<IndexingQueueItem> itemsForInstanceId = itemsForOperation.computeIfAbsent(id, k -> new ArrayList<>());
            itemsForInstanceId.add(item);
        });

        return result;
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
        IndexingQueueItem queueItem = metadata.create(IndexingQueueItem.class);
        queueItem.setOperation(operation);
        queueItem.setEntityId(entityId);
        queueItem.setEntityName(entityName);
        return queueItem;
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
}
