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
import io.jmix.search.searching.RequestContext;
import io.jmix.search.searching.SearchStrategy;
import io.jmix.search.searching.impl.SearchFieldsResolver;
import io.jmix.searchopensearch.searching.strategy.OpenSearchSearchStrategy;
import org.apache.commons.lang3.StringUtils;
import org.opensearch.client.opensearch._types.query_dsl.TextQueryType;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

import static io.jmix.search.searching.impl.SearchFieldsResolver.GETTING_FIELD_WITH_SUBFIELD_WITH_PREFIXES;

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
                conf -> searchFieldsResolver.resolveFieldsWithSubfields(conf, GETTING_FIELD_WITH_SUBFIELD_WITH_PREFIXES),
                (queryBuilder, fields) ->
                        queryBuilder.multiMatch(multiMatchQueryBuilder ->
                                multiMatchQueryBuilder.fields(fields)
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
