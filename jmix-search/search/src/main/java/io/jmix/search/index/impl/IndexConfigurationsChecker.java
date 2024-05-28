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
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.common.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class IndexConfigurationsChecker {
    private final SearchMappingComparator mappingComparator;
    private final SearchSettingsComparator settingsComparator;

    public IndexConfigurationsChecker(SearchMappingComparator searchMappingChecker, SearchSettingsComparator settingsComparator) {
        this.mappingComparator = searchMappingChecker;
        this.settingsComparator = settingsComparator;
    }

    private static final Logger log = LoggerFactory.getLogger(IndexConfigurationsChecker.class);


    protected ObjectMapper objectMapper = new ObjectMapper();

    public ConfiguarionComparingResult compareConfigurations(IndexConfiguration indexConfiguration, GetIndexResponse indexResponse) {
        ComparingState mappingState = isIndexMappingActual(indexConfiguration, indexResponse);
        ComparingState settingsState = isIndexSettingsActual(indexConfiguration, indexResponse);

        return new ConfiguarionComparingResult(
                mappingState.isReindexingNotRequired() && settingsState.isReindexingNotRequired(),
                mappingState.isConfigurationUpdateRequired(),
                settingsState.isConfigurationUpdateRequired()
        );
    }

    protected ComparingState isIndexMappingActual(IndexConfiguration indexConfiguration, GetIndexResponse indexResponse) {
        Map<String, MappingMetadata> mappings = indexResponse.getMappings();
        MappingMetadata indexMappingMetadata = mappings.get(indexConfiguration.getIndexName());
        Map<String, Object> searchIndexMapping = indexMappingMetadata.getSourceAsMap();
        Map<String, Object> applicationMapping = objectMapper.convertValue(
                indexConfiguration.getMapping(),
                new TypeReference<Map<String, Object>>() {
                }
        );
        log.debug("Mappings of index '{}':\nCurrent: {}\nActual: {}",
                indexConfiguration.getIndexName(), applicationMapping, searchIndexMapping);
        return mappingComparator.compare(searchIndexMapping, applicationMapping);

    }

    protected ComparingState isIndexSettingsActual(IndexConfiguration indexConfiguration, GetIndexResponse indexResponse) {
        Map<String, Settings> settings = indexResponse.getSettings();
        Settings currentSettings = settings.get(indexConfiguration.getIndexName());
        Settings actualSettings = indexConfiguration.getSettings();
        return settingsComparator.compare(currentSettings, actualSettings);
    }

    public class ConfiguarionComparingResult {
        private final boolean compatible;
        private final boolean mappingMustBeActualized;
        private final boolean settingsMustBeActualized;

        ConfiguarionComparingResult(boolean compatible, boolean mappingMustBeActualized, boolean settingsMustBeActualized) {
            this.compatible = compatible;
            this.mappingMustBeActualized = mappingMustBeActualized;
            this.settingsMustBeActualized = settingsMustBeActualized;
        }

        public boolean isCompatible() {
            return compatible;
        }

        public boolean isMappingMustBeActualized() {
            return mappingMustBeActualized;
        }

        public boolean isSettingsMustBeActualized() {
            return settingsMustBeActualized;
        }
    }
}
