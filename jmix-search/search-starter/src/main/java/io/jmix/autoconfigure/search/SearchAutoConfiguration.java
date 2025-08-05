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

package io.jmix.autoconfigure.search;

import io.jmix.core.CoreConfiguration;
import io.jmix.data.DataConfiguration;
import io.jmix.search.SearchConfiguration;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.EntityIndexer;
import io.jmix.search.index.IndexManager;
import io.jmix.search.index.impl.NoopEntityIndexer;
import io.jmix.search.index.impl.NoopEntitySearcher;
import io.jmix.search.index.impl.NoopIndexManager;
import io.jmix.search.index.impl.NoopIndexingQueueManager;
import io.jmix.search.index.queue.IndexingQueueManager;
import io.jmix.search.index.queue.impl.JpaIndexingQueueManager;
import io.jmix.search.searching.EntitySearcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({CoreConfiguration.class, DataConfiguration.class, SearchConfiguration.class})
public class SearchAutoConfiguration {

    @Autowired
    protected AutowireCapableBeanFactory beanFactory;

    @Bean("search_IndexManager")
    @ConditionalOnProperty(name = "jmix.search.enabled", havingValue = "false")
    public IndexManager indexManager() { return new NoopIndexManager(); }

    @Bean("search_EntitySearcher")
    @ConditionalOnProperty(name = "jmix.search.enabled", havingValue = "false")
    public EntitySearcher entitySearcher() { return new NoopEntitySearcher(); }

    @Bean("search_EntityIndexer")
    @ConditionalOnProperty(name = "jmix.search.enabled", havingValue = "false")
    public EntityIndexer entityIndexer() { return new NoopEntityIndexer(); }

    @Bean("search_IndexingQueueManager")
    public IndexingQueueManager indexingQueueManager(SearchProperties searchProperties) {
        if (searchProperties.isEnabled()) {
            return beanFactory.createBean(JpaIndexingQueueManager.class);
        }

        return new NoopIndexingQueueManager();
    }
}
