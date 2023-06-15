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

import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.annotation.MessageSourceBasenames;
import io.jmix.search.index.EntityIndexer;
import io.jmix.search.index.impl.IndexStateRegistry;
import liquibase.integration.spring.SpringLiquibase;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import javax.sql.DataSource;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;

@Configuration
@JmixModule
@Import({BaseSearchTestConfiguration.class})
@PropertySource("classpath:/test_support/test-entity-indexing-app.properties")
@EnableWebSecurity
@MessageSourceBasenames({"test_support/messages"})
public class IndexingTestConfiguration {

    @Autowired
    protected AutowireCapableBeanFactory beanFactory;

    @Bean
    public TestAutoDetectableIndexDefinitionScope testAutoDetectableIndexDefinitionScope() {
        return TestAutoDetectableIndexDefinitionScope.builder().packages("test_support.indexing").build();
    }

    @Bean
    public SpringLiquibase liquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("test_support/liquibase/changelog.xml");
        return liquibase;
    }

    @Bean
    public TestBulkRequestsTracker bulkRequestsTracker() {
        return new TestBulkRequestsTracker();
    }

    @Bean
    public TestCommonEntityWrapperManager testCommonEntityWrapperManager(Metadata metadata, DataManager dataManager) {
        return new TestCommonEntityWrapperManager(metadata, dataManager);
    }

    @Bean
    @Primary
    public EntityIndexer testEntityIndexer() {
        return beanFactory.createBean(TestEntityIndexer.class);
    }

    @Bean
    @Primary
    public IndexStateRegistry testIndexStateRegistry() {
        IndexStateRegistry mock = mock(IndexStateRegistry.class);
        Mockito.when(mock.isIndexAvailable(anyString())).thenReturn(true);
        return mock;
    }

    @Bean
    @Primary
    public TestFileStorage testFileStorage() {
        return new TestFileStorage();
    }
}
