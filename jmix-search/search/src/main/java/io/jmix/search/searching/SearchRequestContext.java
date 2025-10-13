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
import java.util.Set;
import java.util.stream.Collectors;

import static io.jmix.search.searching.SearchRequestContext.ProcessingState.*;

/**
 * A container of the search request building information.
 * @param <T> a platform-specific request builder type
 */
public class SearchRequestContext<T> {

    protected final T requestBuilder;
    protected ProcessingState requestPreparingState = UNPROCESSED;
    protected SearchContext searchContext;
    protected List<IndexSearchRequestScope> indexSearchRequestScopes;

    public SearchRequestContext(T requestBuilder, SearchContext searchContext) {
        this.requestBuilder = requestBuilder;
        this.searchContext = searchContext;
    }

    public T getRequestBuilder() {
        if (requestPreparingState == READY || requestPreparingState == UNPROCESSED) {
            return requestBuilder;
        }
        throw noEntitiesForSearchingException();
    }

    public void setEmptyResult() {
        this.requestPreparingState = NO_AVAILABLE_ENTITIES;
    }

    public ProcessingState getProcessingResult() {
        return requestPreparingState;
    }

    public SearchContext getSearchContext() {
        return searchContext;
    }

    public boolean isRequestPossible() {
        return requestPreparingState == READY;
    }

    public void setPositiveResult(List<IndexSearchRequestScope> indexSearchRequestScopes) {
        this.indexSearchRequestScopes = indexSearchRequestScopes;
        requestPreparingState = READY;
    }

    public List<IndexSearchRequestScope> getIndexSearchRequestScopes() {
        if (requestPreparingState == UNPROCESSED) {
            throw requestPreparingIsNotFinishedException();
        }
        if (requestPreparingState == NO_AVAILABLE_ENTITIES) {
            throw noEntitiesForSearchingException();
        }
        return indexSearchRequestScopes;
    }

    public Set<String> getEffectiveIndexes() {
        if (requestPreparingState == UNPROCESSED) {
            throw requestPreparingIsNotFinishedException();
        }
        if (requestPreparingState == NO_AVAILABLE_ENTITIES) {
            throw noEntitiesForSearchingException();
        }
        return indexSearchRequestScopes
                .stream()
                .map(indexSearchData -> indexSearchData.indexConfiguration().getIndexName())
                .collect(Collectors.toSet());
    }

    private static IllegalStateException requestPreparingIsNotFinishedException() {
        return new IllegalStateException("Request preparing is not finished.");
    }

    private static IllegalStateException noEntitiesForSearchingException() {
        return new IllegalStateException("No entities for searching.");
    }

    /**
     * A result of the SearchContext processing that is set during the search request building.
     */
    public enum ProcessingState {

        /**
         * This state is set to {@link SearchRequestContext} before its processing.
         */
        UNPROCESSED,

        /**
         * This state is set to {@link SearchRequestContext} when a {@link SearchContext} processing is finished
         * and a request sending is possible.
         */
        READY,

        /**
         * This state is set to {@link SearchRequestContext} when a {@link SearchContext} processing is finished
         * and a request sending is not possible due to there are no any entities and correspondent indexes for searching.
         * Such result is possible because of the user doesn't have necessary permissions for the requested entities.
         */
        NO_AVAILABLE_ENTITIES
    }
}
