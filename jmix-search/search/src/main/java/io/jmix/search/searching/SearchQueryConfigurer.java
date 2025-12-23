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

/**
 * Provides a mechanism for configuring search queries against a search server.
 * The {@code SearchQueryConfigurer} interface allows for building and customizing search request
 * queries using the provided request context, query configuration, and optional subfields.
 * The query is constructed for one or more indexes based on the configured scope,
 * with optional parameters enabling enhanced flexibility in request customization.
 *
 * @param <RB> type of platform-specific request builder
 * @param <QB> type of platform-specific query builder
 * @param <OB> type of platform-specific object builder
 */
public interface SearchQueryConfigurer<RB, QB, OB> {

    /**
     * Configures a search request using the provided request context and query configuration logic.
     *
     * @param requestContext the context containing request building information,
     *                       including the request builder and processing state management
     * @param businessQueryConfigurer an interface for building and applying business-specific
     *                                 query configurations to a single index
     */
    void configureRequest(SearchRequestContext<RB> requestContext,
                          BusinessQueryConfigurer<QB, OB> businessQueryConfigurer);

    /**
     * Configures request for the data querying from the search server.
     * Provides an ability to add subfields to the query.
     *
     * @param requestContext request context for the request building.
     * @param virtualSubfieldsProvider provider that provides additional subfields by the {@link VirtualSubfieldsProvider.FieldInfo}
     * @param businessQueryConfigurer an interface for building and applying business-specific
     *                                 query configurations to a single index
     */
    void configureRequest(SearchRequestContext<RB> requestContext,
                          VirtualSubfieldsProvider virtualSubfieldsProvider,
                          BusinessQueryConfigurer<QB, OB> businessQueryConfigurer);

    /**
     * An interface for applying business-specific configurations to a query. This interface allows
     * customization of query builders and facilitates the integration of context-specific requirements
     * into the search query.
     *
     * @param <QB> type of query builder that is passed for configuration
     * @param <OB> type of object that is returned after the query has been configured
     */
    interface BusinessQueryConfigurer<QB, OB> {
        OB apply(QB queryBuilder, IndexSearchRequestScope indexSearchRequestScope);
    }
}
