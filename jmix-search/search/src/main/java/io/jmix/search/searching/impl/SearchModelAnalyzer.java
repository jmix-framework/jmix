/*
 * Copyright 2025 Haulmont.
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

package io.jmix.search.searching.impl;

import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;

/**
 * TODO
 */
@Component("search_SearchModelAnalyzer")
public class SearchModelAnalyzer {

    protected final SearchSecurityDecorator securityDecorator;
    protected final IndexConfigurationManager indexConfigurationManager;
    protected final SearchFieldsResolver searchFieldsResolver;

    public SearchModelAnalyzer(SearchSecurityDecorator securityDecorator,
                               IndexConfigurationManager indexConfigurationManager,
                               SearchFieldsResolver searchFieldsResolver) {
        this.securityDecorator = securityDecorator;
        this.indexConfigurationManager = indexConfigurationManager;
        this.searchFieldsResolver = searchFieldsResolver;
    }

    public Map<String, Set<String>> getIndexesWithFields(List<String> entities, Function<String, Set<String>> subfieldsGenerator) {

        Collection<String> entitiesWithSearchConfiguration = getEntitiesWithConfiguration(entities);

        List<String> allowedEntityNames = securityDecorator.resolveEntitiesAllowedToSearch(entitiesWithSearchConfiguration);

        if (allowedEntityNames.isEmpty()) {
            return emptyMap();
        }

        Map<String, Set<String>> notFilteredIndexesWithFields = allowedEntityNames
                .stream()
                .map(indexConfigurationManager::getIndexConfigurationByEntityName)
                .collect(Collectors.toMap(
                        IndexConfiguration::getIndexName,
                        conf -> searchFieldsResolver.resolveFields(conf, subfieldsGenerator)));

        Map<String, Set<String>> result = notFilteredIndexesWithFields
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (result.isEmpty()) {
            return emptyMap();
        }
        return result;
    }

    protected Collection<String> getEntitiesWithConfiguration(List<String> entities) {
        Collection<String> allIndexedEntities = indexConfigurationManager.getAllIndexedEntities();
        if (entities.isEmpty()) {
            return allIndexedEntities;
        }
        return entities
                .stream()
                .filter(allIndexedEntities::contains)
                .toList();
    }
}