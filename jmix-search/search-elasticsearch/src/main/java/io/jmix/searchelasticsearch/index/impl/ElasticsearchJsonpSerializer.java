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
import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.json.JsonpSerializable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.jmix.search.index.impl.BaseJsonpSerializer;
import jakarta.json.stream.JsonGenerator;
import org.springframework.stereotype.Component;

import java.io.StringWriter;

@Component("search_ElasticsearchJsonpSerializer")
public class ElasticsearchJsonpSerializer extends BaseJsonpSerializer<JsonpSerializable> {

    protected final ElasticsearchClient searchClient;

    public ElasticsearchJsonpSerializer(ElasticsearchClient searchClient) {
        this.searchClient = searchClient;
    }

    @Override
    protected JsonNode toJsonNode(JsonpSerializable object) {
        JsonpMapper jsonpMapper = searchClient._transport().jsonpMapper();
        StringWriter stringWriter = new StringWriter();
        JsonGenerator generator = jsonpMapper.jsonProvider().createGenerator(stringWriter);
        object.serialize(generator, jsonpMapper);
        generator.close();
        String stringValue = stringWriter.toString();

        try {
            return objectMapper.readTree(stringValue);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to generate JsonNode", e);
        }
    }
}