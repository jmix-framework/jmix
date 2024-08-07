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

public abstract class IndexConfigurationComparator<
        ClientType,
        IndexStateType,
        IndexSettingsType,
        JsonpSerializableType> {
    private final IndexSettingsComparator<IndexStateType, IndexSettingsType,ClientType, JsonpSerializableType> settingsComparator;
    private final IndexMappingComparator<IndexStateType, JsonpSerializableType, ClientType> mappingComparator;
    protected final MetadataResolver<ClientType, IndexStateType, JsonpSerializableType> metadataResolver;

    public IndexConfigurationComparator(
            IndexMappingComparator<IndexStateType, JsonpSerializableType, ClientType> mappingComparator,
            IndexSettingsComparator<IndexStateType, IndexSettingsType,ClientType, JsonpSerializableType> settingsComparator, MetadataResolver<ClientType, IndexStateType, JsonpSerializableType> metadataResolver) {
        this.mappingComparator = mappingComparator;
        this.settingsComparator = settingsComparator;
        this.metadataResolver = metadataResolver;
    }

    protected ObjectMapper objectMapper = new ObjectMapper();

    public ConfigurationComparingResult compareConfigurations(IndexConfiguration indexConfiguration, ClientType client) {
        IndexStateType indexState  = getIndexState(indexConfiguration, client);
        IndexMappingComparator.MappingComparingResult mappingState = mappingComparator.compare(indexConfiguration, indexState, client);
        IndexSettingsComparator.SettingsComparingResult settingsState = settingsComparator.compareSettings(indexConfiguration, indexState, client);
        return new ConfigurationComparingResult(mappingState, settingsState);
    }

    protected abstract IndexStateType getIndexState(IndexConfiguration indexConfiguration, ClientType client);


    public static class ConfigurationComparingResult {
        private final IndexMappingComparator.MappingComparingResult mappingComparingResult;
        private final IndexSettingsComparator.SettingsComparingResult settingsComparingResult;

        ConfigurationComparingResult(IndexMappingComparator.MappingComparingResult mappingComparingResult, IndexSettingsComparator.SettingsComparingResult settingsComparingResult) {
            this.mappingComparingResult = mappingComparingResult;
            this.settingsComparingResult = settingsComparingResult;
        }

        public boolean isIndexRecreatingRequired(){
            return mappingComparingResult.indexRecreatingIsRequired() || settingsComparingResult.indexRecreatingIsRequired();
        }

        public boolean isMappingUpdateRequired() {
            return mappingComparingResult.configurationUpdateIsRequired();
        }

        public boolean isSettingsUpdateRequired() {
            return settingsComparingResult.configurationUpdateIsRequired();
        }

        public boolean isConfigurationUpdateRequired(){
            return isMappingUpdateRequired() || isSettingsUpdateRequired();
        }
    }
}
