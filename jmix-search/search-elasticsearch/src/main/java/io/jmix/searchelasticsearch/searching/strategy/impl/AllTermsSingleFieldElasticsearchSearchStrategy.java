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

package io.jmix.searchelasticsearch.searching.strategy.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import io.jmix.search.searching.SearchRequestContext;
import io.jmix.search.searching.SearchStrategy;
import org.springframework.stereotype.Component;

/**
 * Describes {@link SearchStrategy} that searches documents with at least one field matches all input terms in any order.
 * Fields with partial match a not suitable.
 */
@Deprecated(since = "2.4", forRemoval = true)
@Component("search_AllTermsSingleFieldElasticsearchSearchStrategy")
public class AllTermsSingleFieldElasticsearchSearchStrategy extends AbstractElasticSearchStrategy{

    public AllTermsSingleFieldElasticsearchSearchStrategy(ElasticSearchQueryConfigurer queryConfigurator) {
        super(queryConfigurator);
    }

    @Override
    public String getName() {
        return "allTermsSingleField";
    }

    @Override
    public void configureRequest(SearchRequestContext<SearchRequest.Builder> requestContext) {
        queryConfigurator.configureRequest(
                requestContext,
                (queryBuilder, scope) ->
                        queryBuilder.multiMatch(multiMatchQueryBuilder ->
                                multiMatchQueryBuilder.fields(scope.getFieldList())
                                        .query(requestContext.getSearchContext().getEscapedSearchText())
                                        .operator(Operator.And)
                        )
        );
    }
}
