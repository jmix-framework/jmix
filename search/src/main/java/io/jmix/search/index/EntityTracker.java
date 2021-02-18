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

package io.jmix.search.index;

import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.data.EntityChangeType;
import io.jmix.data.PersistenceTools;
import io.jmix.data.listener.BeforeDeleteEntityListener;
import io.jmix.data.listener.BeforeInsertEntityListener;
import io.jmix.data.listener.BeforeUpdateEntityListener;
import io.jmix.search.index.mapping.AnnotatedIndexDefinitionsProvider;
import io.jmix.search.index.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@Component(EntityTracker.NAME)
public class EntityTracker implements
        BeforeInsertEntityListener<Object>,
        BeforeUpdateEntityListener<Object>,
        BeforeDeleteEntityListener<Object> {

    private static final Logger log = LoggerFactory.getLogger(EntityTracker.class);

    public static final String NAME = "search_EntityTracker";

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected PersistenceTools persistenceTools;
    @Autowired
    protected AnnotatedIndexDefinitionsProvider indexDefinitionsProvider;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected QueueService queueService;

    @Override
    public void onBeforeInsert(Object entity) {
        log.trace("Track insertion of entity {}", entity);
        handleEntityChange(entity, EntityChangeType.CREATE);
    }

    @Override
    public void onBeforeUpdate(Object entity) {
        log.trace("Track update of entity {}", entity);
        handleEntityChange(entity, EntityChangeType.UPDATE);
    }

    @Override
    public void onBeforeDelete(Object entity) {
        log.trace("Track deletion of entity {}", entity);
        handleEntityChange(entity, EntityChangeType.DELETE);
    }

    protected void handleEntityChange(Object entity, EntityChangeType entityChangeType) {
        try {
            MetaClass metaClass = metadata.getClass(entity);
            Class<?> entityClass = metaClass.getJavaClass();

            if (isDirectlyIndexed(metaClass.getName())) { //todo check dirty fields
                log.info("[IVGA] {} is directly indexed", entityClass);
                String entityId = getEntityIdAsString(entity); //todo use PK property
                queueService.enqueue(metaClass, entityId, entityChangeType);
            }

            Map<MetaClass, Set<String>> dependentEntityIds;
            switch (entityChangeType) {
                case CREATE:
                case UPDATE:
                    dependentEntityIds = getDependentEntityIdsForUpdate(entity, entityClass);
                    log.info("[IVGA] Dependent entities for Create/Update: {}", dependentEntityIds);
                    break;
                case DELETE:
                    dependentEntityIds = getDependentEntityIdsForDelete(entity, entityClass);
                    log.info("[IVGA] Dependent entities for Delete: {}", dependentEntityIds);
                    break;
                default:
                    dependentEntityIds = Collections.emptyMap();
                    break;
            }

            dependentEntityIds.forEach(
                    ((dependentEntityClass, ids) -> queueService.enqueue(dependentEntityClass, ids, EntityChangeType.UPDATE))
            );
        } catch (Exception e) {
            log.error("[IVGA] Failed to enqueue data for entity {} and change type '{}'", entity, entityChangeType, e);
        }
    }

    protected boolean isDirectlyIndexed(String entityName) {
        return indexDefinitionsProvider.isDirectlyIndexed(entityName);
    }

    protected Map<MetaClass, Set<String>> getDependentEntityIdsForUpdate(Object entity, Class<?> entityClass) {
        log.info("[IVGA] getDependentEntityIdsForUpdate: {} ({})", entity, entityClass);
        Set<String> dirtyFields = persistenceTools.getDirtyFields(entity);
        Map<MetaClass, Set<MetaPropertyPath>> dependencies = indexDefinitionsProvider.getDependenciesMetaDataForUpdate(entityClass, dirtyFields);
        return loadDependentEntityIds(entity, dependencies);
    }

    protected Map<MetaClass, Set<String>> getDependentEntityIdsForDelete(Object entity, Class<?> entityClass) {
        log.info("[IVGA] getDependentEntityIdsForDelete: {} ({})", entity, entityClass);
        Map<MetaClass, Set<MetaPropertyPath>> dependencies = indexDefinitionsProvider.getDependenciesMetaDataForDelete(entityClass);
        return loadDependentEntityIds(entity, dependencies);
    }

    //todo improve loading
    protected Map<MetaClass, Set<String>> loadDependentEntityIds(Object entity, Map<MetaClass, Set<MetaPropertyPath>> dependencyMetaData) {
        log.info("[IVGA] Load Dependencies for entity {}: {}", entity, dependencyMetaData);

        Map<MetaClass, Set<String>> result = new HashMap<>();
        for(Map.Entry<MetaClass, Set<MetaPropertyPath>> entry : dependencyMetaData.entrySet()) {
            Set<MetaPropertyPath> properties = entry.getValue();
            if(properties.isEmpty()) {
                continue;
            }
            Set<String> entityIds = new HashSet<>();
            for(MetaPropertyPath property : properties) {
                LoadContext.Query query = new LoadContext.Query(
                        "select e from " + entry.getKey().getName() + " e where e." + property.toPathString()  + " = :refObject"
                );
                query.setParameter("refObject", entity);
                log.info("[IVGA] Query = {}", query);

                LoadContext<Object> loadContext = new LoadContext<>(entry.getKey());
                loadContext.setQuery(query);

                List<String> loadedEntityIds = dataManager.loadList(loadContext).stream()
                        .map(this::getEntityIdAsStringOrNull)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                log.info("[IVGA] Loaded dependent references = {}", loadedEntityIds);
                entityIds.addAll(loadedEntityIds);
            }
            result.put(entry.getKey(), entityIds);
        }

        log.info("[IVGA] LoadDependentEntities result = {}", result);
        return result;
    }

    protected String getEntityIdAsString(Object entity) {
        String id = getEntityIdAsStringOrNull(entity);
        if(id == null) {
            throw new RuntimeException("Entity ID is null");
        }
        return id;
    }

    @Nullable
    protected String getEntityIdAsStringOrNull(Object entity) {
        Object id = EntityValues.getId(entity);
        return id == null ? null : id.toString();
        //todo cases of complex id?
    }
}
