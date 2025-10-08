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

import java.util.Map;
import java.util.Set;

import static io.jmix.search.searching.SearchContextProcessingResult.*;

/**
 * A container of the request building information.
 * @param <T> a platform specific request builder type
 */
public class RequestContext<T> {

    protected final T requestBuilder;
    protected SearchContextProcessingResult requestPreparingState = INITIAL_STATE;
    protected SearchContext searchContext;
    protected Map<String, Set<String>> effectiveIndexNamesWithFields;

    public RequestContext(T requestBuilder, SearchContext searchContext) {
        this.requestBuilder = requestBuilder;
        this.searchContext = searchContext;
    }

    public T getRequestBuilder() {
        if (requestPreparingState == REQUEST_IS_POSSIBLE || requestPreparingState == INITIAL_STATE) {
            return requestBuilder;
        }
        throw noEntitiesForSearchingException();
    }

    public void setEmptyResult() {
        this.requestPreparingState = NO_AVAILABLE_ENTITIES_FOR_SEARCHING;
    }

    public SearchContextProcessingResult getProcessingResult() {
        return requestPreparingState;
    }

    public SearchContext getSearchContext() {
        return searchContext;
    }

    public boolean isRequestPossible() {
        return requestPreparingState == REQUEST_IS_POSSIBLE;
    }

    public void setPositiveResult(Map<String, Set<String>> indexNamesWithFields) {
        this.effectiveIndexNamesWithFields = indexNamesWithFields;
        requestPreparingState = REQUEST_IS_POSSIBLE;
    }

    public Map<String, Set<String>> getEffectiveIndexesWithFields() {
        if (requestPreparingState == INITIAL_STATE) {
            throw requestPreparingIsNotFinishedException();
        }
        if (requestPreparingState == NO_AVAILABLE_ENTITIES_FOR_SEARCHING) {
            throw noEntitiesForSearchingException();
        }
        return effectiveIndexNamesWithFields;
    }

    public Set<String> getEffectiveIndexes() {
        if (requestPreparingState == INITIAL_STATE) {
            throw requestPreparingIsNotFinishedException();
        }
        if (requestPreparingState == NO_AVAILABLE_ENTITIES_FOR_SEARCHING) {
            throw noEntitiesForSearchingException();
        }
        return effectiveIndexNamesWithFields.keySet();
    }

    private static IllegalStateException requestPreparingIsNotFinishedException() {
        return new IllegalStateException("Request preparing is not finished.");
    }

    private static IllegalStateException noEntitiesForSearchingException() {
        return new IllegalStateException("No entities for searching.");
    }

}
