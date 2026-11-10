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
import io.jmix.search.index.IndexConfiguration
import spock.lang.Specification

/**
 * The registry that holds the index configurations of one metadata generation.
 *
 * <p>The property-tracking machinery is left out: a configuration without mapped fields exercises the registry's
 * own bookkeeping, which is what these cases are about.
 */
class IndexConfigurationRegistryTest extends Specification {

    def instanceNameProvider = Mock(InstanceNameProvider)
    def registry = new IndexConfigurationManager.Registry(instanceNameProvider)

    def "a registered configuration is found by entity name"() {
        given:
        def configuration = configuration("test_Order", Order)

        when:
        registry.registerIndexConfiguration(configuration)

        then:
        registry.getIndexConfigurationByEntityName("test_Order").is(configuration)
        registry.hasDefinitionForEntity("test_Order")
        registry.getAllIndexedEntities() == ["test_Order"] as Set
    }

    def "an entity nobody registered is reported as absent rather than by an exception"() {
        expect:
        registry.getIndexConfigurationByEntityName("test_Unknown") == null
        !registry.hasDefinitionForEntity("test_Unknown")
        registry.getIndexConfigurations().isEmpty()
        registry.getAllIndexedEntities().isEmpty()
    }

    def "the classes a configuration affects are registered with it"() {
        given: "a configuration that indexes an order and is affected by changes of its customer"
        def configuration = configuration("test_Order", Order, Customer)

        when:
        registry.registerIndexConfiguration(configuration)

        then:
        registry.isEntityClassRegistered(Order)
        registry.isEntityClassRegistered(Customer)
        !registry.isEntityClassRegistered(Invoice)
    }


    def "properties affected by an update of an unregistered class are an empty set, not null"() {
        expect: "the caller iterates the result without a null check, unlike with the back-reference getters"
        registry.getLocalPropertyNamesAffectedByUpdate(Order) == [] as Set
        registry.getBackRefPropertiesForUpdate(Order) == null
        registry.getBackRefPropertiesForDelete(Order) == null
    }

    protected IndexConfiguration configuration(String entityName, Class<?>... affectedClasses) {
        def mapping = Stub(IndexMappingConfiguration) {
            getFields() >> [:]
            getDisplayedNameDescriptor() >> Stub(DisplayedNameDescriptor) {
                getInstanceNameRelatedProperties() >> []
            }
        }
        return Stub(IndexConfiguration) {
            getEntityName() >> entityName
            getAffectedEntityClasses() >> (affectedClasses as Set)
            getMapping() >> mapping
        }
    }

    private static class Order {}

    private static class Customer {}

    private static class Invoice {}
}
