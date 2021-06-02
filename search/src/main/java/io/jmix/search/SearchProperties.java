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

package io.jmix.search;

import io.jmix.search.index.IndexSchemaManagementStrategy;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "jmix.search")
@ConstructorBinding
public class SearchProperties {

    protected final int searchResultPageSize;
    protected final int maxSearchPageCount;
    protected final int searchReloadEntitiesBatchSize;
    protected final int processQueueBatchSize;
    protected final int maxProcessedQueueItemsPerExecution;
    protected final int reindexEntityEnqueueBatchSize;

    protected final boolean changedEntitiesIndexingEnabled;

    protected final Elasticsearch elasticsearch;

    protected final String defaultSearchStrategy;

    protected final IndexSchemaManagementStrategy indexSchemaManagementStrategy;

    public SearchProperties(
            @DefaultValue("100") int searchResultPageSize,
            @DefaultValue("100") int maxSearchPageCount,
            @DefaultValue("100") int searchReloadEntitiesBatchSize,
            @DefaultValue("100") int processQueueBatchSize,
            @DefaultValue("1000") int maxProcessedQueueItemsPerExecution,
            @DefaultValue("100") int reindexEntityEnqueueBatchSize,
            @DefaultValue("true") boolean changedEntitiesIndexingEnabled,
            @DefaultValue("anyTermAnyField") String defaultSearchStrategy,
            @DefaultValue("create-or-recreate") String indexSchemaManagementStrategy,
            @DefaultValue Elasticsearch elasticsearch) {
        this.searchResultPageSize = searchResultPageSize;
        this.maxSearchPageCount = maxSearchPageCount;
        this.searchReloadEntitiesBatchSize = searchReloadEntitiesBatchSize;
        this.processQueueBatchSize = processQueueBatchSize;
        this.maxProcessedQueueItemsPerExecution = maxProcessedQueueItemsPerExecution;
        this.reindexEntityEnqueueBatchSize = reindexEntityEnqueueBatchSize;
        this.changedEntitiesIndexingEnabled = changedEntitiesIndexingEnabled;
        this.defaultSearchStrategy = defaultSearchStrategy;
        this.indexSchemaManagementStrategy = IndexSchemaManagementStrategy.getByKey(indexSchemaManagementStrategy);
        this.elasticsearch = elasticsearch;
    }

    /**
     * @return max amount of objects displayed on single page of search result
     */
    public int getSearchResultPageSize() {
        return searchResultPageSize;
    }

    /**
     * @return max amount of result pages
     */
    public int getMaxSearchPageCount() {
        return maxSearchPageCount;
    }

    /**
     * @return batch size for post-search entity processing
     */
    public int getSearchReloadEntitiesBatchSize() {
        return searchReloadEntitiesBatchSize;
    }

    /**
     * @return amount of queue items processed in single batch
     */
    public int getProcessQueueBatchSize() {
        return processQueueBatchSize;
    }

    /**
     * @return max amount of items can be processed during single execution
     */
    public int getMaxProcessedQueueItemsPerExecution() {
        return maxProcessedQueueItemsPerExecution;
    }

    /**
     * @return amount of entity instances enqueued in single batch during entity reindex process
     */
    public int getReindexEntityEnqueueBatchSize() {
        return reindexEntityEnqueueBatchSize;
    }

    /**
     * @return true if automatic indexing of changed entities is enabled. False otherwise
     */
    public boolean isChangedEntitiesIndexingEnabled() {
        return changedEntitiesIndexingEnabled;
    }

    /**
     * @return name of default search strategy
     */
    public String getDefaultSearchStrategy() {
        return defaultSearchStrategy;
    }

    /**
     * @return Elasticsearch URL
     */
    public String getElasticsearchUrl() {
        return elasticsearch.url;
    }

    /**
     * @return Elasticsearch login for common base authentication
     */
    public String getElasticsearchLogin() {
        return elasticsearch.login;
    }

    /**
     * @return Elasticsearch password for common base authentication
     */
    public String getElasticsearchPassword() {
        return elasticsearch.password;
    }

    /**
     * @return The way of index schema synchronization
     */
    public IndexSchemaManagementStrategy getIndexSchemaManagementStrategy() {
        return indexSchemaManagementStrategy;
    }

    protected static class Elasticsearch {
        protected final String url;
        protected final String login;
        protected final String password;

        public Elasticsearch(
                @DefaultValue("localhost:9200") String url,
                String login,
                String password) {
            this.url = url;
            this.login = login;
            this.password = password;
        }
    }
}