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
import io.jmix.core.Stores;
import io.jmix.data.persistence.DbmsSpecifics;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;

public class JmixEntityManagerFactoryBean extends LocalContainerEntityManagerFactoryBean {

    private String storeName;
    private DbmsSpecifics dbmsSpecifics;


    protected JmixModules jmixModules;

    public JmixEntityManagerFactoryBean(String storeName,
                                        DataSource dataSource,
                                        PersistenceConfigProcessor persistenceConfigProcessor,
                                        JpaVendorAdapter jpaVendorAdapter,
                                        DbmsSpecifics dbmsSpecifics,
                                        JmixModules jmixModules) {
        this.storeName = storeName;
        this.dbmsSpecifics = dbmsSpecifics;
        this.jmixModules = jmixModules;

        String persistenceXmlPath = getPersistenceXmlPath(storeName);

        generateOrmXml(persistenceConfigProcessor, persistenceXmlPath);
        setPersistenceXmlLocation("classpath:" + persistenceXmlPath);
        setDataSource(dataSource);
        setJpaVendorAdapter(jpaVendorAdapter);
        setupJpaProperties();
    }

    protected String getPersistenceXmlPath(String storeName) {
        String modulePackage = jmixModules.getLast().getBasePackage().replace('.', '/');
        return modulePackage + "/" + (Stores.isMain(storeName) ? "" : storeName + "-") + "persistence.xml";
    }

    protected void generateOrmXml(PersistenceConfigProcessor processor, String persistenceXmlPath) {
        Resource persistenceXmlResource = new ClassPathResource(persistenceXmlPath);
        try {
            File persistenceXmlDir = persistenceXmlResource.getFile().getParentFile();
            processor.createOrmXml(storeName, persistenceXmlDir);
        } catch (IOException e) {
            logger.error("Cannot create orm.xml", e);
        }
    }


    protected void setupJpaProperties() {
        if (!Stores.isMain(storeName))
            getJpaPropertyMap().put(PersistenceSupport.PROP_NAME, storeName);

        getJpaPropertyMap().putAll(dbmsSpecifics.getDbmsFeatures(storeName).getJpaParameters());
    }
}
