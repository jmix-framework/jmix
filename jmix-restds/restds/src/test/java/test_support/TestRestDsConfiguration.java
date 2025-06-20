/*
 * Copyright 2024 Haulmont.
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
import io.jmix.core.security.AddonAuthenticationManagerSupplier;
import io.jmix.core.security.UserRepository;
import io.jmix.data.DataConfiguration;
import io.jmix.data.impl.JmixEntityManagerFactoryBean;
import io.jmix.data.impl.JmixTransactionManager;
import io.jmix.data.persistence.DbmsSpecifics;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.restds.RestDsConfiguration;
import io.jmix.restds.impl.RestAuthenticationManagerSupplier;
import io.jmix.restds.filestorage.RestFileStorage;
import io.jmix.restds.impl.RestPasswordAuthenticator;
import io.jmix.restds.impl.RestTokenHolder;
import io.jmix.security.SecurityConfiguration;
import io.jmix.security.authentication.StandardAuthenticationProvidersProducer;
import jakarta.persistence.EntityManagerFactory;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import test_support.security.TestInMemoryUserRepository;
import test_support.security.TestRestUserRepository;

import javax.sql.DataSource;

@Configuration
@ComponentScan
@Import({RestDsConfiguration.class, SecurityConfiguration.class, EclipselinkConfiguration.class, DataConfiguration.class,
        CoreConfiguration.class})
@PropertySource("classpath:/test_support/test-app.properties")
@JmixModule(dependsOn = {RestDsConfiguration.class, SecurityConfiguration.class, EclipselinkConfiguration.class, DataConfiguration.class})
public class TestRestDsConfiguration {

    @Bean
    FileStorage restService1FileStorage() {
        return new RestFileStorage("restService1", "fs");
    }

    // In production code created by autoconfiguration
    @Bean("restds_RestAuthenticationManagerSupplier")
    @Order(100)
    AddonAuthenticationManagerSupplier restAuthenticationManagerSupplier(StandardAuthenticationProvidersProducer providersProducer,
                                                                                ApplicationEventPublisher publisher,
                                                                                UserDetailsService userDetailsService,
                                                                                ApplicationContext applicationContext) {

        String storeName = applicationContext.getEnvironment().getRequiredProperty("jmix.restds.authentication-provider-store");

        RestPasswordAuthenticator restAuthenticator = applicationContext.getBean(RestPasswordAuthenticator.class);
        restAuthenticator.setDataStoreName(storeName);

        return new RestAuthenticationManagerSupplier(providersProducer, publisher, restAuthenticator, userDetailsService);
    }

    @Bean
    RestTokenHolder restTokenHolder() {
        return new ThreadLocalRestTokenHolder();
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

    @Profile("!passwordGrant")
    @Bean
    public UserRepository inMemoryUserRepository() {
        return new TestInMemoryUserRepository();
    }

    @Profile("passwordGrant")
    @Bean
    public UserRepository restUserRepository() {
        return new TestRestUserRepository();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    @Primary
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }

    @Bean
    @Primary
    public ClusterApplicationEventChannelSupplier clusterApplicationEventChannelSupplier() {
        return new LocalApplicationEventChannelSupplier();
    }
}
