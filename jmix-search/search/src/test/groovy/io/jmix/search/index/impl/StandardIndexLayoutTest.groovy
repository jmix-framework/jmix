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

import io.jmix.search.index.IndexConfiguration
import io.jmix.core.MetadataTools
import io.jmix.search.SearchProperties
import io.jmix.search.index.IndexNameGenerator
import spock.lang.Specification

class StandardIndexLayoutTest extends Specification {

    def multitenancyAdapter = Mock(MultitenancyAdapter)
    def indexNameGenerator = Mock(IndexNameGenerator)
    def searchProperties = Stub(SearchProperties) {
        isSplitIndexesByTenants() >> true
    }
    def layout = new StandardIndexLayout()

    def configuration = Stub(IndexConfiguration) {
        getEntityName() >> "test_Order"
        getEntityClass() >> Object
        isTenantAware() >> true
    }

    def setup() {
        layout.multitenancyAdapter = multitenancyAdapter
        layout.indexNameGenerator = indexNameGenerator
        layout.searchProperties = searchProperties
        layout.metadataTools = jpaMetadataTools()
        multitenancyAdapter.isMultitenancyActive() >> true
    }

    def "a tenant whose id cannot become an index name is skipped, the rest are returned"() {
        given: "a tenant created before the id was validated - a space is not allowed in an index name"
        multitenancyAdapter.getAvailableTenants() >> (["good", "bad tenant"] as Set)
        indexNameGenerator.generateIndexName(configuration, "good") >> "search_index_test_order_good"
        indexNameGenerator.generateIndexName(configuration, "bad tenant") >> {
            throw new IllegalArgumentException("Invalid index name")
        }

        when:
        def indexes = layout.allIndexes(configuration)

        then: """the sweep is what startup synchronization and queue processing walk - one unusable tenant must
                 not take down maintenance of every entity and every other tenant"""
        indexes.size() == 1
        indexes[0].tenantId() == "good"
        indexes[0].indexName() == "search_index_test_order_good"
    }

    def "the tenants are read once for all configurations, not once for each"() {
        given:
        def customer = Stub(IndexConfiguration) {
            getEntityName() >> "test_Customer"
            getEntityClass() >> Object
            isTenantAware() >> true
        }
        indexNameGenerator.generateIndexName(_ as IndexConfiguration, "acme") >> "index_acme"

        when:
        def indexes = layout.allIndexes([configuration, customer])

        then: """the add-on answers this from the database, and the dequeue query is rebuilt for every batch
                 taken from the queue - asking entity by entity would be a query per indexed entity"""
        1 * multitenancyAdapter.getAvailableTenants() >> (["acme"] as Set)
        indexes.keySet() as List == [configuration, customer]
        indexes[customer]*.tenantId() == ["acme"]
    }

    def "an application with nothing split does not ask for the tenants at all"() {
        given:
        def shared = Stub(IndexConfiguration) {
            getEntityName() >> "test_Shared"
            getEntityClass() >> Object
            isTenantAware() >> false
        }
        indexNameGenerator.generateIndexName(shared, null) >> "index_shared"

        when:
        def indexes = layout.allIndexes([shared])

        then: "neither the cost nor the dependency on the tenants table belongs to an application without tenants"
        0 * multitenancyAdapter.getAvailableTenants()
        indexes[shared]*.indexName() == ["index_shared"]
    }

    def "asking for the index of that very tenant is still an error"() {
        given:
        indexNameGenerator.generateIndexName(configuration, "bad tenant") >> {
            throw new IllegalArgumentException("Invalid index name")
        }

        when:
        layout.indexName(configuration, "bad tenant")

        then: "the caller named the tenant and has to hear that it has no index, unlike a sweep over all of them"
        thrown(IllegalArgumentException)
    }
    /** The entities of these tests are ordinary JPA ones: a tenant attribute elsewhere buys nothing. */
    protected MetadataTools jpaMetadataTools() {
        return Stub(MetadataTools) {
            isJpaEntity(_) >> true
        }
    }

}
