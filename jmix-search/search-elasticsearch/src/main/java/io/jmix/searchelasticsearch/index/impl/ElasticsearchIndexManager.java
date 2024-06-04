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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.core.common.util.Preconditions;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.impl.BaseIndexManager;
import io.jmix.search.index.impl.IndexStateRegistry;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.searchelasticsearch.index.ElasticsearchIndexSettingsProvider;
import jakarta.json.spi.JsonProvider;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ElasticsearchIndexManager extends BaseIndexManager {

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchIndexManager.class);

    protected final ElasticsearchClient client;
    protected final ElasticsearchIndexSettingsProvider indexSettingsProcessor;

    public ElasticsearchIndexManager(ElasticsearchClient client,
                                     IndexStateRegistry indexStateRegistry,
                                     IndexConfigurationManager indexConfigurationManager,
                                     SearchProperties searchProperties,
                                     ElasticsearchIndexSettingsProvider indexSettingsProcessor) {
        super(indexConfigurationManager, indexStateRegistry, searchProperties);
        this.client = client;
        this.indexSettingsProcessor = indexSettingsProcessor;
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

    //@Override
    public Map<String, ObjectNode> getIndexMetadata(String indexName) {
        Map<String, IndexState> content = getIndexMetadataMapInternal(indexName);
        Map<String, ObjectNode> result = new HashMap<>();
        //todo convert to objectNodes
        content.forEach((index, indexState) -> {
            JsonNode indexStateJsonNode = toJsonNode(indexState);
            if (indexStateJsonNode.isObject()) {
                result.put(index, (ObjectNode) indexStateJsonNode);
            }
        });
        return result;
    }

    /*@Override
    public GetIndexResponse getIndex(String indexName) { //todo get index data as Json (map indexName - Json)?
        org.opensearch.client.opensearch.indices.GetIndexResponse indexResponse = client.indices().get(builder -> builder.index(indexName));
        Map<String, IndexState> result = indexResponse.result();

    }*/

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

    protected JsonNode toJsonNode(JsonpSerializable object) {
        StringWriter stringWriter = new StringWriter();
        JsonpMapper mapper = client._transport().jsonpMapper();
        JsonGenerator generator = mapper.jsonProvider().createGenerator(stringWriter);
        object.serialize(generator, mapper);
        generator.close();
        String stringValue = stringWriter.toString();

        try {
            return objectMapper.readTree(stringValue);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to generate JsonNode", e);
        }
    }

    protected ObjectNode toObjectNode(JsonpSerializable object) {
        JsonNode jsonNode = toJsonNode(object);
        if (jsonNode.isObject()) {
            return (ObjectNode) jsonNode;
        } else {
            throw new RuntimeException("Unable to convert provided object to ObjectNode: JsonNode type is '" + jsonNode.getNodeType() + "'");
        }
    }

    @Override
    protected boolean isIndexActual(IndexConfiguration indexConfiguration) { //todo
        Preconditions.checkNotNullArgument(indexConfiguration);

        IndexState indexState = getIndexMetadataInternal(indexConfiguration.getIndexName());
        if (indexState == null) {
            return false;
        }
        boolean indexMappingActual = isIndexMappingActual(indexConfiguration, indexState);
        boolean indexSettingsActual = isIndexSettingsActual(indexConfiguration, indexState);

        return indexMappingActual && indexSettingsActual;
    }

    protected Map<String, IndexState> getIndexMetadataMapInternal(String indexName) {
        Preconditions.checkNotNullArgument(indexName);
        try {
            return client.indices().get(builder -> builder.index(indexName).includeDefaults(true)).result();
        } catch (IOException e) {
            throw new RuntimeException("Unable to load metadata of index '" + indexName + "'", e);
        }
    }

    @Nullable
    protected IndexState getIndexMetadataInternal(String indexName) {
        return getIndexMetadataMapInternal(indexName).get(indexName);
    }

    protected boolean isIndexMappingActual(IndexConfiguration indexConfiguration, IndexState currentIndexState) {
        Map<String, Object> currentMapping;
        TypeMapping typeMapping = currentIndexState.mappings();
        if (typeMapping == null) {
            currentMapping = Collections.emptyMap();
        } else {
            ObjectNode currentMappingNode = toObjectNode(typeMapping);
            currentMapping = objectMapper.convertValue(currentMappingNode, new TypeReference<>() { //todo type ref
            });
        }
        Map<String, Object> actualMapping = objectMapper.convertValue(
                indexConfiguration.getMapping(),
                new TypeReference<>() {
                }
        );
        log.debug("Mappings of index '{}':\nCurrent: {}\nActual: {}",
                indexConfiguration.getIndexName(), currentMapping, actualMapping);
        return actualMapping.equals(currentMapping);
    }

    protected boolean isIndexSettingsActual(IndexConfiguration indexConfiguration, IndexState currentIndexState) {
        IndexSettings expectedIndexSettings = indexSettingsProcessor.getSettingsForIndex(indexConfiguration);
        IndexSettings allAppliedSettings = currentIndexState.settings();

        if (allAppliedSettings == null) {
            throw new IllegalArgumentException(
                    "No info about all applied settings for index '" + indexConfiguration.getIndexName() + "'"
            );
        }

        IndexSettings appliedIndexSettings = allAppliedSettings.index();
        if (appliedIndexSettings == null) {
            throw new IllegalArgumentException(
                    "No info about applied index settings for index '" + indexConfiguration.getIndexName() + "'"
            );
        }

        ObjectNode expectedSettingsNode = toObjectNode(expectedIndexSettings);
        ObjectNode appliedSettingsNode = toObjectNode(appliedIndexSettings);

        log.debug("Settings of index '{}':\nExpected: {}\nApplied: {}",
                indexConfiguration.getIndexName(), expectedSettingsNode, appliedSettingsNode);

        return nodeContains(appliedSettingsNode, expectedSettingsNode);
    }

    protected boolean nodeContains(ObjectNode containerNode, ObjectNode contentNode) {
        log.info("[IVGA] Check if node {} contains {}", containerNode, contentNode);
        Iterator<Map.Entry<String, JsonNode>> fieldsIterator = contentNode.fields();
        while (fieldsIterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = fieldsIterator.next();
            String fieldName = entry.getKey();
            log.info("[IVGA] Check field '{}'", fieldName);
            JsonNode contentFieldValue = entry.getValue();
            JsonNode containerFieldValue;
            if (containerNode.has(fieldName)) {
                log.info("[IVGA] Container has field '{}'", fieldName);
                containerFieldValue = containerNode.get(fieldName);
            } else {
                log.info("[IVGA] Container doesn't have field '{}'. STOP - FALSE", fieldName);
                return false;
            }

            if (containerFieldValue == null) {
                log.info("[IVGA] Container has NULL field '{}'. STOP - FALSE", fieldName);
                return false;
            }

            if (!contentFieldValue.getNodeType().equals(containerFieldValue.getNodeType())) {
                log.info("[IVGA] Type of container field ({}) doesn't match the type of content field ({}). STOP - FALSE",
                        containerFieldValue.getNodeType(), contentFieldValue.getNodeType());
                return false;
            }

            if (contentFieldValue.isObject() && containerFieldValue.isObject()) {
                log.info("[IVGA] Both container and content field is objects - check nested structure");
                boolean nestedResult = nodeContains((ObjectNode) containerFieldValue, (ObjectNode) contentFieldValue);
                if (!nestedResult) {
                    log.info("[IVGA] Structures of the nested objects ({}) are different. STOP - FALSE", fieldName);
                    return false;
                }
            }

            if (!containerFieldValue.equals(contentFieldValue)) {
                return false;
            }
        }
        log.info("[IVGA] Structures are the same. TRUE");
        return true;
    }
}
