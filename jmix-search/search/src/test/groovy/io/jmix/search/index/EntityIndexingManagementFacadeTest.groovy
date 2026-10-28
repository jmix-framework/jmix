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

package io.jmix.search.index

import spock.lang.Specification

import static io.jmix.search.index.EntityIndexingManagementFacade.formatResults

class EntityIndexingManagementFacadeTest extends Specification {

    def "a tenantless index is reported without a tenant"() {
        given:
        def results = [new IndexOperationResult<>("demo_Order", "demo_order_index", null, IndexValidationStatus.ACTUAL)]

        expect:
        formatResults("Validation", results) == "Validation result:" + System.lineSeparator() + "\t" +
                "Entity=demo_Order, Index=demo_order_index, Tenant=-, Status=ACTUAL"
    }

    def "every index gets its own line, whatever entity and tenant it belongs to"() {
        given:
        def results = [
                new IndexOperationResult<>("demo_Order", "demo_order_tenant1", "tenant1", IndexSynchronizationStatus.CREATED),
                new IndexOperationResult<>("demo_Order", "demo_order_tenant2", "tenant2", IndexSynchronizationStatus.MISSING),
                new IndexOperationResult<>("demo_Customer", "demo_customer_index", null, IndexSynchronizationStatus.ACTUAL)
        ]

        expect:
        formatResults("Synchronization", results) == "Synchronization result:" + System.lineSeparator() + "\t" +
                "Entity=demo_Order, Index=demo_order_tenant1, Tenant=tenant1, Status=CREATED" +
                System.lineSeparator() + "\t" +
                "Entity=demo_Order, Index=demo_order_tenant2, Tenant=tenant2, Status=MISSING" +
                System.lineSeparator() + "\t" +
                "Entity=demo_Customer, Index=demo_customer_index, Tenant=-, Status=ACTUAL"
    }

    def "an operation that touched no index says so instead of printing an empty line"() {
        expect:
        formatResults("Deletion", []) == "Deletion result: no indexes to work with"
    }

    def "an operation that creates something refuses a tenant that does not exist"() {
        given:
        def facade = facadeKnowing("acme")

        when:
        def result = facade.synchronizeIndexSchemas("", "acmme")

        then: "a typed tenant id has no autocompletion behind it, and creating indexes for a misspelled tenant leaves real indexes nothing will ever use"
        result == "Tenant 'acmme' does not exist"
        0 * facade.indexManager._
    }

    def "an operation that cleans up accepts a tenant that is already gone"() {
        given:
        def facade = facadeKnowing("acme")

        when: "the tenant has been removed, and what it left behind is exactly what has to go"
        facade.deleteIndexes("", "departed")

        then:
        1 * facade.indexManager.deleteIndexes(_, "departed") >> []
    }

    def "an empty field means everything"() {
        given:
        def facade = facadeKnowing("acme")
        def allConfigurations = [Mock(IndexConfiguration)]
        facade.indexConfigurationManager.getAllIndexConfigurations() >> allConfigurations

        when:
        facade.validateIndexes("  ", "  ")

        then: "blanks are not tenant ids and not entity names: they are the absence of a restriction"
        1 * facade.indexManager.validateIndexes(allConfigurations, null) >> []
    }

    protected EntityIndexingManagementFacade facadeKnowing(String... tenants) {
        def facade = new EntityIndexingManagementFacade()
        facade.indexManager = Mock(IndexManager)
        facade.indexingQueueManager = Mock(io.jmix.search.index.queue.IndexingQueueManager)
        facade.indexConfigurationManager = Mock(io.jmix.search.index.mapping.IndexConfigurationManager)
        facade.multitenancyAdapter = Mock(io.jmix.search.index.impl.MultitenancyAdapter) {
            getAvailableTenants() >> (tenants as Set)
        }
        return facade
    }

}
