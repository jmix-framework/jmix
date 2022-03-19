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

import org.elasticsearch.action.search.SearchRequest;

/**
 * Describes the way search context should be processed.
 */
public interface SearchStrategy {

    /**
     * Provides the name of this search strategy.
     * Name should be unique among all search strategies in application.
     *
     * @return name
     */
    String getName();

    /**
     * Configures Elasticsearch {@link SearchRequest}.
     * <p>The main step - create appropriate query based on provided {@link SearchContext} and set it to request.
     * <p>Configuration of another request parameters is optional.
     * Custom highlighting can be configured here. If it wasn't - the default one will be used.
     * <p>Size and offset shouldn't be configured here - these parameters will be overwritten.
     *
     * @param searchRequest Elasticsearch {@link SearchRequest}
     * @param searchContext {@link SearchContext}
     */
    void configureRequest(SearchRequest searchRequest, SearchContext searchContext);
}
