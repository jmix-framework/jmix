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
import io.jmix.search.index.impl.PutMappingService;
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

@Component("search_OpenSearchPutMappingRequestService")
public class OpenSearchPutMappingRequestService implements PutMappingService<PutMappingRequest, JsonpMapper> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Valid
    public PutMappingRequest buildRequest(IndexMappingConfiguration mappingConfiguration, String indexName, JsonpMapper jsonpMapper) {
        return PutMappingRequest.of(builder -> builder.index(indexName).properties(getPropertiesMap(mappingConfiguration, jsonpMapper)));
    }

    private Map<String, Property> getPropertiesMap(IndexMappingConfiguration mappingConfiguration, JsonpMapper jsonpMapper) {

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
            throw new RuntimeException("Unable to parse mapping of index", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getPropertiesMap(IndexMappingConfiguration mappingConfiguration) {
        Map<String, Object> mappingBodyAsMap = objectMapper.convertValue(mappingConfiguration, new TypeReference<Map<String, Object>>() {
        });
        return (Map<String, Object>) mappingBodyAsMap.get("properties");
    }
}
