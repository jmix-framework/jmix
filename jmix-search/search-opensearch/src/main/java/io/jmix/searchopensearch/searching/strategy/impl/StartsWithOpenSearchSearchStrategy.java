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

import io.jmix.search.SearchProperties;
import io.jmix.search.searching.SearchContext;
import io.jmix.search.searching.SearchStrategy;
import io.jmix.search.searching.impl.SearchFieldsResolver;
import io.jmix.searchopensearch.searching.strategy.OpenSearchSearchStrategy;
import org.apache.commons.lang3.StringUtils;
import org.opensearch.client.opensearch._types.query_dsl.TextQueryType;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Class that encapsulates logic of {@link SearchStrategy} that searches documents by prefix.
 */
@Component("search_StartsWithOpenSearchSearchStrategy")
public class StartsWithOpenSearchSearchStrategy extends AbstractOpenSearchStrategy implements OpenSearchSearchStrategy {

    protected final SearchProperties searchProperties;

    protected StartsWithOpenSearchSearchStrategy(SearchFieldsResolver searchFieldsResolver, OpenSearchQueryConfigurator queryConfigurator, SearchProperties searchProperties) {
        super(searchFieldsResolver, queryConfigurator);
        this.searchProperties = searchProperties;
    }

    @Override
    public String getName() {
        return "startsWith";
    }

    @Override
    public void configureRequest(SearchRequest.Builder requestBuilder, SearchContext searchContext) {
        int maxPrefixSize = searchProperties.getMaxPrefixLength();
        if (isSearchTermExceedMaxPrefixSize(searchContext.getSearchText(), maxPrefixSize)
                && searchProperties.isWildcardPrefixQueryEnabled()) {
            configureWildcardQuery(requestBuilder, searchContext);
        } else {
            configureTermsQuery(requestBuilder, searchContext);
        }
    }

    protected void configureTermsQuery(SearchRequest.Builder requestBuilder, SearchContext searchContext) {

        queryConfigurator.configureRequest(
                requestBuilder,
                searchContext.getEntities(),
                searchFieldsResolver::resolveFieldsWithPrefixes,
                (queryBuilder, fields) ->
                        queryBuilder.multiMatch(multiMatchQueryBuilder ->
                                multiMatchQueryBuilder.fields(fields)
                                        .query(searchContext.getEscapedSearchText())
                                        .type(TextQueryType.BestFields)
                        )
        );
    }

    protected void configureWildcardQuery(SearchRequest.Builder requestBuilder, SearchContext searchContext) {
        String searchText = searchContext.getEscapedSearchText();
        String[] searchTerms = searchText.split("\\s+");
        String queryText = Arrays.stream(searchTerms)
                .filter(StringUtils::isNotBlank)
                .map(term -> term + "*")
                .collect(Collectors.joining(" "));

        queryConfigurator.configureRequest(
                requestBuilder,
                searchContext.getEntities(),
                searchFieldsResolver::resolveFields,
                (queryBuilder, fields) ->
                        queryBuilder.queryString(queryStringQueryBuilder ->
                                queryStringQueryBuilder
                                        .fields(fields)
                                        .analyzeWildcard(true)
                                        .query(queryText)
                        )
        );
    }

    protected boolean isSearchTermExceedMaxPrefixSize(String searchText, int maxPrefixSize) {
        String[] searchTerms = searchText.split("\\s+");
        return Arrays.stream(searchTerms).anyMatch(term -> term.length() > maxPrefixSize);
    }
}
