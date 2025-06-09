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

package io.jmix.reportsflowui;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.JmixModules;
import io.jmix.core.Resources;
import io.jmix.core.Stores;
import io.jmix.core.annotation.MessageSourceBasenames;
import io.jmix.core.cluster.ClusterApplicationEventChannelSupplier;
import io.jmix.core.cluster.LocalApplicationEventChannelSupplier;
import io.jmix.core.impl.JmixMessageSource;
import io.jmix.core.security.InMemoryUserRepository;
import io.jmix.core.security.UserRepository;
import io.jmix.data.DataConfiguration;
import io.jmix.data.impl.JmixEntityManagerFactoryBean;
import io.jmix.data.impl.JmixTransactionManager;
import io.jmix.data.persistence.DbmsSpecifics;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.flowui.FlowuiConfiguration;
import io.jmix.flowui.Views;
import io.jmix.flowui.sys.UiAccessChecker;
import io.jmix.flowui.testassist.vaadin.TestServletContext;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.flowui.view.builder.WindowBuilderProcessor;
import io.jmix.reports.ReportsConfiguration;
import io.jmix.reportsflowui.test_support.TestWindowBuilderProcessor;
import io.jmix.reportsflowui.test_support.role.FullAccessRole;
import io.jmix.security.SecurityConfiguration;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.ServletContext;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.scripting.ScriptEvaluator;
import org.springframework.scripting.groovy.GroovyScriptEvaluator;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.List;

@Configuration
@Import({CoreConfiguration.class, DataConfiguration.class, EclipselinkConfiguration.class, SecurityConfiguration.class,
        ReportsConfiguration.class, FlowuiConfiguration.class, ReportsFlowuiConfiguration.class})
@EnableWebSecurity
@PropertySource("classpath:/test_support/test-app.properties")
@MessageSourceBasenames({"test_support/messages"})
public class ReportsFlowuiTestConfiguration {

    @Bean
    public MessageSource messageSource(JmixModules modules, Resources resources) {
        return new JmixMessageSource(modules, resources);
    }

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
    protected LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource,
                                                                          JpaVendorAdapter jpaVendorAdapter,
                                                                          DbmsSpecifics dbmsSpecifics,
                                                                          JmixModules jmixModules,
                                                                          Resources resources) {
        return new JmixEntityManagerFactoryBean(Stores.MAIN, dataSource, jpaVendorAdapter, dbmsSpecifics, jmixModules, resources);
    }

    @Bean
    PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JmixTransactionManager(Stores.MAIN, entityManagerFactory);
    }

    @Bean
    TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    @Bean
    public SpringLiquibase liquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("test_support/liquibase/test-changelog.xml");
        return liquibase;
    }

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }

    @Bean
    public ScriptEvaluator scriptEvaluator() {
        return new GroovyScriptEvaluator();
    }

    @Bean
    public UserRepository userRepository(RoleGrantedAuthorityUtils roleGrantedAuthorityUtils) {
        InMemoryUserRepository repository = new InMemoryUserRepository();
        repository.addUser(User.builder()
                .username("admin")
                .password("{noop}admin")
                .authorities(List.of(roleGrantedAuthorityUtils.createResourceRoleGrantedAuthority(FullAccessRole.NAME)))
                .build());
        return repository;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public ClusterApplicationEventChannelSupplier clusterApplicationEventChannelSupplier() {
        return new LocalApplicationEventChannelSupplier();
    }

    @Bean
    public ServletContext servletContext() {
        return new TestServletContext();
    }

    @Bean
    @Primary
    public WindowBuilderProcessor windowBuilderProcessor(ApplicationContext applicationContext, Views views, ViewRegistry viewRegistry,
                                                         UiAccessChecker uiAccessChecker) {
        return new TestWindowBuilderProcessor(applicationContext, views, viewRegistry, uiAccessChecker);
    }
}
