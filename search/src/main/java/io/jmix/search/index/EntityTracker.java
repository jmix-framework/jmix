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
import io.jmix.data.listener.AfterDeleteEntityListener;
import io.jmix.data.listener.AfterInsertEntityListener;
import io.jmix.data.listener.AfterUpdateEntityListener;
import io.jmix.search.index.mapping.AnnotatedIndexDefinitionsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component(EntityTracker.NAME)
public class EntityTracker implements
        AfterInsertEntityListener<Object>,
        AfterUpdateEntityListener<Object>,
        AfterDeleteEntityListener<Object> {

    private static final Logger log = LoggerFactory.getLogger(EntityTracker.class);

    public static final String NAME = "search_EntityTracker";

    @Autowired
    protected EntityIndexer entityIndexer;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected PersistenceTools persistenceTools;

    @Autowired
    protected AnnotatedIndexDefinitionsProvider indexDefinitionsProvider;

    @Autowired
    protected DataManager dataManager;

    @Override
    public void onAfterInsert(Object entity) {
        log.info("[IVGA] Track insertion of entity {}", entity);
        handleEntity(entity, EntityChangeType.CREATE);
    }

    @Override
    public void onAfterUpdate(Object entity) {
        log.info("[IVGA] Track update of entity {}", entity);
        handleEntity(entity, EntityChangeType.UPDATE);
    }

    @Override
    public void onAfterDelete(Object entity) {
        log.info("[IVGA] Track deletion of entity {}", entity);
        handleEntity(entity, EntityChangeType.DELETE);
    }

    protected void handleEntity(Object entity, EntityChangeType entityChangeType) {
        try {
            Set<String> dirtyFields = persistenceTools.getDirtyFields(entity);
            log.info("[IVGA] Dirty fields: {}", dirtyFields);
            MetaClass metaClass = metadata.getClass(entity);
            Class<?> entityClass = metaClass.getJavaClass();

            if (isDirectlyIndexed(entityClass)) { //todo check dirty fields
                log.info("[IVGA] {} is directly indexed", entityClass);
                Object entityId = EntityValues.getId(entity);
                if (entityId == null) {
                    throw new RuntimeException("Unable to index entity with NULL id");
                }
                entityIndexer.indexEntityById(metaClass, entityId, entityChangeType);
            }

            switch (entityChangeType) {
                case UPDATE:
                    Map<MetaClass, Set<Object>> dependentEntitiesForUpdate = getDependentEntitiesForUpdate(entity, entityClass);
                    log.info("[IVGA] Dependent entities for Update: {}", dependentEntitiesForUpdate);
                    //todo index
                    break;
                case DELETE:
                    Map<MetaClass, Set<Object>> dependentEntitiesForDelete = getDependentEntitiesForDelete(entity, entityClass);
                    log.info("[IVGA] Dependent entities for Delete: {}", dependentEntitiesForDelete);
                    //todo index
                    break;
                default:
                    break;

            }
        } catch (Exception e) {
            log.error("[IVGA] Failed to index data for entity {} and change type '{}'", entity, entityChangeType);
        }
    }

    protected boolean isDirectlyIndexed(Class<?> entityClass) {
        return indexDefinitionsProvider.isDirectlyIndexed(entityClass);
    }

    protected Map<MetaClass, Set<Object>> getDependentEntitiesForUpdate(Object entity, Class<?> entityClass) {
        log.info("[IVGA] getDependentEntitiesForUpdate: {} ({})", entity, entityClass);
        Set<String> dirtyFields = persistenceTools.getDirtyFields(entity);
        Map<MetaClass, Set<MetaPropertyPath>> dependencies = indexDefinitionsProvider.getDependenciesMetaDataForUpdate(entityClass, dirtyFields);
        return loadDependentEntities(entity, dependencies);
    }

    protected Map<MetaClass, Set<Object>> getDependentEntitiesForDelete(Object entity, Class<?> entityClass) {
        log.info("[IVGA] getDependentEntitiesForDelete: {} ({})", entity, entityClass);
        Map<MetaClass, Set<MetaPropertyPath>> dependencies = indexDefinitionsProvider.getDependenciesMetaDataForDelete(entityClass);
        return loadDependentEntities(entity, dependencies);
    }

    //todo improve loading
    protected Map<MetaClass, Set<Object>> loadDependentEntities(Object entity, Map<MetaClass, Set<MetaPropertyPath>> dependencyMetaData) {
        log.info("[IVGA] Load Dependencies for entity {}: {}", entity, dependencyMetaData);

        Map<MetaClass, Set<Object>> result = new HashMap<>();
        for(Map.Entry<MetaClass, Set<MetaPropertyPath>> entry : dependencyMetaData.entrySet()) {
            Set<MetaPropertyPath> properties = entry.getValue();
            if(properties.isEmpty()) {
                continue;
            }
            Set<Object> entities = new HashSet<>();
            for(MetaPropertyPath property : properties) {
                LoadContext.Query query = new LoadContext.Query(
                        "select e from " + entry.getKey().getName() + " e where e." + property.toPathString()  + " = :refObject"
                );
                query.setParameter("refObject", entity);
                log.info("[IVGA] Query = {}", query);

                LoadContext<Object> loadContext = new LoadContext<>(entry.getKey());
                loadContext.setQuery(query);

                List<Object> loaded = dataManager.loadList(loadContext);
                log.info("[IVGA] Loaded dependent references = {}", loaded);
                entities.addAll(loaded);
            }
            result.put(entry.getKey(), entities);
        }

        log.info("[IVGA] LoadDependentEntities result = {}", result);
        return result;
    }
}
