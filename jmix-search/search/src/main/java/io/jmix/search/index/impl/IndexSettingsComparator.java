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

public abstract class IndexSettingsComparator<TState, TSettings, TClient, TJsonp> {
    private static final Logger log = LoggerFactory.getLogger(IndexSettingsComparator.class);

    protected final JsonpSerializer<TJsonp, TClient> jsonpSerializer;
    protected final JsonNodesComparator jsonNodesComparator;

    public IndexSettingsComparator(JsonpSerializer<TJsonp, TClient> jsonpSerializer, JsonNodesComparator jsonNodesComparator) {
        this.jsonpSerializer = jsonpSerializer;
        this.jsonNodesComparator = jsonNodesComparator;
    }

    public SettingsComparingResult compareSettings(IndexConfiguration indexConfiguration, TState currentIndexState, TClient client) {

        ObjectNode expectedSettingsNode = jsonpSerializer.toObjectNode(getExpectedIndexSettings(indexConfiguration), client);
        ObjectNode appliedSettingsNode = jsonpSerializer.toObjectNode(getAppliedIndexSettings(currentIndexState, indexConfiguration.getIndexName()), client);

        log.debug("Settings of index '{}':\nExpected: {}\nApplied: {}",
                indexConfiguration.getIndexName(), expectedSettingsNode, appliedSettingsNode);

        return jsonNodesComparator.nodeContains(appliedSettingsNode, expectedSettingsNode) ? SettingsComparingResult.EQUAL : SettingsComparingResult.NOT_COMPATIBLE;
    }

    protected TJsonp getAppliedIndexSettings(TState currentIndexState, String indexName) {
        TSettings allAppliedSettings = extractAllAppliedIndexSettings(currentIndexState);

        if (allAppliedSettings == null) {
            throw new IllegalArgumentException(
                    "No info about all applied settings for index '" + indexName + "'"
            );
        }

        TSettings appliedIndexSettings = extractAppliedIndexSettings(allAppliedSettings);
        if (appliedIndexSettings == null) {
            throw new IllegalArgumentException(
                    "No info about applied index settings for index '" + indexName + "'"
            );
        }

        //TODO cast check
        return (TJsonp) appliedIndexSettings;
    }

    protected abstract TSettings extractAllAppliedIndexSettings(TState currentIndexState);

    protected abstract TSettings extractAppliedIndexSettings(TSettings allAppliedSettings);

    protected abstract TJsonp getExpectedIndexSettings(IndexConfiguration indexConfiguration);

    public enum SettingsComparingResult implements ConfigurationPartComparingResult {
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
