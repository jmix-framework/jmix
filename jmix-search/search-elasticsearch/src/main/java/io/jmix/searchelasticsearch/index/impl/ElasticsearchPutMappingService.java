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

import co.elastic.clients.elasticsearch.indices.PutMappingRequest;
import co.elastic.clients.json.JsonpMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jmix.search.index.impl.PutMappingService;
import io.jmix.search.index.mapping.IndexMappingConfiguration;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Component("search_ElasticsearchPutMappingService")
public class ElasticsearchPutMappingService implements PutMappingService<PutMappingRequest, JsonpMapper> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public PutMappingRequest buildRequest(IndexMappingConfiguration mappingConfiguration, String indexName, JsonpMapper jsonpMapper) {
        InputStream mappingBodyStream = getMappingAsStream(indexName, mappingConfiguration);
        return PutMappingRequest.of(builder -> builder.index(indexName).withJson(mappingBodyStream));
    }

    private InputStream getMappingAsStream(String indexName, IndexMappingConfiguration mapping) {
        InputStream mappingBodyStream;
        try {
            String mappingBody = objectMapper.writeValueAsString(mapping);
            mappingBodyStream = new ByteArrayInputStream(mappingBody.getBytes());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to update index mapping'" + indexName + "': Failed to parse index mapping.", e);
        }
        return mappingBodyStream;
    }

}
