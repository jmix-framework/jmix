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
import jakarta.annotation.Nullable;

import java.util.Map;

/**
 * Class encapsulates the logic of receiving the index state from the search engine service.
 *
 * @param <TState> search client's specific index state type
 * @param <TJsonp> search client's specific Jsonp type
 */
public abstract class IndexStateResolver<TState, TJsonp> {

    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected final JsonpSerializer<TJsonp> jsonpSerializer;

    protected IndexStateResolver(JsonpSerializer<TJsonp> jsonpSerializer) {
        this.jsonpSerializer = jsonpSerializer;
    }

    @SuppressWarnings("unchecked")
    public ObjectNode getSerializedState(String indexName) {
        TState indexState = getState(indexName);
        if (indexState == null) {
            return objectMapper.createObjectNode();
        }
        return jsonpSerializer.toObjectNode((TJsonp) indexState);
    }

    @Nullable
    public TState getState(String indexName) {
        return getIndexMetadataMapInternal(indexName).get(indexName);
    }

    protected abstract Map<String, TState> getIndexMetadataMapInternal(String indexName);
}
