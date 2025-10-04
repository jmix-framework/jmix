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
import io.jmix.search.searching.RequestContext;
import io.jmix.search.searching.SearchStrategy;
import io.jmix.searchelasticsearch.searching.strategy.ElasticsearchSearchStrategy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import static io.jmix.search.searching.AbstractSearchQueryConfigurator.NO_SUBFIELDS;
import static io.jmix.search.searching.AbstractSearchQueryConfigurator.STANDARD_PREFIX_SUBFIELD;

/**
 * Class that encapsulates logic of {@link SearchStrategy} that searches documents by prefix.
 */
@Component("search_StartsWithElasticsearchSearchStrategy")
public class StartsWithElasticsearchSearchStrategy extends AbstractElasticSearchStrategy implements ElasticsearchSearchStrategy {

    protected final SearchProperties searchProperties;

    public StartsWithElasticsearchSearchStrategy(SearchProperties searchProperties,
                                                 ElasticSearchQueryConfigurator elasticSearchQueryConfigurator) {
        super(elasticSearchQueryConfigurator);
        this.searchProperties = searchProperties;
    }

    @Override
    public String getName() {
        return "startsWith";
    }

    @Override
    public void configureRequest(RequestContext<SearchRequest.Builder> requestContext) {
        int maxPrefixSize = searchProperties.getMaxPrefixLength();
        if (isSearchTermExceedMaxPrefixSize(requestContext.getSearchContext().getSearchText(), maxPrefixSize)
                && searchProperties.isWildcardPrefixQueryEnabled()) {
            configureWildcardQuery(requestContext);
        } else {
            configureTermsQuery(requestContext);
        }
    }

    protected void configureTermsQuery(RequestContext<SearchRequest.Builder> requestContext) {
        queryConfigurator.configureRequest(
                requestContext,
                STANDARD_PREFIX_SUBFIELD,
                (queryBuilder, fields) ->
                        queryBuilder.multiMatch(multiMatchQueryBuilder ->
                                multiMatchQueryBuilder.fields(new ArrayList<>(fields))
                                        .query(requestContext.getSearchContext().getEscapedSearchText())
                                        .type(TextQueryType.BestFields)
                        )
        );
    }

    protected void configureWildcardQuery(RequestContext<SearchRequest.Builder> requestContext) {
        String searchText = requestContext.getSearchContext().getEscapedSearchText();
        String[] searchTerms = searchText.split("\\s+");
        String queryText = Arrays.stream(searchTerms)
                .filter(StringUtils::isNotBlank)
                .map(term -> term + "*")
                .collect(Collectors.joining(" "));
        queryConfigurator.configureRequest(
                requestContext,
                NO_SUBFIELDS,
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
