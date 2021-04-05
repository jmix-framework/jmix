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

import io.jmix.search.searching.SearchResult;

import java.util.*;

public class SearchResultImpl implements SearchResult {
    protected final String searchTerm;
    protected SearchContext searchContext;
    protected final Map<String, Set<SearchResultEntry>> entriesByEntityName = new HashMap<>();
    protected int size = 0;
    protected int effectiveOffset;
    protected boolean moreDataAvailable = false;

    public SearchResultImpl(String searchTerm, SearchContext searchContext) {
        this.searchTerm = searchTerm;
        this.searchContext = searchContext;
        this.effectiveOffset = searchContext.getOffset();
    }

    public void addEntry(SearchResultEntry searchResultEntry) {
        Set<SearchResultEntry> entriesForEntityName = entriesByEntityName.computeIfAbsent(
                searchResultEntry.getEntityName(),
                s -> new LinkedHashSet<>()
        );
        entriesForEntityName.add(searchResultEntry);
        size++;
    }

    public Set<SearchResultEntry> getEntriesByEntityName(String entityName) {
        return entriesByEntityName.get(entityName);
    }

    public boolean isEmpty() {
        return entriesByEntityName.isEmpty();
    }

    public Collection<String> getEntityNames() {
        return entriesByEntityName.keySet();
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public int getSize() {
        return size;
    }

    public int getEffectiveOffset() {
        return effectiveOffset;
    }

    public void incrementOffset() {
        this.effectiveOffset++;
    }

    public SearchContext getSearchContext() {
        return searchContext;
    }

    public boolean isMoreDataAvailable() {
        return moreDataAvailable;
    }

    public void setMoreDataAvailable(boolean moreDataAvailable) {
        this.moreDataAvailable = moreDataAvailable;
    }
}
