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

import io.jmix.core.CoreConfiguration;
import io.jmix.core.IdSerialization;
import io.jmix.core.security.InMemoryUserRepository;
import io.jmix.core.security.UserRepository;
import io.jmix.data.DataConfiguration;
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
import io.jmix.testsupport.config.CommonCoreTestConfiguration;
import io.jmix.testsupport.config.HsqlMemDataSourceTestConfiguration;
import io.jmix.testsupport.config.JpaMainStoreTestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.lang.Nullable;

@Configuration
@Import({
        CoreConfiguration.class,
        DataConfiguration.class,
        EclipselinkConfiguration.class,
        SecurityConfiguration.class,
        SearchConfiguration.class,
        DynAttrConfiguration.class,
        CommonCoreTestConfiguration.class,
        HsqlMemDataSourceTestConfiguration.class,
        JpaMainStoreTestConfiguration.class
})
public class BaseSearchTestConfiguration {

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
    @Primary
    public DynAttrManager dynAttrManager() {
        return new NoopDynAttrManagerImpl();
    }
}
