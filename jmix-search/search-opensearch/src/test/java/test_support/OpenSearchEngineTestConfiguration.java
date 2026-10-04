/*
 * Copyright 2026 Haulmont.
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

package test_support;

import io.jmix.core.*;
import io.jmix.core.annotation.JmixModule;
import io.jmix.multitenancy.MultitenancyConfiguration;
import io.jmix.multitenancy.core.TenantProvider;
import io.jmix.multitenancy.Multitenancy;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.EntityIndexer;
import io.jmix.search.index.IndexManager;
import io.jmix.search.index.impl.IndexStateRegistry;
import io.jmix.search.index.impl.AddonMultitenancyAdapter;
import io.jmix.search.index.impl.MultitenancyAdapter;
import io.jmix.search.index.impl.dynattr.DynamicAttributesSupport;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.searchopensearch.SearchOpenSearchConfiguration;
import io.jmix.searchopensearch.index.OpenSearchIndexSettingsProvider;
import io.jmix.searchopensearch.index.impl.*;
import io.jmix.testsupport.config.LiquibaseTestConfiguration;
import org.apache.hc.core5.http.HttpHost;
import org.opensearch.client.RestClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;

import java.net.URISyntaxException;

/**
 * Boots the context against a real OpenSearch: the index manager and the indexer are the production ones, not the
 * test doubles the other suites use, so index creation and document delivery are actually performed.
 * <p>
 * The Multitenancy add-on is active, the way the starter wires it when the add-on is on the classpath.
 */
@Configuration
@JmixModule
@Import({BaseSearchTestConfiguration.class,
        LiquibaseTestConfiguration.class,
        SearchOpenSearchConfiguration.class,
        MultitenancyConfiguration.class})
@PropertySource("classpath:/test_support/test-entity-indexing-app.properties")
public class OpenSearchEngineTestConfiguration {

    @Bean
    public TestAutoDetectableIndexDefinitionScope testAutoDetectableIndexDefinitionScope() {
        return TestAutoDetectableIndexDefinitionScope.builder().packages("test_support.indexing").build();
    }

    @Bean
    @Primary
    public MultitenancyAdapter multitenancyMultitenancyAdapter(Multitenancy multitenancy) {
        return new AddonMultitenancyAdapter(multitenancy);
    }

    @Bean
    @Primary
    public OpenSearchClient engineClient() {
        HttpHost httpHost;
        try {
            httpHost = HttpHost.create(OpenSearchConnection.getUrl());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid OpenSearch URL: " + OpenSearchConnection.getUrl(), e);
        }
        RestClient restClient = RestClient.builder(httpHost).build();
        return new OpenSearchClient(new RestClientTransport(restClient, new JacksonJsonpMapper()));
    }

    @Bean("search_IndexManager")
    @Primary
    public IndexManager engineIndexManager(OpenSearchClient client,
                                           IndexStateRegistry indexStateRegistry,
                                           IndexConfigurationManager indexConfigurationManager,
                                           SearchProperties searchProperties,
                                           OpenSearchIndexSettingsProvider indexSettingsProvider,
                                           OpenSearchIndexConfigurationComparator configurationComparator,
                                           OpenSearchIndexStateResolver indexStateResolver,
                                           OpenSearchPutMappingRequestBuilder putMappingRequestBuilder) {
        return new OpenSearchIndexManager(client,
                indexStateRegistry,
                indexConfigurationManager,
                searchProperties,
                indexSettingsProvider,
                configurationComparator,
                indexStateResolver,
                putMappingRequestBuilder);
    }

    @Bean("search_EntityIndexer")
    @Primary
    public EntityIndexer engineEntityIndexer(UnconstrainedDataManager dataManager,
                                             FetchPlans fetchPlans,
                                             IndexConfigurationManager indexConfigurationManager,
                                             Metadata metadata,
                                             IdSerialization idSerialization,
                                             IndexStateRegistry indexStateRegistry,
                                             MetadataTools metadataTools,
                                             SearchProperties searchProperties,
                                             OpenSearchClient client,
                                             DynamicAttributesSupport dynamicAttributesSupport,
                                             MultitenancyAdapter multitenancyAdapter) {
        return new OpenSearchEntityIndexer(dataManager,
                fetchPlans,
                indexConfigurationManager,
                metadata,
                idSerialization,
                indexStateRegistry,
                metadataTools,
                searchProperties,
                client,
                dynamicAttributesSupport,
                multitenancyAdapter);
    }
}
