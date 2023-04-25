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

package io.jmix.data;

import io.jmix.core.impl.TransactionManagerLocator;
import io.jmix.data.impl.JmixJtaTransactionManager;
import io.jmix.data.impl.JmixTransactionManager;
import org.springframework.context.ApplicationContext;
import io.jmix.core.Stores;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import org.springframework.beans.factory.annotation.Autowired;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Component("data_StoreAwareLocator")
public class StoreAwareLocator {

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected TransactionManagerLocator transactionManagerLocator;

    public DataSource getDataSource(String storeName) {
        return getBean(storeName, "dataSource", DataSource.class);
    }

    public JdbcTemplate getJdbcTemplate(String storeName) {
        return new JdbcTemplate(getDataSource(storeName));
    }

    public PlatformTransactionManager getTransactionManager(String storeName) {
        return transactionManagerLocator.getTransactionManager(storeName);
    }

    public TransactionTemplate getTransactionTemplate(String storeName) {
        return new TransactionTemplate(getTransactionManager(storeName));
    }

    public EntityManagerFactory getEntityManagerFactory(String storeName) {
        return getBean(storeName, "entityManagerFactory", EntityManagerFactory.class);
    }

    public EntityManager getEntityManager(String storeName) {
        EntityManager entityManager = EntityManagerFactoryUtils.getTransactionalEntityManager(getEntityManagerFactory(storeName));
        if (entityManager == null) {
            throw new IllegalStateException("Cannot get transactional EntityManager for " + storeName + " store");
        }
        return entityManager;
    }

    public String getTransactionManagerKey(String storeName) {
        TransactionManager tm = getTransactionManager(storeName);
        if (tm instanceof JmixTransactionManager) {
            return ((JmixTransactionManager) tm).getKey();
        } else if (tm instanceof JmixJtaTransactionManager) {
            return ((JmixJtaTransactionManager) tm).getKey();
        }
        return storeName + "TransactionManager";
    }

    protected <T> T getBean(String storeName, String beanName, Class<T> beanClass) {
        if (Stores.isMain(storeName)) {
            return applicationContext.getBean(beanName, beanClass);
        } else {
            return applicationContext.getBean(storeName + StringUtils.capitalize(beanName), beanClass);
        }
    }
}
