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

import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.core.Metadata;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.event.AttributeChanges;
import io.jmix.core.event.EntityChangedEvent;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.security.EntityOp;
import io.jmix.data.StoreAwareLocator;
import io.jmix.search.index.mapping.IndexConfigurationProvider;
import io.jmix.search.index.queue.QueueService;
import io.jmix.search.index.queue.entity.QueueItem;
import io.jmix.search.utils.PropertyTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import java.util.*;
import java.util.stream.Collectors;

@Component(EntityTrackingListener.NAME)
public class EntityTrackingListener {

    private static final Logger log = LoggerFactory.getLogger(EntityTrackingListener.class);

    public static final String NAME = "search_EntityTrackingListener";

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected IndexConfigurationProvider indexDefinitionsProvider;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected QueueService queueService;
    @Autowired
    protected PropertyTools propertyTools;
    @Autowired
    protected StoreAwareLocator storeAwareLocator;

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
        EntityOp entityOperation = resolveEntityOperation(event);
        String entityName = metaClass.getName();
        String primaryKeyPropertyName = propertyTools.getPrimaryKeyPropertyNameForIndex(metaClass);

        Object entity = null;
        if (isTrackedEntityReloadingRequired(event.getType(), entityName)) {
            entity = dataManager.load(entityId)
                    .fetchPlanProperties(primaryKeyPropertyName)
                    //.softDeletion(false)
                    .one();
        }

        if (entity != null && indexDefinitionsProvider.isDirectlyIndexed(entityName)) { //todo check dirty fields
            log.debug("{} is directly indexed", entityId);

            queueService.enqueue(entity, entityOperation);
            //primaryKey.ifPresent(pk -> queueService.enqueue(entity, entityOperation));
        }

        Map<MetaClass, Set<String>> dependentEntityPks;
        switch (entityOperation) {
            case UPDATE:
                AttributeChanges changes = event.getChanges();
                dependentEntityPks = getDependentEntityPksForUpdate(entity, entityClass, changes);
                log.debug("Dependent entities for Update: {}", dependentEntityPks);
                break;
            case DELETE:
                dependentEntityPks = getDependentEntityPksForDelete(entity, entityClass);
                log.debug("Dependent entities for Delete: {}", dependentEntityPks);
                break;
            default:
                dependentEntityPks = Collections.emptyMap();
                break;
        }
        dependentEntityPks.forEach(
                ((dependentEntityClass, primaryKeys) -> queueService.enqueue(dependentEntityClass, primaryKeys, EntityOp.UPDATE))
        );
    }

    protected boolean isProcessingRequired(EntityChangedEvent<?> event) {
        Class<?> entityClass = event.getEntityId().getEntityClass();
        return !QueueItem.class.equals(entityClass) && indexDefinitionsProvider.isAffectedEntityClass(entityClass);
    }

    protected Map<MetaClass, Set<String>> getDependentEntityPksForUpdate(Object entity, Class<?> entityClass, AttributeChanges changes) {
        log.debug("Get dependent entity primary keys for updated entity: {}", entity);
        Map<MetaClass, Set<MetaPropertyPath>> dependencies = indexDefinitionsProvider.getDependenciesMetaDataForUpdate(entityClass, changes.getAttributes());
        return loadDependentEntityPks(entity, dependencies);
    }

    protected Map<MetaClass, Set<String>> getDependentEntityPksForDelete(Object entity, Class<?> entityClass) {
        log.debug("Get dependent entity primary keys for deleted entity: {}", entity);
        Map<MetaClass, Set<MetaPropertyPath>> dependencies = indexDefinitionsProvider.getDependenciesMetaDataForDelete(entityClass);
        return loadDependentEntityPks(entity, dependencies);
    }

    protected Map<MetaClass, Set<String>> loadDependentEntityPks(Object entity, Map<MetaClass, Set<MetaPropertyPath>> dependencyMetaData) {
        log.debug("Load dependent entity pks for entity {}: {}", entity, dependencyMetaData);

        Map<MetaClass, Set<String>> result = new HashMap<>();
        for (Map.Entry<MetaClass, Set<MetaPropertyPath>> entry : dependencyMetaData.entrySet()) {
            Set<MetaPropertyPath> properties = entry.getValue();
            if (properties.isEmpty()) {
                continue;
            }
            Set<String> entityPks = new HashSet<>();
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
                String primaryKeyPropertyName = propertyTools.getPrimaryKeyPropertyNameForIndex(metaClass);
                List<String> refObjectPks = dataManager.load(metaClass.getJavaClass())
                        .query(queryString)
                        .parameter("ref", entity)
                        .fetchPlanProperties(primaryKeyPropertyName)
                        .list()
                        .stream()
                        .map(loadedEntity -> getPrimaryKey(metaClass, loadedEntity))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());

                log.debug("Loaded primary keys of dependent references ({}): {}", refObjectPks.size(), refObjectPks);
                entityPks.addAll(refObjectPks);
            }
            result.put(entry.getKey(), entityPks);
        }
        return result;
    }

    protected EntityOp resolveEntityOperation(EntityChangedEvent<?> event) {
        switch (event.getType()) {
            case CREATED:
                return EntityOp.CREATE;
            case UPDATED:
                return EntityOp.UPDATE;
            case DELETED:
                return EntityOp.DELETE;
            default:
                throw new RuntimeException("Unsupported event type '" + event.getType() + "'");
        }
    }

    protected Optional<String> getPrimaryKey(MetaClass metaClass, Object entity) {
        String primaryKeyPropertyName = propertyTools.getPrimaryKeyPropertyNameForIndex(metaClass);
        return Optional.ofNullable(EntityValues.getValue(entity, primaryKeyPropertyName)).map(Object::toString);
    }

    protected boolean isTrackedEntityReloadingRequired(EntityChangedEvent.Type eventType, String entityName) {
        return indexDefinitionsProvider.isDirectlyIndexed(entityName) || !eventType.equals(EntityChangedEvent.Type.CREATED);
    }
}
