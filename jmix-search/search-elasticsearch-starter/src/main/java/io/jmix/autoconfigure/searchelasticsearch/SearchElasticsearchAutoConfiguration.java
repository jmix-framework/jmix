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
import co.elastic.clients.transport.rest5_client.Rest5ClientTransport;
import co.elastic.clients.transport.rest5_client.low_level.Rest5Client;
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
import io.jmix.searchelasticsearch.SearchElasticsearchConfiguration;
import io.jmix.searchelasticsearch.index.ElasticsearchIndexSettingsProvider;
import io.jmix.searchelasticsearch.index.impl.*;
import io.jmix.searchelasticsearch.searching.impl.ElasticsearchEntitySearcher;
import io.jmix.searchelasticsearch.searching.strategy.ElasticsearchSearchStrategy;
import io.jmix.searchelasticsearch.searching.strategy.ElasticsearchSearchStrategyProvider;
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
        SearchElasticsearchConfiguration.class})
public class SearchElasticsearchAutoConfiguration {

    @Autowired
    protected SearchProperties searchProperties;
    @Autowired
    protected SslConfigurer sslConfigurer;

    @Bean("search_ElasticsearchClient")
    @ConditionalOnMissingBean(ElasticsearchClient.class)
    public ElasticsearchClient elasticsearchClient() {
        String url = searchProperties.getServerUrl();
        HttpHost httpHost;
        try {
            httpHost = HttpHost.create(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid Elasticsearch URL: " + url, e);
        }

        CredentialsProvider credentialsProvider = createCredentialsProvider();
        SSLContext sslContext = sslConfigurer.createSslContext();

        Rest5Client restClient = Rest5Client
                .builder(httpHost)
                .setHttpClientConfigCallback(httpClientBuilder -> {
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
                })
                .build();

        Rest5ClientTransport transport = new Rest5ClientTransport(restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }

    @Bean("search_ElasticsearchIndexManager")
    @ConditionalOnProperty(name = "jmix.search.enabled", matchIfMissing = true)
    protected IndexManager elasticsearchIndexManager(ElasticsearchClient client,
                                                     IndexConfigurationManager indexConfigurationManager,
                                                     SearchProperties searchProperties,
                                                     IndexStateRegistry indexStateRegistry,
                                                     ElasticsearchIndexSettingsProvider indexSettingsProcessor,
                                                     ElasticsearchIndexConfigurationComparator configurationComparator,
                                                     ElasticsearchIndexStateResolver indexStateResolver,
                                                     ElasticsearchPutMappingRequestBuilder putMappingRequestBuilder) {
        return new ElasticsearchIndexManager(client,
                indexStateRegistry,
                indexConfigurationManager,
                searchProperties,
                indexSettingsProcessor,
                configurationComparator,
                indexStateResolver,
                putMappingRequestBuilder
        );
    }

    @Bean("search_ElasticsearchEntityIndexer")
    @ConditionalOnProperty(name = "jmix.search.enabled", matchIfMissing = true)
    protected EntityIndexer elasticsearchEntityIndexer(UnconstrainedDataManager dataManager,
                                                       FetchPlans fetchPlans,
                                                       IndexConfigurationManager indexConfigurationManager,
                                                       Metadata metadata,
                                                       IdSerialization idSerialization,
                                                       IndexStateRegistry indexStateRegistry,
                                                       MetadataTools metadataTools,
                                                       SearchProperties searchProperties,
                                                       ElasticsearchClient client,
                                                       DynamicAttributesSupport dynamicAttributesSupport) {
        return new ElasticsearchEntityIndexer(dataManager,
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

    @Bean("search_ElasticsearchEntitySearcher")
    @ConditionalOnProperty(name = "jmix.search.enabled", matchIfMissing = true)
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
            IndexConfigurationManager indexConfigurationManager,
            Collection<ElasticsearchSearchStrategy> searchStrategies,
            SearchProperties applicationProperties) {
        return new ElasticsearchSearchStrategyProvider(indexConfigurationManager, searchStrategies, applicationProperties);
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
