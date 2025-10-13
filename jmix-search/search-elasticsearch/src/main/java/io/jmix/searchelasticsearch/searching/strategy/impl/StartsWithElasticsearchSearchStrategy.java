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

import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import io.jmix.search.SearchProperties;
import io.jmix.search.searching.SearchRequestContext;
import io.jmix.search.searching.SearchStrategy;
import io.jmix.searchelasticsearch.searching.strategy.ElasticSearchQueryConfigurer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

import static io.jmix.search.searching.AbstractSearchQueryConfigurer.NO_VIRTUAL_SUBFIELDS;
import static io.jmix.search.searching.AbstractSearchQueryConfigurer.WITH_PREFIX_VIRTUAL_SUBFIELDS;

/**
 * Class that encapsulates logic of {@link SearchStrategy} that searches documents by prefix.
 */
@Component("search_StartsWithElasticsearchSearchStrategy")
public class StartsWithElasticsearchSearchStrategy extends AbstractElasticSearchStrategy{

    protected final SearchProperties searchProperties;

    public StartsWithElasticsearchSearchStrategy(SearchProperties searchProperties,
                                                 ElasticSearchQueryConfigurer elasticSearchQueryConfigurator) {
        super(elasticSearchQueryConfigurator);
        this.searchProperties = searchProperties;
    }

    @Override
    public String getName() {
        return "startsWith";
    }

    @Override
    public void configureRequest(SearchRequestContext<SearchRequest.Builder> requestContext) {
        int maxPrefixSize = searchProperties.getMaxPrefixLength();
        if (isSearchTermExceedMaxPrefixSize(requestContext.getSearchContext().getSearchText(), maxPrefixSize)
                && searchProperties.isWildcardPrefixQueryEnabled()) {
            configureWildcardQuery(requestContext);
        } else {
            configureTermsQuery(requestContext);
        }
    }

    protected void configureTermsQuery(SearchRequestContext<SearchRequest.Builder> requestContext) {
        queryConfigurator.configureRequest(
                requestContext,
                WITH_PREFIX_VIRTUAL_SUBFIELDS,
                (queryBuilder, scope) ->
                        queryBuilder.multiMatch(multiMatchQueryBuilder ->
                                multiMatchQueryBuilder.fields(scope.getFieldList())
                                        .query(requestContext.getSearchContext().getEscapedSearchText())
                                        .type(TextQueryType.BestFields)
                        )
        );
    }

    protected void configureWildcardQuery(SearchRequestContext<SearchRequest.Builder> requestContext) {
        String searchText = requestContext.getSearchContext().getEscapedSearchText();
        String[] searchTerms = searchText.split("\\s+");
        String queryText = Arrays.stream(searchTerms)
                .filter(StringUtils::isNotBlank)
                .map(term -> term + "*")
                .collect(Collectors.joining(" "));
        queryConfigurator.configureRequest(
                requestContext,
                NO_VIRTUAL_SUBFIELDS,
                (queryBuilder, scope) ->
                        queryBuilder.queryString(queryStringQueryBuilder ->
                                queryStringQueryBuilder
                                        .fields(scope.getFieldList())
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
