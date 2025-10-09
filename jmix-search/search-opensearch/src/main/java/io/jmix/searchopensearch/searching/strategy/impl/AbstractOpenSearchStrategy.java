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
import io.jmix.searchopensearch.searching.strategy.OpenSearchSearchStrategy;
import org.opensearch.client.opensearch.core.SearchRequest;

/**
 * An abstract implementation of the {@link AbstractSearchStrategy} that contains specifics of the OpenSearch.
 */
public abstract class AbstractOpenSearchStrategy
        extends AbstractSearchStrategy<SearchRequest.Builder, OpenSearchQueryConfigurer>
        implements OpenSearchSearchStrategy {

    protected AbstractOpenSearchStrategy(OpenSearchQueryConfigurer queryConfigurator) {
        super(queryConfigurator);
    }

    /**
     * TODO Pavel Aleksandrov
     * @param requestBuilder allows to configure search request
     * @param searchContext  contains details about search being performed
     */
    @Override
    @Deprecated(since = "2.7", forRemoval = true)
    public void configureRequest(SearchRequest.Builder requestBuilder, SearchContext searchContext) {
        throw new UnsupportedOperationException();
    }
}
