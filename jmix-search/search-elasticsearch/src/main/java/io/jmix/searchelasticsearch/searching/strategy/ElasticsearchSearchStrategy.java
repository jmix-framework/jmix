/*
 * Copyright 2026 Haulmont.
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

package io.jmix.searchelasticsearch.searching.strategy;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import io.jmix.search.searching.SearchStrategy;
import org.jspecify.annotations.NullMarked;

/**
 * Elasticsearch-specific {@link SearchStrategy} that builds an Elasticsearch {@link SearchRequest}.
 */
@NullMarked
public interface ElasticsearchSearchStrategy extends SearchStrategy<SearchRequest.Builder> {
}
