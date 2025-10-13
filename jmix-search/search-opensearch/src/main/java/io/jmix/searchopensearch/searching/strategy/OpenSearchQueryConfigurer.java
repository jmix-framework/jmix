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

package io.jmix.searchopensearch.searching.strategy;

import io.jmix.search.searching.AbstractSearchQueryConfigurer;
import io.jmix.search.searching.SearchRequestContext;
import io.jmix.search.searching.IndexSearchRequestScope;
import io.jmix.search.searching.SearchRequestScopeProvider;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.util.ObjectBuilder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * A  OpenSearch-specific implementation of the {@link AbstractSearchQueryConfigurer}
 */
@Component("search_OpenSearchQueryConfigurer")
public class OpenSearchQueryConfigurer extends AbstractSearchQueryConfigurer<SearchRequest.Builder, Query.Builder, ObjectBuilder<Query>> {

    protected OpenSearchQueryConfigurer(SearchRequestScopeProvider searchRequestScopeProvider) {
        super(searchRequestScopeProvider);
    }

    @Override
    protected void querySettingToRequestBuilder(SearchRequestContext<SearchRequest.Builder> requestContext,
                                                BusinessQueryConfigurer<Query.Builder, ObjectBuilder<Query>> businessQueryConfigurer,
                                                List<IndexSearchRequestScope> indexSearchRequestScopes) {
        requestContext.getRequestBuilder().query(createQuery(businessQueryConfigurer, indexSearchRequestScopes).build());
    }

    @Override
    protected ObjectBuilder<Query> createQueryForSingleIndex(
            BusinessQueryConfigurer<Query.Builder, ObjectBuilder<Query>> businessQueryConfigurer,
            IndexSearchRequestScope indexSearchRequestScope) {
        Query.Builder builder = new Query.Builder();
        return businessQueryConfigurer
                .apply(builder, indexSearchRequestScope);
    }

    @Override
    protected ObjectBuilder<Query> createQueryForMultipleIndexes(
            BusinessQueryConfigurer<Query.Builder, ObjectBuilder<Query>> businessQueryConfigurer,
            List<IndexSearchRequestScope> indexSearchRequestScopes) {
        return new Query.Builder()
                .bool(boolBuilder ->
                        boolBuilder.should(createSubqueriesForIndexes(businessQueryConfigurer, indexSearchRequestScopes)));
    }

    protected List<Query> createSubqueriesForIndexes(
            BusinessQueryConfigurer<Query.Builder, ObjectBuilder<Query>> businessQueryConfigurer,
            List<IndexSearchRequestScope> indexSearchRequestScopes) {
        return indexSearchRequestScopes
                .stream()
                .map(scope -> createQueryForSingleIndexInternal(businessQueryConfigurer, scope))
                .toList();
    }

    protected Query createQueryForSingleIndexInternal(
            BusinessQueryConfigurer<Query.Builder, ObjectBuilder<Query>> businessQueryConfigurer,
            IndexSearchRequestScope indexSearchRequestScope) {
        return Query.of(root ->
                root.bool(b -> b
                        .must(m -> m.term(t -> t.field("_index")
                                .value(v -> v.stringValue(indexSearchRequestScope.indexConfiguration().getIndexName()))))
                        .must(m2 -> businessQueryConfigurer.apply(m2, indexSearchRequestScope))
                ));
    }
}
