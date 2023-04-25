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

import io.jmix.core.JmixModules;
import io.jmix.core.Resources;
import io.jmix.data.impl.jta.JmixJtaServerPlatform;
import io.jmix.data.persistence.DbmsSpecifics;
import org.springframework.orm.jpa.JpaVendorAdapter;

import javax.sql.DataSource;

public class JmixJtaEntityManagerFactoryBean extends JmixBaseEntityManagerFactoryBean {

    public JmixJtaEntityManagerFactoryBean(String storeName,
                                           DataSource jtaDataSource,
                                           JpaVendorAdapter jpaVendorAdapter,
                                           DbmsSpecifics dbmsSpecifics,
                                           JmixModules jmixModules,
                                           Resources resources) {
        this.storeName = storeName;
        this.dbmsSpecifics = dbmsSpecifics;
        this.jmixModules = jmixModules;
        this.resources = resources;

        setupPersistenceUnit();
        setJtaDataSource(jtaDataSource);
        setJpaVendorAdapter(jpaVendorAdapter);
        setupJpaProperties();
    }

    @Override
    protected void setupJpaProperties() {
        super.setupJpaProperties();
        getJpaPropertyMap().put("eclipselink.target-server", JmixJtaServerPlatform.class.getName());
        getJpaPropertyMap().put("jakarta.persistence.transactionType", "JTA");
    }
}
