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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jmix.search.index.IndexConfiguration;
import jakarta.annotation.Nullable;

/**
 * The comparator that compares both the application's index configuration and the search index state
 * at the search server.
 *
 * @param <TState>    - type of the index state object
 * @param <TSettings> - type of the settings object
 * @param <TJsonp>    - Jsonp type
 */
public abstract class IndexConfigurationComparator<TState, TSettings, TJsonp> {

    protected final IndexSettingsComparator<TState, TSettings, TJsonp> settingsComparator;
    protected final IndexMappingComparator<TState, TJsonp> mappingComparator;
    protected final IndexStateResolver<TState, TJsonp> indexStateResolver;
    protected final ObjectMapper objectMapper = new ObjectMapper();

    public IndexConfigurationComparator(
            IndexMappingComparator<TState, TJsonp> mappingComparator,
            IndexSettingsComparator<TState, TSettings, TJsonp> settingsComparator,
            IndexStateResolver<TState, TJsonp> indexStateResolver) {
        this.mappingComparator = mappingComparator;
        this.settingsComparator = settingsComparator;
        this.indexStateResolver = indexStateResolver;
    }

    public ConfigurationComparingResult compareConfigurations(IndexConfiguration indexConfiguration) {
        TState indexState = getIndexState(indexConfiguration);
        if (indexState == null) {
            return new ConfigurationComparingResult(
                    MappingComparingResult.NOT_COMPATIBLE,
                    SettingsComparingResult.NOT_COMPATIBLE);
        }
        MappingComparingResult mappingState = mappingComparator.compare(indexConfiguration, indexState);
        SettingsComparingResult settingsState = settingsComparator.compareSettings(indexConfiguration, indexState);
        return new ConfigurationComparingResult(mappingState, settingsState);
    }

    @Nullable
    protected abstract TState getIndexState(IndexConfiguration indexConfiguration);

}
