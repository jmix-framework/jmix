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

package io.jmix.searchopensearch.searching.strategy.impl;

import io.jmix.search.searching.SearchContext;
import io.jmix.search.searching.SearchStrategy;
import io.jmix.search.searching.SearchUtils;
import io.jmix.search.searching.impl.AbstractSearchStrategy;
import io.jmix.searchopensearch.searching.strategy.OpenSearchSearchStrategy;
import org.opensearch.client.opensearch._types.query_dsl.Operator;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Set;

/**
 * Describes {@link SearchStrategy} that searches documents with fields match all input terms in any order.
 * Different terms can be present in different fields.
 */
@Deprecated(since = "2.4", forRemoval = true)
@Component("search_AllTermsAnyFieldOpenSearchSearchStrategy")
public class AllTermsAnyFieldOpenSearchSearchStrategy extends AbstractSearchStrategy
        implements OpenSearchSearchStrategy {

    protected final SearchUtils searchUtils;

    public AllTermsAnyFieldOpenSearchSearchStrategy(SearchUtils searchUtils) {
        this.searchUtils = searchUtils;
    }

    @Override
    public String getName() {
        return "allTermsAnyField";
    }

    @Override
    public void configureRequest(SearchRequest.Builder requestBuilder, SearchContext searchContext) {
        Set<String> effectiveFieldsToSearch = searchUtils.resolveEffectiveSearchFields(searchContext.getEntities());
        requestBuilder.query(queryBuilder ->
                queryBuilder.simpleQueryString(simpleQueryStringQueryBuilder ->
                        simpleQueryStringQueryBuilder.fields(new ArrayList<>(effectiveFieldsToSearch))
                                .query(searchContext.getEscapedSearchText())
                                .defaultOperator(Operator.And)
                )
        );
    }
}
