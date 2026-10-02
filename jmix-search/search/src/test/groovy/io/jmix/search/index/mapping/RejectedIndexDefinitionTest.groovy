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
import io.jmix.search.exception.IndexConfigurationException
import io.jmix.search.exception.IndexDefinitionRejectedException
import io.jmix.search.index.IndexConfiguration
import io.jmix.search.index.impl.IndexStateRegistry
import io.jmix.search.index.mapping.processor.impl.AnnotatedIndexDefinitionProcessor
import io.jmix.search.index.mapping.processor.impl.IndexDefinitionDetector
import spock.lang.Specification

/**
 * What one unusable index definition costs.
 *
 * <p>Before the checks of this task existed, a definition that could not produce a working index cost its own
 * entity: the index was never created and that entity was neither indexed nor searched, while every other entity
 * kept working. The checks must not make it cost more than that, so a definition they reject is dropped and the
 * remaining definitions are applied.
 *
 * <p>Only the checks of this module skip that way. A failure of a different kind keeps taking the whole set down,
 * because that is what it did before and changing it is not this task's to do.
 */
class RejectedIndexDefinitionTest extends Specification {

    static final String GOOD = "demo.OrderIndexDefinition"
    static final String BROKEN = "demo.ProductIndexDefinition"

    def processor = Mock(AnnotatedIndexDefinitionProcessor)
    def scanner = Stub(JmixModulesClasspathScanner) {
        getClassNames(_) >> ([GOOD, BROKEN] as LinkedHashSet)
    }

    List<IndexConfiguration> registered = null

    def manager = new IndexConfigurationManager(
            scanner,
            processor,
            Stub(InstanceNameProvider),
            Stub(IndexDefinitionDetector),
            Stub(IndexStateRegistry)) {

        @Override
        protected void replaceConfigurations(State state, List<IndexConfiguration> configurations) {
            registered = configurations
        }
    }

    def "a definition the checks reject is dropped, the others are applied"() {
        given:
        def order = Stub(IndexConfiguration) {
            getEntityName() >> "demo_Order"
        }
        processor.createIndexConfiguration(GOOD) >> order
        processor.createIndexConfiguration(BROKEN) >> {
            throw new IndexDefinitionRejectedException(
                    "Index definition ProductIndexDefinition of entity 'demo_Product': index name pattern is blank")
        }

        when:
        manager.initializeIndexDefinitions(null)

        then: "the application searches every entity but the one whose definition is unusable"
        registered == [order]
    }

    def "a failure of another kind still takes the whole set down"() {
        given:
        processor.createIndexConfiguration(GOOD) >> Stub(IndexConfiguration)
        processor.createIndexConfiguration(BROKEN) >> {
            throw new IndexConfigurationException("Dynamic attributes module is not present in the application")
        }

        when:
        manager.initializeIndexDefinitions(null)

        then: "it behaved this way before the per-definition checks existed, and that is left alone"
        thrown(IndexConfigurationException)
        registered == null
    }
}
