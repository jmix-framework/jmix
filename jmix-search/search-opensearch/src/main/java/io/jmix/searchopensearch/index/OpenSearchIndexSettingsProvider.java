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

package io.jmix.searchopensearch.index;

import io.jmix.search.index.IndexConfiguration;
import org.opensearch.client.opensearch.indices.IndexSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component("search_OpenSearchIndexSettingsProvider")
public class OpenSearchIndexSettingsProvider {

    protected final List<OpenSearchIndexSettingsConfigurer> configurers;

    protected final OpenSearchIndexSettingsConfigurationContext context;

    protected final IndexSettings commonSettings;
    protected final Map<Class<?>, IndexSettings> indexSpecificSettings;

    @Autowired
    public OpenSearchIndexSettingsProvider(List<OpenSearchIndexSettingsConfigurer> configurers) {
        this.configurers = configurers;
        this.context = configureContext();
        this.commonSettings = context.getCommonSettingsBuilder().build();
        this.indexSpecificSettings = new ConcurrentHashMap<>();
    }

    public IndexSettings getSettingsForIndex(IndexConfiguration indexConfiguration) {
        Class<?> entityClass = indexConfiguration.getEntityClass();
        IndexSettings indexSettings = indexSpecificSettings.get(entityClass);
        if (indexSettings == null) {
            Map<Class<?>, IndexSettings.Builder> allSpecificSettingsBuilders = context.getAllSpecificSettingsBuilders();
            if (allSpecificSettingsBuilders.containsKey(entityClass)) {
                IndexSettings.Builder entitySettingsBuilder = context.getEntitySettingsBuilder(entityClass);
                indexSettings = entitySettingsBuilder.build();
                indexSpecificSettings.put(entityClass, indexSettings);
            } else {
                indexSettings = commonSettings;
            }
        }
        return indexSettings;
    }

    protected OpenSearchIndexSettingsConfigurationContext configureContext() {
        OpenSearchIndexSettingsConfigurationContext context = new OpenSearchIndexSettingsConfigurationContext();
        configurers.forEach(configurer -> configurer.configure(context));
        return context;
    }
}
