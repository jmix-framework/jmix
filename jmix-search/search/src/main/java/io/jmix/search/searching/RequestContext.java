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

import static io.jmix.search.searching.SearchContextProcessingResult.REQUEST_IS_POSSIBLE;

/**
 *
 * @param <T>
 */
public class RequestContext<T> {
    protected final T requestBuilder;
    protected SearchContextProcessingResult searchContextProcessingResult = REQUEST_IS_POSSIBLE;
    protected SearchContext searchContext;
    protected Map<String, Set<String>> effectiveIndexNamesWithFields;

    public RequestContext(T requestBuilder, SearchContext searchContext) {
        this.requestBuilder = requestBuilder;
        this.searchContext = searchContext;
    }

    public T getRequestBuilder() {
        return requestBuilder;
    }

    public void setProcessingResult(SearchContextProcessingResult searchContextProcessingResult) {
        this.searchContextProcessingResult = searchContextProcessingResult;
    }

    public SearchContextProcessingResult getProcessingResult() {
        return searchContextProcessingResult;
    }

    public SearchContext getSearchContext() {
        return searchContext;
    }

    public boolean isRequestPossible(){
        return searchContextProcessingResult == REQUEST_IS_POSSIBLE;
    }

    public void setIndexesWithFields(Map<String, Set<String>> indexNamesWithFields) {
        this.effectiveIndexNamesWithFields = indexNamesWithFields;
    }

    public Map<String, Set<String>> getEffectiveIndexNamesWithFields() {
        return effectiveIndexNamesWithFields;
    }

    public Set<String> getEffectiveIndexNames() {
        return effectiveIndexNamesWithFields.keySet();
    }
}
