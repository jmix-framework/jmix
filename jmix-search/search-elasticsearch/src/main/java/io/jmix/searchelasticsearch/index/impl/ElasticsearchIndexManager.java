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

package io.jmix.searchelasticsearch.index.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.*;
import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.json.JsonpSerializable;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.core.common.util.Preconditions;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.*;
import io.jmix.search.index.impl.BaseIndexManager;
import io.jmix.search.index.impl.IndexStateRegistry;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.index.mapping.IndexMappingConfiguration;
import io.jmix.searchelasticsearch.index.ElasticsearchIndexSettingsProvider;
import jakarta.json.spi.JsonProvider;
import jakarta.json.stream.JsonParser;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Implementation for Elasticsearch
 */
@NullMarked
public class ElasticsearchIndexManager extends BaseIndexManager<IndexState, IndexSettings, JsonpSerializable> {

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchIndexManager.class);

    protected final ElasticsearchClient client;
    protected final ElasticsearchIndexSettingsProvider indexSettingsProcessor;
    protected final ElasticsearchPutMappingRequestBuilder putMappingRequestBuilder;

    public ElasticsearchIndexManager(ElasticsearchClient client,
                                     IndexStateRegistry indexStateRegistry,
                                     IndexConfigurationManager indexConfigurationManager,
                                     SearchProperties searchProperties,
                                     IndexNameGenerator indexNameGenerator,
                                     ElasticsearchIndexSettingsProvider indexSettingsProcessor,
                                     ElasticsearchIndexConfigurationComparator configurationComparator,
                                     ElasticsearchIndexStateResolver indexStateResolver,
                                     ElasticsearchPutMappingRequestBuilder putMappingRequestBuilder
                                     ) {
        super(
                indexConfigurationManager,
                indexStateRegistry,
                searchProperties,
                indexNameGenerator,
                configurationComparator,
                indexStateResolver);
        this.client = client;
        this.indexSettingsProcessor = indexSettingsProcessor;
        this.putMappingRequestBuilder = putMappingRequestBuilder;
    }

    @Override
    protected IndexManipulationResult createIndex(IndexConfiguration indexConfiguration, String indexName) {
        TypeMapping mapping = buildMapping(indexConfiguration);
        IndexSettings settings = buildSettings(indexConfiguration);

        CreateIndexRequest createIndexRequest = CreateIndexRequest.of(
                builder -> builder.index(indexName).mappings(mapping).settings(settings)
        );

        log.info("Create index '{}' with mapping {}", indexName, mapping);

        CreateIndexResponse response;
        try {
            response = client.indices().create(createIndexRequest);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create index '" + indexName + "'", e);
        }

        boolean acknowledged = response.acknowledged();
        // Availability is decided by the caller: it is the one that knows whether an index that was already
        // there counts as created, and it marks the index either way.
        return IndexManipulationResult.of(acknowledged);
    }

    @Override
    public boolean dropIndex(String indexName) {
        Preconditions.checkNotNullArgument(indexName);

        DeleteIndexResponse response;
        try {
            indexStateRegistry.markIndexAsUnavailable(indexName);
            response = client.indices().delete(builder -> builder.index(indexName));
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete index '" + indexName + "'", e);
        }
        log.info("Result of index '{}' deletion: {}", indexName, response.acknowledged() ? "Success" : "Failure");
        return response.acknowledged();
    }

    @Override
    public boolean isIndexExist(String indexName) {
        Preconditions.checkNotNullArgument(indexName);
        try {
            return client.indices().exists(builder -> builder.index(indexName)).value();
        } catch (IOException e) {
            throw new RuntimeException("Unable to check existence of index '" + indexName + "'", e);
        }
    }

    @Override
    public ObjectNode getIndexMetadata(String indexName) {
        return indexStateResolver.getSerializedState(indexName);
    }

    @Override
    public boolean putMapping(String indexName, IndexMappingConfiguration mapping) {
        try {
            return client
                    .indices()
                    .putMapping(putMappingRequestBuilder.buildRequest(mapping, indexName, new JacksonJsonpMapper()))
                    .acknowledged();
        } catch (IOException e) {
            throw new RuntimeException("Problem with sending request to elastic search server.", e);
        }
    }

    protected TypeMapping buildMapping(IndexConfiguration indexConfiguration) {
        String mappingBody;
        try {
            mappingBody = objectMapper.writeValueAsString(indexConfiguration.getMapping());
            JsonpMapper mapper = client._transport().jsonpMapper();
            JsonProvider jsonProvider = mapper.jsonProvider();
            try (StringReader reader = new StringReader(mappingBody)) {
                JsonParser parser = jsonProvider.createParser(reader);
                return TypeMapping._DESERIALIZER.deserialize(parser, mapper);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to parse mapping of entity '" + indexConfiguration.getEntityName() + "'", e);
        }
    }

    protected IndexSettings buildSettings(IndexConfiguration indexConfiguration) {
        return indexSettingsProcessor.getSettingsForIndex(indexConfiguration);
    }
}
