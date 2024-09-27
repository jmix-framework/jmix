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
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jmix.search.index.impl.PutMappingBuilder;
import io.jmix.search.index.mapping.IndexMappingConfiguration;
import jakarta.json.spi.JsonProvider;
import jakarta.json.stream.JsonParser;
import jakarta.validation.Valid;
import org.opensearch.client.json.JsonpDeserializer;
import org.opensearch.client.json.JsonpMapper;
import org.opensearch.client.opensearch._types.mapping.Property;
import org.opensearch.client.opensearch.indices.PutMappingRequest;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.util.Map;

@Component("search_OpenSearchPutMappingRequestBuilder")
public class OpenSearchPutMappingRequestBuilder implements PutMappingBuilder<PutMappingRequest, JsonpMapper> {

    public static final TypeReference<Map<String, Object>> TYPE_REF = new TypeReference<>() {
    };
    private static final String MAPPING_CONFIGURATION_PARSING_EXCEPTION_TEXT = "Unable to parse the mapping of the index.";
    private static final String PROPERTIES_KEY = "properties";
    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Valid
    public PutMappingRequest buildRequest(IndexMappingConfiguration mappingConfiguration, String indexName, JsonpMapper jsonpMapper) {
        return PutMappingRequest.of(builder -> builder.index(indexName).properties(getPropertiesMap(mappingConfiguration, jsonpMapper)));
    }

    protected Map<String, Property> getPropertiesMap(IndexMappingConfiguration mappingConfiguration, JsonpMapper jsonpMapper) {

        JsonpDeserializer<Map<String, Property>> mapJsonpDeserializer = JsonpDeserializer.stringMapDeserializer(Property._DESERIALIZER);
        try {
            Map<String, Object> propertiesMap = getPropertiesMap(mappingConfiguration);
            String mappingBody = objectMapper.writeValueAsString(propertiesMap);
            JsonProvider jsonProvider = jsonpMapper.jsonProvider();
            try (StringReader reader = new StringReader(mappingBody)) {
                JsonParser parser = jsonProvider.createParser(reader);
                return mapJsonpDeserializer.deserialize(parser, jsonpMapper);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(MAPPING_CONFIGURATION_PARSING_EXCEPTION_TEXT, e);
        }
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> getPropertiesMap(IndexMappingConfiguration mappingConfiguration) {
        Map<String, Object> mappingBodyAsMap = objectMapper.convertValue(mappingConfiguration, TYPE_REF);
        if (mappingBodyAsMap != null && mappingBodyAsMap.containsKey(PROPERTIES_KEY)) {
            Object properties = mappingBodyAsMap.get(PROPERTIES_KEY);
            if (properties instanceof Map<?, ?>) {
                return (Map<String, Object>) properties;
            }
        }

        throw new RuntimeException(MAPPING_CONFIGURATION_PARSING_EXCEPTION_TEXT);
    }
}
