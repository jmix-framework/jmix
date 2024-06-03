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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.core.common.util.Preconditions;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.*;
import io.jmix.search.index.impl.IndexStateRegistry;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.searchopensearch.index.OpenSearchIndexSettingsConfigurerProcessor;
import jakarta.json.spi.JsonProvider;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonParser;
import org.opensearch.client.json.JsonpMapper;
import org.opensearch.client.json.JsonpSerializable;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;
import org.opensearch.client.opensearch.indices.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

public class OpenSearchIndexManager implements ESIndexManager {

    private static final Logger log = LoggerFactory.getLogger(OpenSearchIndexManager.class);

    protected final OpenSearchClient client;
    protected final IndexStateRegistry indexStateRegistry;
    protected final IndexConfigurationManager indexConfigurationManager;
    protected final SearchProperties searchProperties;
    protected final OpenSearchIndexSettingsConfigurerProcessor indexSettingsProcessor;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    public OpenSearchIndexManager(OpenSearchClient client,
                                  IndexStateRegistry indexStateRegistry,
                                  IndexConfigurationManager indexConfigurationManager,
                                  SearchProperties searchProperties,
                                  OpenSearchIndexSettingsConfigurerProcessor indexSettingsProcessor) {
        this.client = client;
        this.indexStateRegistry = indexStateRegistry;
        this.indexConfigurationManager = indexConfigurationManager;
        this.searchProperties = searchProperties;
        this.indexSettingsProcessor = indexSettingsProcessor;
    }

