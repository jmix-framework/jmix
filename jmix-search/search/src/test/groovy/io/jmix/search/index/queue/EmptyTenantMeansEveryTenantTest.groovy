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

package io.jmix.search.index.queue

import io.jmix.core.IdSerialization
import io.jmix.core.Metadata
import io.jmix.core.MetadataTools
import io.jmix.core.UnconstrainedDataManager
import io.jmix.core.security.SystemAuthenticator
import io.jmix.data.StoreAwareLocator
import io.jmix.search.SearchProperties
import io.jmix.search.index.EntityIndexer
import io.jmix.search.index.impl.IndexLayout
import io.jmix.search.index.impl.IndexStateRegistry
import io.jmix.search.index.impl.IndexingLocker
import io.jmix.search.index.impl.MultitenancyAdapter
import io.jmix.search.index.mapping.IndexConfigurationManager
import io.jmix.search.index.queue.impl.EnqueueingSessionManager
import io.jmix.search.index.queue.impl.EntityIdsLoaderProvider
import io.jmix.search.index.queue.impl.JpaIndexingQueueManager
import spock.lang.Specification

/**
 * In the scope of an operation a tenant of {@code null} means "every tenant", never "the rows that have no
 * tenant". The second meaning is not needed: an entity that is not split by tenants has a single index, which is
 * what "every tenant" resolves to anyway, and an entity that is split keeps no index for the records that have no
 * tenant - see decision 0013.
 *
 * <p>These tests pin the resolution, not the plumbing: each one asserts which session-manager call the operation
 * ends up in, because that is where the two meanings diverge.
 */
class EmptyTenantMeansEveryTenantTest extends Specification {

    static final String ENTITY = "test_Order"

    def searchProperties = Stub(SearchProperties) {
        getReindexEntityEnqueueBatchSize() >> 10
        getProcessQueueBatchSize() >> 10
    }
    def dataManager = Stub(UnconstrainedDataManager)
    def metadata = Stub(Metadata)
    def metadataTools = Stub(MetadataTools)
    def entityIndexer = Stub(EntityIndexer)
    def storeAwareLocator = Stub(StoreAwareLocator)
    def indexConfigurationManager = Stub(IndexConfigurationManager)
    def idSerialization = Stub(IdSerialization)
    def authenticator = Mock(SystemAuthenticator)
    def locker = Mock(IndexingLocker)
    def indexStateRegistry = Stub(IndexStateRegistry)
    def enqueueingSessionManager = Mock(EnqueueingSessionManager)
    def entityIdsLoaderProvider = Stub(EntityIdsLoaderProvider)
    def indexLayout = Mock(IndexLayout)

    /** No add-on: every operation below must still work, exactly as it did before tenants existed. */
    def multitenancyAdapter = Mock(MultitenancyAdapter) {
        isMultitenancyActive() >> false
    }

    def manager = new JpaIndexingQueueManager(
            searchProperties,
            dataManager,
            metadata,
            metadataTools,
            entityIndexer,
            storeAwareLocator,
            indexConfigurationManager,
            idSerialization,
            authenticator,
            locker,
            indexStateRegistry,
            enqueueingSessionManager,
            entityIdsLoaderProvider,
            multitenancyAdapter
    )

    def setup() {
        manager.indexLayout = indexLayout
    }

    def "a session operation without a tenant covers every tenant of the entity"() {
        when:
        operation.call(manager)

        then: "the one-argument path spreads over every index of the entity; the addressed one would open a single"
        1 * enqueueingSessionManager."$expectedCall"(ENTITY)
        0 * enqueueingSessionManager."$expectedCall"(ENTITY, _)

        where:
        expectedCall     | operation
        "initSession"    | { it.initAsyncEnqueueIndexAll(ENTITY, null) }
        "suspendSession" | { it.suspendAsyncEnqueueIndexAll(ENTITY, null) }
        "resumeSession"  | { it.resumeAsyncEnqueueIndexAll(ENTITY, null) }
        "removeSession"  | { it.terminateAsyncEnqueueIndexAll(ENTITY, null) }
    }

    def "taking the next batch without a tenant takes the next session of any tenant"() {
        when:
        manager.processNextEnqueueingSession((String) null)

        then: "asking for the session that belongs to no tenant would walk past every session a tenant has"
        1 * enqueueingSessionManager.getNextActiveSession()
        0 * enqueueingSessionManager.getNextActiveSession(_)
    }

    def "taking the next batch of an entity without a tenant takes that entity's session of any tenant"() {
        when:
        manager.processEnqueueingSession(ENTITY, (String) null)

        then:
        1 * enqueueingSessionManager.getNextActiveSessionOfEntity(ENTITY)
        0 * enqueueingSessionManager.getSession(ENTITY, _)
    }

    def "the old single-argument operation resolves the same way"() {
        when: "the operation that existed before tenants did"
        manager.processEnqueueingSession(ENTITY)

        then: "it finds the session of a split entity, which belongs to a tenant, instead of reporting none"
        1 * enqueueingSessionManager.getNextActiveSessionOfEntity(ENTITY)
    }

    def "listing the entities of sessions without a tenant lists them all"() {
        when:
        manager.getEntityNamesOfEnqueueingSessions((String) null)

        then:
        1 * enqueueingSessionManager.loadEntityNamesOfSessions()
        0 * enqueueingSessionManager.loadEntityNamesOfSessions(_)
    }

    def "none of it asks for the add-on, so an application without multitenancy is not cut off"() {
        when:
        manager.initAsyncEnqueueIndexAll(ENTITY, null)
        manager.suspendAsyncEnqueueIndexAll(ENTITY, null)
        manager.resumeAsyncEnqueueIndexAll(ENTITY, null)
        manager.terminateAsyncEnqueueIndexAll(ENTITY, null)
        manager.processNextEnqueueingSession((String) null)
        manager.processEnqueueingSession(ENTITY, (String) null)
        manager.getEntityNamesOfEnqueueingSessions((String) null)

        then: "the add-on is only required when a tenant is actually named"
        noExceptionThrown()
        0 * multitenancyAdapter.isMultitenancyActive()
    }

    def "a named tenant still requires the add-on"() {
        when:
        manager.processNextEnqueueingSession("acme")

        then:
        1 * multitenancyAdapter.isMultitenancyActive() >> false
        thrown(IllegalStateException)
    }
}
