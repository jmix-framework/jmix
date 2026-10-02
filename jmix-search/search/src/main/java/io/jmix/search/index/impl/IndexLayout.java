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

import io.jmix.search.index.IndexConfiguration;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Maps an index configuration to the physical indexes of the search engine.
 * <p>
 * This is the only place that answers whether a configuration is split by tenants and which physical index
 * a particular operation works with. Index names are computed, not stored.
 */
public interface IndexLayout {

    /**
     * Tells whether this application stores tenant-aware entities in a separate index per tenant at all.
     * <p>
     * It is false when the multitenancy add-on is absent, and when the application has switched the mode off to
     * keep the indexes it had before the upgrade. Callers that have a configuration at hand ask
     * {@link #isSplitByTenants(IndexConfiguration)} instead; this one is for the code that is still building a
     * configuration and only knows whether the entity has a tenant attribute.
     *
     * @return true if the mode is on
     */
    boolean isSplitByTenantsEnabled();

    /**
     * @return true if the configuration is mapped to a separate index per tenant
     */
    boolean isSplitByTenants(IndexConfiguration configuration);

    /**
     * Returns the index that holds the documents of the configuration for the given tenant.
     * <p>
     * The tenant comes from the entity instance when writing and from the current user when searching.
     *
     * @param tenantId tenant, or null if it is unknown or the caller has none
     * @return index name, or null if there is no index to work with: the configuration is split by tenants,
     * but the tenant is not given
     */
    @Nullable
    String indexName(IndexConfiguration configuration, @Nullable String tenantId);

    /**
     * @return all physical indexes of the configuration: one per known tenant if the configuration is split,
     * a single index otherwise
     */
    List<TenantIndex> allIndexes(IndexConfiguration configuration);

    /**
     * The same for several configurations at once, reading the list of tenants once for the whole call.
     * <p>
     * The single-configuration method reads that list from the database on every call, so a loop over the
     * configurations of the application costs one query per indexed entity. That is acceptable for an
     * administrative sweep and not for the dequeue query, which is rebuilt for every batch taken from the queue.
     *
     * @return the indexes of each configuration, in the order the configurations were given
     */
    Map<IndexConfiguration, List<TenantIndex>> allIndexes(Collection<IndexConfiguration> configurations);

    /**
     * A physical index together with the tenant it holds the data of.
     *
     * @param tenantId tenant, or null if the index is not tenant-specific
     */
    record TenantIndex(@Nullable String tenantId, String indexName) {
    }
}
