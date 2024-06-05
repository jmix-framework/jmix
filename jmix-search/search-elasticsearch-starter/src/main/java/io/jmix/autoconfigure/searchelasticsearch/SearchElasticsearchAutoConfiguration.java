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

package io.jmix.autoconfigure.searchelasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.google.common.base.Strings;
import io.jmix.core.*;
import io.jmix.data.DataConfiguration;
import io.jmix.search.SearchConfiguration;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.EntityIndexer;
import io.jmix.search.index.IndexManager;
import io.jmix.search.index.impl.IndexStateRegistry;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.searching.EntitySearcher;
import io.jmix.search.utils.SslConfigurer;
import io.jmix.searchelasticsearch.SearchElasticsearchConfiguration;
import io.jmix.searchelasticsearch.index.ElasticsearchIndexSettingsProvider;
import io.jmix.searchelasticsearch.index.impl.ElasticsearchEntityIndexer;
import io.jmix.searchelasticsearch.index.impl.ElasticsearchIndexManager;
import io.jmix.searchelasticsearch.searching.impl.ElasticsearchEntitySearcher;
import io.jmix.searchelasticsearch.searching.strategy.ElasticsearchSearchStrategy;
import io.jmix.searchelasticsearch.searching.strategy.ElasticsearchSearchStrategyProvider;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import java.util.Collection;

@AutoConfiguration
@Import({CoreConfiguration.class, DataConfiguration.class, SearchConfiguration.class, SearchElasticsearchConfiguration.class})
public class SearchElasticsearchAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SearchElasticsearchAutoConfiguration.class);

    @Autowired
    protected SearchProperties searchProperties;
    @Autowired
    protected SslConfigurer sslConfigurer;

    @Bean("search_ElasticsearchClient")
    @ConditionalOnMissingBean(ElasticsearchClient.class)
    public ElasticsearchClient elasticsearchClient() {
        CredentialsProvider credentialsProvider = createCredentialsProvider();
        SSLContext sslContext = sslConfigurer.createSslContext();

        String url = searchProperties.getServerUrl();
        RestClient restClient = RestClient
                .builder(HttpHost.create(url))
                .setHttpClientConfigCallback(httpClientBuilder -> {
                    if (credentialsProvider != null) {
                        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }
                    if (sslContext != null) {
                        httpClientBuilder.setSSLContext(sslContext);
                    }
                    return httpClientBuilder;
                })
                .build();


        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }

    @Bean("search_ElasticsearchIndexManager")
    protected IndexManager elasticsearchIndexManager(ElasticsearchClient client,
                                                     IndexConfigurationManager indexConfigurationManager,
                                                     SearchProperties searchProperties,
                                                     IndexStateRegistry indexStateRegistry,
                                                     ElasticsearchIndexSettingsProvider indexSettingsProcessor) {
        return new ElasticsearchIndexManager(client,
                indexStateRegistry,
                indexConfigurationManager,
                searchProperties,
                indexSettingsProcessor
        );
    }

    @Bean("search_ElasticsearchEntityIndexer")
    protected EntityIndexer elasticsearchEntityIndexer(UnconstrainedDataManager dataManager,
                                                       FetchPlans fetchPlans,
                                                       IndexConfigurationManager indexConfigurationManager,
                                                       Metadata metadata,
                                                       IdSerialization idSerialization,
                                                       IndexStateRegistry indexStateRegistry,
                                                       MetadataTools metadataTools,
                                                       SearchProperties searchProperties,
                                                       ElasticsearchClient client) {
        return new ElasticsearchEntityIndexer(dataManager,
                fetchPlans,
                indexConfigurationManager,
                metadata,
                idSerialization,
                indexStateRegistry,
                metadataTools,
                searchProperties,
                client);
    }

    @Bean("search_ElasticsearchEntitySearcher")
    protected EntitySearcher elasticsearchEntitySearcher(ElasticsearchClient client,
                                                         IndexConfigurationManager indexConfigurationManager,
                                                         Metadata metadata,
                                                         MetadataTools metadataTools,
                                                         DataManager secureDataManager,
                                                         InstanceNameProvider instanceNameProvider,
                                                         SearchProperties searchProperties,
                                                         IdSerialization idSerialization,
                                                         SecureOperations secureOperations,
                                                         PolicyStore policyStore,
                                                         ElasticsearchSearchStrategyProvider searchStrategyManager) {
        return new ElasticsearchEntitySearcher(
                client,
                indexConfigurationManager,
                metadata,
                metadataTools,
                secureDataManager,
                instanceNameProvider,
                searchProperties,
                idSerialization,
                secureOperations,
                policyStore,
                searchStrategyManager
        );
    }

    @Bean("search_ElasticsearchSearchStrategyProvider")
    protected ElasticsearchSearchStrategyProvider elasticsearchSearchStrategyProvider(
            Collection<ElasticsearchSearchStrategy> searchStrategies,
            SearchProperties applicationProperties) {
        return new ElasticsearchSearchStrategyProvider(searchStrategies, applicationProperties);
    }

    @Nullable
    protected CredentialsProvider createCredentialsProvider() {
        CredentialsProvider credentialsProvider = null;
        if (!Strings.isNullOrEmpty(searchProperties.getServerLogin())) {
            credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(
                            searchProperties.getServerLogin(),
                            searchProperties.getServerPassword()
                    )
            );
        }
        return credentialsProvider;
    }
}
