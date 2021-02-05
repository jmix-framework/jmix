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

package io.jmix.data.impl;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.datasource.JdbcTransactionObjectSupport;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.persistence.EntityManagerFactory;

public class JmixTransactionManager extends JpaTransactionManager implements ApplicationContextAware {

    protected ApplicationContext applicationContext;

    protected String storeName;

    public JmixTransactionManager(String storeName, EntityManagerFactory entityManagerFactory) {
        this.storeName = storeName;
        setEntityManagerFactory(entityManagerFactory);
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) {
        JdbcTransactionObjectSupport txObject = (JdbcTransactionObjectSupport) status.getTransaction();
        try {
            super.doCommit(status);
        } finally {
            if (getDataSource() != null && TransactionSynchronizationManager.hasResource(getDataSource())) {
                txObject.setConnectionHolder(null);
                TransactionSynchronizationManager.unbindResource(getDataSource());
            }
        }
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) {
        JdbcTransactionObjectSupport txObject = (JdbcTransactionObjectSupport) status.getTransaction();
        try {
            super.doRollback(status);
        } finally {
            if (getDataSource() != null && TransactionSynchronizationManager.hasResource(getDataSource())) {
                txObject.setConnectionHolder(null);
                TransactionSynchronizationManager.unbindResource(getDataSource());
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
