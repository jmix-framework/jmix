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

import io.jmix.core.BeanLocator;
import io.jmix.core.Stores;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Component(StoreAwareLocator.NAME)
public class StoreAwareLocator {

    public static final String NAME = "jmix_StoreAwareLocator";

    @Inject
    protected BeanLocator beanLocator;

    public DataSource getDataSource(String storeName) {
        return getBean(storeName, "dataSource", DataSource.class);
    }

    public JdbcTemplate getJdbcTemplate(String storeName) {
        return new JdbcTemplate(getDataSource(storeName));
    }

    public PlatformTransactionManager getTransactionManager(String storeName) {
        return getBean(storeName, "transactionManager", PlatformTransactionManager.class);
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

    protected <T> T getBean(String storeName, String beanName, Class<T> beanClass) {
        if (Stores.isMain(storeName)) {
            return beanLocator.get(beanName, beanClass);
        } else {
            return beanLocator.get(storeName + StringUtils.capitalize(beanName), beanClass);
        }
    }
}
