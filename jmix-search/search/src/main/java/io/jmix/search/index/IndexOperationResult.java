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

package io.jmix.search.index;

import org.springframework.lang.Nullable;

/**
 * The outcome of an index operation on a single index.
 * <p>
 * An operation over a tenant-aware entity works on one index per tenant, so it produces one result per tenant, and
 * every result names the index it belongs to. This makes the result a self-contained row: it can be reported and
 * grouped without any context around it.
 *
 * @param entityName the entity the index holds the data of
 * @param indexName  the name of the index the operation was performed on
 * @param tenantId   the tenant whose data the index holds, or {@code null} if the index is not tenant-specific
 * @param result     the atomic result of the operation
 * @param <RT>       the type of the atomic operation result
 */
public record IndexOperationResult<RT extends AtomicIndexOperationResult>(
        String entityName,
        String indexName,
        @Nullable String tenantId,
        RT result
) {

    public boolean isSuccess() {
        return result.isSuccess();
    }
}
