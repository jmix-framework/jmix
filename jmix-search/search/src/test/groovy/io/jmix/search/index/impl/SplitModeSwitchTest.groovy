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

package io.jmix.search.index.impl

import io.jmix.core.MetadataTools
import io.jmix.search.SearchProperties
import io.jmix.search.index.IndexConfiguration
import io.jmix.search.index.IndexNameGenerator
import spock.lang.Specification

/**
 * The switch that returns the module to the layout it had before tenant-separated indexes existed.
 *
 * <p>An application that upgrades keeps its indexes working by setting
 * {@code jmix.search.split-indexes-by-tenants} to false, and plans the reindex separately. The mode is on by
 * default, so a new application gets the isolation without configuring anything.
 *
 * <p>Everything downstream reads the mode through the layout, so pinning it here pins the whole behaviour:
 * which index a document is written to, which index a search is addressed to, how many indexes an entity has.
 */
class SplitModeSwitchTest extends Specification {

    static final String TENANTLESS_INDEX = "search_index_test_order"
    static final String ACME_INDEX = "search_index_test_order_acme"

    def multitenancyAdapter = Mock(MultitenancyAdapter)
    def indexNameGenerator = Mock(IndexNameGenerator)

    def configuration = Stub(IndexConfiguration) {
        getEntityName() >> "test_Order"
        getEntityClass() >> Object
        isTenantAware() >> true
    }

    protected StandardIndexLayout layout(boolean splitEnabled, boolean addonActive = true) {
        def layout = new StandardIndexLayout()
        layout.multitenancyAdapter = multitenancyAdapter
        layout.indexNameGenerator = indexNameGenerator
        layout.metadataTools = jpaMetadataTools()
        layout.searchProperties = Stub(SearchProperties) {
            isSplitIndexesByTenants() >> splitEnabled
        }
        multitenancyAdapter.isMultitenancyActive() >> addonActive
        return layout
    }

    def "the mode is on by default, so a tenant-aware entity is split"() {
        expect: "the default of the property is true - see SearchProperties"
        layout(true).isSplitByTenantsEnabled()
        layout(true).isSplitByTenants(configuration)
    }

    def "switched off, a tenant-aware entity goes back to a single shared index"() {
        given:
        def layout = layout(false)
        indexNameGenerator.generateIndexName(configuration, null) >> TENANTLESS_INDEX

        expect: "the name is the one the application had before the upgrade"
        !layout.isSplitByTenants(configuration)
        layout.indexName(configuration, null) == TENANTLESS_INDEX

        and: "a tenant does not change the answer: there is one index and everyone reads and writes it"
        layout.indexName(configuration, "acme") == TENANTLESS_INDEX

        and: "and the entity has exactly that one index, not one per tenant"
        layout.allIndexes(configuration)*.indexName() == [TENANTLESS_INDEX]
        layout.allIndexes(configuration)*.tenantId() == [null]
    }

    def "switched on, the same entity is addressed per tenant"() {
        given:
        def layout = layout(true)
        indexNameGenerator.generateIndexName(configuration, "acme") >> ACME_INDEX
        multitenancyAdapter.getAvailableTenants() >> Set.of("acme")

        expect:
        layout.isSplitByTenants(configuration)
        layout.indexName(configuration, "acme") == ACME_INDEX

        and: "a record with no tenant has no index to go to - decision 0013"
        layout.indexName(configuration, null) == null

        and:
        layout.allIndexes(configuration)*.indexName() == [ACME_INDEX]
    }

    def "without the add-on the mode is off whatever the property says"() {
        given:
        def layout = layout(true, false)
        indexNameGenerator.generateIndexName(configuration, null) >> TENANTLESS_INDEX

        expect: "there are no tenants to separate, so the property has nothing to switch"
        !layout.isSplitByTenantsEnabled()
        !layout.isSplitByTenants(configuration)
        layout.indexName(configuration, "acme") == TENANTLESS_INDEX
    }

    def "an entity without a tenant attribute is never split, in either mode"() {
        given:
        def shared = Stub(IndexConfiguration) {
            getEntityName() >> "test_Country"
            getEntityClass() >> Object
            isTenantAware() >> false
        }
        def layout = layout(splitEnabled)
        indexNameGenerator.generateIndexName(shared, null) >> "search_index_test_country"

        expect:
        !layout.isSplitByTenants(shared)
        layout.indexName(shared, "acme") == "search_index_test_country"

        where:
        splitEnabled << [true, false]
    }
    /** The entities of these tests are ordinary JPA ones: a tenant attribute elsewhere buys nothing. */
    protected MetadataTools jpaMetadataTools() {
        return Stub(MetadataTools) {
            isJpaEntity(_) >> true
        }
    }

}
