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
    private final SearchMappingChecker searchMappingChecker;

    public IndexConfigurationsChecker(SearchMappingChecker searchMappingChecker) {
        this.searchMappingChecker = searchMappingChecker;
    }

    private static final Logger log = LoggerFactory.getLogger(IndexConfigurationsChecker.class);


    protected ObjectMapper objectMapper = new ObjectMapper();

    public boolean areConfigurationsCompatible(IndexConfiguration indexConfiguration, GetIndexResponse indexResponse) {
        boolean indexMappingActual = isIndexMappingActual(indexConfiguration, indexResponse);
        boolean indexSettingsActual = isIndexSettingsActual(indexConfiguration, indexResponse);

        return indexMappingActual && indexSettingsActual;
        }

    protected boolean isIndexMappingActual(IndexConfiguration indexConfiguration, GetIndexResponse indexResponse) {
        Map<String, MappingMetadata> mappings = indexResponse.getMappings();
        MappingMetadata indexMappingMetadata = mappings.get(indexConfiguration.getIndexName());
        Map<String, Object> applicationMapping = indexMappingMetadata.getSourceAsMap();
        Map<String, Object> searchIndexMapping = objectMapper.convertValue(
                indexConfiguration.getMapping(),
                new TypeReference<Map<String, Object>>() {
                }
        );
        log.debug("Mappings of index '{}':\nCurrent: {}\nActual: {}",
                indexConfiguration.getIndexName(), applicationMapping, searchIndexMapping);
        return searchMappingChecker.areMappingsCompatible(searchIndexMapping, applicationMapping);

    }

    protected boolean isIndexSettingsActual(IndexConfiguration indexConfiguration, GetIndexResponse indexResponse) {
        Map<String, Settings> settings = indexResponse.getSettings();
        Settings currentSettings = settings.get(indexConfiguration.getIndexName());
        Settings actualSettings = indexConfiguration.getSettings();
        long unmatchedSettings = actualSettings.keySet().stream().filter(key -> {
            String actualValue = actualSettings.get(key);
            String currentValue = currentSettings.get(key);
            return !actualValue.equals(currentValue);
        }).count();

        return unmatchedSettings == 0;
    }
}
