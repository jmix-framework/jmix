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

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.search.index.IndexConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class IndexSettingsComparator<IndexStateType, ClientType, JsonpSerializableType> {
    private static final Logger log = LoggerFactory.getLogger(IndexSettingsComparator.class);

    private final JsonpSerializer<JsonpSerializableType, ClientType> jsonpSerializer;
    private final JsonNodesComparator jsonNodesComparator;

    public IndexSettingsComparator(JsonpSerializer<JsonpSerializableType, ClientType> jsonpSerializer, JsonNodesComparator jsonNodesComparator) {
        this.jsonpSerializer = jsonpSerializer;
        this.jsonNodesComparator = jsonNodesComparator;
    }

    public  SettingsComparingResult compareSettings(IndexConfiguration indexConfiguration, IndexStateType currentIndexState, ClientType client) {

        ObjectNode expectedSettingsNode = jsonpSerializer.toObjectNode(getExpectedIndexSettings(indexConfiguration), client);
        ObjectNode appliedSettingsNode = jsonpSerializer.toObjectNode(getAppliedIndexSettings(currentIndexState, indexConfiguration.getIndexName()), client);

        log.debug("Settings of index '{}':\nExpected: {}\nApplied: {}",
                indexConfiguration.getIndexName(), expectedSettingsNode, appliedSettingsNode);

        return jsonNodesComparator.nodeContains(appliedSettingsNode, expectedSettingsNode)? SettingsComparingResult.EQUAL: SettingsComparingResult.NOT_COMPATIBLE;
    }

    protected abstract JsonpSerializableType getAppliedIndexSettings(IndexStateType indexConfiguration, String indexName);

    protected abstract JsonpSerializableType getExpectedIndexSettings(IndexConfiguration indexConfiguration);

    public enum SettingsComparingResult implements ConfigurationPartComparingResult{
        EQUAL,
        NOT_COMPATIBLE;

        @Override
        public boolean indexRecreatingIsRequired() {
            return this == NOT_COMPATIBLE;
        }

        @Override
        public boolean configurationUpdateIsRequired() {
            return false;
        }
    }
}
