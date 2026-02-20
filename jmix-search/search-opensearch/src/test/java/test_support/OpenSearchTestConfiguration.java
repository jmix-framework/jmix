/*
 * Copyright 2024 Haulmont.
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

import io.jmix.core.CoreConfiguration;
import io.jmix.core.security.InMemoryUserRepository;
import io.jmix.core.security.UserRepository;
import io.jmix.data.DataConfiguration;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.search.SearchConfiguration;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.EntityIndexer;
import io.jmix.search.index.IndexManager;
import io.jmix.search.index.impl.IndexStateRegistry;
import io.jmix.search.index.impl.StartupIndexSynchronizer;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.index.mapping.processor.impl.IndexDefinitionDetector;
import io.jmix.security.SecurityConfiguration;
import io.jmix.testsupport.config.CommonCoreTestConfiguration;
import io.jmix.testsupport.config.HsqlMemDataSourceTestConfiguration;
import io.jmix.testsupport.config.JpaMainStoreTestConfiguration;
import org.apache.http.HttpHost;
import org.opensearch.client.RestClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Import({
        CoreConfiguration.class,
        DataConfiguration.class,
        EclipselinkConfiguration.class,
        SecurityConfiguration.class,
        SearchConfiguration.class,
        CommonCoreTestConfiguration.class,
        HsqlMemDataSourceTestConfiguration.class,
        JpaMainStoreTestConfiguration.class
})
public class OpenSearchTestConfiguration {

    @Autowired
    SearchProperties searchProperties;

    // Test Search beans
    @Bean("search_StartupIndexSynchronizer")
    @Primary
    public StartupIndexSynchronizer startupIndexSynchronizer() {
        return new TestNoopStartupIndexSynchronizer();
    }

    @Bean("search_EntityIndexer")
    public EntityIndexer entityIndexer() {
        return new TestNoopEntityIndexer();
    }

    @Bean("search_IndexManager")
    public IndexManager indexManager(IndexConfigurationManager indexConfigurationManager,
                                     IndexStateRegistry indexStateRegistry,
                                     SearchProperties searchProperties) {
        return new TestNoopIndexManager(indexConfigurationManager, indexStateRegistry, searchProperties);
    }

    @Bean
    @Primary
    public IndexDefinitionDetector indexDefinitionDetector(@Nullable TestAutoDetectableIndexDefinitionScope testAutoDetectableIndexDefinitionScope) {
        return new TestIndexDefinitionDetector(testAutoDetectableIndexDefinitionScope);
    }

    @Bean
    public OpenSearchClient baseOpenSearchClient() {
        String url = searchProperties.getServerUrl();

        RestClient restClient = RestClient
                .builder(HttpHost.create(url))
                .build();

        OpenSearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new OpenSearchClient(transport);
    }


    // Test Common beans

    @Bean
    public UserRepository userRepository() {
        return new InMemoryUserRepository();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

}
