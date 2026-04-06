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

import co.elastic.clients.elasticsearch.indices.IndexSettings;
import co.elastic.clients.elasticsearch.indices.IndexState;
import co.elastic.clients.json.JsonpSerializable;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.impl.IndexConfigurationComparator;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component("search_ElasticsearchIndexConfigurationComparator")
public class ElasticsearchIndexConfigurationComparator
        extends IndexConfigurationComparator<IndexState, IndexSettings, JsonpSerializable> {

    public ElasticsearchIndexConfigurationComparator(ElasticsearchIndexMappingComparator searchMappingChecker,
                                                     ElastisearchIndexSettingsComparator settingsComparator,
                                                     ElasticsearchIndexStateResolver metadataResolver) {
        super(searchMappingChecker, settingsComparator, metadataResolver);
    }

    @Override
    @Nullable
    protected IndexState getIndexState(IndexConfiguration indexConfiguration) {
        return indexStateResolver.getState(indexConfiguration.getIndexName());
    }

}
