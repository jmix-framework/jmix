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
import io.jmix.core.Stores;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.security.CoreSecurityConfiguration;
import io.jmix.core.security.InMemoryUserRepository;
import io.jmix.core.security.UserRepository;
import io.jmix.data.DataConfiguration;
import io.jmix.data.impl.JmixEntityManagerFactoryBean;
import io.jmix.data.impl.JmixTransactionManager;
import io.jmix.data.persistence.DbmsSpecifics;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.flowui.FlowuiConfiguration;
import io.jmix.multitenancy.MultitenancyConfiguration;
import io.jmix.multitenancyflowui.MultitenancyFlowuiConfiguration;
import io.jmix.security.SecurityConfiguration;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.*;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.scripting.ScriptEvaluator;
import org.springframework.scripting.groovy.GroovyScriptEvaluator;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.ServletContext;
import javax.sql.DataSource;

@Configuration
@ComponentScan
@PropertySource("classpath:/test_support/test-app.properties")
@Import({CoreConfiguration.class,
        DataConfiguration.class,
        SecurityConfiguration.class,
        FlowuiConfiguration.class,
        EclipselinkConfiguration.class
})
@JmixModule(dependsOn = {MultitenancyConfiguration.class, MultitenancyFlowuiConfiguration.class})
@EnableWebSecurity
public class MultitenancyFlowuiTestConfiguration {

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
        return new JmixEntityManagerFactoryBean(Stores.MAIN,
                dataSource,
                jpaVendorAdapter,
                dbmsSpecifics,
                jmixModules,
                resources);
    }

    @Bean
    @Primary
    PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JmixTransactionManager(Stores.MAIN, entityManagerFactory);
    }

    @Bean
    @Primary
    public ServletContext servletContext() {
        return new TestServletContext();
    }

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }

    @Bean
    public ScriptEvaluator scriptEvaluator() {
        return new GroovyScriptEvaluator();
    }

    @Bean(name = "core_UserRepository")
    public UserRepository userRepository() {
        return new InMemoryUserRepository();
    }
}
