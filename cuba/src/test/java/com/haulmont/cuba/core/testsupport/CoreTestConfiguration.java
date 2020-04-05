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

import com.haulmont.cuba.JmixCubaConfiguration;
import com.haulmont.cuba.core.model.common.UserEntityListener;
import io.jmix.core.JmixCoreConfiguration;
import io.jmix.core.Stores;
import io.jmix.core.security.UserSessionSource;
import io.jmix.data.JmixDataConfiguration;
import io.jmix.data.impl.JmixEntityManagerFactoryBean;
import io.jmix.data.impl.JmixTransactionManager;
import io.jmix.data.impl.PersistenceConfigProcessor;
import io.jmix.data.persistence.JpqlSortExpressionProvider;
import io.jmix.ui.JmixUiConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@Import({JmixCoreConfiguration.class, JmixCubaConfiguration.class, JmixDataConfiguration.class, JmixUiConfiguration.class})
@PropertySource("classpath:/com/haulmont/cuba/core/test-app.properties")
public class CoreTestConfiguration {

    @Bean
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
