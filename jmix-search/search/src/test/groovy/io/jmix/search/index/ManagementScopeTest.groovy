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

import io.jmix.search.index.impl.MultitenancyAdapter
import io.jmix.search.index.mapping.IndexConfigurationManager
import io.jmix.search.index.queue.IndexingQueueManager
import spock.lang.Specification

/**
 * How the two fields of a console operation turn into the set of indexes it works on.
 *
 * <p>Two things are pinned here. First, the four cuts: an empty field is the absence of a restriction, so the
 * four combinations of the two fields address the whole application, one entity, one tenant, or a single index.
 *
 * <p>Second, which operations demand that the named tenant exist. The flag is set per operation, and both ways of
 * getting it wrong are quiet: an operation that creates something would act on a misspelled tenant and leave real
 * indexes nothing will ever use, while an operation that cleans up would refuse to remove what a tenant left
 * behind after that tenant was removed.
 */
class ManagementScopeTest extends Specification {

    static final String ENTITY = "demo_Order"
    static final String TENANT = "acme"
    static final String GHOST = "ghost"

    def "the two fields address the whole application, an entity, a tenant, or a single index"() {
        given:
        def facade = facade()
        def allConfigurations = [Mock(IndexConfiguration), Mock(IndexConfiguration)]
        def orderConfiguration = Mock(IndexConfiguration)
        facade.indexConfigurationManager.getAllIndexConfigurations() >> allConfigurations
        facade.indexConfigurationManager.getIndexConfigurationByEntityName(ENTITY) >> orderConfiguration

        when:
        facade.validateIndexes(entityField, tenantField)

        then:
        1 * facade.indexManager.validateIndexes(
                expectedEntity == null ? allConfigurations : [orderConfiguration],
                expectedTenant) >> []

        where:
        entityField | tenantField || expectedEntity | expectedTenant
        ""          | ""          || null           | null
        ENTITY      | ""          || ENTITY         | null
        ""          | TENANT      || null           | TENANT
        ENTITY      | TENANT      || ENTITY         | TENANT
    }

    def "an entity that is not indexed is refused before anything is addressed"() {
        given:
        def facade = facade()
        facade.indexConfigurationManager.isDirectlyIndexed("demo_NotIndexed") >> false

        when:
        def result = facade.validateIndexes("demo_NotIndexed", "")

        then:
        result.contains("is not configured for indexing")
        0 * facade.indexManager._
    }

    def "an operation that creates something refuses a tenant that cannot exist"() {
        given:
        def facade = facade()

        when:
        def result = operation.call(facade)

        then: "a tenant id is typed by hand, and a typo would create indexes nothing will ever write to"
        result.contains("Tenant '$GHOST' does not exist")

        where:
        operation << [
                { it.initAsyncEnqueueing("", GHOST) },
                { it.resumeAsyncEnqueueing("", GHOST) },
                { it.enqueueNextBatch("", GHOST) },
                { it.synchronizeIndexSchemas("", GHOST) },
                { it.recreateIndexes("", GHOST) },
                { it.enqueueIndexAll("", GHOST) }
        ]
    }

    def "an operation that cleans up or reports accepts a tenant that is already gone"() {
        given: "stubs rather than mocks: these operations are allowed through, so they need an answer to return"
        def facade = facadeAnsweringWithNothing()

        when:
        def result = operation.call(facade)

        then: "clearing up after a removed tenant is exactly what these are for"
        !result.contains("does not exist")

        where:
        operation << [
                { it.suspendAsyncEnqueueing("", GHOST) },
                { it.terminateAsyncEnqueueing("", GHOST) },
                { it.validateIndexes("", GHOST) },
                { it.deleteIndexes("", GHOST) },
                { it.emptyIndexingQueue("", GHOST) }
        ]
    }

    /**
     * A facade whose collaborators answer with nothing at all. A Spock mock returns {@code null} for a list,
     * which the result formatter cannot take; a stub returns an empty one.
     */
    protected EntityIndexingManagementFacade facadeAnsweringWithNothing() {
        def facade = new EntityIndexingManagementFacade()
        facade.indexManager = Stub(IndexManager)
        facade.indexingQueueManager = Stub(IndexingQueueManager)
        facade.indexConfigurationManager = Stub(IndexConfigurationManager) {
            isDirectlyIndexed(ENTITY) >> true
            getAllIndexConfigurations() >> []
        }
        facade.multitenancyAdapter = Stub(MultitenancyAdapter) {
            getAvailableTenants() >> ([TENANT] as Set)
        }
        return facade
    }

    protected EntityIndexingManagementFacade facade() {
        def facade = new EntityIndexingManagementFacade()
        facade.indexManager = Mock(IndexManager)
        facade.indexingQueueManager = Mock(IndexingQueueManager)
        facade.indexConfigurationManager = Mock(IndexConfigurationManager) {
            isDirectlyIndexed(ENTITY) >> true
        }
        facade.multitenancyAdapter = Mock(MultitenancyAdapter) {
            getAvailableTenants() >> ([TENANT] as Set)
        }
        return facade
    }
}
