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

import io.jmix.core.Id
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
 * The common way to customize the queue is to subclass {@link JpaIndexingQueueManager} and override a method -
 * to log, to count, to filter. Adding a tenant overload must not take that away: when no tenant is named, the
 * overload with a tenant has to go through the one without it, so a subclass that overrode the latter keeps
 * being called.
 *
 * <p>The indexing path did that from the start. The deletion path did not, and since the change listener calls
 * the two-argument overload, an override of {@code enqueueDeleteByEntityId(Id)} simply stopped being invoked -
 * with nothing in the compiler output or in the subclass's own tests to show for it.
 */
class OverridePointsSurviveTheTenantOverloadTest extends Specification {

    def searchProperties = Stub(SearchProperties)
    def dataManager = Stub(UnconstrainedDataManager)
    def metadata = Stub(Metadata)
    def metadataTools = Stub(MetadataTools)
    def entityIndexer = Stub(EntityIndexer)
    def storeAwareLocator = Stub(StoreAwareLocator)
    def indexConfigurationManager = Stub(IndexConfigurationManager)
    def idSerialization = Stub(IdSerialization)
    def authenticator = Stub(SystemAuthenticator)
    def locker = Stub(IndexingLocker)
    def indexStateRegistry = Stub(IndexStateRegistry)
    def enqueueingSessionManager = Stub(EnqueueingSessionManager)
    def entityIdsLoaderProvider = Stub(EntityIdsLoaderProvider)
    def multitenancyAdapter = Stub(MultitenancyAdapter)
    def indexLayout = Stub(IndexLayout)

    /**
     * What an application writes when it wants to know about every record leaving the index.
     */
    static class CountingQueueManager extends JpaIndexingQueueManager {

        int deletionsSeen = 0
        int indexingsSeen = 0

        CountingQueueManager(Object... args) {
            super(*args)
        }

        @Override
        int enqueueDeleteByEntityId(Id<?> entityId) {
            deletionsSeen++
            return 0
        }

        @Override
        int enqueueIndexByEntityId(Id<?> entityId) {
            indexingsSeen++
            return 0
        }
    }

    def manager = new CountingQueueManager(
            searchProperties, dataManager, metadata, metadataTools, entityIndexer, storeAwareLocator,
            indexConfigurationManager, idSerialization, authenticator, locker, indexStateRegistry,
            enqueueingSessionManager, entityIdsLoaderProvider, multitenancyAdapter)

    def setup() {
        manager.indexLayout = indexLayout
    }

    def "a deletion with no tenant still goes through the override point"() {
        when: "the change listener enqueues a deletion of a record whose tenant it could not read"
        manager.enqueueDeleteByEntityId(Id.of(UUID.randomUUID(), Object), null)

        then:
        manager.deletionsSeen == 1
    }

    def "an indexing with no tenant does too"() {
        when:
        manager.enqueueIndexByEntityId(Id.of(UUID.randomUUID(), Object), null)

        then:
        manager.indexingsSeen == 1
    }
}
