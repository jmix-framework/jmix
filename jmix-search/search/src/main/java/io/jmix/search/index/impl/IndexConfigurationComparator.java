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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.mapping.IndexMappingConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public abstract class IndexConfigurationComparator<IndexStateType, TypeMappingType, IndexSettingsType> {
    private final IndexMappingComparator mappingComparator;
    private final IndexSettingsComparator settingsComparator;

    public IndexConfigurationComparator(IndexMappingComparator searchMappingChecker, IndexSettingsComparator settingsComparator) {
        this.mappingComparator = searchMappingChecker;
        this.settingsComparator = settingsComparator;
    }

    private static final Logger log = LoggerFactory.getLogger(IndexConfigurationComparator.class);

    protected ObjectMapper objectMapper = new ObjectMapper();

    public ConfigurationComparingResult compareConfigurations(IndexConfiguration indexConfiguration) {
        IndexStateType indexState  = getIndexState(indexConfiguration);
        IndexMappingComparator.MappingComparingResult mappingState = compareMappings(indexConfiguration.getMapping(), extractMapping(indexState), indexConfiguration.getIndexName());
        IndexSettingsComparator.SettingsComparingResult settingsState = compareSettings(indexConfiguration, extractSettings(indexState));
        return new ConfigurationComparingResult(mappingState, settingsState);
    }

    protected IndexMappingComparator.MappingComparingResult compareMappings(IndexMappingConfiguration indexMappingConfiguration, TypeMappingType typeMapping, String indexName) {

        Map<String, Object> searchIndexMapping = convertInexMappingToMap(typeMapping);
        Map<String, Object> applicationMapping = objectMapper.convertValue(
                indexMappingConfiguration,
                new TypeReference<Map<String, Object>>() {
                }
        );
        log.debug("Mappings of index '{}':\nCurrent: {}\nActual: {}",
                indexName, applicationMapping, searchIndexMapping);
        return mappingComparator.compare(searchIndexMapping, applicationMapping);
    }

    protected IndexSettingsComparator.SettingsComparingResult compareSettings(IndexConfiguration indexConfiguration, IndexSettingsType serverIndexSettings) {
        Map<String, String> applicationSettings = convertToMap(getApplicationSettings(indexConfiguration));
        Map<String, String> actualSettings = convertToMap(serverIndexSettings);
        return settingsComparator.compare(applicationSettings, actualSettings);
    }

    protected abstract IndexSettingsType getApplicationSettings(IndexConfiguration indexConfiguration);

    protected abstract Map<String, String> convertToMap(IndexSettingsType serverIndexSettings);

    protected abstract IndexSettingsType extractSettings(IndexStateType indexState);

    protected abstract TypeMappingType extractMapping(IndexStateType indexState);

    protected abstract IndexStateType getIndexState(IndexConfiguration indexConfiguration);

    protected abstract Map<String, Object> convertInexMappingToMap(TypeMappingType typeMapping);


    static class ConfigurationComparingResult {
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
