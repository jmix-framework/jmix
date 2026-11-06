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

package io.jmix.searchelasticsearch.searching.impl;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * A tenant-aware entity is mapped to one index per tenant, and one of those indexes can be absent while the rest
 * are fine. Elasticsearch answers a request naming a missing index with an error for the whole request, so
 * without this flag the search of every user breaks because of one tenant.
 */
public class ElasticsearchSearchRequestTest {

    @Test
    @DisplayName("A search request tolerates an index that does not exist")
    public void searchRequestIgnoresUnavailableIndexes() {
        ElasticsearchEntitySearcher searcher =
                new ElasticsearchEntitySearcher(null, null, null, null, null, null, null, null, null, null, null);
        SearchRequest.Builder builder = new SearchRequest.Builder();

        searcher.initRequest(builder, List.of("search_index_customer_tenant_a", "search_index_customer_tenant_b"));

        SearchRequest request = builder.build();
        Assertions.assertEquals(Boolean.TRUE, request.ignoreUnavailable(),
                "one tenant's missing index must not fail the search of everyone else");
        Assertions.assertEquals(List.of("search_index_customer_tenant_a", "search_index_customer_tenant_b"),
                request.index());
    }
}
