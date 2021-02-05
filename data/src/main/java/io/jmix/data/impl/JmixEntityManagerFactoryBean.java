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

import io.jmix.core.JmixModules;
import io.jmix.core.Resources;
import io.jmix.core.Stores;
import io.jmix.data.persistence.DbmsSpecifics;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;

public class JmixEntityManagerFactoryBean extends LocalContainerEntityManagerFactoryBean {

    private String storeName;
    private DbmsSpecifics dbmsSpecifics;
    protected JmixModules jmixModules;
    protected Resources resources;

    public JmixEntityManagerFactoryBean(String storeName,
                                        DataSource dataSource,
                                        JpaVendorAdapter jpaVendorAdapter,
                                        DbmsSpecifics dbmsSpecifics,
                                        JmixModules jmixModules,
                                        Resources resources) {
        this.storeName = storeName;
        this.dbmsSpecifics = dbmsSpecifics;
        this.jmixModules = jmixModules;
        this.resources = resources;

        setupPersistenceUnit();
        setDataSource(dataSource);
        setJpaVendorAdapter(jpaVendorAdapter);
        setupJpaProperties();
    }

    protected void setupPersistenceUnit() {
        String persistenceXmlPath = getPersistenceXmlPath(storeName);
        if (resources.getResource("classpath:" + persistenceXmlPath).exists()) {
            setPersistenceXmlLocation("classpath:" + persistenceXmlPath);
        } else {
            setPersistenceUnitName(storeName);
            setPackagesToScan("");
            logger.warn(String.format("Cannot find '%s'. Empty persistence unit with name '%s' will be created.",
                    persistenceXmlPath,
                    storeName));
        }
    }

    protected String getPersistenceXmlPath(String storeName) {
        String modulePackage = jmixModules.getLast().getBasePackage().replace('.', '/');
        return modulePackage + "/" + (Stores.isMain(storeName) ? "" : storeName + "-") + "persistence.xml";
    }

    protected void setupJpaProperties() {
        if (!Stores.isMain(storeName))
            getJpaPropertyMap().put(PersistenceUnitProperties.STORE_NAME_PROPERTY, storeName);

        getJpaPropertyMap().putAll(dbmsSpecifics.getDbmsFeatures(storeName).getJpaParameters());
    }
}
