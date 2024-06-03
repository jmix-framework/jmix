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
import io.jmix.search.index.ESIndexManager;
import io.jmix.search.index.EntityIndexer;
import io.jmix.search.index.impl.IndexStateRegistry;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.searching.EntitySearcher;
import io.jmix.search.utils.ElasticsearchSslConfigurer;
import io.jmix.searchopensearch.index.OpenSearchIndexSettingsConfigurerProcessor;
import io.jmix.searchopensearch.index.impl.OpenSearchEntityIndexer;
import io.jmix.searchopensearch.index.impl.OpenSearchIndexManager;
import io.jmix.searchopensearch.searching.impl.OpenSearchEntitySearcher;
import io.jmix.searchopensearch.searching.strategy.OpenSearchSearchStrategyManager;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;

@AutoConfiguration
@Import({CoreConfiguration.class, DataConfiguration.class, SearchConfiguration.class})
public class SearchOpenSearchAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SearchOpenSearchAutoConfiguration.class);

    @Autowired
    protected SearchProperties searchProperties;
    @Autowired
    protected ElasticsearchSslConfigurer elasticsearchSslConfigurer;


    /*@Bean("search_OpenSearchClient")
    //@ConditionalOnProperty(name = "jmix.search.platform", havingValue = "os") //todo
    public OpenSearchClient openSearchClient() {
        *//*System.setProperty("javax.net.ssl.trustStore", "com/company/sandbox/keystore/localhost.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "123qwe");*//*

        final org.apache.hc.core5.http.HttpHost host = new org.apache.hc.core5.http.HttpHost("http", "localhost", 9200);
        final org.apache.hc.client5.http.auth.CredentialsProvider credentialsProvider = new org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider();
        // Only for demo purposes. Don't specify your credentials in code.
        //credentialsProvider.setCredentials(new AuthScope(host), new UsernamePasswordCredentials("admin", "admin".toCharArray()));

        *//*final SSLContext sslcontext = SSLContextBuilder
                .create()
                .loadTrustMaterial(null, (chains, authType) -> true)
                .build();*//*

        final ApacheHttpClient5TransportBuilder builder = ApacheHttpClient5TransportBuilder.builder(host);
        builder.setHttpClientConfigCallback(httpClientBuilder -> {
            *//*final TlsStrategy tlsStrategy = ClientTlsStrategyBuilder.create()
                    .setSslContext(SSLContextBuilder.create().build())
                    // See https://issues.apache.org/jira/browse/HTTPCLIENT-2219
                    .setTlsDetailsFactory(new Factory<SSLEngine, TlsDetails>() {
                        @Override
                        public TlsDetails create(final SSLEngine sslEngine) {
                            return new TlsDetails(sslEngine.getSession(), sslEngine.getApplicationProtocol());
                        }
                    })
                    .build();*//*

            final PoolingAsyncClientConnectionManager connectionManager = PoolingAsyncClientConnectionManagerBuilder
                    .create()
                    //.setTlsStrategy(tlsStrategy)
                    .build();

            return httpClientBuilder
                    .setDefaultCredentialsProvider(credentialsProvider)
                    .setConnectionManager(connectionManager);
        });

        final OpenSearchTransport transport = ApacheHttpClient5TransportBuilder.builder(host).build();
        return new OpenSearchClient(transport);
    }*/

    @Bean("search_OpenSearchClient")
    public OpenSearchClient openSearchClient() {
        HttpHost host = HttpHost.create(searchProperties.getElasticsearchUrl());
        CredentialsProvider credentialsProvider = createCredentialsProvider();
        SSLContext sslContext = elasticsearchSslConfigurer.createSslContext();

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
    protected ESIndexManager openSearchIndexManager(OpenSearchClient client,
                                                    IndexStateRegistry indexStateRegistry,
                                                    IndexConfigurationManager indexConfigurationManager,
                                                    SearchProperties searchProperties,
                                                    OpenSearchIndexSettingsConfigurerProcessor indexSettingsProcessor) {
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
                                                      OpenSearchSearchStrategyManager searchStrategyManager) {
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

    @Nullable
    protected CredentialsProvider createCredentialsProvider() {
        CredentialsProvider credentialsProvider = null;
        if (!Strings.isNullOrEmpty(searchProperties.getElasticsearchLogin())) {
            credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(
                            searchProperties.getElasticsearchLogin(),
                            searchProperties.getElasticsearchPassword()
                    )
            );
        }
        return credentialsProvider;
    }
}
