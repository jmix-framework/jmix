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

package io.jmix.searchopensearch.index.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.core.common.util.Preconditions;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.impl.BaseIndexManager;
import io.jmix.search.index.impl.IndexStateRegistry;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.index.mapping.IndexMappingConfiguration;
import io.jmix.searchopensearch.index.OpenSearchIndexSettingsProvider;
import jakarta.json.spi.JsonProvider;
import jakarta.json.stream.JsonParser;
import org.opensearch.client.json.JsonpMapper;
import org.opensearch.client.json.JsonpSerializable;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;
import org.opensearch.client.opensearch.indices.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;

/**
 * Implementation for OpenSearch
 */
public class OpenSearchIndexManager extends BaseIndexManager<IndexState, IndexSettings, JsonpSerializable> {

    private static final Logger log = LoggerFactory.getLogger(OpenSearchIndexManager.class);

    protected final OpenSearchClient client;
    protected final OpenSearchIndexSettingsProvider indexSettingsProcessor;
    protected final OpenSearchPutMappingRequestBuilder putMappingRequestService;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    public OpenSearchIndexManager(OpenSearchClient client,
                                  IndexStateRegistry indexStateRegistry,
                                  IndexConfigurationManager indexConfigurationManager,
                                  SearchProperties searchProperties,
                                  OpenSearchIndexSettingsProvider indexSettingsProcessor,
                                  OpenSearchIndexConfigurationComparator configurationComparator,
                                  OpenSearchIndexStateResolver metadataResolver,
                                  OpenSearchPutMappingRequestBuilder putMappingRequestService) {
        super(indexConfigurationManager, indexStateRegistry, searchProperties, configurationComparator, metadataResolver);
        this.client = client;
        this.indexSettingsProcessor = indexSettingsProcessor;
        this.putMappingRequestService = putMappingRequestService;
    }

    @Override
    public boolean createIndex(IndexConfiguration indexConfiguration) {
        Preconditions.checkNotNullArgument(indexConfiguration);

        String indexName = indexConfiguration.getIndexName();
        TypeMapping mapping = buildMapping(indexConfiguration);
        IndexSettings settings = buildSettings(indexConfiguration);

        CreateIndexRequest createIndexRequest = CreateIndexRequest.of(
                builder -> builder.index(indexName).mappings(mapping).settings(settings)
        );

        log.info("Create index '{}' with mapping {}", indexConfiguration.getIndexName(), mapping);

        CreateIndexResponse response;
        try {
            response = client.indices().create(createIndexRequest);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create index '" + indexName + "'", e);
        }

        boolean acknowledged = Boolean.TRUE.equals(response.acknowledged());
        if (acknowledged) {
            indexStateRegistry.markIndexAsAvailable(indexConfiguration.getEntityName());
        }
        return acknowledged;
    }

    @Override
    public boolean dropIndex(String indexName) {
        Preconditions.checkNotNullArgument(indexName);

        IndexConfiguration indexConfiguration = indexConfigurationManager.getIndexConfigurationByIndexName(indexName);

        DeleteIndexResponse response;
        try {
            indexStateRegistry.markIndexAsUnavailable(indexConfiguration.getEntityName());
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
    public boolean putMapping(String indexName, IndexMappingConfiguration mappingConfiguration) {
        PutMappingRequest request = getPutMappingRequest(indexName, mappingConfiguration);
        try {
            return client.indices().putMapping(request).acknowledged();
        } catch (IOException e) {
            //TODO specify correct exception message
            throw new RuntimeException(e);
        }
    }

    private PutMappingRequest getPutMappingRequest(String indexName, IndexMappingConfiguration mappingConfiguration) {
        return putMappingRequestService.buildRequest(mappingConfiguration, indexName, client._transport().jsonpMapper());
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
            throw new RuntimeException("Unable to parse mapping of index '" + indexConfiguration.getIndexName() + "'", e);
        }
    }

    protected IndexSettings buildSettings(IndexConfiguration indexConfiguration) {
        return indexSettingsProcessor.getSettingsForIndex(indexConfiguration);
    }
}
