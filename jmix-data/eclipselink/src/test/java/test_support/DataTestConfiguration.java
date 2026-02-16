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

package test_support;

import io.jmix.core.JmixModules;
import io.jmix.core.Resources;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.repository.EnableJmixDataRepositories;
import io.jmix.data.impl.JmixEntityManagerFactoryBean;
import io.jmix.data.persistence.DbmsSpecifics;
import io.jmix.data.persistence.JpqlSortExpressionProvider;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.eclipselink.impl.JmixEclipselinkTransactionManager;
import io.jmix.testsupport.config.CommonCoreTestBeans;
import io.jmix.testsupport.config.CoreSecurityTestConfiguration;
import io.jmix.testsupport.config.HsqlEmbeddedDataSourceTestBeans;
import io.jmix.testsupport.config.JpaMainStoreTestBeans;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

@Configuration
@ComponentScan
@PropertySource("classpath:/test_support/test-app.properties")
@EnableJmixDataRepositories
@JmixModule(dependsOn = EclipselinkConfiguration.class)
@Import({CommonCoreTestBeans.class, HsqlEmbeddedDataSourceTestBeans.class,
        JpaMainStoreTestBeans.class, CoreSecurityTestConfiguration.class})
public class DataTestConfiguration {

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
        return new JmixEntityManagerFactoryBean("db1", db1DataSource(), jpaVendorAdapter,
                dbmsSpecifics, jmixModules, resources);
    }

    @Bean
    JpaTransactionManager db1TransactionManager(
            @Qualifier("db1EntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JmixEclipselinkTransactionManager("db1", entityManagerFactory);
    }

    @Bean
    JdbcTemplate db1JdbcTemplate(
            @Qualifier("db1DataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    TransactionTemplate db1TransactionTemplate(
            @Qualifier("db1TransactionManager") PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    @Bean("test_JpqlSortExpressionProvider")
    @Primary
    JpqlSortExpressionProvider jpqlSortExpressionProvider() {
        return new TestJpqlSortExpressionProvider();
    }
}
