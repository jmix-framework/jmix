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
 * A result of the SearchContext processing that is set during the search request building.
 */
public enum SearchContextProcessingResult {

    /**
     * This state is set to {@link RequestContext} before its processing.
     */
    INITIAL_STATE,

    /**
     * This state is set to {@link RequestContext} when a {@link SearchContext} processing is finished
     * and a request sending is possible.
     */
    REQUEST_IS_POSSIBLE,

    /**
     * This state is set to {@link RequestContext} when a {@link SearchContext} processing is finished
     * and a request sending is possible due to there are no any entities and correspondent indexes for searching.
     * Such result is possible because of the user doesn't have necessary permissions for the requested enteties.
     */
    NO_AVAILABLE_ENTITIES_FOR_SEARCHING
}
