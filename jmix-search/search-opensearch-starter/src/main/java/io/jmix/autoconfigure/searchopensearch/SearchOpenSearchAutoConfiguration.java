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
import io.jmix.search.index.EntityIndexer;
import io.jmix.search.index.IndexManager;
import io.jmix.search.index.impl.IndexStateRegistry;
import io.jmix.search.index.impl.dynattr.DynamicAttributesSupport;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.searching.EntitySearcher;
import io.jmix.search.utils.SslConfigurer;
import io.jmix.searchopensearch.SearchOpenSearchConfiguration;
import io.jmix.searchopensearch.index.OpenSearchIndexSettingsProvider;
import io.jmix.searchopensearch.index.impl.*;
import io.jmix.searchopensearch.searching.impl.OpenSearchEntitySearcher;
import io.jmix.searchopensearch.searching.strategy.OpenSearchSearchStrategy;
import io.jmix.searchopensearch.searching.strategy.OpenSearchSearchStrategyProvider;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.opensearch.client.RestClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.jspecify.annotations.Nullable;

import javax.net.ssl.SSLContext;
import java.net.URISyntaxException;
import java.util.Collection;

@AutoConfiguration
@Import({CoreConfiguration.class,
        DataConfiguration.class,
        SearchConfiguration.class,
        SearchOpenSearchConfiguration.class})
public class SearchOpenSearchAutoConfiguration {

    @Autowired
    protected SearchProperties searchProperties;
    @Autowired
    protected SslConfigurer sslConfigurer;

    @Bean("search_OpenSearchClient")
    @ConditionalOnMissingBean(OpenSearchClient.class)
    public OpenSearchClient openSearchClient() {
        String url = searchProperties.getServerUrl();
        HttpHost httpHost;
        try {
            httpHost = HttpHost.create(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid OpenSearch URL: " + url, e);
        }
        CredentialsProvider credentialsProvider = createCredentialsProvider();
        SSLContext sslContext = sslConfigurer.createSslContext();

        RestClient restClient = RestClient.builder(httpHost).
                setHttpClientConfigCallback(httpClientBuilder -> {
                    if (credentialsProvider != null) {
                        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }
                    if (sslContext != null) {
                        TlsStrategy tlsStrategy = ClientTlsStrategyBuilder.create()
                                .setSslContext(sslContext)
                                .build();

                        PoolingAsyncClientConnectionManager connectionManager =
                                PoolingAsyncClientConnectionManagerBuilder.create()
                                        .setTlsStrategy(tlsStrategy)
                                        .build();

                        httpClientBuilder.setConnectionManager(connectionManager);
                    }

                    return httpClientBuilder;
                })
                .build();

        final OpenSearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new OpenSearchClient(transport);
    }

    @Bean("search_OpenSearchIndexManager")
    @ConditionalOnProperty(name = "jmix.search.enabled", matchIfMissing = true)
    protected IndexManager openSearchIndexManager(OpenSearchClient client,
                                                  IndexStateRegistry indexStateRegistry,
                                                  IndexConfigurationManager indexConfigurationManager,
                                                  SearchProperties searchProperties,
                                                  OpenSearchIndexSettingsProvider indexSettingsProcessor,
                                                  OpenSearchIndexConfigurationComparator configurationComparator,
                                                  OpenSearchIndexStateResolver metadataResolver,
                                                  OpenSearchPutMappingRequestBuilder putMappingRequestService) {
        return new OpenSearchIndexManager(
                client,
                indexStateRegistry,
                indexConfigurationManager,
                searchProperties,
                indexSettingsProcessor,
                configurationComparator,
                metadataResolver,
                putMappingRequestService);
    }

    @Bean("search_OpenSearchEntityIndexer")
    @ConditionalOnProperty(name = "jmix.search.enabled", matchIfMissing = true)
    protected EntityIndexer openSearchEntityIndexer(UnconstrainedDataManager dataManager,
                                                    FetchPlans fetchPlans,
                                                    IndexConfigurationManager indexConfigurationManager,
                                                    Metadata metadata,
                                                    IdSerialization idSerialization,
                                                    IndexStateRegistry indexStateRegistry,
                                                    MetadataTools metadataTools,
                                                    SearchProperties searchProperties,
                                                    OpenSearchClient client,
                                                    DynamicAttributesSupport dynamicAttributesSupport) {
        return new OpenSearchEntityIndexer(dataManager,
                fetchPlans,
                indexConfigurationManager,
                metadata,
                idSerialization,
                indexStateRegistry,
                metadataTools,
                searchProperties,
                client,
                dynamicAttributesSupport);
    }

    @Bean("search_OpenSearchEntitySearcher")
    @ConditionalOnProperty(name = "jmix.search.enabled", matchIfMissing = true)
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
            IndexConfigurationManager indexConfigurationManager,
            Collection<OpenSearchSearchStrategy> searchStrategies,
            SearchProperties applicationProperties) {
        return new OpenSearchSearchStrategyProvider(indexConfigurationManager, searchStrategies, applicationProperties);
    }

    @Nullable
    protected CredentialsProvider createCredentialsProvider() {
        if (Strings.isNullOrEmpty(searchProperties.getServerLogin())) {
            return null;
        }

        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        AuthScope authScope = createAuthScope();
        UsernamePasswordCredentials credentials = createCredentials();
        credentialsProvider.setCredentials(authScope, credentials);

        return credentialsProvider;
    }

    protected AuthScope createAuthScope() {
        // TODO [SB4] Same as AuthScope.ANY from old HttpClient 4
        return new AuthScope(null, null, -1, null, null);
    }

    protected UsernamePasswordCredentials createCredentials() {
        String login = searchProperties.getServerLogin();
        String passwordString = searchProperties.getServerPassword();
        char[] password = StringUtils.isNotEmpty(passwordString)
                ? passwordString.toCharArray()
                : new char[0];

        return new UsernamePasswordCredentials(login, password);
    }
}
