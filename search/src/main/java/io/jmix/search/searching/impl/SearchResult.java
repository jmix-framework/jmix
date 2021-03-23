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

package io.jmix.search.searching.impl;

import java.util.*;

/**
 * Represents result of search by some term.
 */
public class SearchResult {
    protected final String searchTerm;
    protected final Map<String, Set<SearchResultEntry>> entriesByEntityName = new HashMap<>();

    public SearchResult(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    /**
     * Appends provided {@link SearchResultEntry} to this {@link SearchResult}.
     *
     * @param searchResultEntry search result entry
     */
    public void addEntry(SearchResultEntry searchResultEntry) {
        Set<SearchResultEntry> entriesForEntityName = entriesByEntityName.computeIfAbsent(
                searchResultEntry.getEntityName(),
                s -> new LinkedHashSet<>()
        );
        entriesForEntityName.add(searchResultEntry);
    }

    /**
     * Gets all {@link SearchResultEntry} specific for provided entity.
     *
     * @param entityName entity name
     * @return set of {@link SearchResultEntry}
     */
    public Set<SearchResultEntry> getEntriesByEntityName(String entityName) {
        return entriesByEntityName.get(entityName);
    }

    /**
     * Checks if there is any data in result.
     *
     * @return true if this {@link SearchResult} contains any {@link SearchResultEntry}, false otherwise
     */
    public boolean isEmpty() {
        return entriesByEntityName.isEmpty();
    }

    /**
     * Gets names of all entities presented in this {@link SearchResult}.
     *
     * @return collection of entity names
     */
    public Collection<String> getEntityNames() {
        return entriesByEntityName.keySet();
    }

    /**
     * Gets term search has been performed with.
     *
     * @return search term
     */
    public String getSearchTerm() {
        return searchTerm;
    }
}
