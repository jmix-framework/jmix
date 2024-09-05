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

import io.jmix.core.common.util.Preconditions;
import io.jmix.search.index.impl.MetadataResolver;
import org.opensearch.client.json.JsonpSerializable;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.indices.IndexState;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component("search_OpenSearchMetadataResolver")
public class OpenSearchMetadataResolver extends MetadataResolver<IndexState, JsonpSerializable> {

    protected final OpenSearchClient client;

    public OpenSearchMetadataResolver(OpenSearchJsonpSerializer jsonpSerializer, OpenSearchClient client) {
        super(jsonpSerializer);
        this.client = client;
    }

    @Override
    protected Map<String, IndexState> getIndexMetadataMapInternal(String indexName) {
        Preconditions.checkNotNullArgument(indexName);
        try {
            return client.indices().get(builder -> builder.index(indexName).includeDefaults(true)).result();
        } catch (IOException e) {
            throw new RuntimeException("Unable to load metadata of index '" + indexName + "'", e);
        }
    }
}