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

import io.jmix.search.index.AtomicIndexOperationResult;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.IndexOperationResult;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.function.Function;

/**
 * Builds the rows an index operation reports.
 * <p>
 * An entity is stored in one index per tenant, so one request runs the operation several times, and every index
 * it reached gets a row. An entity stored in no index at all gets no rows: it is split by tenants and the
 * application has none yet.
 */
@NullMarked
final class IndexOperationResults {

    private IndexOperationResults() {
    }

    /**
     * @param configuration   the entity the operation was asked for
     * @param indexes         indexes the entity is stored in
     * @param resultForIndex  outcome of the operation on one index
     * @return one row per index, in the order the indexes came in
     */
    static <RT extends AtomicIndexOperationResult> List<IndexOperationResult<RT>> perIndex(
            IndexConfiguration configuration,
            List<IndexLayout.TenantIndex> indexes,
            Function<IndexLayout.TenantIndex, RT> resultForIndex) {
        return indexes.stream()
                .map(index -> new IndexOperationResult<>(
                        configuration.getEntityName(),
                        index.indexName(),
                        index.tenantId(),
                        resultForIndex.apply(index)))
                .toList();
    }
}
