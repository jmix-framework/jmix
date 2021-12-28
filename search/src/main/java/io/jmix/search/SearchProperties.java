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

import io.jmix.core.Resources;
import io.jmix.search.index.IndexSchemaManagementStrategy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ConfigurationProperties(prefix = "jmix.search")
@ConstructorBinding
public class SearchProperties {

    /**
     * Max amount of objects displayed on single page of search result.
     */
    protected final int searchResultPageSize;

    /**
     * Max amount of result pages.
     */
    protected final int maxSearchPageCount;

    /**
     * Batch size for post-search entity processing.
     */
    protected final int searchReloadEntitiesBatchSize;

    /**
     * Amount of queue items processed in single batch.
     */
    protected final int processQueueBatchSize;

    /**
     * Amount of entity instances enqueued in single batch during entity reindex process.
     */
    protected final int reindexEntityEnqueueBatchSize;

    /**
     * Whether automatic indexing of changed entities is enabled.
     */
    protected final boolean changedEntitiesIndexingEnabled;

    /**
     * Whether the default Indexing Queue processing quartz scheduling configuration is used.
     */
    protected final boolean useDefaultIndexingQueueProcessingQuartzConfiguration;

    /**
     * Whether all entity instances related to indexes created or recreated on startup should be enqueued
     * automatically.
     */
    protected final boolean enqueueIndexAllOnStartupIndexRecreationEnabled;

    protected final Elasticsearch elasticsearch;

    /**
     * Name of default search strategy
     */
    protected final String defaultSearchStrategy;

    /**
     * CRON expression that is used by default Indexing Queue processing quartz scheduling configuration.
     */
    protected final String indexingQueueProcessingCron;

    /**
     * Prefix for search index name. Index naming template: &lt;prefix&gt;&lt;entity_name&gt;. Default prefix is
     * 'search_index_'.
     */
    protected final String searchIndexNamePrefix;

    /**
     * The way of index schema synchronization.
     */
    protected final IndexSchemaManagementStrategy indexSchemaManagementStrategy;

    /**
     * List of entities that should be automatically enqueued on startup in case of index recreation. Empty list means
     * all indexed entities.
     */
    protected final List<String> enqueueIndexAllOnStartupIndexRecreationEntities;

    public SearchProperties(
            @DefaultValue("100") int searchResultPageSize,
            @DefaultValue("100") int maxSearchPageCount,
            @DefaultValue("100") int searchReloadEntitiesBatchSize,
            @DefaultValue("100") int processQueueBatchSize,
            @DefaultValue("100") int reindexEntityEnqueueBatchSize,
            @DefaultValue("true") boolean changedEntitiesIndexingEnabled,
            @DefaultValue("true") boolean useDefaultIndexingQueueProcessingQuartzConfiguration,
            @DefaultValue("true") boolean enqueueIndexAllOnStartupIndexRecreationEnabled,
            @DefaultValue("") String enqueueIndexAllOnStartupIndexRecreationEntities,
            @DefaultValue("search_index_") String searchIndexNamePrefix,
            @DefaultValue("anyTermAnyField") String defaultSearchStrategy,
            @DefaultValue("create-or-recreate") String indexSchemaManagementStrategy,
            @DefaultValue("0/5 * * * * ?") String indexingQueueProcessingCron,
            @DefaultValue Elasticsearch elasticsearch) {
        this.searchResultPageSize = searchResultPageSize;
        this.maxSearchPageCount = maxSearchPageCount;
        this.searchReloadEntitiesBatchSize = searchReloadEntitiesBatchSize;
        this.processQueueBatchSize = processQueueBatchSize;
        this.reindexEntityEnqueueBatchSize = reindexEntityEnqueueBatchSize;
        this.changedEntitiesIndexingEnabled = changedEntitiesIndexingEnabled;
        this.useDefaultIndexingQueueProcessingQuartzConfiguration = useDefaultIndexingQueueProcessingQuartzConfiguration;
        this.indexingQueueProcessingCron = indexingQueueProcessingCron;
        this.defaultSearchStrategy = defaultSearchStrategy;
        this.indexSchemaManagementStrategy = IndexSchemaManagementStrategy.getByKey(indexSchemaManagementStrategy);
        this.elasticsearch = elasticsearch;
        this.enqueueIndexAllOnStartupIndexRecreationEnabled = enqueueIndexAllOnStartupIndexRecreationEnabled;
        this.enqueueIndexAllOnStartupIndexRecreationEntities = prepareStartupEnqueueingEntities(enqueueIndexAllOnStartupIndexRecreationEntities);
        this.searchIndexNamePrefix = searchIndexNamePrefix;
    }

    /**
     * @see #searchResultPageSize
     */
    public int getSearchResultPageSize() {
        return searchResultPageSize;
    }

    /**
     * @see #maxSearchPageCount
     */
    public int getMaxSearchPageCount() {
        return maxSearchPageCount;
    }

    /**
     * @see #searchReloadEntitiesBatchSize
     */
    public int getSearchReloadEntitiesBatchSize() {
        return searchReloadEntitiesBatchSize;
    }

