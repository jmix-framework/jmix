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

import static io.jmix.search.searching.SearchContextProcessingResult.NO_AVAILABLE_ENTITIES_FOR_SEARCHING;
import static java.util.Collections.emptyMap;

/**
 * //TODO
 * @param <SRB>
 * @param <QB>
 * @param <OB>
 */
public abstract class AbstractSearchQueryConfigurator<SRB, QB, OB> implements SearchQueryConfigurator<SRB, QB, OB> {

    protected final SearchUtils searchUtils;
    protected final IndexConfigurationManager indexConfigurationManager;

    public AbstractSearchQueryConfigurator(SearchUtils searchUtils, IndexConfigurationManager indexConfigurationManager) {
        this.searchUtils = searchUtils;
        this.indexConfigurationManager = indexConfigurationManager;
    }

    @Override
    public void configureRequest(
            RequestContext<SRB> requestContext,
            Function<IndexConfiguration, Set<String>> fieldResolving,
            TargetQueryBuilder<QB, OB> targetQueryBuilder)  {
        List<String> requestedEntities = requestContext.getSearchContext().getEntities();
        Map<String, Set<String>> indexNamesWithFields = getIndexNamesWithFields(requestedEntities, fieldResolving);
        if (indexNamesWithFields.isEmpty()){
            requestContext.setProcessingResult(NO_AVAILABLE_ENTITIES_FOR_SEARCHING);
            return;
        }
        processEntitiesWithFields(requestContext, targetQueryBuilder, indexNamesWithFields);
    }

    protected abstract void processEntitiesWithFields(
            RequestContext<SRB> requestContext,
            TargetQueryBuilder<QB, OB> targetQueryBuilder,
            Map<String, Set<String>> indexNamesWithFields);

    protected Map<String, Set<String>> getIndexNamesWithFields(List<String> entities, Function<IndexConfiguration, Set<String>> fieldResolving){
        if(entities.isEmpty()){
            return emptyMap();
        }

        //TODO
        List<String> allowedEntityNames = searchUtils.resolveEntitiesAllowedToSearch(entities);

        if (allowedEntityNames.isEmpty()) {
            return emptyMap();
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
            return emptyMap();
        }
        return result;
    }

    protected OB createQuery(TargetQueryBuilder<QB, OB> targetQueryBuilder, Map<String, Set<String>> indexesWithFields) {
        if (indexesWithFields.size() > 1) {
            return createQueryForMultipleIndexes(targetQueryBuilder, indexesWithFields);
        }
        return createQueryForSingleIndex(targetQueryBuilder, indexesWithFields);
    }

    protected abstract OB createQueryForSingleIndex(TargetQueryBuilder<QB, OB> targetQueryBuilder, Map<String, Set<String>> indexesWithFields);

    protected abstract OB createQueryForMultipleIndexes(TargetQueryBuilder<QB, OB> targetQueryBuilder, Map<String, Set<String>> indexesWithFields);

}
