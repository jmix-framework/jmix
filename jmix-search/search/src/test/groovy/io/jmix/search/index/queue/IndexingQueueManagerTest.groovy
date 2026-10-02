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
import io.jmix.search.index.IndexConfiguration
import io.jmix.search.index.IndexManipulationResult
import io.jmix.search.index.IndexOperationResult
import io.jmix.core.metamodel.model.MetaClass
import io.jmix.search.index.impl.IndexLayout
import io.jmix.search.index.impl.IndexStateRegistry
import io.jmix.search.index.impl.IndexingLocker
import io.jmix.search.index.impl.MultitenancyAdapter
import io.jmix.search.index.mapping.IndexConfigurationManager
import io.jmix.search.index.queue.impl.EnqueueingSessionManager
import io.jmix.search.index.queue.impl.EntityIdsLoaderProvider
import io.jmix.search.index.queue.impl.JpaIndexingQueueManager
import io.jmix.search.index.queue.entity.EnqueueingSession
import spock.lang.Specification
import spock.lang.Unroll

class IndexingQueueManagerTest extends Specification {

    def searchProperties = Stub(SearchProperties) {
        getReindexEntityEnqueueBatchSize() >> 10
        getProcessQueueBatchSize() >> 10
    }
    def dataManager = Stub(UnconstrainedDataManager)
    def metadata = Stub(Metadata)
    def metadataTools = Stub(MetadataTools)
    def entityIndexer = Stub(EntityIndexer)
    def storeAwareLocator = Stub(StoreAwareLocator)
    def indexConfigurationManager = Mock(IndexConfigurationManager)
    def idSerialization = Stub(IdSerialization)
    def authenticator = Mock(SystemAuthenticator)
    def locker = Mock(IndexingLocker)
    def indexStateRegistry = Stub(IndexStateRegistry)
    def enqueueingSessionManager = Mock(EnqueueingSessionManager)
    def entityIdsLoaderProvider = Stub(EntityIdsLoaderProvider)
    def multitenancyAdapter = Mock(MultitenancyAdapter)
    def indexLayout = Mock(IndexLayout)

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

    /**
     * The indexes a configuration is mapped to, as the helper methods below declare them.
     */
    def indexesByConfiguration = [:]

    def setup() {
        manager.indexLayout = indexLayout
        indexLayout.allIndexes(_ as Collection) >> { args ->
            // A closure response whose only parameter is a Collection gets the whole argument list, not the
            // first argument, so the collection has to be taken out of it by hand.
            Collection<IndexConfiguration> configurations = args[0]
            configurations.collectEntries { [(it): indexesByConfiguration[it]] }
        }
    }

    def "the batch leaves out only the tenant whose index is unavailable"() {
        given: "two tenants of one entity, one of them without an index"
        def partial = splitConfiguration("test_Partial", ["tenant-a": "index_a", "tenant-b": "index_b"])
        indexConfigurationManager.getAllIndexConfigurations() >> [partial]
        indexStateRegistry.isIndexAvailable("index_a") >> false
        indexStateRegistry.isIndexAvailable("index_b") >> true
        metadata.getClass(_) >> Stub(MetaClass)

        when:
        def query = manager.createDequeueLoadContext(10).getQuery()

        then: """records of tenant-b keep flowing; taking the items of tenant-a would fill the batch with work
                 that cannot be done, and since the batch is the oldest items first, the same items would fill
                 every following batch and the queue would stop for everyone"""
        query.getParameters().values().containsAll(["test_Partial", "tenant-a"])
        !query.getParameters().values().contains("tenant-b")
        query.getQueryString().count("not (") == 2
    }

    def "items enqueued without a tenant are left out together with their entity"() {
        given: "the tenant of an item is unknown, so there is no telling which index it is addressed to"
        def partial = splitConfiguration("test_Partial", ["tenant-a": "index_a"])
        indexConfigurationManager.getAllIndexConfigurations() >> [partial]
        indexStateRegistry.isIndexAvailable("index_a") >> false
        metadata.getClass(_) >> Stub(MetaClass)

        when:
        def query = manager.createDequeueLoadContext(10).getQuery()

        then:
        query.getQueryString().contains("q.tenantId is null")
    }

