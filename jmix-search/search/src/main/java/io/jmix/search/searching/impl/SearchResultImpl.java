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

import io.jmix.search.searching.SearchContext;
import io.jmix.search.searching.SearchResult;
import io.jmix.search.searching.SearchResultEntry;
import io.jmix.search.searching.SearchStrategy;

import java.util.*;

public class SearchResultImpl implements SearchResult {
    protected SearchContext searchContext;
    protected final Map<String, Collection<SearchResultEntry>> entriesByEntityName = new HashMap<>();
    protected final Collection<SearchResultEntry> allEntries = new ArrayList<>();
    protected int size = 0;
    protected int effectiveOffset;
    protected boolean moreDataAvailable = false;
    protected SearchStrategy searchStrategy;

    public SearchResultImpl(SearchContext searchContext, SearchStrategy searchStrategy) {
        this.searchContext = searchContext;
        this.effectiveOffset = searchContext.getOffset();
        this.searchStrategy = searchStrategy;
    }

    public void addEntry(SearchResultEntry searchResultEntry) {
        Collection<SearchResultEntry> entriesForEntityName = entriesByEntityName.computeIfAbsent(
                searchResultEntry.getEntityName(),
                s -> new ArrayList<>()
        );
        entriesForEntityName.add(searchResultEntry);
        allEntries.add(searchResultEntry);
        size++;
    }

    @Override
    public Collection<SearchResultEntry> getEntriesByEntityName(String entityName) {
        return Collections.unmodifiableCollection(entriesByEntityName.get(entityName));
    }

    @Override
    public Collection<SearchResultEntry> getAllEntries() {
        return Collections.unmodifiableCollection(allEntries);
    }

    @Override
    public boolean isEmpty() {
        return entriesByEntityName.isEmpty();
    }

    @Override
    public Collection<String> getEntityNames() {
        return Collections.unmodifiableCollection(entriesByEntityName.keySet());
    }

    @Override
    public String getSearchText() {
        return searchContext.getSearchText();
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public int getEffectiveOffset() {
        return effectiveOffset;
    }

    public void incrementOffset() {
        this.effectiveOffset++;
    }

    @Override
    public SearchContext getSearchContext() {
        return searchContext;
    }

    @Override
    public boolean isMoreDataAvailable() {
        return moreDataAvailable;
    }

    @Override
    public SearchStrategy getSearchStrategy() {
        return searchStrategy;
    }

    @Override
    public SearchContext createNextPageSearchContext() {
        return new SearchContext(this.searchContext.getSearchText())
                .setSize(this.searchContext.getSize())
                .setEntities(this.searchContext.getEntities())
                .setOffset(getEffectiveOffset());
    }

    public void setMoreDataAvailable(boolean moreDataAvailable) {
        this.moreDataAvailable = moreDataAvailable;
    }
}
