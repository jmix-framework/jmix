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

import java.util.Optional;


/**
 * Class encapsulates logic of both the settings from the application's index configuration
 * and the settings from the index state from the search server comparing.
 *
 * @param <TState>    the received from the server index state type
 * @param <TSettings> specific for the search client settings type
 * @param <TJsonp>    specific for the search client Jsonp type
 */
public abstract class IndexSettingsComparator<TState, TSettings, TJsonp> {

    private static final Logger log = LoggerFactory.getLogger(IndexSettingsComparator.class);

    protected final JsonpSerializer<TJsonp> jsonpSerializer;
    protected final JsonNodesComparator jsonNodesComparator;

    public IndexSettingsComparator(JsonpSerializer<TJsonp> jsonpSerializer, JsonNodesComparator jsonNodesComparator) {
        this.jsonpSerializer = jsonpSerializer;
        this.jsonNodesComparator = jsonNodesComparator;
    }

    public SettingsComparingResult compareSettings(IndexConfiguration indexConfiguration, TState currentIndexState) {

        ObjectNode expectedSettingsNode = jsonpSerializer.toObjectNode(getExpectedIndexSettings(indexConfiguration));
        ObjectNode appliedSettingsNode
                = jsonpSerializer.toObjectNode(getAppliedIndexSettings(
                        currentIndexState,
                        indexConfiguration.getIndexName()));

        log.debug("Settings of index '{}':\nExpected: {}\nApplied: {}",
                indexConfiguration.getIndexName(), expectedSettingsNode, appliedSettingsNode);

        return jsonNodesComparator.nodeContains(
                appliedSettingsNode,
                expectedSettingsNode) ? SettingsComparingResult.EQUAL : SettingsComparingResult.NOT_COMPATIBLE;
    }

    @SuppressWarnings("unchecked")
    protected TJsonp getAppliedIndexSettings(TState currentIndexState, String indexName) {
        Optional<TSettings> appliedIndexSettings = extractAppliedIndexSettings(currentIndexState, indexName);
        if (appliedIndexSettings.isEmpty()) {
            throw new IllegalArgumentException(
                    "No info about applied index settings for index '" + indexName + "'"
            );
        }
        return (TJsonp) appliedIndexSettings;
    }

    protected abstract Optional<TSettings> extractAppliedIndexSettings(TState currentIndexState, String indexName);

    protected abstract TJsonp getExpectedIndexSettings(IndexConfiguration indexConfiguration);

}