    def "an entity that is not split by tenants is left out whole"() {
        given:
        def shared = configuration("test_Shared", ["shared_index"])
        indexConfigurationManager.getAllIndexConfigurations() >> [shared]
        indexStateRegistry.isIndexAvailable("shared_index") >> false
        metadata.getClass(_) >> Stub(MetaClass)

        when:
        def query = manager.createDequeueLoadContext(10).getQuery()

        then: "all of its items carry no tenant, so the single condition covers them"
        query.getQueryString().contains("q.tenantId is null")
        query.getParameters().values().contains("test_Shared")
    }

    def "a tenant-aware entity of an application without tenants has no index to write to"() {
        given:
        def withoutTenants = splitConfiguration("test_TenantAware", [:])
        indexConfigurationManager.getAllIndexConfigurations() >> [withoutTenants]
        metadata.getClass(_) >> Stub(MetaClass)

        when:
        def query = manager.createDequeueLoadContext(10).getQuery()

        then:
        query.getQueryString().contains("not (q.entityName = :e0)")
        query.getParameters().get("e0") == "test_TenantAware"
    }

    def "the dequeue query has no exclusion when every index is available"() {
        given:
        def healthy = configuration("test_Healthy", ["healthy_index"])
        indexConfigurationManager.getAllIndexConfigurations() >> [healthy]
        indexStateRegistry.isIndexAvailable("healthy_index") >> true
        metadata.getClass(_) >> Stub(MetaClass)

        when:
        def query = manager.createDequeueLoadContext(10).getQuery()

        then:
        !query.getQueryString().contains("not (")
        query.getParameters().isEmpty()
    }

    protected IndexConfiguration configuration(String entityName, List<String> indexNames) {
        def configuration = Stub(IndexConfiguration)
        configuration.getEntityName() >> entityName
        indexesByConfiguration[configuration] = indexNames.collect {
            new IndexLayout.TenantIndex(null, it)
        }
        indexLayout.isSplitByTenants(configuration) >> false
        return configuration
    }

    protected IndexConfiguration splitConfiguration(String entityName, Map<String, String> indexNamesByTenant) {
        def configuration = Stub(IndexConfiguration)
        configuration.getEntityName() >> entityName
        indexesByConfiguration[configuration] = indexNamesByTenant.collect { tenantId, indexName ->
            new IndexLayout.TenantIndex(tenantId, indexName)
        }
        indexLayout.isSplitByTenants(configuration) >> true
        return configuration
    }

    @Unroll
    def "tenant methods require multitenancy: #caseName"() {
        given:
        multitenancyAdapter.isMultitenancyActive() >> false

        when:
        action.call(manager)

        then:
        thrown(IllegalStateException)

        where:
        caseName << [
                "getEntityNamesOfEnqueueingSessions",
                "initAsyncEnqueueIndexAll with a tenant and no entity",
                "suspendAsyncEnqueueIndexAll with a tenant and no entity",
                "resumeAsyncEnqueueIndexAll with a tenant and no entity",
                "terminateAsyncEnqueueIndexAll with a tenant and no entity",
                "processNextEnqueueingSession(tenant)",
                "processNextEnqueueingSession(tenant, batch)",
                "initAsyncEnqueueIndexAll(entity, tenant)",
                "suspendAsyncEnqueueIndexAll(entity, tenant)",
                "resumeAsyncEnqueueIndexAll(entity, tenant)",
                "terminateAsyncEnqueueIndexAll(entity, tenant)",
                "processEnqueueingSession(entity, tenant)",
                "processEnqueueingSession(entity, tenant, batch)"
        ]
        action << [
                { m -> m.getEntityNamesOfEnqueueingSessions("tenant-1") },
                { m -> m.initAsyncEnqueueIndexAll(null, "tenant-1") },
                { m -> m.suspendAsyncEnqueueIndexAll(null, "tenant-1") },
                { m -> m.resumeAsyncEnqueueIndexAll(null, "tenant-1") },
                { m -> m.terminateAsyncEnqueueIndexAll(null, "tenant-1") },
                { m -> m.processNextEnqueueingSession("tenant-1") },
                { m -> m.processNextEnqueueingSession("tenant-1", 1) },
                { m -> m.initAsyncEnqueueIndexAll("test_Entity", "tenant-1") },
                { m -> m.suspendAsyncEnqueueIndexAll("test_Entity", "tenant-1") },
                { m -> m.resumeAsyncEnqueueIndexAll("test_Entity", "tenant-1") },
                { m -> m.terminateAsyncEnqueueIndexAll("test_Entity", "tenant-1") },
                { m -> m.processEnqueueingSession("test_Entity", "tenant-1") },
                { m -> m.processEnqueueingSession("test_Entity", "tenant-1", 1) }
        ]
    }

