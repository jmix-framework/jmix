/*
 * Copyright 2021 Haulmont.
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
import io.jmix.core.cluster.ClusterApplicationEventChannelSupplier;
import io.jmix.core.cluster.LocalApplicationEventChannelSupplier;
import io.jmix.core.impl.JmixMessageSource;
import io.jmix.core.security.InMemoryUserRepository;
import io.jmix.core.security.UserRepository;
import io.jmix.data.DataConfiguration;
import io.jmix.data.impl.JmixEntityManagerFactoryBean;
import io.jmix.data.impl.JmixTransactionManager;
import io.jmix.data.persistence.DbmsSpecifics;
import io.jmix.dynattr.DynAttrConfiguration;
import io.jmix.dynattr.DynAttrManager;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.search.SearchConfiguration;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.EntityIndexer;
import io.jmix.search.index.IndexManager;
import io.jmix.search.index.impl.IndexStateRegistry;
import io.jmix.search.index.impl.StartupIndexSynchronizer;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.index.mapping.processor.impl.IndexDefinitionDetector;
import io.jmix.search.index.queue.IndexingQueueManager;
import io.jmix.security.SecurityConfiguration;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;

import org.springframework.lang.Nullable;
import jakarta.persistence.EntityManagerFactory;

import javax.sql.DataSource;

@Configuration
@Import({
        CoreConfiguration.class,
        DataConfiguration.class,
        EclipselinkConfiguration.class,
        SecurityConfiguration.class,
        SearchConfiguration.class,
        DynAttrConfiguration.class})
public class BaseSearchTestConfiguration {

    @Autowired
    SearchProperties searchProperties;
    @Autowired
    AutowireCapableBeanFactory beanFactory;

    // Test Search beans

    @Bean("search_StartupIndexSynchronizer")
    @Primary
    public StartupIndexSynchronizer startupIndexSynchronizer() {
        return new TestNoopStartupIndexSynchronizer();
    }

    @Bean("search_EntityIndexer")
    public EntityIndexer entityIndexer() {
        return new TestNoopEntityIndexer();
    }

    @Bean("search_IndexManager")
    public IndexManager indexManager(IndexConfigurationManager indexConfigurationManager,
                                     IndexStateRegistry indexStateRegistry,
                                     SearchProperties searchProperties) {
        return new TestNoopIndexManager(indexConfigurationManager, indexStateRegistry, searchProperties);
    }

    @Bean("search_JpaIndexingQueueManager")
    public IndexingQueueManager indexingQueueManager() {
        return beanFactory.createBean(TestJpaIndexingQueueManager.class);
    }

    @Bean
    public TestIndexingQueueItemsTracker testIndexingQueueItemsTracker(IdSerialization idSerialization) {
        return new TestIndexingQueueItemsTracker(idSerialization);
    }

    @Bean
    @Primary
    public IndexDefinitionDetector indexDefinitionDetector(@Nullable TestAutoDetectableIndexDefinitionScope testAutoDetectableIndexDefinitionScope) {
        return new TestIndexDefinitionDetector(testAutoDetectableIndexDefinitionScope);
    }

    // Test Common beans

    @Bean
    public UserRepository userRepository() {
        return new InMemoryUserRepository();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public DataSource dataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl("jdbc:hsqldb:mem:testdb");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
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
    public MessageSource messageSource(JmixModules modules, Resources resources) {
        return new JmixMessageSource(modules, resources);
    }

    @Bean
    public ClusterApplicationEventChannelSupplier clusterApplicationEventChannelSupplier() {
        return new LocalApplicationEventChannelSupplier();
    }

    @Bean
    @Primary
    public DynAttrManager dynAttrManager(){
        return new NoopDynAttrManagerImpl();
    }
}
