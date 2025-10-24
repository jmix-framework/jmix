/*
 * Copyright 2021 Haulmont.
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
 * Describes the way search context should be processed.
 * @param <T> platform-specific request builder type
 */
public interface SearchStrategy<T> {

    /**
     * Provides the name of this search strategy.
     * Name should be unique among all search strategies in application.
     *
     * @return name
     */
    String getName();

    /**
     * Configures the provided search request context based on the current search strategy.
     *
     * @param requestContext the {@link SearchRequestContext} to configure, which provides the necessary
     *                        metadata and state of the search preparation process. The type parameter
     *                        {@code T} represents the platform-specific request builder associated with
     *                        the context.
     */
    default void configureRequest(SearchRequestContext<T> requestContext) {
        throw new UnsupportedOperationException();
    }

}
