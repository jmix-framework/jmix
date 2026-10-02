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

package io.jmix.search.index.impl;

import io.jmix.searchelasticsearch.index.impl.ElasticsearchEntityIndexer;

import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

/**
 * The request that actually leaves for the engine, not the list prepared for it.
 * <p>
 * Which indexes a deletion is routed to is checked a layer up, in EntityDeletionRoutingTest. Nothing until now
 * looked at what ends up in the bulk request itself: a deletion addressed to one tenant must carry no operation
 * against another tenant's index, and one with no tenant known must carry an operation per index of the entity.
 * <p>
 * Lives in the package of BaseEntityIndexer because the documents it is handed are a protected record of it.
 */
class ElasticsearchDeleteRequestTest {

    static final String INDEX_A = "search_index_customer_tenanta";
    static final String INDEX_B = "search_index_customer_tenantb";
    static final String DOCUMENT_ID = "Customer.7b9c";

    @Test
    @DisplayName("A deletion addressed to one tenant sends a single operation, against that tenant's index")
    void deletionOfOneTenantTouchesOneIndex() {
        List<BulkOperation> operations = new CapturingIndexer().deleteFrom(INDEX_A).operations();

        Assertions.assertEquals(1, operations.size(), "One record, one index, one operation");
        Assertions.assertEquals(INDEX_A, operations.get(0).delete().index());
        Assertions.assertEquals(DOCUMENT_ID, operations.get(0).delete().id());
    }

    @Test
    @DisplayName("A deletion with no tenant known sends one operation per index of the entity")
    void deletionWithUnknownTenantTouchesEveryIndex() {
        List<BulkOperation> operations = new CapturingIndexer().deleteFrom(INDEX_A, INDEX_B).operations();

        Assertions.assertEquals(2, operations.size());
        Assertions.assertEquals(List.of(INDEX_A, INDEX_B),
                operations.stream().map(operation -> operation.delete().index()).toList());
    }

    /**
     * Keeps the request instead of sending it, so the client is never reached.
     */
    static class CapturingIndexer extends ElasticsearchEntityIndexer {

        BulkRequest captured;

        CapturingIndexer() {
            super(null, null, null, null, null, null, null, null, null, null, null);
        }

        BulkRequest deleteFrom(String... indexNames) {
            deleteByGroupedDocIds(Arrays.stream(indexNames)
                    .map(indexName -> new DocumentToDelete(DOCUMENT_ID, indexName))
                    .toList());
            return captured;
        }

        @Override
        protected Refresh resolveRefresh() {
            return Refresh.False;
        }

        @Override
        protected BulkResponse executeBulkRequest(BulkRequest request) {
            captured = request;
            return createNoopBulkResponse();
        }
    }
}
