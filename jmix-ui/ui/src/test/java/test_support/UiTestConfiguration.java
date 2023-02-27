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

import component.composite.component.TestCommentaryPanel;
import component.composite.component.TestEventPanel;
import component.composite.component.TestProgrammaticCommentaryPanel;
import component.composite.component.TestStepperField;
import component.composite.loader.TestCommentaryPanelLoader;
import component.composite.loader.TestEventPanelLoader;
import component.composite.loader.TestStepperFieldLoader;
import io.jmix.core.CoreConfiguration;
import io.jmix.core.JmixModules;
import io.jmix.core.Resources;
import io.jmix.core.Stores;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.impl.JmixMessageSource;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.core.security.CoreSecurityConfiguration;
import io.jmix.core.security.InMemoryUserRepository;
import io.jmix.core.security.UserRepository;
import io.jmix.data.DataConfiguration;
import io.jmix.data.impl.JmixEntityManagerFactoryBean;
import io.jmix.data.impl.JmixTransactionManager;
import io.jmix.data.persistence.DbmsSpecifics;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.ui.UiConfiguration;
import io.jmix.ui.menu.MenuBuilder;
import io.jmix.ui.menu.MenuConfig;
import io.jmix.ui.menu.SideMenuBuilder;
import io.jmix.ui.sys.ActionsConfiguration;
import io.jmix.ui.sys.registration.ComponentRegistration;
import io.jmix.ui.sys.registration.ComponentRegistrationBuilder;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import test_support.bean.TestAppMenuBuilder;
import test_support.bean.TestMenuConfig;
import test_support.bean.TestSideMenuBuilder;
import test_support.entity.sec.User;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Configuration
@ComponentScan
@PropertySource("classpath:/test_support/test-ui-app.properties")
@Import({
        UiConfiguration.class,
        EclipselinkConfiguration.class,
        DataConfiguration.class,
        CoreConfiguration.class
})
@JmixModule(dependsOn = CoreConfiguration.class)
public class UiTestConfiguration {

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
    public ActionsConfiguration actions(ApplicationContext applicationContext,
                                        AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        ActionsConfiguration actionsConfiguration = new ActionsConfiguration(applicationContext, metadataReaderFactory);
        actionsConfiguration.setBasePackages(Collections.singletonList("screen_actions.action"));
        return actionsConfiguration;
    }

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }

    @Bean
    public MenuConfig menuConfig() {
        return new TestMenuConfig();
    }

    @Bean
    public SideMenuBuilder sideMenuBuilder() {
        return new TestSideMenuBuilder();
    }

    @Bean
    @Primary
    public MenuBuilder appMenuBuilder() {
        return new TestAppMenuBuilder();
    }

    @Bean
    public ComponentRegistration testProgrammaticCommentaryPanel() {
        return ComponentRegistrationBuilder.create(TestProgrammaticCommentaryPanel.NAME)
                .withComponentClass(TestProgrammaticCommentaryPanel.class)
                .build();
    }

    @Bean
    public ComponentRegistration testCommentaryPanel() {
        return ComponentRegistrationBuilder.create(TestCommentaryPanel.NAME)
                .withComponentClass(TestCommentaryPanel.class)
                .withComponentLoaderClass(TestCommentaryPanelLoader.class)
                .build();
    }

    @Bean
    public ComponentRegistration testStepperField() {
        return ComponentRegistrationBuilder.create(TestStepperField.NAME)
                .withComponentClass(TestStepperField.class)
                .withComponentLoaderClass(TestStepperFieldLoader.class)
                .build();
    }

    @Bean
    public ComponentRegistration testEventPanel() {
        return ComponentRegistrationBuilder.create(TestEventPanel.NAME)
                .withComponentClass(TestEventPanel.class)
                .withComponentLoaderClass(TestEventPanelLoader.class)
                .build();
    }


    @EnableWebSecurity
    static class SecurityConfiguration extends CoreSecurityConfiguration {
        public UserRepository userRepository() {
            InMemoryUserRepository repository = new InMemoryUserRepository();
            User user = new User();
            user.setLogin("admin");
            repository.addUser(user);
  	        return repository;
         }
    }

}