    /**
     * @see #processQueueBatchSize
     */
    public int getProcessQueueBatchSize() {
        return processQueueBatchSize;
    }

    /**
     * @see #reindexEntityEnqueueBatchSize
     */
    public int getReindexEntityEnqueueBatchSize() {
        return reindexEntityEnqueueBatchSize;
    }

    /**
     * @see #changedEntitiesIndexingEnabled
     */
    public boolean isChangedEntitiesIndexingEnabled() {
        return changedEntitiesIndexingEnabled;
    }

    /**
     * @see #useDefaultIndexingQueueProcessingQuartzConfiguration
     */
    public boolean isUseDefaultIndexingQueueProcessingQuartzConfiguration() {
        return useDefaultIndexingQueueProcessingQuartzConfiguration;
    }

    /**
     * @see #indexingQueueProcessingCron
     */
    public String getIndexingQueueProcessingCron() {
        return indexingQueueProcessingCron;
    }

    /**
     * @see #searchIndexNamePrefix
     */
    public String getSearchIndexNamePrefix() {
        return searchIndexNamePrefix;
    }

    /**
     * @see #enqueueIndexAllOnStartupIndexRecreationEnabled
     */
    public boolean isEnqueueIndexAllOnStartupIndexRecreationEnabled() {
        return enqueueIndexAllOnStartupIndexRecreationEnabled;
    }

    /**
     * @see #enqueueIndexAllOnStartupIndexRecreationEntities
     */
    public List<String> getEnqueueIndexAllOnStartupIndexRecreationEntities() {
        return enqueueIndexAllOnStartupIndexRecreationEntities;
    }

    /**
     * @see #defaultSearchStrategy
     */
    public String getDefaultSearchStrategy() {
        return defaultSearchStrategy;
    }

    /**
     * @see Elasticsearch#url
     */
    public String getElasticsearchUrl() {
        return elasticsearch.url;
    }

    /**
     * @see Elasticsearch#login
     */
    public String getElasticsearchLogin() {
        return elasticsearch.login;
    }

    /**
     * @see Elasticsearch#password
     */
    public String getElasticsearchPassword() {
        return elasticsearch.password;
    }

    /**
     * @see SSL#certificateLocation
     */
    public String getElasticsearchSslCertificateLocation() {
        return elasticsearch.ssl.certificateLocation;
    }

    /**
     * @see SSL#certificateAlias
     */
    public String getElasticsearchSslCertificateAlias() {
        return elasticsearch.ssl.certificateAlias;
    }

    /**
     * @see SSL#certificateFactoryType
     */
    public String getElasticsearchSslCertificateFactoryType() {
        return elasticsearch.ssl.certificateFactoryType;
    }

    /**
     * @see SSL#keyStoreType
     */
    public String getElasticsearchSslKeyStoreType() {
        return elasticsearch.ssl.keyStoreType;
    }

    /**
     * @see #indexSchemaManagementStrategy
     */
    public IndexSchemaManagementStrategy getIndexSchemaManagementStrategy() {
        return indexSchemaManagementStrategy;
    }

    protected List<String> prepareStartupEnqueueingEntities(String enqueueIndexAllOnStartupIndexRecreationEntities) {
        List<String> result;
        if (StringUtils.isBlank(enqueueIndexAllOnStartupIndexRecreationEntities)) {
            result = Collections.emptyList();
        } else {
            result = Arrays.asList(enqueueIndexAllOnStartupIndexRecreationEntities.split(","));
        }
        return result;
    }

    protected static class Elasticsearch {

        /**
         * Elasticsearch URL.
         */
        protected final String url;

        /**
         * Elasticsearch login for common base authentication.
         */
        protected final String login;

        /**
         * Elasticsearch password for common base authentication.
         */
        protected final String password;

        protected final SSL ssl;

        public Elasticsearch(
                @DefaultValue("localhost:9200") String url,
                String login,
                String password,
                @DefaultValue SSL ssl) {
            this.url = url;
            this.login = login;
            this.password = password;
            this.ssl = ssl;
        }
    }

    protected static class SSL {

        /**
         * Location of CA certificate for connection to Elasticsearch service. Location is handled according to the
         * rules of {@link Resources}
         */
        protected final String certificateLocation;

        /**
         * Alias what will be used to store certificate to Key Store. "es_client_ca" by default.
         */
        protected final String certificateAlias;

        /**
         * Type of Certificate Factory. "X.509" by default.
         */
        protected final String certificateFactoryType;

        /**
         * Type of Key Store. "pkcs12" by default.
         */
        protected final String keyStoreType;

        public SSL(
                String certificateLocation,
                @DefaultValue("es_client_ca") String certificateAlias,
                @DefaultValue("X.509") String certificateFactoryType,
                @DefaultValue("pkcs12") String keyStoreType) {
            this.certificateLocation = certificateLocation;
            this.certificateAlias = certificateAlias;
            this.certificateFactoryType = certificateFactoryType;
            this.keyStoreType = keyStoreType;
        }
    }
}