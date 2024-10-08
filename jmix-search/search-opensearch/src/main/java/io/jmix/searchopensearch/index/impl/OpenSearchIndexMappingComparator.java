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

import io.jmix.search.index.impl.IndexMappingComparator;
import io.jmix.search.index.impl.MappingFieldComparator;
import jakarta.annotation.Nullable;
import org.opensearch.client.json.JsonpSerializable;
import org.opensearch.client.opensearch.indices.IndexState;
import org.springframework.stereotype.Component;

@Component("search_OpenSearchIndexMappingComparator")
public class OpenSearchIndexMappingComparator extends IndexMappingComparator<IndexState, JsonpSerializable> {

    public OpenSearchIndexMappingComparator(
            MappingFieldComparator mappingFieldComparator,
            OpenSearchJsonpSerializer jsonpSerializer) {
        super(mappingFieldComparator, jsonpSerializer);
    }

    @Override
    @Nullable
    protected JsonpSerializable extractTypeMapping(IndexState currentIndexState) {
        return currentIndexState.mappings();
    }
}
