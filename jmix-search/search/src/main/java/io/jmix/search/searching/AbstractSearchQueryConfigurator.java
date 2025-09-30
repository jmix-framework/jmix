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

package io.jmix.search.searching;

import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.mapping.IndexConfigurationManager;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * //TODO
 * @param <SRB>
 * @param <QB>
 * @param <OB>
 */
public abstract class AbstractSearchQueryConfigurator<SRB, QB, OB> implements SearchQueryConfigurator<SRB, QB, OB> {

    protected static final String THERE_ARE_NO_INDEXES_FOR_SEARCHING_MESSAGE_KEY = "ThereAreNoIndexesForSearchingMessage";
    protected final SearchUtils searchUtils;
    protected final IndexConfigurationManager indexConfigurationManager;

    public AbstractSearchQueryConfigurator(SearchUtils searchUtils, IndexConfigurationManager indexConfigurationManager) {
        this.searchUtils = searchUtils;
        this.indexConfigurationManager = indexConfigurationManager;
    }

    protected Map<String, Set<String>> getIndexNamesWithFields(List<String> entities, Function<IndexConfiguration, Set<String>> fieldResolving) throws NoAllowedEntitiesForSearching {
        if(entities.isEmpty()){
            throw new NoAllowedEntitiesForSearching();
        }

        //TODO
        List<String> allowedEntityNames = searchUtils.resolveEntitiesAllowedToSearch(entities);

        if (allowedEntityNames.isEmpty()) {
            throw new NoAllowedEntitiesForSearching();
        }

        Map<String, Set<String>> notFilteredIndexesWithFields = allowedEntityNames
                .stream()
                .map(indexConfigurationManager::getIndexConfigurationByEntityName)
                .collect(Collectors.toMap(IndexConfiguration::getIndexName, fieldResolving));

        Map<String, Set<String>> result = notFilteredIndexesWithFields
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (result.isEmpty()) {
            throw new NoAllowedEntitiesForSearching();
        }
        return result;
    }

}
