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

import io.jmix.core.*;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.cluster.ClusterApplicationEventChannelSupplier;
import io.jmix.core.cluster.LocalApplicationEventChannelSupplier;
import io.jmix.core.impl.JmixMessageSource;
import io.jmix.core.security.CoreSecurityConfiguration;
import io.jmix.data.DataConfiguration;
import io.jmix.data.impl.JmixEntityManagerFactoryBean;
import io.jmix.data.impl.JmixTransactionManager;
import io.jmix.data.persistence.DbmsSpecifics;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.flowui.FlowuiConfiguration;
import io.jmix.flowui.settings.UserSettingsCache;
import io.jmix.flowui.settings.UserSettingsService;
import io.jmix.flowui.testassist.vaadin.TestServletContext;
import io.jmix.flowuidata.FlowuiDataConfiguration;
import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.ServletContext;
import liquibase.integration.spring.SpringLiquibase;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import overridden_settings.test_support.TestJmixDetailsSettingsBinder;

import javax.sql.DataSource;

@Configuration
@Import({FlowuiConfiguration.class, EclipselinkConfiguration.class, DataConfiguration.class,
        CoreConfiguration.class, FlowuiDataConfiguration.class})
@PropertySource("classpath:/test_support/test-flowui-data-app.properties")
@JmixModule
public class FlowuiDataTestConfiguration {

    @Bean
    public MessageSource messageSource(JmixModules modules, Resources resources) {
        return new JmixMessageSource(modules, resources);
    }

    @Bean
    DataSource dataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl("jdbc:hsqldb:mem:testdb");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    @Bean
    @Primary
    LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource,
                                                                JpaVendorAdapter jpaVendorAdapter,
                                                                DbmsSpecifics dbmsSpecifics,
                                                                JmixModules jmixModules,
                                                                Resources resources) {
        return new JmixEntityManagerFactoryBean(Stores.MAIN, dataSource, jpaVendorAdapter, dbmsSpecifics, jmixModules, resources);
    }

    @Bean
    @Primary
    PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JmixTransactionManager(Stores.MAIN, entityManagerFactory);
    }

    @Bean
    @Primary
    JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @Primary
    TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }

    @Bean
    @Primary
    public ServletContext servletContext() {
        return new TestServletContext();
    }

    @Bean("liquibase")
    public SpringLiquibase liquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("test_support/liquibase/test-changelog.xml");
        return liquibase;
    }

    @Bean("test_UserSettingsCache")
    @Primary
    UserSettingsCache userSettingsCache(UserSettingsService userSettingsService) {
        return new TestUserSettingsCacheImpl(userSettingsService);
    }

    @Order(JmixOrder.HIGHEST_PRECEDENCE + 100)
    @Bean("test_TestJmixDetailsSettingsBinder")
    TestJmixDetailsSettingsBinder testJmixDetailsSettingsBinder() {
        return new TestJmixDetailsSettingsBinder();
    }

    @Bean
    public ClusterApplicationEventChannelSupplier clusterApplicationEventChannelSupplier() {
        return new LocalApplicationEventChannelSupplier();
    }

    @EnableWebSecurity
    protected class CoreSecurity extends CoreSecurityConfiguration {
    }
}
