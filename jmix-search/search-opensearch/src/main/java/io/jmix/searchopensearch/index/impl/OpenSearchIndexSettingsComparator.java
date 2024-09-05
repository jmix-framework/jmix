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
import io.jmix.search.index.impl.IndexSettingsComparator;
import io.jmix.search.index.impl.JsonNodesComparator;
import io.jmix.searchopensearch.index.OpenSearchIndexSettingsProvider;
import org.opensearch.client.json.JsonpSerializable;
import org.opensearch.client.opensearch.indices.IndexSettings;
import org.opensearch.client.opensearch.indices.IndexState;
import org.springframework.stereotype.Component;

@Component("search_OpenSearchIndexSettingsComparator")
public class OpenSearchIndexSettingsComparator extends IndexSettingsComparator<IndexState, IndexSettings, JsonpSerializable> {

    protected final OpenSearchIndexSettingsProvider settingsProvider;

    public OpenSearchIndexSettingsComparator(OpenSearchJsonpSerializer jsonpSerializer, JsonNodesComparator jsonNodesComparator, OpenSearchIndexSettingsProvider settingsProvider) {
        super(jsonpSerializer, jsonNodesComparator);
        this.settingsProvider = settingsProvider;
    }

    @Override
    protected IndexSettings extractAllAppliedIndexSettings(IndexState currentIndexState) {
        return currentIndexState.settings();
    }

    @Override
    protected IndexSettings extractAppliedIndexSettings(IndexSettings allAppliedSettings) {
        //TODO
        return allAppliedSettings.index();
    }

    @Override
    protected JsonpSerializable getExpectedIndexSettings(IndexConfiguration indexConfiguration) {
        return settingsProvider.getSettingsForIndex(indexConfiguration);
    }
}
