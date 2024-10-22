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
import io.jmix.search.searching.SearchContext;
import io.jmix.search.searching.SearchStrategy;

import io.jmix.search.searching.impl.AbstractSearchStrategy;
import io.jmix.searchelasticsearch.searching.strategy.ElasticsearchSearchStrategy;
import org.springframework.stereotype.Component;

/**
 * Describes {@link SearchStrategy} that searches documents with fields match all input terms in any order.
 * Different terms can be present in different fields.
 */
@Deprecated(since = "2.4", forRemoval = true)
@Component("search_AllTermsAnyFieldElasticsearchSearchStrategy")
public class AllTermsAnyFieldElasticsearchSearchStrategy extends AbstractSearchStrategy
        implements ElasticsearchSearchStrategy {

    @Override
    public String getName() {
        return "allTermsAnyField";
    }

    @Override
    public void configureRequest(SearchRequest.Builder requestBuilder, SearchContext searchContext) {
        requestBuilder.query(queryBuilder ->
                queryBuilder.simpleQueryString(simpleQueryStringQueryBuilder ->
                        simpleQueryStringQueryBuilder.fields("*")
                                .query(searchContext.getSearchText())
                                .defaultOperator(Operator.And)
                )
        );
    }
}
