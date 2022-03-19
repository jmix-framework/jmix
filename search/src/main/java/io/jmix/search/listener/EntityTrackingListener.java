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
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.data.PersistenceHints;
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

import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    protected Cache<Id<?>, Set<Id<?>>> removalDependencies = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();

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

        Set<Id<?>> dependentEntityIds = getEntityIdsDependentOnRemovedEntity(removedEntityId, metaClass);
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

        if (EntityChangedEvent.Type.UPDATED.equals(eventType)) {
            Set<Id<?>> dependentEntityIds = getEntityIdsDependentOnUpdatedEntity(entityId, metaClass, changes);

            if (!dependentEntityIds.isEmpty()) {
                indexingQueueManager.enqueueIndexCollectionByEntityIds(dependentEntityIds);
            }
        } else if (EntityChangedEvent.Type.DELETED.equals(eventType)) {
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
        return searchProperties.isChangedEntitiesIndexingEnabled();
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

    protected Set<Id<?>> getEntityIdsDependentOnUpdatedEntity(Id<?> updatedEntityId, MetaClass metaClass, AttributeChanges changes) {
        Class<?> entityClass = updatedEntityId.getEntityClass();
        Map<MetaClass, Set<MetaPropertyPath>> dependenciesMetaData;
        dependenciesMetaData = indexConfigurationManager.getDependenciesMetaDataForUpdate(entityClass, changes.getAttributes());
        return loadDependentEntityIds(updatedEntityId, metaClass, dependenciesMetaData);
    }

    protected Set<Id<?>> getEntityIdsDependentOnRemovedEntity(Id<?> removedEntityId, MetaClass metaClass) {
        Class<?> entityClass = removedEntityId.getEntityClass();
        Map<MetaClass, Set<MetaPropertyPath>> dependenciesMetaData;
        dependenciesMetaData = indexConfigurationManager.getDependenciesMetaDataForDelete(entityClass);
        return loadDependentEntityIds(removedEntityId, metaClass, dependenciesMetaData);
    }

    protected Set<Id<?>> loadDependentEntityIds(Id<?> targetEntityId,
                                                MetaClass targetMetaClass,
                                                Map<MetaClass, Set<MetaPropertyPath>> dependencyMetaData) {
        log.debug("Load dependent entity pks for entity {}: {}", targetEntityId, dependencyMetaData);

        Set<Id<?>> result = new HashSet<>();
        for (Map.Entry<MetaClass, Set<MetaPropertyPath>> entry : dependencyMetaData.entrySet()) {
            Set<MetaPropertyPath> properties = entry.getValue();
            if (properties.isEmpty()) {
                continue;
            }

            MetaClass metaClass = entry.getKey();
            String entityName = metaClass.getName();
            for (MetaPropertyPath propertyPath : properties) {
                log.debug("Load entities '{}' dependent via property '{}'", entityName, propertyPath);

                DependentEntitiesQuery dependentEntitiesQuery = new DependentEntitiesQueryBuilder()
                        .loadEntity(entityName)
                        .byProperty(propertyPath)
                        .dependedOn(targetMetaClass, targetEntityId)
                        .buildQuery();
                log.debug("{}", dependentEntitiesQuery);

                List<Id<?>> refObjectIds = performLoadingDependentEntityIds(metaClass, dependentEntitiesQuery);
                log.debug("Loaded primary keys of dependent references ({}): {}", refObjectIds.size(), refObjectIds);
                result.addAll(refObjectIds);
            }
        }

        return result;
    }

    protected List<Id<?>> performLoadingDependentEntityIds(MetaClass metaClass, DependentEntitiesQuery dependentEntitiesQuery) {
        return dataManager.load(metaClass.getJavaClass())
                .query(dependentEntitiesQuery.getQuery())
                .parameters(dependentEntitiesQuery.getParameters())
                .hint(PersistenceHints.SOFT_DELETION, false)
                .joinTransaction(true)
                .list()
                .stream()
                .map(Id::of)
                .collect(Collectors.toList());
    }

    private class DependentEntitiesQueryBuilder {

        private String entityName;
        private MetaPropertyPath propertyPath;
        private MetaClass targetMetaClass;
        private Id<?> targetEntityId;

        private int currentEntityIndex;
        private String currentEntityAlias;
        private StringBuilder currentPropertyPathSb;
        private StringBuilder querySb;
        private int propertiesLevels;
        private MetaProperty currentLevelProperty;
        private int currentLevelPropertyIndex;
        private String targetPrimaryKeyName;

        private Map<String, Object> parameters;

        protected DependentEntitiesQueryBuilder loadEntity(String entityName) {
            this.entityName = entityName;
            return this;
        }

        protected DependentEntitiesQueryBuilder byProperty(MetaPropertyPath propertyPath) {
            this.propertyPath = propertyPath;
            return this;
        }

        protected DependentEntitiesQueryBuilder dependedOn(MetaClass metaClass, Id<?> entityId) {
            this.targetMetaClass = metaClass;
            this.targetEntityId = entityId;
            return this;
        }

        protected DependentEntitiesQuery buildQuery() {
            initQuery();
            processProperties();
            return new DependentEntitiesQuery(querySb.toString(), parameters);
        }

        private void initQuery() {
            parameters = new HashMap<>();
            currentEntityIndex = 1;
            currentEntityAlias = "e1";
            initPropertyPathStringBuilderForCurrentEntity();
            querySb = new StringBuilder("select ")
                    .append(currentEntityAlias)
                    .append(" from ")
                    .append(entityName)
                    .append(' ')
                    .append(currentEntityAlias);
        }

        private void processProperties() {
            targetPrimaryKeyName = metadataTools.getPrimaryKeyName(targetMetaClass);
            MetaProperty[] metaProperties = propertyPath.getMetaProperties();
            propertiesLevels = metaProperties.length;
            currentLevelPropertyIndex = 0;
            Stream.of(metaProperties).forEach(this::processPropertyLevel);
        }

        private void processPropertyLevel(MetaProperty property) {
            currentLevelProperty = property;

            appendCurrentLevelProperty();
            if (isJoinRequired(property)) {
                joinWithNextEntity();
                initPropertyPathStringBuilderForCurrentEntity();
            }
            if (isLastLevelProperty()) {
                appendWhereBlock();
            }

            currentLevelPropertyIndex++;
        }

        private boolean isLastLevelProperty() {
            return currentLevelPropertyIndex == propertiesLevels - 1;
        }

        private boolean isJoinRequired(MetaProperty property) {
            boolean oneToMany = property.getAnnotatedElement().isAnnotationPresent(OneToMany.class);
            boolean manyToMany = property.getAnnotatedElement().isAnnotationPresent(ManyToMany.class);
            return oneToMany || manyToMany;
        }

        private void appendCurrentLevelProperty() {
            currentPropertyPathSb.append('.').append(currentLevelProperty.getName());
        }

        private void joinWithNextEntity() {
            currentEntityIndex++;
            currentEntityAlias = "e" + currentEntityIndex;
            querySb.append(" join ").append(currentPropertyPathSb).append(' ').append(currentEntityAlias);
        }

        private void initPropertyPathStringBuilderForCurrentEntity() {
            currentPropertyPathSb = new StringBuilder(currentEntityAlias);
        }

        private void appendWhereBlock() {
            querySb.append(" where ").append(currentPropertyPathSb).append('.').append(targetPrimaryKeyName).append(" = :ref");
            parameters.put("ref", targetEntityId.getValue());
        }
    }

    private static class DependentEntitiesQuery {
        private final String query;
        private final Map<String, Object> parameters;

        private DependentEntitiesQuery(String query, Map<String, Object> parameters) {
            this.query = query;
            this.parameters = parameters;
        }

        public String getQuery() {
            return query;
        }

        public Map<String, Object> getParameters() {
            return parameters;
        }

        @Override
        public String toString() {
            return "DependentEntitiesQuery{" +
                    "query='" + query + '\'' +
                    ", parameters=" + parameters +
                    '}';
        }
    }
}