    @Override
    public boolean createIndex(IndexConfiguration indexConfiguration) {
        Preconditions.checkNotNullArgument(indexConfiguration);

        String indexName = indexConfiguration.getIndexName();
        TypeMapping mapping = buildMapping(indexConfiguration); //todo
        IndexSettings settings = buildSettings(indexConfiguration); //todo

        CreateIndexRequest createIndexRequest = CreateIndexRequest.of(
                builder -> builder.index(indexName).mappings(mapping).settings(settings)
        );

        log.info("Create index '{}' with mapping {}", indexConfiguration.getIndexName(), mapping); //todo debug?

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
    public Map<IndexConfiguration, Boolean> recreateIndexes() { //todo super class
        Collection<IndexConfiguration> indexConfigurations = indexConfigurationManager.getAllIndexConfigurations();
        return recreateIndexes(indexConfigurations);
    }

    @Override
    public Map<IndexConfiguration, Boolean> recreateIndexes(Collection<IndexConfiguration> indexConfigurations) { //todo super class
        Preconditions.checkNotNullArgument(indexConfigurations);

        Map<IndexConfiguration, Boolean> result = new HashMap<>();
        indexConfigurations.forEach(config -> {
            boolean created = recreateIndex(config);
            result.put(config, created);
        });
        return result;
    }

    @Override
    public boolean recreateIndex(IndexConfiguration indexConfiguration) { //todo super class
        Preconditions.checkNotNullArgument(indexConfiguration);

        String indexName = indexConfiguration.getIndexName();
        if (isIndexExist(indexName)) {
            boolean dropped = dropIndex(indexName);
            if (!dropped) {
                return false;
            }
        }
        return createIndex(indexConfiguration);
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
    public Map<IndexConfiguration, IndexValidationStatus> validateIndexes() { //todo super class
        Collection<IndexConfiguration> indexConfigurations = indexConfigurationManager.getAllIndexConfigurations();
        return validateIndexes(indexConfigurations);
    }

    @Override
    public Map<IndexConfiguration, IndexValidationStatus> validateIndexes(Collection<IndexConfiguration> indexConfigurations) {
        Preconditions.checkNotNullArgument(indexConfigurations);

        Map<IndexConfiguration, IndexValidationStatus> result = new HashMap<>();
        indexConfigurations.forEach(config -> {
            IndexValidationStatus status = validateIndex(config);
            result.put(config, status);
        });
        return result;
    }

    @Override
    public IndexValidationStatus validateIndex(IndexConfiguration indexConfiguration) {
        Preconditions.checkNotNullArgument(indexConfiguration);

        IndexValidationStatus status;
        if (isIndexExist(indexConfiguration.getIndexName())) {
            if (isIndexActual(indexConfiguration)) {
                status = IndexValidationStatus.ACTUAL;
                indexStateRegistry.markIndexAsAvailable(indexConfiguration.getEntityName());
            } else {
                status = IndexValidationStatus.IRRELEVANT;
                indexStateRegistry.markIndexAsUnavailable(indexConfiguration.getEntityName());
            }
        } else {
            status = IndexValidationStatus.MISSING;
            indexStateRegistry.markIndexAsUnavailable(indexConfiguration.getEntityName());
        }

        log.info("Validation status of search index '{}' (entity '{}'): {}",
                indexConfiguration.getIndexName(), indexConfiguration.getEntityName(), status);
        return status;
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

    @Override
    public Map<IndexConfiguration, IndexSynchronizationStatus> synchronizeIndexSchemas() { //todo super class
        Collection<IndexConfiguration> indexConfigurations = indexConfigurationManager.getAllIndexConfigurations();
        return synchronizeIndexSchemas(indexConfigurations);
    }

    @Override
    public Map<IndexConfiguration, IndexSynchronizationStatus> synchronizeIndexSchemas(Collection<IndexConfiguration> indexConfigurations) { //todo super class
        Preconditions.checkNotNullArgument(indexConfigurations);

        Map<IndexConfiguration, IndexSynchronizationStatus> result = new HashMap<>();
        indexConfigurations.forEach(config -> {
            IndexSynchronizationStatus status = synchronizeIndexSchema(config);
            result.put(config, status);
        });
        return result;
    }

    @Override
    public IndexSynchronizationStatus synchronizeIndexSchema(IndexConfiguration indexConfiguration) { //todo super class
        Preconditions.checkNotNullArgument(indexConfiguration);

        IndexSchemaManagementStrategy strategy = searchProperties.getIndexSchemaManagementStrategy();
        return synchronizeIndexSchema(indexConfiguration, strategy);
    }

    protected IndexSynchronizationStatus synchronizeIndexSchema(IndexConfiguration indexConfiguration, IndexSchemaManagementStrategy strategy) { //todo super class
        log.info("Synchronize search index '{}' (entity '{}') according to strategy '{}'",
                indexConfiguration.getIndexName(), indexConfiguration.getEntityName(), strategy);
        IndexSynchronizationStatus status;
        boolean indexExist = isIndexExist(indexConfiguration.getIndexName());
        if (indexExist) {
            boolean indexActual = isIndexActual(indexConfiguration);
            if (indexActual) {
                status = IndexSynchronizationStatus.ACTUAL;
                indexStateRegistry.markIndexAsAvailable(indexConfiguration.getEntityName());
            } else {
                status = handleIrrelevantIndex(indexConfiguration, strategy);
            }
        } else {
            status = handleMissingIndex(indexConfiguration, strategy);
        }
        log.info("Synchronization status of search index '{}' (entity '{}'): {}",
                indexConfiguration.getIndexName(), indexConfiguration.getEntityName(), status);
        return status;
    }

    protected IndexSynchronizationStatus handleIrrelevantIndex(IndexConfiguration indexConfiguration, IndexSchemaManagementStrategy strategy) { //todo super class
        IndexSynchronizationStatus status;
        if (IndexSchemaManagementStrategy.CREATE_OR_RECREATE.equals(strategy)) {
            boolean created = recreateIndex(indexConfiguration);
            if (created) {
                status = IndexSynchronizationStatus.RECREATED;
                indexStateRegistry.markIndexAsAvailable(indexConfiguration.getEntityName());
            } else {
                status = IndexSynchronizationStatus.IRRELEVANT;
                indexStateRegistry.markIndexAsUnavailable(indexConfiguration.getEntityName());
            }
        } else {
            status = IndexSynchronizationStatus.IRRELEVANT;
            indexStateRegistry.markIndexAsUnavailable(indexConfiguration.getEntityName());
        }
        return status;
    }

    protected IndexSynchronizationStatus handleMissingIndex(IndexConfiguration indexConfiguration, IndexSchemaManagementStrategy strategy) { //todo super class
        IndexSynchronizationStatus status;

        if (IndexSchemaManagementStrategy.NONE.equals(strategy)) {
            status = IndexSynchronizationStatus.MISSING;
        } else {
            boolean created = createIndex(indexConfiguration);
            if (created) {
                status = IndexSynchronizationStatus.CREATED;
                indexStateRegistry.markIndexAsAvailable(indexConfiguration.getEntityName());
            } else {
                status = IndexSynchronizationStatus.MISSING;
                indexStateRegistry.markIndexAsUnavailable(indexConfiguration.getEntityName());
            }
        }
        return status;
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

    protected JsonNode toJsonNode(JsonpSerializable object) {
        StringWriter stringWriter = new StringWriter();
        JsonpMapper mapper = client._transport().jsonpMapper();
        JsonGenerator generator = mapper.jsonProvider().createGenerator(stringWriter);
        object.serialize(generator, mapper);
        generator.close();
        String stringValue = stringWriter.toString();

        try {
            JsonNode jsonNode = objectMapper.readTree(stringValue);
            return jsonNode;
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

    protected boolean isIndexActual(IndexConfiguration indexConfiguration) { //todo
        Preconditions.checkNotNullArgument(indexConfiguration);

        //GetIndexResponse indexResponse = getIndex(indexConfiguration.getIndexName());
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
            currentMapping = objectMapper.convertValue(currentMappingNode, new TypeReference<>() {
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
        IndexSettings expectedSettings = indexSettingsProcessor.getSettingsForIndex(indexConfiguration);
        IndexSettings allCurrentSettings = currentIndexState.settings();
        IndexSettings defaults = currentIndexState.defaults();

        if (allCurrentSettings == null) {
            throw new IllegalArgumentException("No info about all current settings for index '" + indexConfiguration.getIndexName() + "'");
        }

        IndexSettings currentSettings = allCurrentSettings.index();
        if (currentSettings == null) {
            throw new IllegalArgumentException("No info about index current settings for index '" + indexConfiguration.getIndexName() + "'");
        }

        ObjectNode expectedSettingsNode = toObjectNode(expectedSettings);
        ObjectNode currentSettingsNode = toObjectNode(currentSettings);
        ObjectNode defaultsNode = defaults == null ? objectMapper.createObjectNode() : toObjectNode(defaults);
        //todo implement inclusion check
        Map<String, Object> expectedSettingsMap = objectMapper.convertValue(expectedSettingsNode, new TypeReference<>() {
        });
        Map<String, Object> currentSettingsMap = objectMapper.convertValue(currentSettingsNode, new TypeReference<>() {
        });
        Map<String, Object> defaultsMap = objectMapper.convertValue(defaultsNode, new TypeReference<>() {
        });

        log.debug("Settings of index '{}':\nCurrent: {}\nExpected: {}",
                indexConfiguration.getIndexName(), currentSettingsMap, expectedSettingsMap);

        boolean settingsActual = nodeContains(currentSettingsNode, expectedSettingsNode);


        return settingsActual;
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
