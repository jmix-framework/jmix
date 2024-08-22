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

import io.jmix.core.*;
import io.jmix.core.datastore.AbstractDataStore;
import io.jmix.core.entity.EntityPropertyChangeEvent;
import io.jmix.core.entity.EntityPropertyChangeListener;
import io.jmix.core.entity.EntitySystemAccess;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.restds.annotation.RestDataStoreEntity;
import io.jmix.restds.auth.RestAuthenticator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("UnnecessaryLocalVariable")
@Component("restds_RestDataStore")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RestDataStore extends AbstractDataStore {

    private final ApplicationContext applicationContext;
    private final RestSerialization restSerialization;
    private final RestFilterBuilder restFilterBuilder;
    private final RestEntityEventManager entityEventManager;
    private final RestSaveContextProcessor saveContextProcessor;
    private final FetchPlanRepository fetchPlanRepository;

    protected String storeName;

    private RestInvoker restInvoker;

    public RestDataStore(ApplicationContext applicationContext, RestSerialization restSerialization, RestFilterBuilder restFilterBuilder,
                         RestEntityEventManager entityEventManager, RestSaveContextProcessor saveContextProcessor, FetchPlanRepository fetchPlanRepository) {
        this.applicationContext = applicationContext;
        this.restSerialization = restSerialization;
        this.restFilterBuilder = restFilterBuilder;
        this.entityEventManager = entityEventManager;
        this.saveContextProcessor = saveContextProcessor;
        this.fetchPlanRepository = fetchPlanRepository;
    }

    public RestInvoker getRestInvoker() {
        return restInvoker;
    }

    @Override
    @Nullable
    protected Object loadOne(LoadContext<?> context) {
        Object id = context.getId();
        if (id == null) {
            throw new IllegalArgumentException("Id is null");
        }
        Class<Object> entityClass = context.getEntityMetaClass().getJavaClass();
        String entityName = getEntityName(context.getEntityMetaClass());
        String fetchPlan = getFetchPlan(context);

        RestInvoker.LoadParams params = new RestInvoker.LoadParams(entityName, id, fetchPlan);
        String json = restInvoker.load(params);
        Object entity = restSerialization.fromJson(json, entityClass);

        if (entity != null) {
            updateEntityState(entity, fetchPlan);
            entityEventManager.publishEntityLoadingEvent(entity);
        }
        return entity;
    }

    @Override
    protected List<Object> loadAll(LoadContext<?> context) {
        Class<Object> entityClass = context.getEntityMetaClass().getJavaClass();
        String entityName = getEntityName(context.getEntityMetaClass());
        String fetchPlan = getFetchPlan(context);

        RestInvoker.LoadListParams params = new RestInvoker.LoadListParams(entityName,
                getMaxResults(context.getQuery()),
                getFirstResult(context.getQuery()),
                createRestSort(context.getQuery()),
                createRestFilter(context),
                fetchPlan);
        String json = restInvoker.loadList(params);
        List<Object> entities = restSerialization.fromJsonCollection(json, entityClass);

        for (Object entity : entities) {
            updateEntityState(entity, fetchPlan);
            entityEventManager.publishEntityLoadingEvent(entity);
        }
        return entities;
    }

    private void updateEntityState(Object entity, @Nullable String fetchPlanName) {
        MetaClass metaClass = metadata.getClass(entity);
        FetchPlan fetchPlan = fetchPlanName == null ?
            fetchPlanRepository.getFetchPlan(metaClass, FetchPlan.BASE) :
            fetchPlanRepository.getFetchPlan(metaClass, fetchPlanName);

        updateEntityStateRecursive(entity, fetchPlan, new HashSet<>());
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

        if (fetchPlan != null) {
            Set<String> loadedProperties = fetchPlan.getProperties().stream()
                    .map(FetchPlanProperty::getName)
                    .collect(Collectors.toCollection(HashSet::new));
            EntityEntry entityEntry = EntitySystemAccess.getEntityEntry(entity);
            entityEntry.setLoadedProperties(loadedProperties);
            entityEntry.addPropertyChangeListener(new UpdatingLoadedPropertiesListener(), false);
        }

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
            }
        }
    }

    private String getEntityName(MetaClass localMetaClass) {
        String restEntityName = (String) metadataTools.getMetaAnnotationAttributes(localMetaClass.getAnnotations(), RestDataStoreEntity.class)
                .get("remoteName");
        return restEntityName != null ? restEntityName : localMetaClass.getName();
    }

    @Nullable
    private String getFetchPlan(LoadContext<?> context) {
        String fetchPlan = context.getFetchPlan() == null ?
                null : StringUtils.defaultIfEmpty(context.getFetchPlan().getName(), null);
        return fetchPlan;
    }

    private int getMaxResults(@Nullable LoadContext.Query query) {
        return query == null ? 0 : query.getMaxResults();
    }

    private int getFirstResult(@Nullable LoadContext.Query query) {
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
    private String createRestSort(@Nullable LoadContext.Query query) {
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
        String entityName = context.getEntityMetaClass().getName();
        long count = restInvoker.count(entityName, createRestFilter(context));
        return count;
    }

    @Override
    protected Set<Object> saveAll(SaveContext context) {
        Set<Object> saved = new HashSet<>();
        saveContextProcessor.normalizeCompositionItems(context);
        for (Object entity : context.getEntitiesToSave()) {
            String entityName = getEntityName(metadata.getClass(entity));
            FetchPlan fetchPlan = null;
            String savedEntityJson;
            boolean isNew = entityStates.isNew(entity);
            if (isNew) {
                entityEventManager.publishEntitySavingEvent(entity, true);
                String entityJson = restSerialization.toJson(entity, true);
                savedEntityJson = restInvoker.create(entityName, entityJson);
            } else {
                Object id = EntityValues.getId(entity);
                if (id == null) {
                    throw new IllegalArgumentException("Entity id is null for " + entity);
                }
                entityEventManager.publishEntitySavingEvent(entity, false);

                String entityJson = restSerialization.toJson(entity, false);
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
        return null;
    }

    @Override
    protected Object beginSaveTransaction(boolean joinTransaction) {
        return null;
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
        this.restInvoker = applicationContext.getBean(RestInvoker.class, storeName, getRestAuthenticator());
    }

    private RestAuthenticator getRestAuthenticator() {
        return applicationContext.getBean(RestAuthenticator.class);
    }

    protected static class DummyTransactionContextState implements TransactionContextState {
    }

    private static class UpdatingLoadedPropertiesListener implements EntityPropertyChangeListener, Serializable {
        @Override
        public void propertyChanged(EntityPropertyChangeEvent event) {
            String property = event.getProperty();
            Set<String> loadedProperties = EntitySystemAccess.getEntityEntry(event.getItem()).getLoadedProperties();
            if (loadedProperties != null) {
                loadedProperties.add(property);
            }
        }
    }
}