    def "getEntityNamesOfEnqueueingSessions delegates to session manager"() {
        given:
        multitenancyAdapter.isMultitenancyActive() >> true

        when:
        def result = manager.getEntityNamesOfEnqueueingSessions("tenant-1")

        then:
        1 * enqueueingSessionManager.loadEntityNamesOfSessions("tenant-1") >> ["test_Entity"]
        result == ["test_Entity"]
    }

    def "initAsyncEnqueueIndexAll with a tenant initializes sessions for tenant-aware entities only"() {
        given:
        multitenancyAdapter.isMultitenancyActive() >> true
        def tenantConfig = Mock(IndexConfiguration)
        tenantConfig.isTenantAware() >> true
        tenantConfig.getEntityName() >> "test_TenantEntity"
        def tenantlessConfig = Mock(IndexConfiguration)
        tenantlessConfig.isTenantAware() >> false
        tenantlessConfig.getEntityName() >> "test_TenantlessEntity"
        indexConfigurationManager.getAllIndexConfigurations() >> [tenantConfig, tenantlessConfig]

        when:
        def results = manager.initAsyncEnqueueIndexAll(null, "tenant-1")

        then:
        1 * enqueueingSessionManager.initSession("test_TenantEntity", "tenant-1") >>
                List.of(new IndexOperationResult<>("test_TenantEntity", "test_TenantEntity_tenant-1", null, IndexManipulationResult.SUCCESS))
        0 * enqueueingSessionManager.initSession("test_TenantlessEntity", "tenant-1")
        results.stream().map { it.entityName() }.toList() == ["test_TenantEntity"]
    }

    @Unroll
    def "#methodName delegates to session manager for tenant-aware entities"() {
        given:
        multitenancyAdapter.isMultitenancyActive() >> true
        def tenantConfig = Mock(IndexConfiguration)
        tenantConfig.isTenantAware() >> true
        tenantConfig.getEntityName() >> "test_TenantEntity"
        def tenantlessConfig = Mock(IndexConfiguration)
        tenantlessConfig.isTenantAware() >> false
        tenantlessConfig.getEntityName() >> "test_TenantlessEntity"
        indexConfigurationManager.getAllIndexConfigurations() >> [tenantConfig, tenantlessConfig]

        when:
        def results = action.call(manager)

        then:
        1 * enqueueingSessionManager."$sessionMethod"("test_TenantEntity", "tenant-1") >>
                List.of(new IndexOperationResult<>("test_TenantEntity", "test_TenantEntity_tenant-1", null, IndexManipulationResult.SUCCESS))
        0 * enqueueingSessionManager."$sessionMethod"("test_TenantlessEntity", "tenant-1")
        results.stream().map { it.entityName() }.toList() == ["test_TenantEntity"]

        where:
        methodName << [
                "suspendAsyncEnqueueIndexAll with a tenant and no entity",
                "resumeAsyncEnqueueIndexAll with a tenant and no entity",
                "terminateAsyncEnqueueIndexAll with a tenant and no entity"
        ]
        sessionMethod << [
                "suspendSession",
                "resumeSession",
                "removeSession"
        ]
        action << [
                { m -> m.suspendAsyncEnqueueIndexAll(null, "tenant-1") },
                { m -> m.resumeAsyncEnqueueIndexAll(null, "tenant-1") },
                { m -> m.terminateAsyncEnqueueIndexAll(null, "tenant-1") }
        ]
    }

