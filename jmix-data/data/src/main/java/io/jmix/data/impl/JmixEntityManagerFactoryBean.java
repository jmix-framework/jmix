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
import io.jmix.data.persistence.DbmsSpecifics;
import org.springframework.orm.jpa.JpaVendorAdapter;

import javax.sql.DataSource;

public class JmixEntityManagerFactoryBean extends JmixBaseEntityManagerFactoryBean {

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
}
