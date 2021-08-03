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

import io.jmix.core.Id;
import io.jmix.core.Metadata;
import io.jmix.core.UnconstrainedDataManager;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import java.util.*;
import java.util.stream.Collectors;

@Component("search_EntityTrackingListener")
public class EntityTrackingListener {

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

    protected enum CheckState {
        OLD,
        NEW
    }

    @EventListener
    public void onEntityChangedBeforeCommit(EntityChangedEvent<?> event) {
        try {
            if (isProcessingRequired(event)) {
                log.trace("Process event: {}", event);
                processEvent(event);
            }
        } catch (Exception e) {
            log.error("Failed to process event {}", event, e);
        }
    }

    protected void processEvent(EntityChangedEvent<?> event) {
        Id<?> entityId = event.getEntityId();
        Class<?> entityClass = entityId.getEntityClass();
        MetaClass metaClass = metadata.getClass(entityClass);
        EntityChangedEvent.Type eventType = event.getType();
        String entityName = metaClass.getName();

        if (indexConfigurationManager.isDirectlyIndexed(entityName)) { //todo check dirty fields
            log.debug("{} is directly indexed", entityId);

            switch (eventType) {
                case CREATED:
                case UPDATED:
                    indexingQueueManager.enqueueIndexByEntityId(entityId);
                    break;
                case DELETED:
                    indexingQueueManager.enqueueDeleteByEntityId(entityId);
                    break;
            }
        }

        Object entityInstance = loadInstance(entityId, eventType);

        AttributeChanges changes = event.getChanges();
        Set<Id<?>> dependentEntityIds = getDependentEntityIds(entityInstance, entityClass, changes, eventType);

        if (!dependentEntityIds.isEmpty()) {
            indexingQueueManager.enqueueIndexCollectionByEntityIds(dependentEntityIds);
        }
    }

    protected boolean isProcessingRequired(EntityChangedEvent<?> event) {
        if (!searchProperties.isChangedEntitiesIndexingEnabled()) {
            return false;
        }

        Class<?> entityClass = event.getEntityId().getEntityClass();
        return !IndexingQueueItem.class.equals(entityClass) && indexConfigurationManager.isAffectedEntityClass(entityClass);
    }

    protected Object loadInstance(Id<?> entityId, EntityChangedEvent.Type eventType) {
        // Load last instance state if it has been deleted or actual state otherwise
        // instance will be used in dependencies search later
        boolean joinTransaction = !EntityChangedEvent.Type.DELETED.equals(eventType);
        return dataManager.load(entityId)
                .joinTransaction(joinTransaction)
                .hint(PersistenceHints.SOFT_DELETION, false)
                .one();
    }

    protected Set<Id<?>> getDependentEntityIds(Object entity, Class<?> entityClass, AttributeChanges changes, EntityChangedEvent.Type eventType) {
        Set<Id<?>> dependentEntityIds;
        Map<MetaClass, Set<MetaPropertyPath>> dependenciesMetaData;
        switch (eventType) {
            case CREATED:
            case UPDATED:
                dependenciesMetaData = indexConfigurationManager.getDependenciesMetaDataForUpdate(entityClass, changes.getAttributes());
                return loadDependentEntityIds(entity, dependenciesMetaData, CheckState.NEW);
            case DELETED:
                dependenciesMetaData = indexConfigurationManager.getDependenciesMetaDataForDelete(entityClass);
                return loadDependentEntityIds(entity, dependenciesMetaData, CheckState.OLD);
            default:
                dependentEntityIds = Collections.emptySet();
                break;
        }

        return dependentEntityIds;
    }

    protected Set<Id<?>> loadDependentEntityIds(Object targetInstance, Map<MetaClass, Set<MetaPropertyPath>> dependencyMetaData, CheckState checkState) {
        log.debug("Load dependent entity pks for entity {}: {}", targetInstance, dependencyMetaData);

        Set<Id<?>> result = new HashSet<>();
        for (Map.Entry<MetaClass, Set<MetaPropertyPath>> entry : dependencyMetaData.entrySet()) {
            Set<MetaPropertyPath> properties = entry.getValue();
            if (properties.isEmpty()) {
                continue;
            }
            String entityName = entry.getKey().getName();
            for (MetaPropertyPath propertyPath : properties) {
                log.debug("Load entities '{}' dependent via property '{}'", entityName, propertyPath);
                MetaProperty[] metaProperties = propertyPath.getMetaProperties();
                int currentEntityIndex = 1;
                String currentEntityAlias = "e1";
                StringBuilder currentPropertyPathSb = new StringBuilder(currentEntityAlias);
                StringBuilder querySb = new StringBuilder("select ")
                        .append(currentEntityAlias)
                        .append(" from ")
                        .append(entityName)
                        .append(' ')
                        .append(currentEntityAlias);
                for (int i = 0; i < metaProperties.length; i++) {
                    MetaProperty property = metaProperties[i];
                    currentPropertyPathSb.append('.').append(property.getName());
                    if (i == metaProperties.length - 1) {
                        querySb.append(" where ").append(currentPropertyPathSb).append(" = :ref");
                    } else {
                        boolean oneToMany = property.getAnnotatedElement().isAnnotationPresent(OneToMany.class);
                        boolean manyToMany = property.getAnnotatedElement().isAnnotationPresent(ManyToMany.class);
                        if (oneToMany || manyToMany) {
                            currentEntityIndex++;
                            currentEntityAlias = "e" + currentEntityIndex;
                            querySb.append(" join ").append(currentPropertyPathSb).append(' ').append(currentEntityAlias);
                            currentPropertyPathSb = new StringBuilder(currentEntityAlias);
                        }
                    }
                }
                String queryString = querySb.toString();
                log.debug("Query String: {}", queryString);

                MetaClass metaClass = entry.getKey();

                List<Id<?>> refObjectIds;
                switch (checkState) {
                    case OLD:
                        refObjectIds = loadDependentEntityIds(metaClass, queryString, targetInstance, false);
                        break;
                    case NEW:
                        refObjectIds = loadDependentEntityIds(metaClass, queryString, targetInstance, true);
                        break;
                    default:
                        refObjectIds = Collections.emptyList();
                }

                log.debug("Loaded primary keys of dependent references ({}): {}", refObjectIds.size(), refObjectIds);
                result.addAll(refObjectIds);
            }
        }
        return result;
    }

    protected List<Id<?>> loadDependentEntityIds(MetaClass loadedEntity, String queryString, Object targetInstance, boolean joinTransaction) {
        return dataManager.load(loadedEntity.getJavaClass())
                .query(queryString)
                .parameter("ref", targetInstance)
                .hint(PersistenceHints.SOFT_DELETION, false)
                .joinTransaction(joinTransaction)
                .list()
                .stream()
                .map(Id::of)
                .collect(Collectors.toList());
    }
}
