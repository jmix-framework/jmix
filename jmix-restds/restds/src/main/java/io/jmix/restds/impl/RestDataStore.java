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
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

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

    protected String storeName;

    private RestInvoker restInvoker;

    public RestDataStore(ApplicationContext applicationContext, RestSerialization restSerialization, RestFilterBuilder restFilterBuilder,
                         RestEntityEventManager entityEventManager, RestSaveContextProcessor saveContextProcessor) {
        this.applicationContext = applicationContext;
        this.restSerialization = restSerialization;
        this.restFilterBuilder = restFilterBuilder;
        this.entityEventManager = entityEventManager;
        this.saveContextProcessor = saveContextProcessor;
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
        String entityName = context.getEntityMetaClass().getName();
        Class<Object> entityClass = context.getEntityMetaClass().getJavaClass();
        String fetchPlan = extractFetchPlan(context);

        RestInvoker.LoadParams params = new RestInvoker.LoadParams(entityName, id, fetchPlan);
        Object entity = restSerialization.fromJson(
                restInvoker.load(params),
                entityClass);

        if (entity != null) {
            setNotNew(entity);
            entityEventManager.publishEntityLoadingEvent(entity);
        }
        return entity;
    }

    @Override
    protected List<Object> loadAll(LoadContext<?> context) {
        if (context.getQuery() == null) {
            throw new IllegalArgumentException("LoadContext.Query is null");
        }
        String entityName = context.getEntityMetaClass().getName();
        Class<Object> entityClass = context.getEntityMetaClass().getJavaClass();
        String fetchPlan = extractFetchPlan(context);

        RestInvoker.LoadListParams params = new RestInvoker.LoadListParams(entityName,
                context.getQuery().getMaxResults(),
                context.getQuery().getFirstResult(),
                createRestSort(context.getQuery().getSort()),
                createRestFilter(context.getQuery()),
                fetchPlan);
        List<Object> entities = restSerialization.fromJsonCollection(
                restInvoker.loadList(params),
                entityClass);

        for (Object entity : entities) {
            setNotNew(entity);
            entityEventManager.publishEntityLoadingEvent(entity);
        }
        return entities;
    }

    private void setNotNew(Object entity) {
        setNotNewRecursive(entity, new HashSet<>());
    }

    private void setNotNewRecursive(Object entity, Set<Object> visited) {
        if (visited.contains(entity))
            return;
        visited.add(entity);

        entityStates.setNew(entity, false);

        for (MetaProperty property : metadata.getClass(entity).getProperties()) {
            if (property.getRange().isClass()) {
                Object value = EntityValues.getValue(entity, property.getName());
                if (value != null) {
                    if (value instanceof Collection) {
                        for (Object item : ((Collection<?>) value)) {
                            setNotNewRecursive(item, visited);
                        }
                    } else {
                        setNotNewRecursive(value, visited);
                    }
                }
            }
        }
    }

    @Nullable
    private String extractFetchPlan(LoadContext<?> context) {
        String fetchPlan = context.getFetchPlan() == null ?
                null : StringUtils.defaultIfEmpty(context.getFetchPlan().getName(), null);
        return fetchPlan;
    }

    @Nullable
    private String createRestFilter(@Nullable LoadContext.Query query) {
        if (query == null || (query.getQueryString() == null && query.getCondition() == null))
            return null;
        else
            return restFilterBuilder.build(query.getQueryString(), query.getCondition(), query.getParameters());
    }

    @Nullable
    private String createRestSort(@Nullable Sort sort) {
        if (sort == null)
            return null;
        List<Sort.Order> sortOrders = sort.getOrders();
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
        long count = restInvoker.count(entityName, createRestFilter(context.getQuery()));
        return count;
    }

    @Override
    protected Set<Object> saveAll(SaveContext context) {
        Set<Object> saved = new HashSet<>();
        saveContextProcessor.normalizeCompositionItems(context);
        for (Object entity : context.getEntitiesToSave()) {
            MetaClass metaClass = metadata.getClass(entity);
            String entityJson = restSerialization.toJson(entity);
            String savedEntityJson;
            boolean isNew = entityStates.isNew(entity);
            if (isNew) {
                entityEventManager.publishEntitySavingEvent(entity, true);
                savedEntityJson = restInvoker.create(metaClass.getName(), entityJson);
            } else {
                Object id = EntityValues.getId(entity);
                if (id == null) {
                    throw new IllegalArgumentException("Entity id is null for " + entity);
                }
                entityEventManager.publishEntitySavingEvent(entity, false);
                savedEntityJson = restInvoker.update(metaClass.getName(), id.toString(), entityJson);
            }
            Object savedEntity = restSerialization.fromJson(savedEntityJson, entity.getClass());
            if (savedEntity == null) {
                throw new IllegalStateException("Saved entity is null");
            }
            if (isNew && EntityValues.getId(entity) == null) {
                // set new ID to the passed instance to let the framework match the saved instance with the original one
                EntityValues.setId(entity, EntityValues.getId(savedEntity));
            }
            setNotNew(savedEntity);
            entityEventManager.publishEntitySavedEvent(entity, savedEntity, isNew);
            saved.add(savedEntity);
        }
        return saved;
    }

    @Override
    protected Set<Object> deleteAll(SaveContext context) {
        Set<Object> saved = new HashSet<>();
        for (Object entity : context.getEntitiesToRemove()) {
            MetaClass metaClass = metadata.getClass(entity);
            Object id = EntityValues.getId(entity);
            if (id == null) {
                throw new IllegalArgumentException("Entity id is null for " + entity);
            }
            restInvoker.delete(metaClass.getName(), id.toString());
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

        Environment environment = applicationContext.getEnvironment();
        String baseUrl = environment.getRequiredProperty(storeName + ".baseUrl");
        String clientId = environment.getRequiredProperty(storeName + ".clientId");
        String clientSecret = environment.getRequiredProperty(storeName + ".clientSecret");

        this.restInvoker = applicationContext.getBean(RestInvoker.class, new RestConnectionParams(baseUrl, clientId, clientSecret));
    }

    protected static class DummyTransactionContextState implements TransactionContextState {
    }
}
