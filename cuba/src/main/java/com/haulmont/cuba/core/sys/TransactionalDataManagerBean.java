/*
 * Copyright 2019 Haulmont.
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

package com.haulmont.cuba.core.sys;

import com.haulmont.cuba.core.TransactionalDataManager;
import com.haulmont.cuba.core.Transactions;
import com.haulmont.cuba.core.entity.contracts.Id;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.EntitySet;
import com.haulmont.cuba.core.global.FluentLoader;
import com.haulmont.cuba.core.global.FluentValueLoader;
import com.haulmont.cuba.core.global.FluentValuesLoader;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.core.global.ValueLoadContext;
import io.jmix.core.EntityStates;
import io.jmix.core.Metadata;
import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.KeyValueEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

@Component(TransactionalDataManager.NAME)
public class TransactionalDataManagerBean implements TransactionalDataManager {

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected Transactions transactions;

    @Autowired
    private Metadata metadata;

    @Autowired
    private EntityStates entityStates;

    @Autowired
    private TransactionalActionFactory transactionalActionFactory;

    @Autowired
    protected ApplicationContext applicationContext;

    @Override
    public <E extends Entity> FluentLoader<E> load(Class<E> entityClass) {
        FluentLoader<E> fluentLoader = applicationContext.getBean(FluentLoader.class, entityClass);
        fluentLoader.setDataManager(dataManager.getDelegate());
        return fluentLoader;
    }

    @Override
    public <E extends Entity, K> FluentLoader.ById<E> load(Id<E, K> entityId) {
        FluentLoader<E> fluentLoader = applicationContext.getBean(FluentLoader.class, entityId.getEntityClass());
        fluentLoader.setDataManager(dataManager.getDelegate());
        return fluentLoader.id(entityId.getValue());
    }

    @Override
    public FluentValuesLoader loadValues(String queryString) {
        FluentValuesLoader loader = applicationContext.getBean(FluentValuesLoader.class, queryString);
        loader.setDataManager(dataManager.getDelegate());
        return loader;
    }

    @Override
    public <T> FluentValueLoader<T> loadValue(String queryString, Class<T> valueClass) {
        FluentValueLoader loader = applicationContext.getBean(FluentValueLoader.class, queryString, valueClass);
        loader.setDataManager(dataManager.getDelegate());
        return loader;
    }

    @Nullable
    @Override
    public <E extends Entity> E load(LoadContext<E> context) {
        context.setJoinTransaction(true);
        return dataManager.load(context);
    }

    @Override
    public <E extends Entity> List<E> loadList(LoadContext<E> context) {
        context.setJoinTransaction(true);
        return dataManager.loadList(context);
    }

    @Override
    public List<KeyValueEntity> loadValues(ValueLoadContext context) {
        context.setJoinTransaction(true);
        return dataManager.loadValues(context);
    }

    @Override
    public EntitySet save(Entity... entities) {
        CommitContext cc = new CommitContext(entities);
        cc.setJoinTransaction(true);
        return dataManager.commit(cc);
    }

    @Override
    public <E extends Entity> E save(E entity) {
        CommitContext cc = new CommitContext(entity);
        cc.setJoinTransaction(true);
        return dataManager.commit(cc).get(entity);
    }

    @Override
    public <E extends Entity> E save(E entity, @Nullable FetchPlan view) {
        CommitContext cc = new CommitContext();
        cc.addInstanceToCommit(entity, view);
        cc.setJoinTransaction(true);
        return dataManager.commit(cc).get(entity);
    }

    @Override
    public <E extends Entity> E save(E entity, @Nullable String viewName) {
        CommitContext cc = new CommitContext();
        cc.addInstanceToCommit(entity, viewName);
        cc.setJoinTransaction(true);
        return dataManager.commit(cc).get(entity);
    }

    @Override
    public void remove(Entity entity) {
        CommitContext cc = new CommitContext();
        cc.addInstanceToRemove(entity);
        cc.setJoinTransaction(true);
        dataManager.commit(cc);
    }

    @Override
    public void remove(Entity entity, boolean softDeletion) {
        CommitContext cc = new CommitContext();
        cc.addInstanceToRemove(entity);
        cc.setJoinTransaction(true);
        cc.setSoftDeletion(softDeletion);
        dataManager.commit(cc);
    }

    @Override
    public <T extends Entity> T create(Class<T> entityClass) {
        return metadata.create(entityClass);
    }

    @Override
    public <T extends Entity, K> T getReference(Class<T> entityClass, K id) {
        T entity = metadata.create(entityClass);
        EntityValues.setId(entity, id);
        entityStates.makePatch(entity);
        return entity;
    }

    @Override
    public TransactionalDataManager secure() {
        return new Secure(dataManager, transactions, applicationContext);
    }

    @Override
    public Transactions transactions() {
        return transactions;
    }

    @Override
    public TransactionalAction commitAction(Supplier<CommitContext> supplier) {
        return transactionalActionFactory.getTransactionalAction(supplier, true);
    }

    private static class Secure extends TransactionalDataManagerBean {

        public Secure(DataManager dataManager, Transactions transactions, ApplicationContext applicationContext) {
            this.dataManager = dataManager.secure();
            this.transactions = transactions;
            this.applicationContext = applicationContext;
        }
    }
}
