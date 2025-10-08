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
import io.jmix.search.searching.SubfieldsProvider;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;

/**
 * This class contains methods for getting information for the search request building.
 * The analysis is based on the {@link IndexConfiguration} objects and entities metadata processing.
 */
@Component("search_SearchModelAnalyzer")
public class SearchModelAnalyzer {

    protected final SearchSecurityDecorator securityDecorator;
    protected final IndexConfigurationManager indexConfigurationManager;
    protected final SearchFieldsProvider searchFieldsProvider;

    public SearchModelAnalyzer(SearchSecurityDecorator securityDecorator,
                               IndexConfigurationManager indexConfigurationManager,
                               SearchFieldsProvider searchFieldsProvider) {
        this.securityDecorator = securityDecorator;
        this.indexConfigurationManager = indexConfigurationManager;
        this.searchFieldsProvider = searchFieldsProvider;
    }

    /**
     * Calculates a map for the search request building.
     * The method takes into account user rights to entities and their parameters.
     * The method adds additional subfields name of which can be provided with the subfieldsProvider parameter.
     *
     * @param entities - a collection of the entity names for the search request building
     * @param subfieldsProvider a {@link Function} for getting subfields of the index field.
     *                           If the function returns an empty set,
     *                           the only initial field name will be added to the result.
     * @return a map that contains indexNames as keys and sets of correspondent fieldNames as values
     * from the correspondent {@link io.jmix.search.index.mapping.IndexMappingConfiguration} for each index.
     */
    public Map<String, Set<String>> getIndexesWithFields(List<String> entities, SubfieldsProvider subfieldsProvider) {

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
                        conf -> searchFieldsProvider.resolveFields(conf, subfieldsProvider)));

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