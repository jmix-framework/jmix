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

import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.impl.IndexConfigurationComparator;
import io.jmix.search.index.impl.IndexMappingComparator;
import io.jmix.search.index.impl.IndexSettingsComparator;
import io.jmix.searchopensearch.index.OpenSearchIndexSettingsProvider;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;
import org.opensearch.client.opensearch.indices.IndexSettings;
import org.opensearch.client.opensearch.indices.IndexState;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OpenSearchIndexConfigurationComparator extends IndexConfigurationComparator<IndexState, TypeMapping, IndexSettings> {
    private final OpenSearchIndexSettingsProvider indexSettingsProvider;

    public OpenSearchIndexConfigurationComparator(IndexMappingComparator searchMappingChecker,
                                                  IndexSettingsComparator settingsComparator,
                                                  OpenSearchIndexSettingsProvider indexSettingsProvider) {
        super(searchMappingChecker, settingsComparator);
        this.indexSettingsProvider = indexSettingsProvider;
    }

    @Override
    protected IndexSettings getApplicationSettings(IndexConfiguration indexConfiguration) {
        return indexSettingsProvider.getSettingsForIndex(indexConfiguration);
    }

    @Override
    protected Map<String, String> convertToMap(IndexSettings serverIndexSettings) {
        return null;
    }

    @Override
    protected IndexSettings extractSettings(IndexState indexState) {
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