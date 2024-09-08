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
import io.jmix.search.index.mapping.AdvancedSearchSettings;
import io.jmix.searchopensearch.index.impl.OpenSearchAdvancedIndexSettingsConfigurer;
import org.opensearch.client.opensearch.indices.IndexSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component("search_OpenSearchIndexSettingsProvider")
public class OpenSearchIndexSettingsProvider {

    protected final List<OpenSearchIndexSettingsConfigurer> configurers;
    protected final OpenSearchAdvancedIndexSettingsConfigurer advancedIndexSettingsConfigurer;

    protected final OpenSearchIndexSettingsConfigurationContext context;
    protected final OpenSearchIndexSettingsConfigurationContext advancedIndexContext;

    protected final IndexSettings commonSettings;
    protected final Map<Class<?>, IndexSettings> indexSpecificSettings;

    @Autowired
    public OpenSearchIndexSettingsProvider(List<OpenSearchIndexSettingsConfigurer> configurers,
                                           OpenSearchAdvancedIndexSettingsConfigurer advancedIndexSettingsConfigurer) {
        this.configurers = getCustomConfigurers(configurers);
        this.advancedIndexSettingsConfigurer = advancedIndexSettingsConfigurer;
        this.context = configureContext();
        this.advancedIndexContext = configureAdvancedIndexContext();
        this.commonSettings = context.getCommonSettingsBuilder().build();
        this.indexSpecificSettings = new ConcurrentHashMap<>();
    }

    public IndexSettings getSettingsForIndex(IndexConfiguration indexConfiguration) {
        Class<?> entityClass = indexConfiguration.getEntityClass();
        AdvancedSearchSettings advancedSearchSettings = indexConfiguration.getAdvancedSearchSettings();

        OpenSearchIndexSettingsConfigurationContext activeContext;

        if(advancedSearchSettings != null && advancedSearchSettings.isEnabled()) { //todo remove nullability check
            activeContext = advancedIndexContext;
        } else {
            activeContext = context;
        }

        IndexSettings indexSettings = indexSpecificSettings.get(entityClass);
        if (indexSettings == null) {
            Map<Class<?>, IndexSettings.Builder> allSpecificSettingsBuilders = activeContext.getAllSpecificSettingsBuilders();
            if (allSpecificSettingsBuilders.containsKey(entityClass)) {
                IndexSettings.Builder entitySettingsBuilder = activeContext.getEntitySettingsBuilder(entityClass);
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
        advancedIndexSettingsConfigurer.configure(context);
        return context;
    }

    protected OpenSearchIndexSettingsConfigurationContext configureAdvancedIndexContext() {
        OpenSearchIndexSettingsConfigurationContext context = new OpenSearchIndexSettingsConfigurationContext(); // todo single context
        advancedIndexSettingsConfigurer.configure(context);
        return context;
    }

    protected List<OpenSearchIndexSettingsConfigurer> getCustomConfigurers(List<OpenSearchIndexSettingsConfigurer> configurers) {
        return configurers.stream()
                .filter(c -> !isSystemConfigurer(c))
                .toList();
    }

    protected boolean isSystemConfigurer(OpenSearchIndexSettingsConfigurer configurer) {
        return configurer instanceof OpenSearchAdvancedIndexSettingsConfigurer;
    }
}
