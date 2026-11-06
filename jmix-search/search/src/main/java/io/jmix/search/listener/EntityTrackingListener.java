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
import io.jmix.core.datastore.DataStoreEntitySavingEvent;
import io.jmix.core.datastore.DataStoreEventListener;
import io.jmix.core.event.AttributeChanges;
import io.jmix.core.event.EntityChangedEvent;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.data.StoreAwareLocator;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.impl.MultitenancyAdapter;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.index.queue.IndexingQueueManager;
import io.jmix.search.index.queue.entity.IndexingQueueItem;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.Nullable;
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
    protected SearchProperties searchProperties;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected MultitenancyAdapter multitenancyAdapter;
    @Autowired
    protected DependentEntitiesResolver dependentEntitiesResolver;

    protected Cache<Id<?>, Set<Id<?>>> removalDependencies = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();

    /**
     * Tenants of the records being saved, carried from the save to the moment they are enqueued.
     * <p>
     * The enqueueing happens on {@code EntityChangedEvent}, which brings an id and the changed attributes but no
     * instance - and the tenant of a record is not among the changes, because it does not change. Reading the
     * record back would cost a query on the path of the application's own save; taking the tenant off the
     * instance the save already holds costs nothing.
     * <p>
     * Short-lived on purpose: the entry is consumed a moment later, within the same save, and the expiry is a
     * safety net for the case where the change event never arrives.
     */
    protected Cache<Id<?>, String> tenantsOfSavedRecords = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();

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

    /**
     * Remembers the tenant of every record being saved.
     * <p>
     * This hook runs after the entity has been persisted or merged, which matters: the Multitenancy add-on
     * assigns the tenant of a new record while saving it, so at the earlier {@code beforeEntitySave} the
     * attribute of a new record is still empty.
     */
    @Override
    public void entitySaving(DataStoreEntitySavingEvent event) {
        if (!isChangeTrackingEnabled()) {
            return;
        }
        for (Object entity : event.getEntities()) {
            MetaClass metaClass = metadata.getClass(entity);
            if (!indexConfigurationManager.isDirectlyIndexed(metaClass.getName())) {
                continue;
            }
            String tenantId = tenantOfSavedRecord(entity);
            if (tenantId != null) {
                tenantsOfSavedRecords.put(Id.of(entity), tenantId);
            }
        }
    }

    /**
     * @return tenant of the record, or {@code null} if it belongs to none or its tenant cannot be read
     */
    @Nullable
    protected String tenantOfSavedRecord(Object entity) {
        // A fetch plan built in code does not include system properties, and the tenant attribute is one of them,
        // so it can be absent on a record the application loaded narrowly. There is nothing wrong with not
        // knowing: the tenant of an indexing item is worked out from the record when the queue is processed.
        if (!multitenancyAdapter.isTenantIdReadable(entity)) {
            log.debug("Tenant of {} cannot be read, the queue item will not carry it", entity);
            return null;
        }
        return multitenancyAdapter.getTenantIdForInstance(entity);
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
                    indexingQueueManager.enqueueIndexByEntityId(entityId, rememberedTenant(entityId));
                    break;
                case UPDATED:
                    if (isUpdateRequired(entityClass, changes)) {
                        indexingQueueManager.enqueueIndexByEntityId(entityId, rememberedTenant(entityId));
                    }
                    break;
                case DELETED:
                    indexingQueueManager.enqueueDeleteByEntityId(entityId, tenantOfDeletedRecord(metaClass, changes));
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

    /**
     * @return the tenant remembered while the record was being saved, or {@code null} if it was not remembered -
     * the record was saved outside this data store, its tenant attribute was not loaded, or it has none
     */
    @Nullable
    protected String rememberedTenant(Id<?> entityId) {
        String tenantId = tenantsOfSavedRecords.getIfPresent(entityId);
        tenantsOfSavedRecords.invalidate(entityId);
        return tenantId;
    }

    /**
     * Reads the tenant of a record being deleted off the event.
     * <p>
     * The changes of a deletion carry the old values of the record's attributes, so the tenant is available here
     * while the record still exists. It has to be taken now: once the deletion reaches the queue the record is gone
     * and nothing can tell which tenant's index holds its document.
     *
     * @return tenant of the record, or {@code null} if the entity has no tenant attribute or the value is not set
     */
    @Nullable
    protected String tenantOfDeletedRecord(MetaClass metaClass, AttributeChanges changes) {
        MetaProperty tenantProperty = metadataTools.findTenantIdProperty(metaClass);
        if (tenantProperty == null) {
            return null;
        }
        return changes.getOldValue(tenantProperty.getName());
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
