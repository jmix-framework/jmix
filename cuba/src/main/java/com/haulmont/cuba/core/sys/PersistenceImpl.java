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

import com.haulmont.cuba.core.*;
import io.jmix.core.BeanLocator;
import io.jmix.core.Stores;
import io.jmix.data.*;
import io.jmix.data.persistence.DbTypeConverter;
import io.jmix.data.persistence.DbmsSpecifics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Component(Persistence.NAME)
public class PersistenceImpl implements Persistence {

    private static final Logger log = LoggerFactory.getLogger(PersistenceImpl.class);

    protected volatile boolean softDeletion = true;

    @Inject
    private BeanLocator beanLocator;

    @Inject
    protected PersistenceTools tools;

    protected EntityManagerFactory jpaEmf;

    @Inject
    protected Transactions transactions;

    @Inject
    protected DbmsSpecifics dbmsSpecifics;

    @Inject
    protected Stores stores;

    @Inject
    @Named("entityManagerFactory")
    public void setFactory(LocalContainerEntityManagerFactoryBean factoryBean) {
        this.jpaEmf = factoryBean.getObject();
    }

    @Override
    public PersistenceTools getTools() {
        return tools;
    }

    @Override
    public DbTypeConverter getDbTypeConverter() {
        return dbmsSpecifics.getDbTypeConverter();
    }

    @Override
    public DbTypeConverter getDbTypeConverter(String store) {
        return dbmsSpecifics.getDbTypeConverter(store);
    }

    @Override
    public void runInTransaction(Transaction.Runnable runnable) {
        createTransaction().execute(runnable);
    }

    @Override
    public void runInTransaction(String store, Transaction.Runnable runnable) {
        createTransaction(store).execute(store, runnable);
    }

    @Override
    public <T> T callInTransaction(Transaction.Callable<T> callable) {
        return createTransaction().execute(callable);
    }

    @Override
    public <T> T callInTransaction(String store, Transaction.Callable<T> callable) {
        return createTransaction(store).execute(store, callable);
    }

    @Override
    public Transaction createTransaction(TransactionParams params) {
        return transactions.create(params);
    }

    @Override
    public Transaction createTransaction(String store, TransactionParams params) {
        return transactions.create(store, params);
    }

    @Override
    public Transaction createTransaction() {
        return transactions.create();
    }

    @Override
    public Transaction createTransaction(String store) {
        return transactions.create(store);
    }

    @Override
    public Transaction getTransaction() {
        return transactions.get();
    }

    @Override
    public Transaction getTransaction(String store) {
        return transactions.get(store);
    }

    @Override
    public boolean isInTransaction() {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }

    @Override
    public EntityManager getEntityManager() {
        return getEntityManager(Stores.MAIN);
    }

    @Override
    public EntityManager getEntityManager(String store) {
        if (!TransactionSynchronizationManager.isActualTransactionActive())
            throw new IllegalStateException("No active transaction");

        EntityManagerFactory emf;
        if (Stores.isMain(store))
            emf = this.jpaEmf;
        else
            emf = beanLocator.get("entityManagerFactory_" + store);

        javax.persistence.EntityManager jpaEm = EntityManagerFactoryUtils.doGetTransactionalEntityManager(emf, null, true);
        if (jpaEm == null) {
            throw new RuntimeException("Unable to get JPA EntityManager from EntityManagerFactoryUtils");
        }

        if (!jpaEm.isJoinedToTransaction())
            throw new IllegalStateException("No active transaction for " + store + " database");

        EntityManager entityManager = createEntityManager(jpaEm);

        Boolean softDeletion = (Boolean) jpaEm.getProperties().get(OrmProperties.SOFT_DELETION);
        if (softDeletion == null) { // new EntityManager
            entityManager.setSoftDeletion(this.softDeletion);
        }

        return entityManager;
    }

    protected EntityManager createEntityManager(javax.persistence.EntityManager jpaEm) {
        return (EntityManager) beanLocator.getPrototype(EntityManager.NAME, jpaEm);
    }

    @Override
    public boolean isSoftDeletion() {
        return softDeletion;
    }

    @Override
    public void setSoftDeletion(boolean value) {
        softDeletion = value;
    }

    @Override
    public DataSource getDataSource() {
        return getDataSource(Stores.MAIN);
    }

    @Override
    public DataSource getDataSource(String store) {
        if (Stores.isMain(store))
            return (DataSource) beanLocator.get("dataSource");
        else
            return (DataSource) beanLocator.get("dataSource_" + store);
    }

    /**
     * INTERNAL.
     * Destroys the persistence configuration. Further use of this bean instance is impossible.
     */
    public void dispose() {
        jpaEmf.close();
        for (String store : stores.getAdditional()) {
            EntityManagerFactory emf = beanLocator.get("entityManagerFactory_" + store);
            emf.close();
        }
    }

    protected EntityManagerFactory getJpaEmf(String store) {
        if (Stores.isMain(store))
            return jpaEmf;
        else
            return beanLocator.get("entityManagerFactory_" + store);
    }
}
