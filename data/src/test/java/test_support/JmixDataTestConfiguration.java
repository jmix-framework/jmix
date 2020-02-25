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

import io.jmix.core.Metadata;
import io.jmix.core.Stores;
import io.jmix.core.annotation.JmixModule;
import io.jmix.data.JmixDataConfiguration;
import io.jmix.data.impl.JmixEclipseLinkJpaVendorAdapter;
import io.jmix.data.impl.JmixTransactionManager;
import io.jmix.data.impl.PersistenceConfigProcessor;
import io.jmix.data.persistence.DbmsSpecifics;
import io.jmix.data.persistence.JpqlSortExpressionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@ComponentScan
@PropertySource("classpath:/test_support/test-app.properties")
@JmixModule(dependsOn = JmixDataConfiguration.class)
public class JmixDataTestConfiguration {

    @Autowired
    protected Environment environment;

    @Bean
    @Primary
    protected DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .generateUniqueName(true)
                .setType(EmbeddedDatabaseType.HSQL)
                .build();
    }

    @Bean
    protected DataSource db1DataSource() {
        return new EmbeddedDatabaseBuilder()
                .generateUniqueName(true)
                .setType(EmbeddedDatabaseType.HSQL)
                .build();
    }

    @Bean
    protected LocalContainerEntityManagerFactoryBean db1EntityManagerFactory(
            Metadata metadata, DbmsSpecifics dbmsSpecifics, JmixEclipseLinkJpaVendorAdapter jpaVendorAdapter) {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setPersistenceXmlLocation("file:" +
                new PersistenceConfigProcessor(environment, metadata, dbmsSpecifics).create("db1"));
        factoryBean.setDataSource(db1DataSource());
        factoryBean.setJpaVendorAdapter(jpaVendorAdapter);
        return factoryBean;
    }

    @Bean
    protected JpaTransactionManager db1TransactionManager(
            @Qualifier("db1DataSource") DataSource dataSource,
            @Qualifier("db1EntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JmixTransactionManager("db1");
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        transactionManager.setDataSource(dataSource);
        return transactionManager;
    }

    @Bean
    protected JdbcTemplate db1JdbcTemplate(
            @Qualifier("db1DataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    protected TransactionTemplate db1TransactionTemplate(
            @Qualifier("db1TransactionManager") PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    @Bean(JpqlSortExpressionProvider.NAME)
    protected JpqlSortExpressionProvider jpqlSortExpressionProvider() {
        return new TestJpqlSortExpressionProvider();
    }
}
