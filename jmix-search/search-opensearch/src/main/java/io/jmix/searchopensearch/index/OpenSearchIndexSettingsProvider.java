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

package io.jmix.searchopensearch.index;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.search.index.IndexConfiguration;
import jakarta.json.spi.JsonProvider;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonParser;
import org.opensearch.client.json.JsonpMapper;
import org.opensearch.client.json.JsonpSerializable;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.indices.IndexSettings;
import org.opensearch.client.opensearch.indices.IndexSettingsAnalysis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component("search_OpenSearchIndexSettingsProvider")
public class OpenSearchIndexSettingsProvider {

    protected final OpenSearchClient client;

    protected final List<OpenSearchIndexSettingsConfigurer> customConfigurers;
    protected final List<OpenSearchIndexSettingsConfigurer> systemConfigurers;

    protected final OpenSearchIndexSettingsConfigurationContext context;

    protected final Map<Class<?>, IndexSettings> effectiveIndexSettings;

    protected final IndexSettings commonIndexSettings;
    protected final IndexSettingsAnalysis commonAnalysisSettings;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public OpenSearchIndexSettingsProvider(List<OpenSearchIndexSettingsConfigurer> configurers, OpenSearchClient client) {
        this.client = client;

        this.customConfigurers = new ArrayList<>();
        this.systemConfigurers = new ArrayList<>();
        prepareConfigurers(configurers);

        this.context = configureContext();

        this.commonIndexSettings = context.getCommonIndexSettingsBuilder().build();
        this.commonAnalysisSettings = context.getCommonAnalysisBuilder().build();
        this.effectiveIndexSettings = new ConcurrentHashMap<>();
    }

    public IndexSettings getSettingsForIndex(IndexConfiguration indexConfiguration) {
        Class<?> entityClass = indexConfiguration.getEntityClass();
        IndexSettings resultIndexSettings = this.effectiveIndexSettings.get(entityClass);
        if (resultIndexSettings == null) {
            Map<Class<?>, IndexSettings.Builder> indexSettingsBuilders = context.getAllSpecificIndexSettingsBuilders();
            IndexSettings entityIndexSettings;
            if (indexSettingsBuilders.containsKey(entityClass)) {
                // Merge common and entity-specific index settings
                IndexSettings.Builder entityIndexSettingsBuilder = indexSettingsBuilders.get(entityClass);
                entityIndexSettings = entityIndexSettingsBuilder.build();

                ObjectNode commonIndexSettingsNode = toObjectNode(commonIndexSettings);
                ObjectNode entityIndexSettingsNode = toObjectNode(entityIndexSettings);
                ObjectNode mergedIndexSettingsNode = JsonNodeFactory.instance.objectNode();
                mergedIndexSettingsNode.setAll(commonIndexSettingsNode);
                entityIndexSettingsNode.fieldNames().forEachRemaining(childName -> {
                    JsonNode specificChildNode = entityIndexSettingsNode.get(childName);
                    mergedIndexSettingsNode.set(childName, specificChildNode);
                });

                entityIndexSettings = deserializeIndexSettings(mergedIndexSettingsNode.toString());
            } else {
                entityIndexSettings = copyIndexSettings(commonIndexSettings);
            }

            Map<Class<?>, IndexSettingsAnalysis.Builder> analysisBuilders = context.getAllSpecificAnalysisBuilders();
            IndexSettingsAnalysis entityAnalysisSettings;
            if (analysisBuilders.containsKey(entityClass)) {
                // Merge common and entity-specific analysis settings
                ObjectNode commonAnalysisSettingsNode = toObjectNode(commonAnalysisSettings);
                IndexSettingsAnalysis.Builder entityAnalysisBuilder = analysisBuilders.get(entityClass);
                entityAnalysisSettings = entityAnalysisBuilder.build();

                ObjectNode entityAnalysisSettingsNode = toObjectNode(entityAnalysisSettings);
                ObjectNode mergedAnalysisSettingsNode = JsonNodeFactory.instance.objectNode();
                mergedAnalysisSettingsNode.setAll(commonAnalysisSettingsNode);
                entityAnalysisSettingsNode.fieldNames().forEachRemaining(childName -> {
                    JsonNode specificChildNode = entityAnalysisSettingsNode.get(childName);
                    JsonNode baseChildNode = mergedAnalysisSettingsNode.path(childName);
                    if (specificChildNode.isObject()) {
                        ObjectNode specificChildObjectNode = (ObjectNode) specificChildNode;
                        if (baseChildNode.isObject()) {
                            ((ObjectNode) baseChildNode).setAll(specificChildObjectNode);
                        } else {
                            mergedAnalysisSettingsNode.set(childName, specificChildObjectNode);
                        }
                    }
                });

                entityAnalysisSettings = deserializeAnalysisSettings(mergedAnalysisSettingsNode.toString());
            } else {
                entityAnalysisSettings = copyAnalysisSettings(commonAnalysisSettings);
            }

            /*
            Create result index settings as combination of merged index and analysis settings
             */
            IndexSettings.Builder resultBuilder = new IndexSettings.Builder().index(entityIndexSettings);

            /*
            Do not add analysis if they are empty.
            */
            if (!toObjectNode(entityAnalysisSettings).isEmpty()) {
                resultBuilder.analysis(entityAnalysisSettings);
            }

            resultIndexSettings = resultBuilder.build();

            /*
            Move content of the 'index' node (index settings) to the root level.
            Both structures are valid during creation but response for index metadata request
            contains settings on root level. So this 'movement' simplifies following settings comparison.
             */
            ObjectNode resultIndexSettingsNode = toObjectNode(resultIndexSettings);
            JsonNode indexNode = resultIndexSettingsNode.path("index");
            if (indexNode.isObject()) {
                resultIndexSettingsNode.setAll((ObjectNode) indexNode);
                resultIndexSettingsNode.remove("index");
            }

            resultIndexSettings = deserializeIndexSettings(resultIndexSettingsNode.toString());
            this.effectiveIndexSettings.put(entityClass, resultIndexSettings);
        }
        return resultIndexSettings;
    }

