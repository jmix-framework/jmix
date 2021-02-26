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

package io.jmix.search.index.queue;

import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.EntityOp;
import io.jmix.data.StoreAwareLocator;
import io.jmix.search.index.EntityIndexer;
import io.jmix.search.utils.PropertyTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component("search_JpaQueueService")
public class JpaQueueService implements QueueService {

    private static final Logger log = LoggerFactory.getLogger(JpaQueueService.class);

    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected EntityIndexer entityIndexer;
    @Autowired
    protected StoreAwareLocator storeAwareLocator;
    @Autowired
    protected FetchPlans fetchPlans;
    @Autowired
    protected PropertyTools propertyTools;

    @Override
    public void enqueue(MetaClass entityClass, String entityPk, EntityOp entityChangeType) {
        QueueItem queueItem = createQueueItem(entityClass.getName(), entityPk, entityChangeType);
        enqueue(queueItem);
    }

    @Override
    public void enqueue(MetaClass entityClass, Collection<String> entityPks, EntityOp entityChangeType) {
        List<QueueItem> queueItems = entityPks.stream()
                .map(id -> createQueueItem(entityClass.getName(), id, entityChangeType))
                .collect(Collectors.toList());

        enqueue(queueItems);
    }

    @Override
    public void enqueue(QueueItem queueItem) {
        enqueue(Collections.singletonList(queueItem));
    }

    @Override
    public void enqueue(Collection<QueueItem> queueItems) {
        log.trace("Enqueue items: {}", queueItems);
        TransactionTemplate transactionTemplate = storeAwareLocator.getTransactionTemplate(Stores.MAIN);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        transactionTemplate.executeWithoutResult(status -> {
            EntityManager entityManager = storeAwareLocator.getEntityManager(Stores.MAIN);
            queueItems.forEach(entityManager::persist);
        });
    }

    @Override
    public void enqueue(String entityName, int batchSize) {
        if(batchSize <= 0) {
            log.error("Size of enqueuing batch during reindex entity must be positive");
        }

        MetaClass metaClass = metadata.getClass(entityName);
        String primaryKeyPropertyName = propertyTools.getPrimaryKeyPropertyNameForSearch(metaClass);
        FetchPlan fetchPlan = fetchPlans.builder(metaClass.getJavaClass())
                .add(primaryKeyPropertyName)
                .build();
        int batchOffset = 0;
        int batchLoaded;
        do {
            List<Object> entities = dataManager.load(metaClass.getJavaClass())
                    .all()
                    .firstResult(batchOffset) //todo integer limit?
                    .maxResults(batchSize)
                    .fetchPlan(fetchPlan)
                    .list();

            List<QueueItem> queueItems = entities.stream()
                    .map(entity -> EntityValues.getValue(entity, primaryKeyPropertyName))
                    .filter(Objects::nonNull)
                    .map(pk -> createQueueItem(entityName, pk.toString(), EntityOp.UPDATE))
                    .collect(Collectors.toList());

            enqueue(queueItems);

            batchLoaded = entities.size();
            batchOffset += batchLoaded;
        } while (batchLoaded == batchSize);
    }

    @Override
    public int processQueue(int batchSize, int maxProcessedPerExecution) {
        if(batchSize <= 0) {
            log.error("Size of batch during queue processing must be positive");
        }

        List<QueueItem> queueItems;
        int count = 0;
        do {
            queueItems = dataManager.load(QueueItem.class)
                    .query("select q from search_Queue q order by q.createdDate asc")
                    .maxResults(batchSize)
                    .list();
            log.trace("Dequeued {} items", queueItems.size());

            if(queueItems.isEmpty()) {
                break;
            }

            count += queueItems.size(); //todo Return actual amount of indexed entities from 'entityIndexer.indexEntities'
            entityIndexer.indexEntities(queueItems); //todo handle failed commands of bulk request

            SaveContext saveContext = new SaveContext();
            saveContext.removing(queueItems);
            dataManager.save(saveContext);
        } while (queueItems.size() == batchSize && (maxProcessedPerExecution <= 0 || count < maxProcessedPerExecution));

        return count;
    }

    @Override
    public void emptyQueue(String entityName) {
        TransactionTemplate transactionTemplate = storeAwareLocator.getTransactionTemplate(Stores.MAIN);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transactionTemplate.executeWithoutResult(status -> {
            log.debug("Empty queue for entity '{}'", entityName);
            EntityManager entityManager = storeAwareLocator.getEntityManager(Stores.MAIN);
            Query query = entityManager.createQuery("delete from search_Queue q where q.entityName = ?1");
            query.setParameter(1, entityName);
            int deleted = query.executeUpdate();
            log.debug("{} records for entity '{}' have been deleted from queue", entityName, deleted);
        });
    }

    protected QueueItem createQueueItem(String entityName, String entityPk, EntityOp entityChangeType) {
        QueueItem queueItem = metadata.create(QueueItem.class);
        queueItem.setChangeType(entityChangeType.getId());
        queueItem.setEntityId(entityPk);
        queueItem.setEntityName(entityName);

        return queueItem;
    }
}
