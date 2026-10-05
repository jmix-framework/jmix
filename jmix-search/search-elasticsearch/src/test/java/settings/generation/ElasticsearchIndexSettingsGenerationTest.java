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

package settings.generation;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest5_client.Rest5ClientTransport;
import co.elastic.clients.transport.rest5_client.low_level.Rest5Client;
import io.jmix.core.impl.metadata.MetadataGenerationManager;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.mapping.ExtendedSearchSettings;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.index.mapping.IndexMappingConfiguration;
import io.jmix.searchelasticsearch.index.ElasticsearchIndexSettingsProvider;
import io.jmix.searchelasticsearch.index.impl.ElasticsearchExtendedIndexSettingsConfigurer;
import org.apache.hc.core5.http.HttpHost;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import test_support.entity.TestReferenceEntity;
import test_support.entity.TestSubReferenceEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Index settings of an entity must follow the index configurations of the metadata generation they are
 * requested in: a dynamic entity can appear after startup and gets a new Java class in every generation.
 */
class ElasticsearchIndexSettingsGenerationTest {

    AtomicLong generationId = new AtomicLong();
    Map<Long, Collection<IndexConfiguration>> configurationsByGeneration = new HashMap<>();
    ElasticsearchIndexSettingsProvider provider;

    @BeforeEach
    void setUp() {
        IndexConfigurationManager indexConfigurationManager = Mockito.mock(IndexConfigurationManager.class);
        Mockito.when(indexConfigurationManager.getAllIndexConfigurations()).thenAnswer(invocation ->
                configurationsByGeneration.getOrDefault(generationId.get(), List.of()));
        MetadataGenerationManager metadataGenerationManager = Mockito.mock(MetadataGenerationManager.class);
        Mockito.when(metadataGenerationManager.getPinnedOrCurrentGenerationId()).thenAnswer(invocation ->
                generationId.get());

        Rest5Client restClient = Rest5Client.builder(new HttpHost("http", "localhost", 9200)).build();
        ElasticsearchClient client = new ElasticsearchClient(new Rest5ClientTransport(restClient, new JacksonJsonpMapper()));
        provider = new ElasticsearchIndexSettingsProvider(
                List.of(new ElasticsearchExtendedIndexSettingsConfigurer(indexConfigurationManager)), client);
        ReflectionTestUtils.setField(provider, "metadataGenerationManager", metadataGenerationManager);
    }

    @Test
    void getSettingsForIndex_entityAddedInLaterGeneration_includesPrefixAnalyzers() {
        IndexConfiguration configuration = configuration(TestReferenceEntity.class, true);
        configurationsByGeneration.put(1L, List.of(configuration));

        generationId.set(1);
        IndexSettings settings = provider.getSettingsForIndex(configuration);

        assertTrue(hasPrefixAnalyzer(settings));
    }

    @Test
    void getSettingsForIndex_entityClassChangedInNewGeneration_usesConfigurationOfNewGeneration() {
        IndexConfiguration firstConfiguration = configuration(TestReferenceEntity.class, false);
        configurationsByGeneration.put(1L, List.of(firstConfiguration));
        IndexConfiguration secondConfiguration = configuration(TestSubReferenceEntity.class, true);
        configurationsByGeneration.put(2L, List.of(secondConfiguration));

        generationId.set(1);
        IndexSettings firstSettings = provider.getSettingsForIndex(firstConfiguration);
        generationId.set(2);
        IndexSettings secondSettings = provider.getSettingsForIndex(secondConfiguration);

        assertFalse(hasPrefixAnalyzer(firstSettings));
        assertTrue(hasPrefixAnalyzer(secondSettings));
    }

    IndexConfiguration configuration(Class<?> entityClass, boolean extendedSearchEnabled) {
        return new IndexConfiguration(
                "test_DynamicEntity",
                entityClass,
                "search_index_test_dynamicentity",
                Mockito.mock(IndexMappingConfiguration.class),
                Set.of(entityClass),
                instance -> true,
                ExtendedSearchSettings.builder().setEnabled(extendedSearchEnabled).build());
    }

    boolean hasPrefixAnalyzer(IndexSettings settings) {
        return settings.analysis() != null && settings.analysis().analyzer().containsKey("jmix_prefix_analyzer");
    }
}
