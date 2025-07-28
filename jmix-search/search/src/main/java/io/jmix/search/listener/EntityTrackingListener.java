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

package io.jmix.search.listener;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.jmix.core.*;
import io.jmix.core.datastore.AbstractDataStore;
import io.jmix.core.datastore.DataStoreBeforeEntitySaveEvent;
import io.jmix.core.datastore.DataStoreCustomizer;
import io.jmix.core.datastore.DataStoreEventListener;
import io.jmix.core.event.AttributeChanges;
import io.jmix.core.event.EntityChangedEvent;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.data.StoreAwareLocator;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.index.queue.IndexingQueueManager;
import io.jmix.search.index.queue.entity.IndexingQueueItem;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static io.jmix.core.event.EntityChangedEvent.Type.DELETED;
import static io.jmix.core.event.EntityChangedEvent.Type.UPDATED;

@Component("search_EntityTrackingListener")
public class EntityTrackingListener implements DataStoreEventListener, DataStoreCustomizer {

    private static final Logger log = LoggerFactory.getLogger(EntityTrackingListener.class);

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected IndexConfigurationManager indexConfigurationManager;
    @Autowired
    protected UnconstrainedDataManager dataManager;
    @Autowired
    protected IndexingQueueManager indexingQueueManager;
    @Autowired
    protected StoreAwareLocator storeAwareLocator;
    @Autowired
    protected SearchProperties searchProperties;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected DependentEntitiesResolver dependentEntitiesResolver;

    protected Cache<Id<?>, Set<Id<?>>> removalDependencies = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();

    @Autowired
    private DynamicAttributesTracker dynamicAttributesTracker;

    protected enum CheckState {
        OLD,
        NEW
    }

    @Override
    public void customize(DataStore dataStore) {
        if (dataStore instanceof AbstractDataStore) {
            AbstractDataStore abstractStore = (AbstractDataStore) dataStore;
            abstractStore.registerInterceptor(this);
        }
    }

    @Override
    public void beforeEntitySave(DataStoreBeforeEntitySaveEvent event) {
        /*
            This event is used only for resolving indexing entity instances dependent on some removed entity instance.
            Dependencies are found before performing removal because it's required to keep all links between
            instances involved in affected relationship.

            Attempt to load dependencies within processing of EntityChangedEvent requires another transaction to access
            before-removal database state, but it can lead to deadlock on some databases (like MSSQL, HyperSQL) without
            additional configuration.

            Found dependencies are stored into short-term in-memory cache from which they will be retrieved and enqueued
            within processing of EntityChangedEvent.
         */
        if (isChangeTrackingEnabled()) {
            SaveContext saveContext = event.getSaveContext();
            Collection<Object> entitiesToRemove = saveContext.getEntitiesToRemove();
            for (Object entity : entitiesToRemove) {
                if (isRemovedEntityProcessingRequired(entity)) {
                    try {
                        log.trace("Process entity: {}", entity);
                        processRemovedEntity(entity);
                    } catch (Exception e) {
                        log.error("Failed to process entity {}", entity, e);
                    }
                }
            }
        }
    }

    @EventListener
    public void onEntityChangedBeforeCommit(EntityChangedEvent<?> event) {
        if (isEntityChangedEventProcessingRequired(event)) {
            try {
                log.trace("Process event: {}", event);
                processEntityChangedEvent(event);
            } catch (Exception e) {
                log.error("Failed to process event {}", event, e);
            }
        }
    }

    protected void processRemovedEntity(Object removedEntity) {
        Id<?> removedEntityId = Id.of(removedEntity);
        MetaClass metaClass = metadata.getClass(removedEntity);

        Set<Id<?>> dependentEntityIds = dependentEntitiesResolver.getEntityIdsDependentOnRemovedEntity(removedEntityId, metaClass);
        if (!dependentEntityIds.isEmpty()) {
            removalDependencies.put(removedEntityId, dependentEntityIds);
        }
    }

    protected void processEntityChangedEvent(EntityChangedEvent<?> event) {
        Id<?> entityId = event.getEntityId();
        Class<?> entityClass = entityId.getEntityClass();
        MetaClass metaClass = metadata.getClass(entityClass);
        EntityChangedEvent.Type eventType = event.getType();
        String entityName = metaClass.getName();

        AttributeChanges changes = event.getChanges();
        if (indexConfigurationManager.isDirectlyIndexed(entityName)) {
            log.debug("{} is directly indexed", entityId);

            switch (eventType) {
                case CREATED:
                    indexingQueueManager.enqueueIndexByEntityId(entityId);
                    break;
                case UPDATED:
                    if (isUpdateRequired(entityClass, changes)) {
                        indexingQueueManager.enqueueIndexByEntityId(entityId);
                    }
                    break;
                case DELETED:
                    indexingQueueManager.enqueueDeleteByEntityId(entityId);
                    break;
            }
        }

        if (UPDATED == eventType) {
            Set<Id<?>> dependentEntityIds = dependentEntitiesResolver.getEntityIdsDependentOnUpdatedEntity(entityId, metaClass, changes);

            if (!dependentEntityIds.isEmpty()) {
                indexingQueueManager.enqueueIndexCollectionByEntityIds(dependentEntityIds);
            }
        } else if (DELETED == eventType) {
            Set<Id<?>> dependentEntityIds = removalDependencies.getIfPresent(entityId);
            if (CollectionUtils.isNotEmpty(dependentEntityIds)) {
                indexingQueueManager.enqueueIndexCollectionByEntityIds(dependentEntityIds);
                removalDependencies.invalidate(entityId);
            }
        }
    }

    protected boolean isUpdateRequired(Class<?> entityClass, AttributeChanges changes) {
        Set<String> affectedLocalPropertyNames = new HashSet<>(indexConfigurationManager.getLocalPropertyNamesAffectedByUpdate(entityClass));
        if(metadataTools.isSoftDeletable(entityClass)) {
            affectedLocalPropertyNames.add(metadataTools.findDeletedDateProperty(entityClass));
        }
        return changes.getAttributes()
                .stream()
                .anyMatch(affectedLocalPropertyNames::contains);
    }

    protected boolean isChangeTrackingEnabled() {
        return searchProperties.isEnabled() && searchProperties.isChangedEntitiesIndexingEnabled();
    }

    protected boolean isRemovedEntityProcessingRequired(Object entity) {
        MetaClass metaClass = metadata.getClass(entity);
        Class<?> entityClass = metaClass.getJavaClass();
        return isEntityClassCanBeProcessed(entityClass);
    }

    protected boolean isEntityChangedEventProcessingRequired(EntityChangedEvent<?> event) {
        if (!isChangeTrackingEnabled()) {
            return false;
        }
        Class<?> entityClass = event.getEntityId().getEntityClass();
        return isEntityClassCanBeProcessed(entityClass);
    }

    protected boolean isEntityClassCanBeProcessed(Class<?> entityClass) {
        return !IndexingQueueItem.class.equals(entityClass) && indexConfigurationManager.isAffectedEntityClass(entityClass);
    }

}
