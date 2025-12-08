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

package io.jmix.search.searching;

import io.jmix.search.utils.Constants;

import java.util.List;
import java.util.Set;

import static java.util.Collections.emptySet;

/**
 * Implements common logic for all platform-specific implementations.
 * Contains "out of the box" SubfieldsProvider-s for the typical cases.
 *
 * @param <RB> platform-specific SearchRequestBuilder type
 * @param <QB> platform-specific QueryBuilder type
 * @param <OB> platform-specific ObjectBuilder type
 */
public abstract class AbstractSearchQueryConfigurer<RB, QB, OB> implements SearchQueryConfigurer<RB, QB, OB> {

    public static final VirtualSubfieldsProvider NO_VIRTUAL_SUBFIELDS = fieldInfo -> emptySet();
    public static final VirtualSubfieldsProvider WITH_PREFIX_VIRTUAL_SUBFIELDS =
            fieldInfo -> Set.of(fieldInfo.fieldName() + "." + Constants.PREFIX_SUBFIELD_NAME);

    protected final SearchRequestScopeProvider searchRequestScopeProvider;

    protected AbstractSearchQueryConfigurer(SearchRequestScopeProvider searchRequestScopeProvider) {
        this.searchRequestScopeProvider = searchRequestScopeProvider;
    }

    @Override
    public void configureRequest(SearchRequestContext<RB> requestContext,
                                 BusinessQueryConfigurer<QB, OB> businessQueryConfigurer) {
        configureRequest(requestContext, NO_VIRTUAL_SUBFIELDS, businessQueryConfigurer);
    }

    @Override
    public void configureRequest(SearchRequestContext<RB> requestContext,
                                 VirtualSubfieldsProvider virtualSubfieldsProvider,
                                 BusinessQueryConfigurer<QB, OB> businessQueryConfigurer) {
        List<String> requestedEntities = requestContext.getSearchContext().getEntities();
        List<IndexSearchRequestScope> indexSearchRequestScopes =
                searchRequestScopeProvider.getSearchRequestScope(requestedEntities, virtualSubfieldsProvider);
        if (indexSearchRequestScopes.isEmpty()) {
            requestContext.setEmptyResult();
            return;
        }
        setQueryToRequestBuilder(requestContext, businessQueryConfigurer, indexSearchRequestScopes);
        requestContext.setPositiveResult(indexSearchRequestScopes);
    }

    /**
     * Configures the search query in the provided request builder by applying business-specific configurations and
     * setting up the query parameters within the specified search request scopes.
     *
     * @param requestContext context of the search request, providing the request builder and related information
     * @param businessQueryConfigurer business-specific query configuration logic to apply to the query builder
     * @param indexSearchRequestScopes list of search request scopes that define the boundaries of the search query
     */
    protected abstract void setQueryToRequestBuilder(SearchRequestContext<RB> requestContext,
                                                     BusinessQueryConfigurer<QB, OB> businessQueryConfigurer,
                                                     List<IndexSearchRequestScope> indexSearchRequestScopes);

    /**
     * Creates a query object based on the provided query configuration and the given index search request scopes.
     *
     * @param businessQueryConfigurer business-specific query configuration logic to apply to the query builder
     * @param indexSearchRequestScopes list of index search request scopes defining the boundaries of the query
     * @return configured query object for single or multiple index search request scopes
     */
    protected OB createQuery(BusinessQueryConfigurer<QB, OB> businessQueryConfigurer,
                             List<IndexSearchRequestScope> indexSearchRequestScopes) {
        if (indexSearchRequestScopes.size() > 1) {
            return createQueryForMultipleIndexes(businessQueryConfigurer, indexSearchRequestScopes);
        }
        return createQueryForSingleIndex(businessQueryConfigurer, indexSearchRequestScopes.iterator().next());
    }

    /**
     * Creates a query object for a single index based on the provided query configuration
     * and the given index search request scope.
     *
     * @param businessQueryConfigurer the business-specific query configuration logic to apply to the query builder
     * @param indexSearchRequestScope the index search request scope defining the boundaries of the query
     * @return configured query object for the specified single index search request scope
     */
    protected abstract OB createQueryForSingleIndex(BusinessQueryConfigurer<QB, OB> businessQueryConfigurer,
                                                    IndexSearchRequestScope indexSearchRequestScope);

    /**
     * Creates a query object for multiple indexes based on the provided query configuration
     * and the list of index search request scopes.
     *
     * @param businessQueryConfigurer the business-specific query configuration logic to apply to the query builder
     * @param indexSearchRequestScopes the list of index search request scopes defining the boundaries of the query
     * @return configured query object for the specified multiple index search request scopes
     */
    protected abstract OB createQueryForMultipleIndexes(BusinessQueryConfigurer<QB, OB> businessQueryConfigurer,
                                                        List<IndexSearchRequestScope> indexSearchRequestScopes);
}
