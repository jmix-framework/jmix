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
import io.jmix.search.utils.SslConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({CoreConfiguration.class, DataConfiguration.class, SearchConfiguration.class})
public class SearchAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SearchAutoConfiguration.class);

    @Autowired
    protected SearchProperties searchProperties;
    @Autowired
    protected SslConfigurer sslConfigurer;

    /*@Bean("search_RestHighLevelClient")
    @ConditionalOnMissingBean(RestHighLevelClient.class)
    public RestHighLevelClient elasticSearchClient() {
        log.debug("Create simple ES Client");

        String esUrl = searchProperties.getElasticsearchUrl();
        HttpHost esHttpHost = HttpHost.create(esUrl);
        RestClientBuilder restClientBuilder = RestClient.builder(esHttpHost);

        CredentialsProvider credentialsProvider = createCredentialsProvider();
        SSLContext sslContext = sslConfigurer.createSslContext();

        restClientBuilder.setHttpClientConfigCallback(httpClientBuilder -> {
            if (credentialsProvider != null) {
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
            if (sslContext != null) {
                httpClientBuilder.setSSLContext(sslContext);
            }
            return httpClientBuilder;
        });

        return new RestHighLevelClientBuilder(restClientBuilder.build())
                .setApiCompatibilityMode(searchProperties.isRestHighLevelClientApiCompatibilityModeEnabled())
                .build();
    }*/

    /*@Bean("search_ElasticsearchClient") //todo
    @ConditionalOnProperty(name = "jmix.search.platform", havingValue = "es") //todo
    public ElasticsearchClient elasticsearchClient() {
        CredentialsProvider credentialsProvider = createCredentialsProvider();
        SSLContext sslContext = sslConfigurer.createSslContext();

        String esUrl = searchProperties.getElasticsearchUrl();
        RestClient restClient = RestClient
                .builder(HttpHost.create(esUrl))
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


        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }*/

    /*@Bean("search_OpenSearchClient") //todo
    @ConditionalOnProperty(name = "jmix.search.platform", havingValue = "os") //todo
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

    /*@Bean("search_ElasticsearchIndexManager")
    @ConditionalOnProperty(name = "jmix.search.platform", havingValue = "es") //todo
    protected ESIndexManager elasticsearchIndexManager(ElasticsearchClient client,
                                                       IndexConfigurationManager indexConfigurationManager,
                                                       SearchProperties searchProperties,
                                                       IndexStateRegistry indexStateRegistry,
                                                       ElasticsearchIndexSettingsConfigurerProcessor indexSettingsProcessor) {
        return new ElasticsearchIndexManager(client, indexStateRegistry, indexConfigurationManager, searchProperties, indexSettingsProcessor);
    }

    @Bean("search_OpenSearchIndexManager")
    @ConditionalOnProperty(name = "jmix.search.platform", havingValue = "os") //todo
    protected ESIndexManager openSearchIndexManager(OpenSearchClient client,
                                                    IndexStateRegistry indexStateRegistry,
                                                    IndexConfigurationManager indexConfigurationManager,
                                                    SearchProperties searchProperties,
                                                    OpenSearchIndexSettingsConfigurerProcessor indexSettingsProcessor) {
        return new OpenSearchIndexManager(client, indexStateRegistry, indexConfigurationManager, searchProperties, indexSettingsProcessor);
    }

    @Bean("search_ElasticsearchEntityIndexer")
    @ConditionalOnProperty(name = "jmix.search.platform", havingValue = "es") //todo
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

    @Bean("search_OpenSearchEntityIndexer")
    @ConditionalOnProperty(name = "jmix.search.platform", havingValue = "os") //todo
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
    @ConditionalOnProperty(name = "jmix.search.platform", havingValue = "os") //todo
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

    @Bean("search_ElasticsearchEntitySearcher")
    @ConditionalOnProperty(name = "jmix.search.platform", havingValue = "es") //todo
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
                                                         ElasticsearchSearchStrategyManager searchStrategyManager) {
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
    }*/
}
