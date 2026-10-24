/*
 * Copyright 2020 Haulmont.
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

import io.jmix.core.annotation.Experimental;
import org.jspecify.annotations.Nullable;

/**
 * Builds the name of a search index and owns the rules a name must obey.
 * <p>
 * A name comes from a pattern: the placeholders {@code {entityName}} and {@code {tenantId}} are replaced, and
 * everything else is taken as written. The pattern is either the one an entity declares in
 * {@link io.jmix.search.index.annotation.JmixEntitySearchIndex#indexName()} or the application-wide one from the
 * properties.
 * <p>
 * A name is computed on every request and never stored. A stored name is derived state: somebody has to fill it,
 * extend it when a tenant appears, drop it when the metadata generation changes and keep it consistent across a
 * cluster - while recomputing it costs about a microsecond. Hence the rule for caching anything around this
 * interface: cache what a name is computed <em>from</em>, such as the set of tenants, never the name itself.
 * <p>
 * Marked experimental because the rules below rest on a tenant identifier that has no rules of its own: nothing
 * checks its characters, its case or whether it collides with the {@code no_tenant} sentinel. Once that contract
 * exists, what this interface has to reject will change with it.
 */
@Experimental
public interface IndexNameGenerator {

    /**
     * Builds the name of the index that holds the data of the configuration for the given tenant.
     *
     * @param configuration index configuration
     * @param tenantId      tenant whose index is named, or null if the data of the entity is not split by tenants
     * @return the index name
     * @throws IllegalArgumentException if the resulting name is not accepted by the search engine
     */
    String generateIndexName(IndexConfiguration configuration, @Nullable String tenantId);

    /**
     * Checks that an entity may name its indexes by the given pattern.
     * <p>
     * A placeholder is required exactly where one pattern has to produce several names: a pattern of an entity
     * whose data is split by tenants must contain {@code {tenantId}}, a pattern of any other entity must not.
     * {@code {entityName}} is never required in a pattern that belongs to a single entity.
     *
     * @param pattern       pattern to check
     * @param source        what declares the pattern, for the error message
     * @param splitByTenants whether the data of the entity is stored in a separate index per tenant
     * @throws io.jmix.search.exception.IndexDefinitionRejectedException if the pattern cannot be used: the entity
     * that declared it is left out of indexing, while the rest of the application keeps working
     */
    void validateEntityIndexNamePattern(String pattern, String source, boolean splitByTenants);
}
