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

import io.jmix.core.LoadContext;
import io.jmix.core.SaveContext;
import io.jmix.core.Sort;
import io.jmix.core.ValueLoadContext;
import io.jmix.core.datastore.AbstractDataStore;
import io.jmix.core.metamodel.model.MetaClass;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("UnnecessaryLocalVariable")
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GenericRestDataStore extends AbstractDataStore {

    private final ApplicationContext applicationContext;
    private final GenericRestFilterBuilder restFilterBuilder;

    protected String storeName;

    private GenericRestClient client;

    public GenericRestDataStore(ApplicationContext applicationContext, GenericRestFilterBuilder restFilterBuilder) {
        this.applicationContext = applicationContext;
        this.restFilterBuilder = restFilterBuilder;
    }

    @Override
    @Nullable
    protected Object loadOne(LoadContext<?> context) {
        Object id = context.getId();
        String entityName = context.getEntityMetaClass().getName();
        Class<Object> entityClass = context.getEntityMetaClass().getJavaClass();
        Object entity = client.load(entityName, entityClass, id);
        if (entity != null) {
            entityStates.setNew(entity, false);
        }
        return entity;
    }

    @Override
    protected List<Object> loadAll(LoadContext<?> context) {
        String entityName = context.getEntityMetaClass().getName();
        Class<Object> entityClass = context.getEntityMetaClass().getJavaClass();
        List<Object> entities = client.loadList(entityName, entityClass,
                context.getQuery().getMaxResults(), context.getQuery().getFirstResult(),
                createRestSort(context.getQuery().getSort()),
                createRestFilter(context.getQuery()));
        for (Object entity : entities) {
            entityStates.setNew(entity, false);
        }
        return entities;
    }

    @Nullable
    private String createRestFilter(@Nullable LoadContext.Query query) {
        if (query == null || query.getCondition() == null)
            return null;
        else
            return restFilterBuilder.build(query.getCondition());
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
        long count = client.count(entityName, createRestFilter(context.getQuery()));
        return count;
    }

    @Override
    protected Set<Object> saveAll(SaveContext context) {
        Set<Object> saved = new HashSet<>();
        for (Object entity : context.getEntitiesToSave()) {
            MetaClass metaClass = metadata.getClass(entity);
            Object savedEntity;
            if (entityStates.isNew(entity)) {
                savedEntity = client.create(metaClass.getName(), entity);
            } else {
                savedEntity = client.update(metaClass.getName(), entity);
            }
            entityStates.setNew(savedEntity, false);
            saved.add(savedEntity);
        }
        return saved;
    }

    @Override
    protected Set<Object> deleteAll(SaveContext context) {
        Set<Object> saved = new HashSet<>();
        for (Object entity : context.getEntitiesToRemove()) {
            MetaClass metaClass = metadata.getClass(entity);
            client.delete(metaClass.getName(), entity);
            saved.add(entity);
        }
        return saved;
    }

    @Override
    protected List<Object> loadAllValues(ValueLoadContext context) {
        return List.of();
    }

    @Override
    protected long countAllValues(ValueLoadContext context) {
        return 0;
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

        this.client = applicationContext.getBean(GenericRestClient.class, new RestConnectionParams(baseUrl, clientId, clientSecret));
    }

    protected static class DummyTransactionContextState implements TransactionContextState {
    }
}
