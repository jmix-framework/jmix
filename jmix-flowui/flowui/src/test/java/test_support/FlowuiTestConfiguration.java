/*
 * Copyright 2022 Haulmont.
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

package test_support;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.JmixModules;
import io.jmix.core.Resources;
import io.jmix.core.annotation.JmixModule;
import io.jmix.data.DataConfiguration;
import io.jmix.data.impl.JmixEntityManagerFactoryBean;
import io.jmix.data.persistence.DbmsSpecifics;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.eclipselink.impl.JmixEclipselinkTransactionManager;
import io.jmix.flowui.FlowuiConfiguration;
import io.jmix.testsupport.config.CommonCoreTestBeans;
import io.jmix.testsupport.config.CoreSecurityTestConfiguration;
import io.jmix.testsupport.config.HsqlMemDataSourceTestBeans;
import io.jmix.testsupport.config.JpaMainStoreTestBeans;
import io.jmix.flowui.testassist.FlowuiServletTestBeans;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;

@Configuration
@Import({FlowuiConfiguration.class, EclipselinkConfiguration.class, DataConfiguration.class,
        CoreConfiguration.class, CommonCoreTestBeans.class,
        HsqlMemDataSourceTestBeans.class, JpaMainStoreTestBeans.class,
        FlowuiServletTestBeans.class, CoreSecurityTestConfiguration.class})
@PropertySource("classpath:/test_support/test-flowui-app.properties")
@JmixModule
public class FlowuiTestConfiguration {

    @Bean
    DataSource db1DataSource() {
        return new EmbeddedDatabaseBuilder()
                .generateUniqueName(true)
                .setType(EmbeddedDatabaseType.HSQL)
                .build();
    }

    @Bean
    LocalContainerEntityManagerFactoryBean db1EntityManagerFactory(JpaVendorAdapter jpaVendorAdapter,
                                                                   DbmsSpecifics dbmsSpecifics,
                                                                   JmixModules jmixModules,
                                                                   Resources resources) {
        return new JmixEntityManagerFactoryBean("db1", db1DataSource(), jpaVendorAdapter, dbmsSpecifics,
                jmixModules, resources);
    }

    @Bean
    JpaTransactionManager db1TransactionManager(
            @Qualifier("db1EntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JmixEclipselinkTransactionManager("db1", entityManagerFactory);
    }

    @Bean(name = "test_InMemoryStoreDescriptor")
    TestInMemoryStoreDescriptor inMemoryStoreDescriptor() {
        return new TestInMemoryStoreDescriptor();
    }

    @Bean(name = "test_InMemoryDataStore")
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    TestInMemoryDataStore inMemoryDataStore() {
        return new TestInMemoryDataStore();
    }
}
