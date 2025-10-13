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
 * @param <RB> a platform-specific SearchRequestBuilder type
 * @param <QB> a platform-specific QueryBuilder type
 * @param <OB> a platform-specific ObjectBuilder type
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
        querySettingToRequestBuilder(requestContext, businessQueryConfigurer, indexSearchRequestScopes);
        requestContext.setPositiveResult(indexSearchRequestScopes);
    }

    /**
     * Configures the search request by applying query settings using the provided request context,
     * business query configurer, and index search request scopes.
     *
     * @param requestContext the container holding the request building information and platform-specific request builder
     * @param businessQueryConfigurer the configurer used for building queries for given index search scopes
     * @param indexSearchRequestScopes a list of search request scopes representing the targeted indexes and their configurations
     */
    protected abstract void querySettingToRequestBuilder(SearchRequestContext<RB> requestContext,
                                                         BusinessQueryConfigurer<QB, OB> businessQueryConfigurer,
                                                         List<IndexSearchRequestScope> indexSearchRequestScopes);

    protected OB createQuery(BusinessQueryConfigurer<QB, OB> businessQueryConfigurer,
                             List<IndexSearchRequestScope> indexSearchRequestScopes) {
        if (indexSearchRequestScopes.size() > 1) {
            return createQueryForMultipleIndexes(businessQueryConfigurer, indexSearchRequestScopes);
        }
        return createQueryForSingleIndex(businessQueryConfigurer, indexSearchRequestScopes.iterator().next());
    }

    protected abstract OB createQueryForSingleIndex(BusinessQueryConfigurer<QB, OB> businessQueryConfigurer,
                                                    IndexSearchRequestScope indexSearchRequestScope);

    protected abstract OB createQueryForMultipleIndexes(BusinessQueryConfigurer<QB, OB> businessQueryConfigurer,
                                                        List<IndexSearchRequestScope> indexSearchRequestScopes);
}
