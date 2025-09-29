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

import io.jmix.core.DevelopmentException;
import io.jmix.core.Messages;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.mapping.IndexConfigurationManager;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractSearchQueryConfigurator<SRB, QB, OB> implements SearchQueryConfigurator<SRB, QB, OB> {

    protected final SearchUtils searchUtils;
    protected final IndexConfigurationManager indexConfigurationManager;
    protected final Messages messages;

    public AbstractSearchQueryConfigurator(SearchUtils searchUtils, IndexConfigurationManager indexConfigurationManager, Messages messages) {
        this.searchUtils = searchUtils;
        this.indexConfigurationManager = indexConfigurationManager;
        this.messages = messages;
    }

    protected Map<String, Set<String>> getIndexNamesWithFields(List<String> entities, Function<IndexConfiguration, Set<String>> fieldResolving) {
        if(entities.isEmpty()){
            throwException();
        }

        List<String> allowedEntityNames = searchUtils.resolveEntitiesAllowedToSearch(entities);

        if (allowedEntityNames.isEmpty()) {
            throwException();
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
            throwException();
        }
        return result;
    }

    private void throwException() {
        throw new DevelopmentException(messages.getMessage(this.getClass(), "ThereAreNoIndexesForSearchingMessage"));
    }
}
