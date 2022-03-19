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

import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.TransactionParams;
import com.haulmont.cuba.core.Transactions;
import io.jmix.core.Stores;
import io.jmix.eclipselink.impl.EclipselinkPersistenceSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import javax.inject.Named;

@Component(Transactions.NAME)
public class TransactionsImpl implements Transactions {

    @Autowired
    @Named("transactionManager")
    protected PlatformTransactionManager transactionManager;

    @Autowired
    protected PersistenceImpl persistence;

    @Autowired
    protected EclipselinkPersistenceSupport persistenceSupport;

    @Autowired
    protected ApplicationContext applicationContext;

    @Override
    public Transaction create(TransactionParams params) {
        return new TransactionImpl(transactionManager, persistence, persistenceSupport, false, params, Stores.MAIN);
    }

    @Override
    public Transaction create(String storeName, TransactionParams params) {
        return new TransactionImpl(getTransactionManager(storeName), persistence, persistenceSupport, false, params, storeName);
    }

    @Override
    public Transaction create() {
        return new TransactionImpl(transactionManager, persistence, persistenceSupport, false, null, Stores.MAIN);
    }

    @Override
    public Transaction create(String storeName) {
        return new TransactionImpl(getTransactionManager(storeName), persistence, persistenceSupport, false, null, storeName);
    }

    @Override
    public Transaction get() {
        return new TransactionImpl(transactionManager, persistence, persistenceSupport, true, null, Stores.MAIN);
    }

    @Override
    public Transaction get(String storeName) {
        return new TransactionImpl(getTransactionManager(storeName), persistence, persistenceSupport, true, null, storeName);
    }

    protected PlatformTransactionManager getTransactionManager(String store) {
        PlatformTransactionManager tm;
        if (Stores.isMain(store))
            tm = this.transactionManager;
        else
            tm = applicationContext.getBean("transactionManager_" + store, PlatformTransactionManager.class);
        return tm;
    }
}
