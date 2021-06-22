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

import java.util.Collection;

/**
 * Represents result of search by some text.
 */
public interface SearchResult {

    /**
     * Gets amount of objects in this {@link SearchResult}
     *
     * @return amount of objects
     */
    int getSize();

    /**
     * Gets search offset currently applied to load content of this {@link SearchResult} including post-load security filtration
     * <p>Use this offset to perform next-page search.
     *
     * @return effective offset
     */
    int getEffectiveOffset();

    /**
     * Gets names of all entities presented in this {@link SearchResult}.
     *
     * @return collection of entity names
     */
    Collection<String> getEntityNames();

    /**
     * Gets all {@link SearchResultEntry} specific for provided entity.
     *
     * @param entityName entity name
     * @return collection of {@link SearchResultEntry}
     */
    Collection<SearchResultEntry> getEntriesByEntityName(String entityName);

    /**
     * Gets all {@link SearchResultEntry}
     *
     * @return collection of {@link SearchResultEntry}
     */
    Collection<SearchResultEntry> getAllEntries();

    /**
     * Gets text search has been performed with.
     *
     * @return search text
     */
    String getSearchText();

    /**
     * Gets {@link SearchContext} that was used to gain this {@link SearchResult}.
     *
     * @return {@link SearchContext}
     */
    SearchContext getSearchContext();

    /**
     * Checks if there is any data in result.
     *
     * @return true if this {@link SearchResult} contains any {@link SearchResultEntry}, false otherwise
     */
    boolean isEmpty();

    /**
     * Checks if there are more objects suitable for next-page search.
     *
     * @return true if index contains more suitable data, false otherwise
     */
    boolean isMoreDataAvailable();

    /**
     * Gets {@link SearchStrategy} that was used to gain this {@link SearchResult}.
     *
     * @return {@link SearchStrategy}
     */
    SearchStrategy getSearchStrategy();

    /**
     * Creates new {@link SearchContext} based on current one and describes next-page search
     *
     * @return {@link SearchContext}
     */
    SearchContext createNextPageSearchContext();
}
