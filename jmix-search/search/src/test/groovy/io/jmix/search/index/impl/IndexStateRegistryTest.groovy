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

import io.jmix.search.index.mapping.IndexConfigurationManager
import spock.lang.Specification

class IndexStateRegistryTest extends Specification {

    def "an entity indexed after startup is unavailable until it is marked available"() {
        given:
        def manager = Mock(IndexConfigurationManager)
        // The constructor sees one entity; a later metadata generation adds a second one.
        manager.getAllIndexedEntities() >>> [["a"], ["a", "b"], ["a", "b"], ["a", "b"]]
        manager.isDirectlyIndexed(_ as String) >> true
        def registry = new IndexStateRegistry(manager)

        expect:
        registry.getAllUnavailableIndexedEntities() == ["a", "b"]
        !registry.isIndexAvailable("b")

        when:
        registry.markIndexAsAvailable("b")

        then:
        registry.getAllUnavailableIndexedEntities() == ["a"]
        registry.isIndexAvailable("b")
    }

    def "marking an entity not indexed in the current metadata generation as available is ignored"() {
        given:
        def manager = Mock(IndexConfigurationManager)
        manager.getAllIndexedEntities() >> ["a"]
        manager.isDirectlyIndexed("a") >> true
        manager.isDirectlyIndexed("retired") >> false
        def registry = new IndexStateRegistry(manager)

        when:
        registry.markIndexAsAvailable("retired")

        then:
        noExceptionThrown()
        !registry.getIndexAvailabilityStates().containsKey("retired")
        !registry.isIndexAvailable("retired")
    }
}
