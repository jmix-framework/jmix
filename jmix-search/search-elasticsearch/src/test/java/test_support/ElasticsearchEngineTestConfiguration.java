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

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest5_client.Rest5ClientTransport;
import co.elastic.clients.transport.rest5_client.low_level.Rest5Client;
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
import io.jmix.searchelasticsearch.SearchElasticsearchConfiguration;
import io.jmix.searchelasticsearch.index.ElasticsearchIndexSettingsProvider;
import io.jmix.searchelasticsearch.index.impl.*;
import io.jmix.testsupport.config.LiquibaseTestConfiguration;
import org.apache.hc.core5.http.HttpHost;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;

import java.net.URISyntaxException;

/**
 * Boots the context against a real Elasticsearch: the index manager and the indexer are the production ones, not
 * the test doubles the other suites use, so index creation and document delivery are actually performed.
 * <p>
 * The Multitenancy add-on is active, the way the starter wires it when the add-on is on the classpath.
 */
@Configuration
@JmixModule
@Import({BaseSearchTestConfiguration.class,
        LiquibaseTestConfiguration.class,
        SearchElasticsearchConfiguration.class,
        MultitenancyConfiguration.class})
@PropertySource("classpath:/test_support/test-entity-indexing-app.properties")
public class ElasticsearchEngineTestConfiguration {

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
    public ElasticsearchClient engineClient() {
        HttpHost httpHost;
        try {
            httpHost = HttpHost.create(ElasticsearchConnection.getUrl());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid Elasticsearch URL: " + ElasticsearchConnection.getUrl(), e);
        }
        Rest5Client restClient = Rest5Client.builder(httpHost).build();
        return new ElasticsearchClient(new Rest5ClientTransport(restClient, new JacksonJsonpMapper()));
    }

    @Bean("search_IndexManager")
    @Primary
    public IndexManager engineIndexManager(ElasticsearchClient client,
                                           IndexConfigurationManager indexConfigurationManager,
                                           SearchProperties searchProperties,
                                           IndexStateRegistry indexStateRegistry,
                                           ElasticsearchIndexSettingsProvider indexSettingsProvider,
                                           ElasticsearchIndexConfigurationComparator configurationComparator,
                                           ElasticsearchIndexStateResolver indexStateResolver,
                                           ElasticsearchPutMappingRequestBuilder putMappingRequestBuilder) {
        return new ElasticsearchIndexManager(client,
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
                                             ElasticsearchClient client,
                                             DynamicAttributesSupport dynamicAttributesSupport,
                                             MultitenancyAdapter multitenancyAdapter) {
        return new ElasticsearchEntityIndexer(dataManager,
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
