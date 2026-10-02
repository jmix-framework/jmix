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

import io.jmix.core.MetadataTools;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.IndexNameGenerator;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Maps a tenant-aware configuration to a separate index per tenant, and any other configuration to a single index.
 */
@Component("search_IndexLayout")
public class StandardIndexLayout implements IndexLayout {

    private static final Logger log = LoggerFactory.getLogger(StandardIndexLayout.class);

    @Autowired
    protected IndexNameGenerator indexNameGenerator;

    @Autowired
    protected MultitenancyAdapter multitenancyAdapter;

    @Autowired
    protected SearchProperties searchProperties;

    @Autowired
    protected MetadataTools metadataTools;

    /**
     * Keeps the warning about a tenant attribute outside JPA to one line per entity: the question is asked on
     * every write and every search.
     */
    protected final Map<String, Boolean> warnedAboutNonJpaTenantAttribute = new ConcurrentHashMap<>();

    @Override
    public boolean isSplitByTenantsEnabled() {
        return searchProperties.isSplitIndexesByTenants() && multitenancyAdapter.isMultitenancyActive();
    }

    /**
     * Tells whether the data of the configuration is stored in a separate index per tenant.
     * <p>
     * A tenant attribute on an entity of a store other than JPA buys nothing: the add-on fills the attribute from
     * {@code JpaDataStore} alone and constrains rows through an EclipseLink criterion, so such an entity has no
     * tenants to be split by. Splitting it would produce an index per tenant with nothing in any of them.
     */
    @Override
    public boolean isSplitByTenants(IndexConfiguration configuration) {
        if (!isSplitByTenantsEnabled() || !configuration.isTenantAware()) {
            return false;
        }
        if (!metadataTools.isJpaEntity(configuration.getEntityClass())) {
            warnedAboutNonJpaTenantAttribute.computeIfAbsent(configuration.getEntityName(), entityName -> {
                log.warn("Entity '{}' has a tenant attribute but is not stored in JPA: multitenancy does not apply"
                        + " to it, and its index is not split by tenants", entityName);
                return Boolean.TRUE;
            });
            return false;
        }
        return true;
    }

    @Nullable
    @Override
    public String indexName(IndexConfiguration configuration, @Nullable String tenantId) {
        if (!isSplitByTenants(configuration)) {
            return tenantlessIndex(configuration);
        }
        return tenantId == null ? null : tenantIndex(configuration, tenantId);
    }

    @Override
    public List<TenantIndex> allIndexes(IndexConfiguration configuration) {
        return allIndexes(configuration, availableTenants(List.of(configuration)));
    }

    @Override
    public Map<IndexConfiguration, List<TenantIndex>> allIndexes(Collection<IndexConfiguration> configurations) {
        Set<String> tenants = availableTenants(configurations);
        Map<IndexConfiguration, List<TenantIndex>> indexes = new LinkedHashMap<>();
        for (IndexConfiguration configuration : configurations) {
            indexes.put(configuration, allIndexes(configuration, tenants));
        }
        return indexes;
    }

    /**
     * Reads the tenants of the application, and only if at least one of the configurations needs them.
     * <p>
     * The add-on answers this from the database, so an application whose entities are none of them tenant-aware,
     * or that keeps the indexes it had before the split, must not pay for the question - nor depend on the
     * tenants table being readable.
     */
    protected Set<String> availableTenants(Collection<IndexConfiguration> configurations) {
        return configurations.stream().anyMatch(this::isSplitByTenants)
                ? multitenancyAdapter.getAvailableTenants()
                : Set.of();
    }

    protected List<TenantIndex> allIndexes(IndexConfiguration configuration, Set<String> tenants) {
        if (!isSplitByTenants(configuration)) {
            return List.of(new TenantIndex(null, tenantlessIndex(configuration)));
        }
        List<TenantIndex> indexes = tenants.stream()
                .map(tenantId -> indexOfTenantOrNull(configuration, tenantId))
                .filter(Objects::nonNull)
                .toList();
        if (indexes.isEmpty()) {
            log.info("Entity '{}' has no index yet: it is split by tenants and there are no tenants",
                    configuration.getEntityName());
        }
        return indexes;
    }

    protected String tenantlessIndex(IndexConfiguration configuration) {
        return indexNameGenerator.generateIndexName(configuration, null);
    }

    protected String tenantIndex(IndexConfiguration configuration, String tenantId) {
        return indexNameGenerator.generateIndexName(configuration, tenantId);
    }

    /**
     * Skips a tenant whose id cannot become an index name instead of failing the whole sweep.
     * <p>
     * A tenant created before the id was validated can hold anything - a space, a {@code #}, a value too long.
     * Asking for that one index is still an error, but every caller of {@code allIndexes} is maintaining or
     * searching all of them, and one unusable tenant must not stop the other tenants, the other entities and
     * startup synchronization along with them.
     */
    @Nullable
    protected TenantIndex indexOfTenantOrNull(IndexConfiguration configuration, String tenantId) {
        try {
            return new TenantIndex(tenantId, tenantIndex(configuration, tenantId));
        } catch (RuntimeException e) {
            log.error("Entity '{}' has no usable index for tenant '{}': its data is neither indexed nor searched",
                    configuration.getEntityName(), tenantId, e);
            return null;
        }
    }
}
