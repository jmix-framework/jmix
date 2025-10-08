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

import java.util.List;

/**
 * An interface for implementing common logic for the search request configuration.
 *
 * @param <SRB> a platform specific SearchRequestBuilder
 * @param <QB>  a platform specific QueryBuilder
 * @param <OB>  a platform specific ObjectBuilder
 */
public interface SearchQueryConfigurator<SRB, QB, OB> {

    /**
     * Configures request for the data querying from the search server.
     *
     * @param requestContext     a request context for the request building.
     * @param targetQueryBuilder a builder that builds query for the single index.
     */
    void configureRequest(RequestContext<SRB> requestContext,
                          TargetQueryBuilder<QB, OB> targetQueryBuilder);

    /**
     * Configures request for the data querying from the search server.
     * Provides an ability to add subfields to the query.
     *
     * @param requestContext     a request context for the request building.
     * @param subfieldsProvider - a provider that provides additional subfields by the {@link SubfieldsProvider.FieldInfo}
     * @param targetQueryBuilder a builder that builds query for the single index.
     */
    void configureRequest(RequestContext<SRB> requestContext,
                          SubfieldsProvider subfieldsProvider,
                          TargetQueryBuilder<QB, OB> targetQueryBuilder);

    /**
     * A common interface for query building for the single index.
     *
     * @param <QB> a platform specific QueryBuilder
     * @param <OB> a platform specific ObjectBuilder
     */
    interface TargetQueryBuilder<QB, OB> {
        OB apply(QB queryBuilder, List<String> fields);
    }
}
