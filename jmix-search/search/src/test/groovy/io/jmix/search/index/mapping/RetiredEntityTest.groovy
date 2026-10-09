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
import io.jmix.core.impl.metadata.MetadataGenerationManager
import io.jmix.core.impl.scanning.JmixModulesClasspathScanner
import io.jmix.search.index.IndexConfiguration
import io.jmix.search.index.impl.IndexStateRegistry
import io.jmix.search.index.mapping.processor.impl.AnnotatedIndexDefinitionProcessor
import io.jmix.search.index.mapping.processor.impl.IndexDefinitionDetector
import spock.lang.Specification

/**
 * What survives a rebuild of the index definitions.
 *
 * <p>Definitions are rebuilt in two ways. One is the console operation that recomputes them and synchronizes the
 * schemas right after; the other happens by itself when a new metadata generation is published and nothing
 * follows it. Forgetting which indexes are ready is right for the first and ruinous for the second: nothing would
 * ever mark them ready again, and indexing would stop for good.
 */
class RetiredEntityTest extends Specification {

    def indexStateRegistry = new IndexStateRegistry()
    def indexConfigurationManager = new IndexConfigurationManager(
            Stub(JmixModulesClasspathScanner) {
                getClassNames(_) >> ([] as Set)
            },
            Stub(AnnotatedIndexDefinitionProcessor),
            Stub(InstanceNameProvider),
            Stub(IndexDefinitionDetector),
            indexStateRegistry)

    def setup() {
        indexConfigurationManager.metadataGenerationManager = Stub(MetadataGenerationManager) {
            getPinnedOrCurrentGenerationId() >> 1L
        }
    }

    def "a rebuild that nothing repairs keeps what is known to be ready"() {
        given:
        indexStateRegistry.markIndexAsAvailable("search_index_test_order")

        when: "this is the lazy rebuild, the one a new metadata generation triggers"
        indexConfigurationManager.replaceConfigurations(
                new IndexConfigurationManager.State(new IndexConfigurationManager.Registry(Stub(InstanceNameProvider))),
                [])

        then: """clearing here would leave every index unavailable with no synchronization to follow, and the
                 queue would stop feeding all of them until the application is restarted"""
        indexStateRegistry.isIndexAvailable("search_index_test_order")
    }

    def "the explicit refresh forgets it, because synchronization follows immediately"() {
        given:
        indexStateRegistry.markIndexAsAvailable("search_index_test_order")

        when:
        indexConfigurationManager.refreshIndexDefinitions()

        then: """an index whose mapping has just changed must not be written to before it has been checked again,
                 and the caller of this operation synchronizes the schemas right after"""
        !indexStateRegistry.isIndexAvailable("search_index_test_order")
    }

    def "marking an index of an entity that left the indexed set changes nothing and throws nothing"() {
        when: "a background operation can still hold the name of an index whose entity is gone"
        indexStateRegistry.markIndexAsAvailable("search_index_test_retired")

        then:
        noExceptionThrown()

        and: "the name is never asked about again, because it is no longer produced by any configuration"
        indexStateRegistry.isIndexAvailable("search_index_test_retired")
    }

    def "what a caller was handed does not change under it when the definitions are rebuilt"() {
        given:
        def state = new IndexConfigurationManager.State(
                new IndexConfigurationManager.Registry(Stub(InstanceNameProvider)))
        indexConfigurationManager.replaceConfigurations(state, [])
        def held = state.registry.getIndexConfigurations()

        when: "a rebuild lands while the caller is still walking what it was given"
        indexConfigurationManager.replaceConfigurations(state, [])

        then: """the rebuild assembles a registry of its own and publishes it, so the one the caller holds stays
                 exactly as it was - the next caller gets the new one"""
        held.is(state.registry.getIndexConfigurations()) == false
        held.isEmpty()
    }

    def "a configuration whose mapping carries the data of a tenant is left out whichever way it was built"() {
        given:
        def processor = Mock(AnnotatedIndexDefinitionProcessor)
        def manager = new IndexConfigurationManager(
                Stub(JmixModulesClasspathScanner) { getClassNames(_) >> ([] as Set) },
                processor,
                Stub(InstanceNameProvider),
                Stub(IndexDefinitionDetector),
                indexStateRegistry)
        def good = Stub(IndexConfiguration) { getEntityName() >> "test_Order" }
        def leaking = Stub(IndexConfiguration) { getEntityName() >> "test_Product" }

        when:
        def accepted = manager.accepted([good, leaking])

        then: "the check is asked about the assembled configuration, not about the way it was assembled"
        1 * processor.checkNoTenantDataInSharedIndex({ it.contains("test_Order") }, _)
        1 * processor.checkNoTenantDataInSharedIndex({ it.contains("test_Product") }, _) >> {
            throw new io.jmix.search.exception.IndexDefinitionRejectedException("maps tenant data")
        }
        accepted == [good]
    }
}
