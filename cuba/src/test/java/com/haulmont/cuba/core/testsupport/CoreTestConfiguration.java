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

package com.haulmont.cuba.core.testsupport;

import com.haulmont.cuba.CubaConfiguration;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.core.model.common.UserEntityListener;
import io.jmix.core.CoreConfiguration;
import io.jmix.core.Stores;
import io.jmix.data.DataConfiguration;
import io.jmix.data.impl.JmixEntityManagerFactoryBean;
import io.jmix.data.impl.JmixTransactionManager;
import io.jmix.data.impl.PersistenceConfigProcessor;
import io.jmix.data.persistence.JpqlSortExpressionProvider;
import io.jmix.dynattr.DynAttrConfiguration;
import io.jmix.dynattrui.DynAttrUiConfiguration;
import io.jmix.fsfilestorage.FileSystemFileStorageConfiguration;
import io.jmix.ui.UiConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@Import({CoreConfiguration.class, CubaConfiguration.class, DataConfiguration.class, UiConfiguration.class,
        DynAttrConfiguration.class, DynAttrUiConfiguration.class, FileSystemFileStorageConfiguration.class})
@PropertySource("classpath:/com/haulmont/cuba/core/test-core-app.properties")
public class CoreTestConfiguration {

    @Bean
    @Primary
    DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .generateUniqueName(true)
                .setType(EmbeddedDatabaseType.HSQL)
                .build();
    }

    @Bean
    @Primary
    LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource, PersistenceConfigProcessor processor, JpaVendorAdapter jpaVendorAdapter) {
        return new JmixEntityManagerFactoryBean(Stores.MAIN, dataSource, processor, jpaVendorAdapter);
    }

    @Bean
    @Primary
    PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JmixTransactionManager(Stores.MAIN, entityManagerFactory);
    }

    @Bean(name = "test_UserEntityListener")
    UserEntityListener userEntityListener() {
        return new UserEntityListener();
    }

    @Bean(name = UserSessionSource.NAME)
    UserSessionSource userSessionSource() {
        return new TestUserSessionSource();
    }

    @Bean(name = JpqlSortExpressionProvider.NAME)
    JpqlSortExpressionProvider jpqlSortExpressionProvider() {
        return new TestJpqlSortExpressionProvider();
    }

    @Bean
    TestEventsListener testEventsListener() {
        return new TestEventsListener();
    }

    @Bean
    TestAppContextLifecycleListener testAppContextLifecycleListener() {
        return new TestAppContextLifecycleListener();
    }
}
