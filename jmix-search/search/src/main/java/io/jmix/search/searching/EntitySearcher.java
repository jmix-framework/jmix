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

import io.jmix.core.FetchPlan;

import java.util.Collection;
import java.util.Map;

/**
 * Provides functionality for searching entities in index.
 */
public interface EntitySearcher {

    /**
     * Performs search in search indexes according to provided {@link SearchContext} and default {@link SearchStrategy}.
     * <p>See {@link EntitySearcher#search(SearchContext, SearchStrategy)}
     *
     * @param searchContext runtime settings of specific search
     * @return {@link SearchResult} with found objects
     */
    SearchResult search(SearchContext searchContext);

    /**
     * Performs search in search indexes according to provided {@link SearchContext} and {@link SearchStrategy}.
     *
     * @param searchContext  runtime settings of specific search
     * @param searchStrategy the way incoming search text should be processed
     * @return {@link SearchResult} with found objects
     */
    SearchResult search(SearchContext searchContext, SearchStrategy searchStrategy);

    /**
     * Performs search of next page according to {@link SearchContext} and {@link SearchResult} contained
     * in provided {@link SearchStrategy} related to previous page.
     *
     * @param previousSearchResult {@link SearchResult} of previous page
     * @return {@link SearchResult} with found objects
     */
    SearchResult searchNextPage(SearchResult previousSearchResult);
}
