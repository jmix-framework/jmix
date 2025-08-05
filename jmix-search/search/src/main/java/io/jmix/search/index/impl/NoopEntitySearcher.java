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

package io.jmix.search.index.impl;

import io.jmix.search.searching.EntitySearcher;
import io.jmix.search.searching.SearchContext;
import io.jmix.search.searching.SearchResult;
import io.jmix.search.searching.impl.SearchResultImpl;

public class NoopEntitySearcher implements EntitySearcher {

    @Override
    public SearchResult search(SearchContext searchContext) {
        return new SearchResultImpl(searchContext, "anyTermAnyField");
    }

    @Override
    public SearchResult search(SearchContext searchContext, String searchStrategy) {
        return new SearchResultImpl(searchContext, searchStrategy);
    }

    @Override
    public SearchResult searchNextPage(SearchResult previousSearchResult) {
        return previousSearchResult;
    }
}
