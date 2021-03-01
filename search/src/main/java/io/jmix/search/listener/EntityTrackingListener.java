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
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.security.EntityOp;
import io.jmix.search.index.mapping.AnnotatedIndexDefinitionsProvider;
import io.jmix.search.index.queue.QueueItem;
import io.jmix.search.index.queue.QueueService;
import io.jmix.search.utils.PropertyTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component(EntityTrackingListener.NAME)
public class EntityTrackingListener {

    private static final Logger log = LoggerFactory.getLogger(EntityTrackingListener.class);

    public static final String NAME = "search_EntityTrackingListener";

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected AnnotatedIndexDefinitionsProvider indexDefinitionsProvider;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected QueueService queueService;
    @Autowired
    protected PropertyTools propertyTools;

    @EventListener
    public void onEntityChangedBeforeCommit(EntityChangedEvent<?> event) {
        try {
            Id<?> entityId = event.getEntityId();
            Class<?> entityClass = entityId.getEntityClass();
            MetaClass metaClass = metadata.getClass(entityClass);

            if (isProcessingRequired(entityClass)) {
                EntityOp entityOperation = resolveEntityOperation(event);
                String entityName = metaClass.getName();
                String primaryKeyPropertyName = propertyTools.getPrimaryKeyPropertyNameForSearch(metaClass);
                Object entity = dataManager.load(entityId)
                        .fetchPlanProperties(primaryKeyPropertyName)
                        .softDeletion(false)
                        .one();

                if (indexDefinitionsProvider.isDirectlyIndexed(entityName)) { //todo check dirty fields
                    log.debug("{} is directly indexed", entityId);
                    Optional<String> primaryKey = getPrimaryKey(metaClass, entity);
                    log.debug("Primary Key of tracked entity: {}", primaryKey);
                    primaryKey.ifPresent(pk -> queueService.enqueue(metaClass, pk, entityOperation));
                }

                Map<MetaClass, Set<String>> dependentEntityPks;
                switch (entityOperation) {
                    case CREATE:
                    case UPDATE:
                        AttributeChanges changes = event.getChanges();
                        dependentEntityPks = getDependentEntityPksForUpdate(entity, entityClass, changes);
                        log.debug("Dependent entities for Create/Update: {}", dependentEntityPks);
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
        } catch(Exception e){
            log.error("Failed to enqueue data for entity {} and change type '{}'", event.getEntityId(), event.getType(), e);
        }
    }

    protected boolean isProcessingRequired(Class<?> entityClass) {
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

    //todo improve loading
    protected Map<MetaClass, Set<String>> loadDependentEntityPks(Object entity, Map<MetaClass, Set<MetaPropertyPath>> dependencyMetaData) {
        log.debug("Load dependent entity pks for entity {}: {}", entity, dependencyMetaData);

        Map<MetaClass, Set<String>> result = new HashMap<>();
        for(Map.Entry<MetaClass, Set<MetaPropertyPath>> entry : dependencyMetaData.entrySet()) {
            Set<MetaPropertyPath> properties = entry.getValue();
            if(properties.isEmpty()) {
                continue;
            }
            Set<String> entityPks = new HashSet<>();
            for(MetaPropertyPath property : properties) {
                MetaClass metaClass = entry.getKey();
                String primaryKeyPropertyName = propertyTools.getPrimaryKeyPropertyNameForSearch(metaClass);
                List<String> refObjectPks = dataManager.load(metaClass.getJavaClass())
                        .query("select e from " + entry.getKey().getName() + " e where e." + property.toPathString() + " = :refObject")
                        .parameter("refObject", entity)
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
        String primaryKeyPropertyName = propertyTools.getPrimaryKeyPropertyNameForSearch(metaClass);
        return Optional.ofNullable(EntityValues.getValue(entity, primaryKeyPropertyName)).map(Object::toString);
    }
}
