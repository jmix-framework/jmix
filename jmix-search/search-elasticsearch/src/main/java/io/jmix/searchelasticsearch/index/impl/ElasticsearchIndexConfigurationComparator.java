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

import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.IndexState;
import co.elastic.clients.elasticsearch.transform.Settings;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.impl.IndexConfigurationComparator;
import io.jmix.search.index.impl.IndexMappingComparator;
import io.jmix.search.index.impl.IndexSettingsComparator;

import java.util.Map;

public class ElasticsearchIndexConfigurationComparator extends IndexConfigurationComparator<IndexState, TypeMapping, Settings> {
    public ElasticsearchIndexConfigurationComparator(IndexMappingComparator searchMappingChecker, IndexSettingsComparator settingsComparator) {
        super(searchMappingChecker, settingsComparator);
    }

    @Override
    protected Settings getApplicationSettings(IndexConfiguration indexConfiguration) {
        return null;
    }

    @Override
    protected Map<String, String> convertToMap(Settings serverIndexSettings) {
        return null;
    }

    @Override
    protected Settings extractSettings(IndexState indexState) {
        return null;
    }

    @Override
    protected TypeMapping extractMapping(IndexState indexState) {
        return null;
    }

    @Override
    protected IndexState getIndexState(IndexConfiguration indexConfiguration) {
        return null;
    }

    @Override
    protected Map<String, Object> convertInexMappingToMap(TypeMapping typeMapping) {
        return null;
    }
}
