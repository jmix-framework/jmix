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

import io.jmix.search.index.impl.ExtendedSearchConstants;
import io.jmix.search.searching.impl.SearchModelAnalyzer;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.util.Collections.emptySet;

/**
 * //TODO
 *
 * @param <SRB>
 * @param <QB>
 * @param <OB>
 */
public abstract class AbstractSearchQueryConfigurator<SRB, QB, OB> implements SearchQueryConfigurator<SRB, QB, OB> {

    public static final Function<String, Set<String>> NO_SUBFIELDS = fieldName -> emptySet();
    public static final Function<String, Set<String>> STANDARD_PREFIX_SUBFIELD =
            fieldName -> Set.of(fieldName + "." + ExtendedSearchConstants.PREFIX_SUBFIELD_NAME);

    protected final SearchModelAnalyzer searchModelAnalyzer;

    protected AbstractSearchQueryConfigurator(SearchModelAnalyzer searchModelAnalyzer) {
        this.searchModelAnalyzer = searchModelAnalyzer;
    }

    @Override
    public void configureRequest(
            RequestContext<SRB> requestContext,
            TargetQueryBuilder<QB, OB> targetQueryBuilder) {
        configureRequest(requestContext, NO_SUBFIELDS, targetQueryBuilder);
    }

    @Override
    public void configureRequest(
            RequestContext<SRB> requestContext,
            Function<String, Set<String>> subfieldsGenerator,
            TargetQueryBuilder<QB, OB> targetQueryBuilder) {
        List<String> requestedEntities = requestContext.getSearchContext().getEntities();
        Map<String, Set<String>> indexNamesWithFields =
                searchModelAnalyzer.getIndexesWithFields(requestedEntities, subfieldsGenerator);
        if (indexNamesWithFields.isEmpty()) {
            requestContext.setEmptyResult();
            return;
        }
        processEntitiesWithFields(requestContext, targetQueryBuilder, indexNamesWithFields);
        requestContext.setPositiveResult(indexNamesWithFields);
    }

    protected abstract void processEntitiesWithFields(
            RequestContext<SRB> requestContext,
            TargetQueryBuilder<QB, OB> targetQueryBuilder,
            Map<String, Set<String>> indexNamesWithFields);

    protected OB createQuery(TargetQueryBuilder<QB, OB> targetQueryBuilder, Map<String, Set<String>> indexesWithFields) {
        if (indexesWithFields.size() > 1) {
            return createQueryForMultipleIndexes(targetQueryBuilder, indexesWithFields);
        }
        return createQueryForSingleIndex(targetQueryBuilder, indexesWithFields);
    }

    protected abstract OB createQueryForSingleIndex(
            TargetQueryBuilder<QB, OB> targetQueryBuilder,
            Map<String, Set<String>> indexesWithFields);

    protected abstract OB createQueryForMultipleIndexes(
            TargetQueryBuilder<QB, OB> targetQueryBuilder,
            Map<String, Set<String>> indexesWithFields);

}
