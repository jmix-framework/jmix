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

package io.jmix.autoconfigure.searchopensearch;

import com.google.common.base.Strings;
import io.jmix.core.*;
import io.jmix.data.DataConfiguration;
import io.jmix.search.SearchConfiguration;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.IndexManager;
import io.jmix.search.index.EntityIndexer;
import io.jmix.search.index.impl.IndexStateRegistry;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.searching.EntitySearcher;
import io.jmix.search.utils.SslConfigurer;
import io.jmix.searchopensearch.SearchOpenSearchConfiguration;
import io.jmix.searchopensearch.index.OpenSearchIndexSettingsProvider;
import io.jmix.searchopensearch.index.impl.OpenSearchEntityIndexer;
import io.jmix.searchopensearch.index.impl.OpenSearchIndexManager;
import io.jmix.searchopensearch.searching.impl.OpenSearchEntitySearcher;
import io.jmix.searchopensearch.searching.strategy.OpenSearchSearchStrategy;
import io.jmix.searchopensearch.searching.strategy.OpenSearchSearchStrategyProvider;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.opensearch.client.RestClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.rest_client.RestClientTransport;
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
@Import({CoreConfiguration.class, DataConfiguration.class, SearchConfiguration.class, SearchOpenSearchConfiguration.class})
public class SearchOpenSearchAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SearchOpenSearchAutoConfiguration.class);

    @Autowired
    protected SearchProperties searchProperties;
    @Autowired
    protected SslConfigurer sslConfigurer;

    @Bean("search_OpenSearchClient")
    @ConditionalOnMissingBean(OpenSearchClient.class)
    public OpenSearchClient openSearchClient() {
        HttpHost host = HttpHost.create(searchProperties.getServerUrl());
        CredentialsProvider credentialsProvider = createCredentialsProvider();
        SSLContext sslContext = sslConfigurer.createSslContext();

        RestClient restClient = RestClient.builder(host).
                setHttpClientConfigCallback(httpClientBuilder -> {
                    if (credentialsProvider != null) {
                        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }
                    if (sslContext != null) {
                        httpClientBuilder.setSSLContext(sslContext);
                    }
                    return httpClientBuilder;
                }).build();

        final OpenSearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new OpenSearchClient(transport);
    }

    @Bean("search_OpenSearchIndexManager")
    protected IndexManager openSearchIndexManager(OpenSearchClient client,
                                                  IndexStateRegistry indexStateRegistry,
                                                  IndexConfigurationManager indexConfigurationManager,
                                                  SearchProperties searchProperties,
                                                  OpenSearchIndexSettingsProvider indexSettingsProcessor) {
        return new OpenSearchIndexManager(client, indexStateRegistry, indexConfigurationManager, searchProperties, indexSettingsProcessor);
    }

    @Bean("search_OpenSearchEntityIndexer")
    protected EntityIndexer openSearchEntityIndexer(UnconstrainedDataManager dataManager,
                                                    FetchPlans fetchPlans,
                                                    IndexConfigurationManager indexConfigurationManager,
                                                    Metadata metadata,
                                                    IdSerialization idSerialization,
                                                    IndexStateRegistry indexStateRegistry,
                                                    MetadataTools metadataTools,
                                                    SearchProperties searchProperties,
                                                    OpenSearchClient client) {
        return new OpenSearchEntityIndexer(dataManager,
                fetchPlans,
                indexConfigurationManager,
                metadata,
                idSerialization,
                indexStateRegistry,
                metadataTools,
                searchProperties,
                client);
    }

    @Bean("search_OpenSearchEntitySearcher")
    protected EntitySearcher openSearchEntitySearcher(OpenSearchClient client,
                                                      IndexConfigurationManager indexConfigurationManager,
                                                      Metadata metadata,
                                                      MetadataTools metadataTools,
                                                      DataManager secureDataManager,
                                                      InstanceNameProvider instanceNameProvider,
                                                      SearchProperties searchProperties,
                                                      IdSerialization idSerialization,
                                                      SecureOperations secureOperations,
                                                      PolicyStore policyStore,
                                                      OpenSearchSearchStrategyProvider searchStrategyManager) {
        return new OpenSearchEntitySearcher(
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

    @Bean("search_OpenSearchSearchStrategyProvider")
    protected OpenSearchSearchStrategyProvider openSearchSearchStrategyProvider(
            Collection<OpenSearchSearchStrategy> searchStrategies,
            SearchProperties applicationProperties) {
        return new OpenSearchSearchStrategyProvider(searchStrategies, applicationProperties);
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
