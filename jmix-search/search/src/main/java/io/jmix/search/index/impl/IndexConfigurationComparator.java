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

import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.toMap;

@Component("search_IndexConfigurationComparator")
public class IndexConfigurationComparator {
    private final IndexMappingComparator mappingComparator;
    private final IndexSettingsComparator settingsComparator;

    public IndexConfigurationComparator(IndexMappingComparator searchMappingChecker, IndexSettingsComparator settingsComparator) {
        this.mappingComparator = searchMappingChecker;
        this.settingsComparator = settingsComparator;
    }

    private static final Logger log = LoggerFactory.getLogger(IndexConfigurationComparator.class);

    protected ObjectMapper objectMapper = new ObjectMapper();

    public ConfigurationComparingResult compareConfigurations(IndexConfiguration indexConfiguration, GetIndexResponse indexResponse) {
        IndexMappingComparator.MappingComparingResult mappingState = compareMappings(indexConfiguration, indexResponse);
        IndexSettingsComparator.SettingsComparingResult settingsState = compareSettings(indexConfiguration, indexResponse);
        return new ConfigurationComparingResult(mappingState, settingsState);
    }

    protected IndexMappingComparator.MappingComparingResult compareMappings(IndexConfiguration indexConfiguration, GetIndexResponse indexResponse) {
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

    protected IndexSettingsComparator.SettingsComparingResult compareSettings(IndexConfiguration indexConfiguration, GetIndexResponse indexResponse) {
        Map<String, Settings> settings = indexResponse.getSettings();
        Map<String, String> currentSettings = convertToMap(settings.get(indexConfiguration.getIndexName()));
        Map<String, String> actualSettings = convertToMap(indexConfiguration.getSettings());
        return settingsComparator.compare(currentSettings, actualSettings);
    }

    private Map<String, String> convertToMap(Settings settings){
        return settings.keySet().stream().collect(toMap(identity(), settings::get));
    }

    static class ConfigurationComparingResult {
        private final IndexMappingComparator.MappingComparingResult mappingComparingResult;
        private final IndexSettingsComparator.SettingsComparingResult settingsComparingResult;

        ConfigurationComparingResult(IndexMappingComparator.MappingComparingResult mappingComparingResult, IndexSettingsComparator.SettingsComparingResult settingsComparingResult) {
            this.mappingComparingResult = mappingComparingResult;
            this.settingsComparingResult = settingsComparingResult;
        }

        public boolean indexRecreatingIsRequired(){
            return mappingComparingResult.indexRecreatingIsRequired() || settingsComparingResult.indexRecreatingIsRequired();
        }

        public boolean mappingUpdateIsRequired() {
            return mappingComparingResult.configurationUpdateIsRequired();
        }

        public boolean settingsUpdateIsRequired() {
            return settingsComparingResult.configurationUpdateIsRequired();
        }

        public boolean configurationUpdate(){
            return mappingUpdateIsRequired() || settingsUpdateIsRequired();
        }
    }
}
