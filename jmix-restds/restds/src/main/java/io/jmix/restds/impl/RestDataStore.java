/*
 * Copyright 2024 Haulmont.
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

package io.jmix.restds.impl;

import com.google.common.base.Strings;
import io.jmix.core.*;
import io.jmix.core.datastore.AbstractDataStore;
import io.jmix.core.entity.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.restds.annotation.RestDataStoreEntity;
import io.jmix.restds.exception.InvalidFetchPlanException;
import io.jmix.restds.filestorage.RestFileStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DataStore implementation working with entities through generic REST.
 */
@SuppressWarnings("UnnecessaryLocalVariable")
@Component("restds_RestDataStore")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RestDataStore extends AbstractDataStore {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private RestSerialization restSerialization;
    @Autowired
    private RestFilterBuilder restFilterBuilder;
    @Autowired
    private RestEntityEventManager entityEventManager;
    @Autowired
    private RestSaveContextProcessor saveContextProcessor;
    @Autowired
    private FetchPlanRepository fetchPlanRepository;
    @Autowired
    private RestDsLoadedPropertiesInfoFactory loadedPropertiesInfoFactory;
    @Autowired
    private FetchPlanSerialization fetchPlanSerialization;
    @Autowired
    private FileStorageLocator fileStorageLocator;

    private String storeName;

    private RestInvoker restInvoker;

    private RestCapabilities restCapabilities;

    public RestInvoker getRestInvoker() {
        return restInvoker;
    }

    @Override
    @Nullable
    protected Object loadOne(LoadContext<?> context) {
        Object id = context.getId();
        Class<Object> entityClass = context.getEntityMetaClass().getJavaClass();
        String entityName = getEntityName(context.getEntityMetaClass());
        String fetchPlan = getFetchPlanNameOrJson(context.getFetchPlan());
        Object entity = null;
        if (id != null) {
            RestInvoker.LoadParams params = new RestInvoker.LoadParams(entityName, id, fetchPlan);
            String json = restInvoker.load(params);
            entity = restSerialization.fromJson(json, entityClass);
        } else {
            RestInvoker.LoadListParams params = new RestInvoker.LoadListParams(entityName,
                    1,
                    getFirstResult(context.getQuery()),
                    createRestSort(context.getQuery()),
                    createRestFilter(context),
                    fetchPlan);
            String json = restInvoker.loadList(params);
            List<Object> entities = restSerialization.fromJsonCollection(json, entityClass);
            if (!entities.isEmpty()) {
                entity = entities.get(0);
            }
        }
        if (entity != null) {
            updateEntityState(entity, context.getFetchPlan());
            entityEventManager.publishEntityLoadingEvent(entity);
        }
        return entity;
    }

    @Override
    protected List<Object> loadAll(LoadContext<?> context) {
        Class<Object> entityClass = context.getEntityMetaClass().getJavaClass();
        String entityName = getEntityName(context.getEntityMetaClass());
        String fetchPlan = getFetchPlanNameOrJson(context.getFetchPlan());

        RestInvoker.LoadListParams params = new RestInvoker.LoadListParams(entityName,
                getMaxResults(context.getQuery()),
                getFirstResult(context.getQuery()),
                createRestSort(context.getQuery()),
                createRestFilter(context),
                fetchPlan);
        String json = restInvoker.loadList(params);
        List<Object> entities = restSerialization.fromJsonCollection(json, entityClass);

        for (Object entity : entities) {
            updateEntityState(entity, context.getFetchPlan());
            entityEventManager.publishEntityLoadingEvent(entity);
        }
        return entities;
    }

    private void updateEntityState(Object entity, @Nullable FetchPlan fetchPlan) {
        if (fetchPlan == null) {
            fetchPlan = fetchPlanRepository.getFetchPlan(metadata.getClass(entity), FetchPlan.BASE);
        }
        updateEntityStateRecursive(entity, fetchPlan, new HashSet<>());
    }

    private void updateEntityStateRecursive(Object entity, @Nullable FetchPlan fetchPlan, Set<Object> visited) {
        if (visited.contains(entity))
            return;
        visited.add(entity);

        entityStates.setNew(entity, false);

        MetaClass metaClass = metadata.getClass(entity);

        for (MetaProperty property : metaClass.getProperties()) {
            if (property.getRange().isClass()) {
                Object value = EntityValues.getValue(entity, property.getName());
                if (value != null) {
                    FetchPlan valueFetchPlan = null;
                    if (fetchPlan != null) {
                        FetchPlanProperty valueFetchPlanProp = fetchPlan.getProperty(property.getName());
                        valueFetchPlan = valueFetchPlanProp == null ? null : valueFetchPlanProp.getFetchPlan();
                    }

                    if (value instanceof Collection) {
                        for (Object item : ((Collection<?>) value)) {
                            updateEntityStateRecursive(item, valueFetchPlan, visited);
                        }
                    } else {
                        updateEntityStateRecursive(value, valueFetchPlan, visited);
                    }
                }
            } else if (property.getRange().isDatatype()
                    && FileRef.class.isAssignableFrom(property.getRange().asDatatype().getJavaClass())) {
                // Replace remote file storage name with the corresponding RestFileStorage name in FileRef objects
                FileRef remoteFileRef = EntityValues.getValue(entity, property.getName());
                if (remoteFileRef != null) {
                    FileRef fileRef = new FileRef(convertFromRemoteFileStorage(remoteFileRef.getStorageName()),
                            remoteFileRef.getPath(), remoteFileRef.getFileName(), remoteFileRef.getParameters());
                    EntityValues.setValue(entity, property.getName(), fileRef);
                }
            }
        }

        if (fetchPlan != null) {
            LoadedPropertiesInfo loadedPropertiesInfo = loadedPropertiesInfoFactory.create();
            for (FetchPlanProperty fetchPlanProperty : fetchPlan.getProperties()) {
                loadedPropertiesInfo.registerProperty(fetchPlanProperty.getName(), true);
            }
            EntityEntry entityEntry = EntitySystemAccess.getEntityEntry(entity);
            entityEntry.setLoadedPropertiesInfo(loadedPropertiesInfo);
            entityEntry.addPropertyChangeListener(new UpdatingLoadedPropertiesListener(), false);
        }
    }

    private String convertFromRemoteFileStorage(String remoteStorageName) {
        for (FileStorage fileStorage : fileStorageLocator.getAll()) {
            if (fileStorage instanceof RestFileStorage rfs && rfs.getRemoteStorageName().equals(remoteStorageName)) {
                return rfs.getStorageName();
            }
        }
        return remoteStorageName;
    }

    private String convertToRemoteFileStorage(String storageName) {
        for (FileStorage fileStorage : fileStorageLocator.getAll()) {
            if (fileStorage instanceof RestFileStorage rfs && rfs.getStorageName().equals(storageName)) {
                return rfs.getRemoteStorageName();
            }
        }
        return storageName;
    }

    private String getEntityName(MetaClass localMetaClass) {
        String restEntityName = (String) metadataTools.getMetaAnnotationAttributes(localMetaClass.getAnnotations(), RestDataStoreEntity.class)
                .get("remoteName");
        return restEntityName != null ? restEntityName : localMetaClass.getName();
    }

    @Nullable
    private String getFetchPlanNameOrJson(@Nullable FetchPlan fetchPlan) {
        if (fetchPlan == null)
            return null;
        else if (Strings.isNullOrEmpty(fetchPlan.getName())) {
            if (restCapabilities.isInlineFetchPlanEnabled()) {
                // optimize URL for frequent case with _base fetch plan
                FetchPlan baseFetchPlan = fetchPlanRepository.getFetchPlan(fetchPlan.getEntityClass(), FetchPlan.BASE);
                if (fetchPlan.contentEquals(baseFetchPlan))
                    return FetchPlan.BASE;
                else
                    return fetchPlanSerialization.toJson(fetchPlan, this::getEntityName);
            } else
                throw new InvalidFetchPlanException(storeName);
        } else {
            return fetchPlan.getName();
        }
    }

    private int getMaxResults(LoadContext.@Nullable Query query) {
        return query == null ? 0 : query.getMaxResults();
    }

    private int getFirstResult(LoadContext.@Nullable Query query) {
        return query == null ? 0 : query.getFirstResult();
    }

    @Nullable
    private String createRestFilter(LoadContext<?> context) {
        if (!context.getIds().isEmpty()) {
            return restFilterBuilder.build(context.getEntityMetaClass(), context.getIds());
        } else {
            LoadContext.Query query = context.getQuery();
            if (query == null || (query.getQueryString() == null && query.getCondition() == null))
                return null;
            else
                return restFilterBuilder.build(query.getQueryString(), query.getCondition(), query.getParameters());
        }
    }

    @Nullable
    private String createRestSort(LoadContext.@Nullable Query query) {
        if (query == null || query.getSort() == null)
            return null;
        List<Sort.Order> sortOrders = query.getSort().getOrders();
        if (sortOrders.isEmpty())
            return null;
        return sortOrders.stream()
                .map(order ->
                        (order.getDirection() == Sort.Direction.DESC ? "-" : "") + order.getProperty())
                .collect(Collectors.joining(","));
    }

    @Override
    protected long countAll(LoadContext<?> context) {
        String entityName = getEntityName(context.getEntityMetaClass());
        long count = restInvoker.count(entityName, createRestFilter(context));
        return count;
    }

    @Override
    protected Set<Object> saveAll(SaveContext context) {
        Set<Object> saved = new HashSet<>();
        Set<FileRef> fileRefs = saveContextProcessor.process(context);
        for (Object entity : context.getEntitiesToSave()) {
            String entityName = getEntityName(metadata.getClass(entity));
            FetchPlan fetchPlan = null;
            String savedEntityJson;
            boolean isNew = entityStates.isNew(entity);
            if (isNew) {
                entityEventManager.publishEntitySavingEvent(entity, true);
                String entityJson = serializeToJson(entity, true, fileRefs);
                savedEntityJson = restInvoker.create(entityName, entityJson);
            } else {
                Object id = EntityValues.getId(entity);
                if (id == null) {
                    throw new IllegalArgumentException("Entity id is null for " + entity);
                }
                entityEventManager.publishEntitySavingEvent(entity, false);

                String entityJson = serializeToJson(entity, false, fileRefs);
                savedEntityJson = restInvoker.update(entityName, id.toString(), entityJson);
            }
            Object savedEntity = restSerialization.fromJson(savedEntityJson, entity.getClass());
            if (savedEntity == null) {
                throw new IllegalStateException("Saved entity is null");
            }
            if (isNew && EntityValues.getId(entity) == null) {
                // set new ID to the passed instance to let the framework match the saved instance with the original one
                EntityValues.setId(entity, EntityValues.getId(savedEntity));
            }
            updateEntityState(savedEntity, fetchPlan);
            entityEventManager.publishEntitySavedEvent(entity, savedEntity, isNew);
            saved.add(savedEntity);
        }
        return saved;
    }

    private String serializeToJson(Object entity, boolean isNew, Set<FileRef> fileRefs) {
        String json = restSerialization.toJson(entity, isNew);
        if (fileRefs.isEmpty()) {
            return json;
        } else {
            // Replace file storage name in FileRef objects. This cannot be done in Java objects because then they
            // would be modified which could affect the calling code.
            // Using simple string replace instead of JSON/Java transformations for better performance.
            StringBuilder result = new StringBuilder(json);
            for (FileRef fileRef : fileRefs) {
                String storageName = fileRef.getStorageName();
                String remoteStorageName = convertToRemoteFileStorage(storageName);
                if (!remoteStorageName.equals(storageName)) {
                    int idx;
                    while ((idx = result.indexOf(storageName)) != -1) {
                        result.replace(idx, idx + storageName.length(), remoteStorageName);
                    }
                }
            }
            return result.toString();
        }
    }

    @Override
    protected Set<Object> deleteAll(SaveContext context) {
        Set<Object> saved = new HashSet<>();
        for (Object entity : context.getEntitiesToRemove()) {
            String entityName = getEntityName(metadata.getClass(entity));
            Object id = EntityValues.getId(entity);
            if (id == null) {
                throw new IllegalArgumentException("Entity id is null for " + entity);
            }
            restInvoker.delete(entityName, id.toString());
            entityEventManager.publishEntityRemovedEvent(entity);
            saved.add(entity);
        }
        return saved;
    }

    @Override
    protected List<Object> loadAllValues(ValueLoadContext context) {
        throw new UnsupportedOperationException("Loading scalar values is not supported");
    }

    @Override
    protected long countAllValues(ValueLoadContext context) {
        throw new UnsupportedOperationException("Loading scalar values is not supported");
    }

    @Override
    protected Object beginLoadTransaction(boolean joinTransaction) {
        return new Object();
    }

    @Override
    protected Object beginSaveTransaction(boolean joinTransaction) {
        return new Object();
    }

    @Override
    protected void commitTransaction(Object transaction) {

    }

    @Override
    protected void rollbackTransaction(Object transaction) {

    }

    @Override
    protected TransactionContextState getTransactionContextState(boolean isJoinTransaction) {
        return new DummyTransactionContextState();
    }

    @Override
    public String getName() {
        return storeName;
    }

    @Override
    public void setName(String name) {
        storeName = name;
        restInvoker = applicationContext.getBean(RestInvoker.class, storeName);
        restCapabilities = new RestCapabilities(restInvoker);
    }

    private static class DummyTransactionContextState implements TransactionContextState {
    }

    private static class UpdatingLoadedPropertiesListener implements EntityPropertyChangeListener, Serializable {
        @Override
        public void propertyChanged(EntityPropertyChangeEvent event) {
            String property = event.getProperty();
            LoadedPropertiesInfo loadedPropertiesInfo = EntitySystemAccess.getEntityEntry(event.getItem()).getLoadedPropertiesInfo();
            if (loadedPropertiesInfo != null) {
                loadedPropertiesInfo.registerProperty(property, true);
            }
        }
    }
}