    def "processNextEnqueueingSession for tenant returns zero when no sessions"() {
        given:
        multitenancyAdapter.isMultitenancyActive() >> true
        enqueueingSessionManager.getNextActiveSession("tenant-1") >> null

        when:
        def result = manager.processNextEnqueueingSession("tenant-1", 2)

        then:
        result == 0
        1 * authenticator.begin()
        1 * authenticator.end()
        0 * locker._
    }

    def "processEnqueueingSession for entity and tenant returns zero when session is missing"() {
        given:
        multitenancyAdapter.isMultitenancyActive() >> true
        enqueueingSessionManager.getSession("test_Entity", "tenant-1") >> null

        when:
        def result = manager.processEnqueueingSession("test_Entity", "tenant-1", 2)

        then:
        result == 0
        1 * authenticator.begin()
        1 * authenticator.end()
        0 * locker._
    }

    def "initAsyncEnqueueIndexAll for entity and tenant delegates to session manager"() {
        given:
        multitenancyAdapter.isMultitenancyActive() >> true

        when:
        def result = manager.initAsyncEnqueueIndexAll("test_Entity", "tenant-1")

        then:
        1 * enqueueingSessionManager.initSession("test_Entity", "tenant-1") >>
                List.of(new IndexOperationResult<>("test_Entity", "test_Entity_tenant-1", null, IndexManipulationResult.SUCCESS))
        result == List.of(new IndexOperationResult<>("test_Entity", "test_Entity_tenant-1", null, IndexManipulationResult.SUCCESS))
    }

    @Unroll
    def "#methodName delegates to session manager for entity and tenant"() {
        given:
        multitenancyAdapter.isMultitenancyActive() >> true

        when:
        def result = action.call(manager)

        then:
        1 * enqueueingSessionManager."$sessionMethod"("test_Entity", "tenant-1") >>
                List.of(new IndexOperationResult<>("test_Entity", "test_Entity_tenant-1", null, IndexManipulationResult.SUCCESS))
        result == List.of(new IndexOperationResult<>("test_Entity", "test_Entity_tenant-1", null, IndexManipulationResult.SUCCESS))

        where:
        methodName << [
                "suspendAsyncEnqueueIndexAll",
                "resumeAsyncEnqueueIndexAll",
                "terminateAsyncEnqueueIndexAll"
        ]
        sessionMethod << [
                "suspendSession",
                "resumeSession",
                "removeSession"
        ]
        action << [
                { m -> m.suspendAsyncEnqueueIndexAll("test_Entity", "tenant-1") },
                { m -> m.resumeAsyncEnqueueIndexAll("test_Entity", "tenant-1") },
                { m -> m.terminateAsyncEnqueueIndexAll("test_Entity", "tenant-1") }
        ]
    }

    def "processNextEnqueueingSession for tenant returns zero when lock is not acquired"() {
        given:
        multitenancyAdapter.isMultitenancyActive() >> true
        def session = Mock(EnqueueingSession) {
            getEntityName() >> "test_Entity"
        }
        enqueueingSessionManager.getNextActiveSession("tenant-1") >> session
        locker.tryLockEntityForEnqueueIndexAll("test_Entity") >> false

        when:
        def result = manager.processNextEnqueueingSession("tenant-1", 2)

        then:
        result == 0
        1 * authenticator.begin()
        1 * authenticator.end()
        0 * locker.unlockEntityForEnqueueIndexAll(_)
    }

    def "processEnqueueingSession for entity and tenant returns zero when lock is not acquired"() {
        given:
        multitenancyAdapter.isMultitenancyActive() >> true
        def session = Mock(EnqueueingSession) {
            getEntityName() >> "test_Entity"
        }
        enqueueingSessionManager.getSession("test_Entity", "tenant-1") >> session
        locker.tryLockEntityForEnqueueIndexAll("test_Entity") >> false

        when:
        def result = manager.processEnqueueingSession("test_Entity", "tenant-1", 2)

        then:
        result == 0
        1 * authenticator.begin()
        1 * authenticator.end()
        0 * locker.unlockEntityForEnqueueIndexAll(_)
    }
}
