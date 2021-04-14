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
import io.jmix.search.index.impl.IndexingLocker;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.index.queue.IndexingQueueManager;
import io.jmix.search.index.queue.entity.IndexingQueueItem;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;
import java.util.stream.Collectors;

@Component("search_JpaIndexingQueueManager")
public class JpaIndexingQueueManager implements IndexingQueueManager {

    private static final Logger log = LoggerFactory.getLogger(JpaIndexingQueueManager.class);

    @Autowired
    protected DataManager dataManager;
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
    public void enqueueIndex(Object entityInstance) {
        Preconditions.checkNotNullArgument(entityInstance);
        enqueueIndexCollection(Collections.singletonList(entityInstance));
    }

    @Override
    public void enqueueIndexCollection(Collection<Object> entityInstances) {
        Preconditions.checkNotNullArgument(entityInstances);
        enqueue(entityInstances, IndexingOperation.INDEX);
    }

    @Override
    public void enqueueIndexByEntityId(Id<?> entityId) {
        enqueueIndexCollectionByEntityIds(Collections.singletonList(entityId));
    }

    @Override
    public void enqueueIndexCollectionByEntityIds(Collection<Id<?>> entityIds) {
        enqueueByIds(entityIds, IndexingOperation.INDEX);
    }

    @Override
    public void enqueueIndexAll() {
        Collection<IndexConfiguration> indexConfigurations = indexConfigurationManager.getAllIndexConfigurations();
        indexConfigurations.forEach(config -> enqueueIndexAll(config.getEntityName()));
    }

    @Override
    public void enqueueIndexAll(String entityName) {
        enqueueIndexAll(entityName, searchApplicationProperties.getReindexEntityEnqueueBatchSize());
    }

    @Override
    public void enqueueDelete(Object entityInstance) {
        Preconditions.checkNotNullArgument(entityInstance);
        enqueueDeleteCollection(Collections.singletonList(entityInstance));
    }

    @Override
    public void enqueueDeleteCollection(Collection<Object> entityInstances) {
        Preconditions.checkNotNullArgument(entityInstances);
        enqueue(entityInstances, IndexingOperation.DELETE);
    }

    @Override
    public void enqueueDeleteByEntityId(Id<?> entityId) {
        enqueueDeleteCollectionByEntityIds(Collections.singletonList(entityId));
    }

    @Override
    public void enqueueDeleteCollectionByEntityIds(Collection<Id<?>> entityIds) {
        enqueueByIds(entityIds, IndexingOperation.DELETE);
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

    protected void enqueueIndexAll(String entityName, int batchSize) {
        if (batchSize <= 0) {
            log.error("Size of enqueuing batch during reindex entity must be positive");
            return;
        }

        IndexConfiguration indexConfiguration = indexConfigurationManager.getIndexConfigurationByEntityName(entityName);
        if (indexConfiguration == null) {
            log.warn("Unable to enqueue instances of entity '{}' - entity is not configured for indexing", entityName);
            return;
        }

        MetaClass metaClass = metadata.getClass(entityName);
        int batchOffset = 0;
        int batchLoaded;
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

            enqueue(queueItems);

            batchLoaded = instances.size();
            batchOffset += batchLoaded;
        } while (batchLoaded == batchSize);
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

                count += queueItems.size(); //todo Return actual amount of indexed entities from 'entityIndexer'

                Map<IndexingOperation, List<Id<?>>> idsGroupedByOperation = queueItems.stream().collect(
                        Collectors.groupingBy(
                                IndexingQueueItem::getOperation,
                                Collectors.mapping(
                                        item -> idSerialization.stringToId(item.getEntityId()),
                                        Collectors.toList()
                                )
                        )
                );
                List<Id<?>> idsForIndex = idsGroupedByOperation.get(IndexingOperation.INDEX);
                List<Id<?>> idsForDelete = idsGroupedByOperation.get(IndexingOperation.DELETE);
                //todo handle failed commands of bulk request
                if (CollectionUtils.isNotEmpty(idsForIndex)) {
                    entityIndexer.indexCollectionByEntityIds(idsForIndex);
                }
                if (CollectionUtils.isNotEmpty(idsForDelete)) {
                    entityIndexer.deleteCollectionByEntityIds(idsForDelete);
                }
                //todo check case update after delete (restore entity?)

                SaveContext saveContext = new SaveContext();
                saveContext.removing(queueItems);
                dataManager.save(saveContext);
            } while (queueItems.size() == batchSize && (maxProcessedPerExecution <= 0 || queueItems.size() <= maxProcessedPerExecution));
        } finally {
            locker.unlockQueueProcessing();
            authenticator.end();
        }

        log.debug("{} queue items have been successfully processed", count);
        return count;
    }

    protected void enqueue(Collection<Object> entityInstances, IndexingOperation operation) {
        List<Id<?>> ids = entityInstances.stream().map(Id::of).collect(Collectors.toList());
        enqueueByIds(ids, operation);
    }

    protected void enqueueByIds(Collection<Id<?>> entityIds, IndexingOperation operation) {
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
        enqueue(queueItems);
    }

    protected void enqueue(Collection<IndexingQueueItem> queueItems) {
        log.trace("Enqueue items: {}", queueItems);
        TransactionTemplate transactionTemplate = storeAwareLocator.getTransactionTemplate(Stores.MAIN);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        transactionTemplate.executeWithoutResult(status -> {
            EntityManager entityManager = storeAwareLocator.getEntityManager(Stores.MAIN);
            queueItems.forEach(entityManager::persist);
        });
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
