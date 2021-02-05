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

import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.SaveContext;
import io.jmix.core.Stores;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.Authenticator;
import io.jmix.data.EntityChangeType;
import io.jmix.data.StoreAwareLocator;
import io.jmix.search.index.EntityIndexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
    protected Authenticator authenticator;
    @Autowired
    protected StoreAwareLocator storeAwareLocator;

    @Override
    public void enqueue(MetaClass entityClass, String entityId, EntityChangeType entityChangeType) {
        log.info("[IVGA] Enqueue entity: CLass={}, ID={}, ChangeType={}", entityClass, entityId, entityChangeType);
        QueueItem queueItem = createQueueItem(entityClass, entityId, null, entityChangeType);
        enqueue(queueItem);
    }

    @Override
    public void enqueue(MetaClass entityClass, Collection<String> entityIds, EntityChangeType entityChangeType) {
        log.info("[IVGA] Enqueue entities: CLass={}, IDs={}, ChangeType={}", entityClass, entityIds, entityChangeType);
        List<QueueItem> queueItems = entityIds.stream()
                .map(id -> createQueueItem(entityClass, id, null, entityChangeType))
                .collect(Collectors.toList());

        enqueue(queueItems);
    }

    @Override
    public void enqueue(QueueItem queueItem) {
        enqueue(Collections.singletonList(queueItem));
    }

    @Override
    public void enqueue(Collection<QueueItem> queueItems) {
        EntityManager entityManager = storeAwareLocator.getEntityManager(Stores.MAIN);
        TransactionTemplate transactionTemplate = storeAwareLocator.getTransactionTemplate(Stores.MAIN);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        transactionTemplate.execute(status -> {
            log.info("[IVGA] Persist {}", queueItems);
            queueItems.forEach(entityManager::persist);
            return null;
        });
    }

    @Override
    public void processQueue() {
        log.trace("[IVGA] Process queue");
        int batchSize = 100; //todo
        List<QueueItem> queueItems;

        try {
            authenticator.begin();
            do {
                queueItems = dataManager.load(QueueItem.class)
                        .query("select q from search_Queue q order by q.createdDate asc")
                        .maxResults(batchSize)
                        .list();
                log.trace("[IVGA] Dequeued items: {}", queueItems);

                entityIndexer.indexEntities(queueItems); //todo handle failed commands of bulk request

                SaveContext saveContext = new SaveContext();
                saveContext.removing(queueItems);
                dataManager.save(saveContext);
            } while (queueItems.size() == batchSize);
        } finally {
            authenticator.end();
        }
    }

    protected QueueItem createQueueItem(MetaClass entityClass, String entityId, String entityName, EntityChangeType entityChangeType) {
        QueueItem queueItem = metadata.create(QueueItem.class);
        queueItem.setChangeType(entityChangeType.getId());
        queueItem.setEntityClass(entityClass.getName());
        queueItem.setEntityId(entityId);
        queueItem.setEntityName(entityName);

        return queueItem;
    }
}
