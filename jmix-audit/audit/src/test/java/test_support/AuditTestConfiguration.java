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

import io.jmix.audit.AuditConfiguration;
import io.jmix.core.JmixModules;
import io.jmix.core.Resources;
import io.jmix.core.Stores;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.impl.JmixMessageSource;
import io.jmix.core.security.InMemoryUserRepository;
import io.jmix.core.security.ServiceUserProvider;
import io.jmix.core.security.UserRepository;
import io.jmix.core.security.impl.SystemAuthenticationProvider;
import io.jmix.core.security.user.DefaultServiceUserProvider;
import io.jmix.data.impl.JmixEntityManagerFactoryBean;
import io.jmix.data.impl.JmixTransactionManager;
import io.jmix.data.persistence.DbmsSpecifics;
import io.jmix.eclipselink.impl.JmixEclipselinkTransactionManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Configuration
@ComponentScan
@PropertySource("classpath:/test_support/test-app.properties")
@JmixModule(dependsOn = AuditConfiguration.class)
//@Import(AuditTestConfiguration.TestSecurityConfiguration.class)
public class AuditTestConfiguration {

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
    CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }

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
        return new JmixEntityManagerFactoryBean("db1", db1DataSource(), jpaVendorAdapter, dbmsSpecifics, jmixModules, resources);
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

    @Bean
    AuthenticationManager authenticationManager(UserRepository userRepository, ServiceUserProvider serviceUserProvider) {
        List<AuthenticationProvider> providers = new ArrayList<>();
        SystemAuthenticationProvider systemAuthenticationProvider = new SystemAuthenticationProvider(userRepository, serviceUserProvider);
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userRepository);
        providers.add(systemAuthenticationProvider);
        providers.add(daoAuthenticationProvider);
        return new ProviderManager(providers);
    }

    @Bean
    public ServiceUserProvider serviceUsersProvider() {
        return new DefaultServiceUserProvider();
    }

    @Bean
    public UserRepository userRepository() {
        return new InMemoryUserRepository();
    }

//    @EnableWebSecurity
//    static class TestSecurityConfiguration {
//    }
//    @EnableWebSecurity
//    static class TestSecurityConfiguration extends CoreSecurityConfiguration {
//    }
}
