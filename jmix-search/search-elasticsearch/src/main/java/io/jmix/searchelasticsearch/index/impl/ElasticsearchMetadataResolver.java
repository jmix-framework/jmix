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
import co.elastic.clients.elasticsearch.indices.IndexState;
import co.elastic.clients.json.JsonpSerializable;
import io.jmix.core.common.util.Preconditions;
import io.jmix.search.index.impl.MetadataResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class ElasticsearchMetadataResolver extends MetadataResolver<ElasticsearchClient, IndexState, JsonpSerializable> {
    protected ElasticsearchMetadataResolver(ElasticsearchJsonpSerializer jsonpSerializer) {
        super(jsonpSerializer);
    }

    @Override
    protected Map<String, IndexState> getIndexMetadataMapInternal(String indexName, ElasticsearchClient client) {
        Preconditions.checkNotNullArgument(indexName);
        try {
            return client.indices().get(builder -> builder.index(indexName).includeDefaults(true)).result();
        } catch (IOException e) {
            throw new RuntimeException("Unable to load metadata of index '" + indexName + "'", e);
        }
    }
}