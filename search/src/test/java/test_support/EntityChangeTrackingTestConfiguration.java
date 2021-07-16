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

package test_support;/*
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

import io.jmix.core.DataManager;
import io.jmix.core.IdSerialization;
import io.jmix.core.Metadata;
import io.jmix.core.Stores;
import io.jmix.core.annotation.JmixModule;
import io.jmix.data.impl.liquibase.JmixLiquibase;
import io.jmix.data.impl.liquibase.LiquibaseChangeLogProcessor;
import io.jmix.search.index.queue.IndexingQueueManager;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.*;
import test_support.change_tracking.TestRootEntityIndexDefinition;

import javax.sql.DataSource;

@Configuration
@JmixModule
@Import({BaseSearchTestConfiguration.class})
@PropertySource("classpath:/test_support/test-app.properties")
public class EntityChangeTrackingTestConfiguration {

    @Autowired
    protected AutowireCapableBeanFactory beanFactory;

    @Bean
    public TestAutoDetectableIndexDefinitionScope testAutoDetectableIndexDefinitionScope() {
        return TestAutoDetectableIndexDefinitionScope.builder()
                .classes(TestRootEntityIndexDefinition.class)
                .build();
    }

    @Bean
    public SpringLiquibase liquibase(DataSource dataSource, LiquibaseChangeLogProcessor processor) {
        JmixLiquibase liquibase = new JmixLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLogContent(processor.createMasterChangeLog(Stores.MAIN));
        return liquibase;
    }

    @Bean
    public TestIndexingQueueItemsTracker testIndexingQueueItemsTracker(IdSerialization idSerialization) {
        return new TestIndexingQueueItemsTracker(idSerialization);
    }

    @Bean("search_JpaIndexingQueueManager")
    @Primary
    public IndexingQueueManager indexingQueueManager() {
        return beanFactory.createBean(TestJpaIndexingQueueManager.class);
    }

    @Bean
    public TestEntityWrapperManager testEntityWrapperManager(Metadata metadata, DataManager dataManager) {
        return new TestEntityWrapperManager(metadata, dataManager);
    }
}
