/*
 * Copyright 2021 Haulmont.
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

import io.jmix.search.searching.SearchContext;
import io.jmix.search.searching.SearchStrategy;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.SimpleQueryStringBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Component;

/**
 * Describes {@link SearchStrategy} that searches documents with fields match all input terms in any order.
 * Different terms can be present in different fields.
 */
@Component("search_AllTermsAnyFieldSearchStrategy")
public class AllTermsAnyFieldSearchStrategy extends AbstractSearchStrategy {

    @Override
    public String getName() {
        return "allTermsAnyField";
    }

    @Override
    public void configureRequest(SearchRequest searchRequest, SearchContext searchContext) {
        String searchText = searchContext.getSearchText();
        SimpleQueryStringBuilder queryBuilder = QueryBuilders.simpleQueryStringQuery(searchText)
                .field("*")
                .defaultOperator(Operator.AND);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(queryBuilder);
        searchRequest.source(searchSourceBuilder);
    }
}
