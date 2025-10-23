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

package io.jmix.searchelasticsearch.searching.strategy.impl;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import io.jmix.search.searching.SearchContext;
import io.jmix.search.searching.impl.AbstractSearchStrategy;
import io.jmix.searchelasticsearch.searching.strategy.ElasticSearchQueryConfigurer;
import io.jmix.searchelasticsearch.searching.strategy.ElasticsearchSearchStrategy;

/**
 * Abstract base class for Elasticsearch-specific search strategy implementations.
 * It provides a foundational structure for search strategies that configure Elasticsearch search requests.
 * Subclasses should implement specific search logic and provide concrete configurations for search requests.
 */
public abstract class AbstractElasticSearchStrategy
        extends AbstractSearchStrategy<SearchRequest.Builder, ElasticSearchQueryConfigurer>
        implements ElasticsearchSearchStrategy {

    protected AbstractElasticSearchStrategy(ElasticSearchQueryConfigurer queryConfigurator) {
        super(queryConfigurator);
    }

    /**
     * Configures the search request using the provided request builder and search context.
     *
     * @param requestBuilder the builder used to configure the search request
     * @param searchContext the context containing search parameters such as search text, size, offset, and entities
     * @throws UnsupportedOperationException as this method is not intended for usage
     */
    @Override
    @Deprecated(since = "2.7", forRemoval = true)
    public void configureRequest(SearchRequest.Builder requestBuilder, SearchContext searchContext) {
        throw new UnsupportedOperationException();
    }
}
