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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "jmix.search")
@ConstructorBinding
public class SearchApplicationProperties {

    protected final int searchResultPageSize;
    protected final int maxSearchPageCount;
    protected final int searchReloadEntitiesBatchSize;
    protected final int processQueueBatchSize;
    protected final int maxProcessedQueueItemsPerExecution;
    protected final int reindexEntityEnqueueBatchSize;

    protected final boolean autoMapIndexFileContent;

    protected final boolean changedEntitiesIndexingEnabled;
    protected final boolean startupIndexSynchronizationEnabled;

    protected final Elasticsearch elasticsearch;

    protected final String defaultSearchStrategy;

    public SearchApplicationProperties(
            @DefaultValue("100") int searchResultPageSize,
            @DefaultValue("100") int maxSearchPageCount,
            @DefaultValue("100") int searchReloadEntitiesBatchSize,
            @DefaultValue("100") int processQueueBatchSize,
            @DefaultValue("1000") int maxProcessedQueueItemsPerExecution,
            @DefaultValue("100") int reindexEntityEnqueueBatchSize,
            @DefaultValue("false") boolean autoMapIndexFileContent,
            @DefaultValue("true") boolean changedEntitiesIndexingEnabled,
            @DefaultValue("true") boolean startupIndexSynchronizationEnabled,
            @DefaultValue("anyTermAnyField") String defaultSearchStrategy,
            @DefaultValue Elasticsearch elasticsearch) {
        this.searchResultPageSize = searchResultPageSize;
        this.maxSearchPageCount = maxSearchPageCount;
        this.searchReloadEntitiesBatchSize = searchReloadEntitiesBatchSize;
        this.processQueueBatchSize = processQueueBatchSize;
        this.maxProcessedQueueItemsPerExecution = maxProcessedQueueItemsPerExecution;
        this.reindexEntityEnqueueBatchSize = reindexEntityEnqueueBatchSize;
        this.autoMapIndexFileContent = autoMapIndexFileContent;
        this.changedEntitiesIndexingEnabled = changedEntitiesIndexingEnabled;
        this.startupIndexSynchronizationEnabled = startupIndexSynchronizationEnabled;
        this.defaultSearchStrategy = defaultSearchStrategy;
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
     * @return true if Auto Mapping strategy indexes file content. False if only file name is indexed
     */
    public boolean isAutoMapIndexFileContent() {
        return autoMapIndexFileContent;
    }

    /**
     * @return true if automatic indexing of changed entities is enabled. False otherwise
     */
    public boolean isChangedEntitiesIndexingEnabled() {
        return changedEntitiesIndexingEnabled;
    }

    /**
     * @return true is synchronization of indices on application startup is enabled. False otherwise
     */
    public boolean isStartupIndexSynchronizationEnabled() {
        return startupIndexSynchronizationEnabled;
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
     * @return true if AWS IAM user is used to access to Elasticsearch service, false if common base authentication is used
     */
    public boolean isElasticsearchAwsIamAuthentication() {
        return elasticsearch.aws.iamAuth;
    }

    /**
     * @return AWS Elasticsearch region. It's used to sign requests if IAM authentication is enabled
     */
    public String getElasticsearchAwsRegion() {
        return elasticsearch.aws.region;
    }

    /**
     * @return AWS Elasticsearch service name. It's used to sign requests if IAM authentication is enabled
     */
    public String getElasticsearchAwsServiceName() {
        return elasticsearch.aws.serviceName;
    }

    /**
     * @return Access Key of AWS IAM user that is used to access to Elasticsearch service if IAM authentication is enabled
     */
    public String getElasticsearchAwsAccessKey() {
        return elasticsearch.aws.accessKey;
    }

    /**
     * @return Secret Key of AWS IAM user that is used to access to Elasticsearch service if IAM authentication is enabled
     */
    public String getElasticsearchAwsSecretKey() {
        return elasticsearch.aws.secretKey;
    }

    protected static class Elasticsearch {
        protected final String url;
        protected final String login;
        protected final String password;
        protected final AWS aws;

        public Elasticsearch(
                @DefaultValue("localhost:9200") String url,
                String login,
                String password,
                @DefaultValue AWS aws) {
            this.url = url;
            this.login = login;
            this.password = password;
            this.aws = aws;
        }
    }

    protected static class AWS {
        protected final boolean iamAuth;
        protected final String region;
        protected final String serviceName;
        protected final String accessKey;
        protected final String secretKey;

        public AWS(
                @DefaultValue("false") boolean iamAuth,
                String region,
                @DefaultValue("es") String serviceName,
                String accessKey,
                String secretKey) {
            this.iamAuth = iamAuth;
            this.region = region;
            this.serviceName = serviceName;
            this.accessKey = accessKey;
            this.secretKey = secretKey;
        }
    }
}
