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

package io.jmix.data;

import io.jmix.core.*;
import io.jmix.core.annotation.JmixModule;
import io.jmix.data.impl.*;
import io.jmix.data.persistence.DbmsSpecifics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@ComponentScan
@JmixModule(dependsOn = JmixCoreConfiguration.class)
@EnableTransactionManagement
public class JmixDataConfiguration {

    protected Environment environment;

    @Autowired
    protected void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Bean
    protected LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource, Metadata metadata, DbmsSpecifics dbmsSpecifics,
            JmixEclipseLinkJpaVendorAdapter jpaVendorAdapter) {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setPersistenceXmlLocation(createPersistenceXml(metadata, dbmsSpecifics));
        factoryBean.setDataSource(dataSource);
        factoryBean.setJpaVendorAdapter(jpaVendorAdapter);
        return factoryBean;
    }

    @Bean
    protected JpaTransactionManager transactionManager(DataSource dataSource,
                                                       EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JmixTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        transactionManager.setDataSource(dataSource);
        return transactionManager;
    }

    @Bean
    protected JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    protected TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    @Bean(name = PersistentAttributesLoadChecker.NAME)
    protected PersistentAttributesLoadChecker persistentAttributesLoadChecker(BeanLocator beanLocator) {
        return new DataPersistentAttributesLoadChecker(beanLocator);
    }

    @Bean(name = EntitySystemStateSupport.NAME)
    protected EntitySystemStateSupport entitySystemStateSupport() {
        return new DataEntitySystemStateSupport();
    }

    protected String createPersistenceXml(Metadata metadata, DbmsSpecifics dbmsSpecifics) {
        String fileName = environment.getProperty("jmix.workDir") + "/META-INF/persistence.xml";
        PersistenceConfigProcessor processor = new PersistenceConfigProcessor(
                environment, metadata, dbmsSpecifics, Stores.MAIN, fileName);
        processor.create();
        return "file:" + fileName;
    }
}
