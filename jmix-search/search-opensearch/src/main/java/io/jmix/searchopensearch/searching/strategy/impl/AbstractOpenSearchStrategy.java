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

package io.jmix.searchopensearch.searching.strategy.impl;

import io.jmix.search.searching.SearchContext;
import io.jmix.search.searching.impl.AbstractSearchStrategy;
import io.jmix.search.searching.impl.SearchFieldsResolver;
import io.jmix.searchopensearch.searching.strategy.OpenSearchSearchStrategy;
import org.opensearch.client.opensearch.core.SearchRequest;

public abstract class AbstractOpenSearchStrategy
        extends AbstractSearchStrategy<SearchRequest.Builder, OpenSearchQueryConfigurator>
        implements OpenSearchSearchStrategy {
    protected AbstractOpenSearchStrategy(SearchFieldsResolver searchFieldsResolver, OpenSearchQueryConfigurator queryConfigurator) {
        super(searchFieldsResolver, queryConfigurator);
    }

    @Override
    public void configureRequest(SearchRequest.Builder requestBuilder, SearchContext searchContext) {
        throw new UnsupportedOperationException();
    }

}
