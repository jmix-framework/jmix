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

package io.jmix.search.index.mapping

import io.jmix.core.InstanceNameProvider
import io.jmix.core.impl.scanning.JmixModulesClasspathScanner
import io.jmix.search.index.IndexConfiguration
import io.jmix.search.index.impl.IndexLayout
import io.jmix.search.index.impl.IndexStateRegistry
import io.jmix.search.index.mapping.processor.impl.AnnotatedIndexDefinitionProcessor
import io.jmix.search.index.mapping.processor.impl.IndexDefinitionDetector
import spock.lang.Specification

/**
 * An index name comes from two sources now — the application-wide pattern and the pattern an entity declares for
 * itself — so two entities can end up claiming one index by a plain mistake.
 */
class IndexNameUniquenessTest extends Specification {

    def indexLayout = Mock(IndexLayout)

    def manager = new IndexConfigurationManager(
            Stub(JmixModulesClasspathScanner),
            Stub(AnnotatedIndexDefinitionProcessor),
            Stub(InstanceNameProvider),
            Stub(IndexDefinitionDetector),
            Stub(IndexStateRegistry))

    def setup() {
        manager.indexLayout = indexLayout
    }

    def "of two entities claiming the same index, the first keeps it and the second is dropped"() {
        given:
        def order = sharedConfiguration("demo_Order", "orders")
        def orderLine = sharedConfiguration("demo_OrderLine", "orders")

        when:
        def accepted = manager.dropIndexNameCollisions([order, orderLine])

        then: """one mistake costs the entity that made it: before the check existed the second configuration
                 silently took over the index of the first, and every other entity kept working either way"""
        accepted == [order]
    }

    def "distinct names pass"() {
        given:
        def order = sharedConfiguration("demo_Order", "orders")
        def orderLine = sharedConfiguration("demo_OrderLine", "order_lines")

        when:
        def accepted = manager.dropIndexNameCollisions([order, orderLine])

        then:
        accepted == [order, orderLine]
    }

    def "a collision between a split and a shared entity is caught"() {
        given:
        def shared = sharedConfiguration("demo_Product", "catalog_sampleTenant")
        def split = splitConfiguration("demo_Supplier", "catalog_sampleTenant")

        when:
        def accepted = manager.dropIndexNameCollisions([shared, split])

        then:
        accepted == [shared]
    }

    def "tenants of one entity never collide with each other"() {
        given: "the only configuration is split, so its name is taken for a sample tenant"
        def split = splitConfiguration("demo_Supplier", "suppliers_sampleTenant")

        expect:
        manager.dropIndexNameCollisions([split]) == [split]
    }

    def "an empty set of configurations is fine"() {
        expect:
        manager.dropIndexNameCollisions([]).isEmpty()
    }

    protected IndexConfiguration sharedConfiguration(String entityName, String indexName) {
        def configuration = Stub(IndexConfiguration)
        configuration.getEntityName() >> entityName
        indexLayout.isSplitByTenants(configuration) >> false
        indexLayout.indexName(configuration, null) >> indexName
        return configuration
    }

    protected IndexConfiguration splitConfiguration(String entityName, String sampleIndexName) {
        def configuration = Stub(IndexConfiguration)
        configuration.getEntityName() >> entityName
        indexLayout.isSplitByTenants(configuration) >> true
        indexLayout.indexName(configuration, "sampleTenant") >> sampleIndexName
        return configuration
    }
}
