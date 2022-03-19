/*
 * Copyright 2021 Haulmont.
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

import io.jmix.core.JmixModuleDescriptor;
import io.jmix.core.JmixModules;
import io.jmix.core.Resources;
import io.jmix.core.Stores;
import io.jmix.data.persistence.DbmsSpecifics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import java.util.List;

public class JmixBaseEntityManagerFactoryBean extends LocalContainerEntityManagerFactoryBean {
    protected String storeName;
    protected DbmsSpecifics dbmsSpecifics;
    protected JmixModules jmixModules;
    protected Resources resources;

    private static final Logger log = LoggerFactory.getLogger(JmixBaseEntityManagerFactoryBean.class);

    protected void setupPersistenceUnit() {
        List<JmixModuleDescriptor> modules = jmixModules.getAll();
        for (int i = modules.size() - 1; i >= 0; i--) {
            String path = getPersistenceXmlPath(modules.get(i), storeName);
            if (resources.getResource("classpath:" + path).exists()) {
                log.info("Using persistence.xml at {} for '{}' store", path, storeName);
                setPersistenceXmlLocation("classpath:" + path);
                return;
            }
        }
        log.warn("Cannot find persistence.xml for '{}' store. Falling back to classpath scan for entity classes.", storeName);
        setPersistenceUnitName(storeName);
        setPackagesToScan("");
    }

    protected String getPersistenceXmlPath(JmixModuleDescriptor module, String storeName) {
        String dir = module.getBasePackage().replace('.', '/');
        return dir + "/" + (Stores.isMain(storeName) ? "" : storeName + "-") + "persistence.xml";
    }

    protected void setupJpaProperties() {
        if (!Stores.isMain(storeName))
            getJpaPropertyMap().put(PersistenceUnitProperties.STORE_NAME_PROPERTY, storeName);

        getJpaPropertyMap().putAll(dbmsSpecifics.getDbmsFeatures(storeName).getJpaParameters());
    }
}