    protected OpenSearchIndexSettingsConfigurationContext configureContext() {
        OpenSearchIndexSettingsConfigurationContext context = new OpenSearchIndexSettingsConfigurationContext();
        systemConfigurers.forEach(configurer -> configurer.configure(context));
        customConfigurers.forEach(configurer -> configurer.configure(context));
        return context;
    }

    protected void prepareConfigurers(List<OpenSearchIndexSettingsConfigurer> configurers) {
        configurers.forEach(configurer -> {
            if (configurer.isSystem()) {
                systemConfigurers.add(configurer);
            } else {
                customConfigurers.add(configurer);
            }
        });
    }

    protected IndexSettings copyIndexSettings(IndexSettings source) {
        String serializedSource = serializeJsonpSerializable(source);
        return deserializeIndexSettings(serializedSource);
    }

    protected IndexSettingsAnalysis copyAnalysisSettings(IndexSettingsAnalysis source) {
        String serializedSource = serializeJsonpSerializable(source);
        return deserializeAnalysisSettings(serializedSource);
    }

    protected String serializeJsonpSerializable(JsonpSerializable object) {
        StringWriter stringWriter = new StringWriter();
        JsonpMapper mapper = client._transport().jsonpMapper();
        JsonGenerator generator = mapper.jsonProvider().createGenerator(stringWriter);
        object.serialize(generator, mapper);
        generator.close();
        return stringWriter.toString();
    }

    protected IndexSettings deserializeIndexSettings(String serializedSettings) {
        JsonpMapper jsonpMapper = client._transport().jsonpMapper();
        JsonProvider jsonProvider = jsonpMapper.jsonProvider();
        try (StringReader reader = new StringReader(serializedSettings)) {
            JsonParser parser = jsonProvider.createParser(reader);
            return IndexSettings._DESERIALIZER.deserialize(parser, jsonpMapper);
        }
    }

    protected IndexSettingsAnalysis deserializeAnalysisSettings(String serializedSettings) {
        JsonpMapper jsonpMapper = client._transport().jsonpMapper();
        JsonProvider jsonProvider = jsonpMapper.jsonProvider();
        try (StringReader reader = new StringReader(serializedSettings)) {
            JsonParser parser = jsonProvider.createParser(reader);
            return IndexSettingsAnalysis._DESERIALIZER.deserialize(parser, jsonpMapper);
        }
    }

    // TODO extract to platform-specific utils
    protected JsonNode toJsonNode(JsonpSerializable object) {
        String stringValue = serializeJsonpSerializable(object);

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
}
