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
import io.jmix.core.Metadata;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.searching.SearchContext;
import io.jmix.search.searching.SearchStrategy;
import io.jmix.search.searching.SearchUtils;
import io.jmix.search.searching.impl.AbstractSearchStrategy;
import io.jmix.search.searching.impl.SearchFieldsResolver;
import io.jmix.searchelasticsearch.searching.strategy.ElasticsearchSearchStrategy;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class that encapsulates logic of {@link SearchStrategy} that searches documents by prefix.
 */
@Component("search_StartsWithElasticsearchSearchStrategy")
public class StartsWithElasticsearchSearchStrategy extends AbstractSearchStrategy implements ElasticsearchSearchStrategy {

    protected final IndexConfigurationManager indexConfigurationManager;
    protected final SearchProperties searchProperties;
    protected final SecureOperations secureOperations;
    protected final PolicyStore policyStore;
    protected final Metadata metadata;
    protected final SearchUtils searchUtils;
    protected final ElasticSearchQueryConfigurator elasticSearchQueryConfigurator;
    protected final SearchFieldsResolver searchFieldsResolver;

    public StartsWithElasticsearchSearchStrategy(IndexConfigurationManager indexConfigurationManager,
                                                 SearchProperties searchProperties,
                                                 SecureOperations secureOperations,
                                                 PolicyStore policyStore,
                                                 Metadata metadata,
                                                 SearchUtils searchUtils, ElasticSearchQueryConfigurator elasticSearchQueryConfigurator, SearchFieldsResolver searchFieldsResolver) {
        this.indexConfigurationManager = indexConfigurationManager;
        this.searchProperties = searchProperties;
        this.secureOperations = secureOperations;
        this.policyStore = policyStore;
        this.metadata = metadata;
        this.searchUtils = searchUtils;
        this.elasticSearchQueryConfigurator = elasticSearchQueryConfigurator;
        this.searchFieldsResolver = searchFieldsResolver;
    }

    @Override
    public String getName() {
        return "startsWith";
    }

    @Override
    public void configureRequest(SearchRequest.Builder requestBuilder, SearchContext searchContext) {
        int maxPrefixSize = searchProperties.getMaxPrefixLength();
        List<String> entities = searchContext.getEntities();
        if (isSearchTermExceedMaxPrefixSize(searchContext.getSearchText(), maxPrefixSize)
                && searchProperties.isWildcardPrefixQueryEnabled()) {
            configureWildcardQuery(requestBuilder, searchContext, entities);
        } else {
            configureTermsQuery(requestBuilder, searchContext, entities);
        }
    }

    protected void configureTermsQuery(SearchRequest.Builder requestBuilder, SearchContext searchContext, List<String> entities) {
        elasticSearchQueryConfigurator.configureRequest(
                requestBuilder,
                entities,
                searchFieldsResolver::resolveFieldsWithPrefixes,
                (queryBuilder, fields) ->
                        queryBuilder.multiMatch(multiMatchQueryBuilder ->
                                multiMatchQueryBuilder.fields(new ArrayList<>(fields))
                                        .query(searchContext.getEscapedSearchText())
                                        .type(TextQueryType.BestFields)
                        )
        );
    }

    protected void configureWildcardQuery(SearchRequest.Builder requestBuilder, SearchContext searchContext, List<String> entities) {
        String searchText = searchContext.getEscapedSearchText();
        String[] searchTerms = searchText.split("\\s+");
        String queryText = Arrays.stream(searchTerms)
                .filter(StringUtils::isNotBlank)
                .map(term -> term + "*")
                .collect(Collectors.joining(" "));
        elasticSearchQueryConfigurator.configureRequest(
                requestBuilder,
                entities,
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
