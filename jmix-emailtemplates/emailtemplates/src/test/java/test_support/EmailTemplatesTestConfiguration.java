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

package test_support;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.JmixModules;
import io.jmix.core.Resources;
import io.jmix.core.Stores;
import io.jmix.core.security.InMemoryUserRepository;
import io.jmix.core.security.UserRepository;
import io.jmix.data.DataConfiguration;
import io.jmix.data.impl.JmixEntityManagerFactoryBean;
import io.jmix.data.impl.JmixTransactionManager;
import io.jmix.data.persistence.DbmsSpecifics;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.email.EmailConfiguration;
import io.jmix.emailtemplates.EmailTemplatesConfiguration;
import io.jmix.reports.ReportsConfiguration;
import io.jmix.security.SecurityConfiguration;
import io.jmix.security.StandardSecurityConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.mail.javamail.JavaMailSender;
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

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Collections;

@Configuration
@Import({CoreConfiguration.class, DataConfiguration.class, EmailTemplatesConfiguration.class, EmailConfiguration.class,
        ReportsConfiguration.class, SecurityConfiguration.class, EclipselinkConfiguration.class,
        EmailTemplatesTestConfiguration.TestStandardSecurityConfiguration.class})
public class EmailTemplatesTestConfiguration {
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
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }

    @Bean
    TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    @Bean
    public ScriptEvaluator scriptEvaluator() {
        return new GroovyScriptEvaluator();
    }

    @Bean(name = "core_UserRepository")
    public UserRepository userRepository() {
        InMemoryUserRepository repository = new InMemoryUserRepository();
        repository.addUser(User.builder()
                .username("admin")
                .password("{noop}admin")
                .authorities(Collections.emptyList())
                .build());
        return repository;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    @Primary
    public JavaMailSender jmixMailSender() {
        return new TestMailSender();
    }

    @EnableWebSecurity
    public static class TestStandardSecurityConfiguration extends StandardSecurityConfiguration {
    }
}
