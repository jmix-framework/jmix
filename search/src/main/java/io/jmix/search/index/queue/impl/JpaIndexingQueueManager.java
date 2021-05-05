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
import io.jmix.search.SearchApplicationProperties;
import io.jmix.search.index.EntityIndexer;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.IndexResult;
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

@Component("search_JpaIndexingQueueManager")
public class JpaIndexingQueueManager implements IndexingQueueManager {

    private static final Logger log = LoggerFactory.getLogger(JpaIndexingQueueManager.class);

    @Autowired
    protected UnsafeDataManager dataManager;
    @Autowired
    protected Metadata metadata;
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
    protected SearchApplicationProperties searchApplicationProperties;

    @Override
    public void emptyQueue(String entityName) {
        Preconditions.checkNotEmptyString(entityName);
        TransactionTemplate transactionTemplate = storeAwareLocator.getTransactionTemplate(Stores.MAIN);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transactionTemplate.executeWithoutResult(status -> {
            log.debug("Empty queue for entity '{}'", entityName);
            EntityManager entityManager = storeAwareLocator.getEntityManager(Stores.MAIN);
            Query query = entityManager.createQuery("delete from search_IndexingQueue q where q.entityName = ?1");
            query.setParameter(1, entityName);
            int deleted = query.executeUpdate();
            log.debug("{} records for entity '{}' have been deleted from queue", entityName, deleted);
        });
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
        return enqueueIndexAll(entityName, searchApplicationProperties.getReindexEntityEnqueueBatchSize());
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
        return processNextBatch(searchApplicationProperties.getProcessQueueBatchSize());
    }

    @Override
    public int processNextBatch(int batchSize) {
        return processQueue(batchSize, batchSize);
    }

    @Override
    public int processEntireQueue() {
        return processQueue(searchApplicationProperties.getProcessQueueBatchSize(), -1);
    }

    protected int enqueueIndexAll(String entityName, int batchSize) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("Size of enqueuing batch during reindex entity must be positive");
        }

        IndexConfiguration indexConfiguration = indexConfigurationManager.getIndexConfigurationByEntityName(entityName);
        if (indexConfiguration == null) {
            throw new IllegalArgumentException(String.format("Unable to enqueue instances of entity '%s' - entity is not configured for indexing", entityName));
        }

        MetaClass metaClass = metadata.getClass(entityName);
        int batchOffset = 0;
        int batchLoaded;
        int total = 0;
        do {
            List<Object> instances = dataManager.load(metaClass.getJavaClass())
                    .all()
                    .firstResult(batchOffset) //todo integer limit?
                    .maxResults(batchSize)
                    .list();

            List<IndexingQueueItem> queueItems = instances.stream()
                    .map(instance -> idSerialization.idToString(Id.of(instance)))
                    .map(id -> createQueueItem(entityName, id, IndexingOperation.INDEX))
                    .collect(Collectors.toList());

            int enqueued = enqueue(queueItems);
            total += enqueued;

            batchLoaded = instances.size();
            batchOffset += batchLoaded;
        } while (batchLoaded == batchSize);

        return total;
    }

    protected int processQueue(int batchSize, int maxProcessedPerExecution) {
        //todo check global conditions to proceed (ES availability, etc)

        log.debug("Start processing queue");
        int count = 0;
        boolean locked = locker.tryLockQueueProcessing();
        if (!locked) {
            log.debug("Unable to process queue: queue is being processed at the moment");
            return count;
        }

        try {
            authenticator.begin();
            if (batchSize <= 0) {
                throw new IllegalArgumentException("Size of batch during queue processing must be positive");
            }

            List<IndexingQueueItem> queueItems;
            do {
                queueItems = dataManager.load(IndexingQueueItem.class)
                        .query("select q from search_IndexingQueue q order by q.createdDate asc")
                        .maxResults(batchSize)
                        .list();
                log.debug("Dequeued {} items: {}", queueItems.size(), queueItems);

                if (queueItems.isEmpty()) {
                    break;
                }
                List<IndexingQueueItem> successfullyProcessedQueueItems = processQueueItems(queueItems);

                SaveContext saveContext = new SaveContext();
                saveContext.removing(successfullyProcessedQueueItems);
                dataManager.save(saveContext);

                count += successfullyProcessedQueueItems.size();
            } while (queueItems.size() == batchSize && (maxProcessedPerExecution <= 0 || queueItems.size() <= maxProcessedPerExecution));
        } finally {
            locker.unlockQueueProcessing();
            authenticator.end();
        }

        log.debug("{} queue items have been successfully processed", count);
        return count;
    }

    protected List<IndexingQueueItem> processQueueItems(List<IndexingQueueItem> queueItems) {
        Map<IndexingOperation, Map<Id<?>, List<IndexingQueueItem>>> groupedQueueItems = groupQueueItems(queueItems);

        Map<Id<?>, List<IndexingQueueItem>> itemsForIndex = groupedQueueItems.get(IndexingOperation.INDEX);
        Map<Id<?>, List<IndexingQueueItem>> itemsForDelete = groupedQueueItems.get(IndexingOperation.DELETE);
        //todo check case update after delete (restore entity?)

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
        itemsGroup.keySet().removeAll(failedIds);
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
                    IndexConfiguration indexConfiguration = indexConfigurationManager.getIndexConfigurationByEntityName(metaClass.getName());
                    if (indexConfiguration == null) {
                        return null;
                    } else {
                        String serializedEntityId = idSerialization.idToString(id);
                        return createQueueItem(metaClass, serializedEntityId, operation);
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
}
