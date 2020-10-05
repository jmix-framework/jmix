/*
 * Copyright 2020 Haulmont.
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

import io.jmix.core.Stores;
import io.jmix.data.persistence.DbmsSpecifics;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;

public class JmixEntityManagerFactoryBean extends LocalContainerEntityManagerFactoryBean {

    private String storeName;
    private DbmsSpecifics dbmsSpecifics;

    public JmixEntityManagerFactoryBean(String storeName,
                                        DataSource dataSource,
                                        PersistenceConfigProcessor persistenceConfigProcessor,
                                        JpaVendorAdapter jpaVendorAdapter,
                                        DbmsSpecifics dbmsSpecifics) {
        this.storeName = storeName;
        this.dbmsSpecifics = dbmsSpecifics;
        setPersistenceXmlLocation("file:" + persistenceConfigProcessor.create(storeName));
        setDataSource(dataSource);
        setJpaVendorAdapter(jpaVendorAdapter);
    }

    @Override
    public void afterPropertiesSet() throws PersistenceException {
        super.afterPropertiesSet();

        if (!Stores.isMain(storeName))
            getJpaPropertyMap().put(PersistenceSupport.PROP_NAME, storeName);
        getJpaPropertyMap().putAll(dbmsSpecifics.getDbmsFeatures(storeName).getJpaParameters());
    }
}
