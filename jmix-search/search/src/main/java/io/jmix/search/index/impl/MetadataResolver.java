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

package io.jmix.search.index.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

public abstract class MetadataResolver<TClient, TState, TJsonp> {

    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected final JsonpSerializer<TJsonp, TClient> jsonpSerializer;

    protected MetadataResolver(JsonpSerializer<TJsonp, TClient> jsonpSerializer) {
        this.jsonpSerializer = jsonpSerializer;
    }

    public ObjectNode getIndexMetadata(String indexName, TClient client) {
        TState indexState = getIndexMetadataInternal(indexName, client);
        if (indexState == null) {
            return objectMapper.createObjectNode();
        }
        //TODO
        return jsonpSerializer.toObjectNode((TJsonp) indexState, client);
    }

    public TState getIndexMetadataInternal(String indexName, TClient client) {
        return getIndexMetadataMapInternal(indexName, client).get(indexName);
    }

    protected abstract Map<String, TState> getIndexMetadataMapInternal(String indexName, TClient client);
}
