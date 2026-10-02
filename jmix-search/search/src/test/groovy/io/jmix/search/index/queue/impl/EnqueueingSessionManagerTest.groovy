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

package io.jmix.search.index.queue.impl

import io.jmix.core.DataManager
import io.jmix.core.FluentLoader
import io.jmix.core.FluentValuesLoader
import io.jmix.core.entity.KeyValueEntity
import io.jmix.search.index.IndexConfiguration
import io.jmix.search.index.IndexManipulationResult
import io.jmix.search.index.IndexOperationResult
import io.jmix.search.index.mapping.IndexConfigurationManager
import io.jmix.search.index.queue.entity.EnqueueingSession
import io.jmix.search.index.impl.IndexLayout
import io.jmix.search.index.impl.MultitenancyAdapter
import spock.lang.Specification

class EnqueueingSessionManagerTest extends Specification {

    def dataManager = Mock(DataManager)
    def indexConfigurationManager = Mock(IndexConfigurationManager)
    def multitenancyAdapter = Mock(MultitenancyAdapter)
    def indexLayout = Mock(IndexLayout)
    def indexConfig = Mock(IndexConfiguration)

    def manager = Spy(EnqueueingSessionManager)

    def setup() {
        manager.@dataManager = dataManager
        manager.@indexConfigurationManager = indexConfigurationManager
        manager.@multitenancyAdapter = multitenancyAdapter
        manager.@indexLayout = indexLayout
        indexConfigurationManager.getIndexConfigurationByEntityName("test_Entity") >> indexConfig
    }

    private static class TestableEnqueueingSessionManager extends EnqueueingSessionManager {
        Map<String, IndexManipulationResult> initSessionResults = [:]
        List<String> initSessionCalls = []
        Map<String, IndexManipulationResult> suspendSessionResults = [:]
        List<String> suspendSessionCalls = []
        Map<String, IndexManipulationResult> resumeSessionResults = [:]
        List<String> resumeSessionCalls = []
        Map<String, IndexManipulationResult> removeSessionResults = [:]
        List<String> removeSessionCalls = []

        @Override
        protected IndexManipulationResult initSessionInternal(String entityName, String tenantId) {
            initSessionCalls.add(tenantId)
            return initSessionResults.getOrDefault(tenantId, IndexManipulationResult.FAILURE)
        }

        @Override
        protected IndexManipulationResult suspendSessionInternal(String entityName, String tenantId) {
            suspendSessionCalls.add(tenantId)
            return suspendSessionResults.getOrDefault(tenantId, IndexManipulationResult.FAILURE)
        }

        @Override
        protected IndexManipulationResult resumeSessionInternal(String entityName, String tenantId) {
            resumeSessionCalls.add(tenantId)
            return resumeSessionResults.getOrDefault(tenantId, IndexManipulationResult.FAILURE)
        }

        @Override
        protected IndexManipulationResult removeSessionInternal(String entityName, String tenantId) {
            removeSessionCalls.add(tenantId)
            return removeSessionResults.getOrDefault(tenantId, IndexManipulationResult.FAILURE)
        }
    }

    def "a session that carries no tenant for a split entity produces no row"() {
        given: "a leftover of an application upgraded to split indexes, or of a tenant that has been removed"
        indexLayout.indexName(indexConfig, null) >> null

        when:
        def result = manager.toResult("test_Entity", null, IndexManipulationResult.SUCCESS)

        then: """the session is removed or suspended as asked either way; what cannot be produced is a row, and
                 a row naming an index that does not exist would be a worse report than none"""
        result.isEmpty()
    }

    def "a session that has an index produces one row naming it"() {
        given:
        indexLayout.indexName(indexConfig, "tenant-1") >> "idx_tenant1"

        when:
        def result = manager.toResult("test_Entity", "tenant-1", IndexManipulationResult.SUCCESS)

        then:
        result == [new IndexOperationResult<>("test_Entity", "idx_tenant1", "tenant-1",
                IndexManipulationResult.SUCCESS)]
    }

    def "getSession returns session when indexed and tenantId is null"() {
        given:
        def session = Mock(EnqueueingSession)
        indexConfigurationManager.isDirectlyIndexed("test_Entity") >> true
        manager.loadEnqueueingSessionEntityByEntityName("test_Entity", null) >> Optional.of(session)

        when:
        def result = manager.getSession("test_Entity", null)

        then:
        result == session
        0 * multitenancyAdapter._
    }

    def "getSession returns null when session is missing"() {
        given:
        indexConfigurationManager.isDirectlyIndexed("test_Entity") >> true
        manager.loadEnqueueingSessionEntityByEntityName("test_Entity", null) >> Optional.empty()

        when:
        def result = manager.getSession("test_Entity", null)

        then:
        result == null
    }

    def "getSession throws for non-indexed entity"() {
        given:
        indexConfigurationManager.isDirectlyIndexed("test_Entity") >> false

        when:
        manager.getSession("test_Entity", null)

        then:
        thrown(IllegalArgumentException)
    }

    def "getSession throws when the index of the entity is not split by tenants"() {
        given:
        indexConfigurationManager.isDirectlyIndexed("test_Entity") >> true
        indexLayout.isSplitByTenants(indexConfig) >> false

        when:
        manager.getSession("test_Entity", "tenant-1")

        then:
        thrown(IllegalArgumentException)
        0 * manager.loadEnqueueingSessionEntityByEntityName(_, _)
    }

    def "session management methods throw when the index of the entity is not split by tenants"() {
        given:
        indexLayout.isSplitByTenants(indexConfig) >> false

        when:
        def action = switch (methodName) {
            case "initSession" -> { manager.initSession("test_Entity", "tenant-1") }
            case "suspendSession" -> { manager.suspendSession("test_Entity", "tenant-1") }
            case "resumeSession" -> { manager.resumeSession("test_Entity", "tenant-1") }
            case "removeSession" -> { manager.removeSession("test_Entity", "tenant-1") }
            default -> { throw new IllegalArgumentException("Unsupported methodName: " + methodName) }
        }
        action.call()

        then:
        thrown(IllegalArgumentException)

        where:
        methodName << ["initSession", "suspendSession", "resumeSession", "removeSession"]
    }

    def "getSession throws when entity is not tenant-aware"() {
        given:
        indexConfigurationManager.isDirectlyIndexed("test_Entity") >> true
        indexLayout.isSplitByTenants(indexConfig) >> false

        when:
        manager.getSession("test_Entity", "tenant-1")

        then:
        thrown(IllegalArgumentException)
        0 * manager.loadEnqueueingSessionEntityByEntityName(_, _)
    }

    def "getSession loads tenant-specific session when entity is tenant-aware"() {
        given:
        def session = Mock(EnqueueingSession)
        indexConfigurationManager.isDirectlyIndexed("test_Entity") >> true
        indexLayout.isSplitByTenants(indexConfig) >> true
        manager.loadEnqueueingSessionEntityByEntityName("test_Entity", "tenant-1") >> Optional.of(session)

        when:
        def result = manager.getSession("test_Entity", "tenant-1")

        then:
        result == session
    }

    def "getSession without tenantId throws for tenant-aware entity"() {
        given:
        indexConfigurationManager.isDirectlyIndexed("test_Entity") >> true
        indexLayout.isSplitByTenants(indexConfig) >> true

        when:
        manager.getSession("test_Entity")

        then:
        thrown(IllegalArgumentException)
    }

    def "initSession without tenantId initializes all tenants for tenant-aware entity"() {
        given:
        def manager = new TestableEnqueueingSessionManager()
        manager.@multitenancyAdapter = multitenancyAdapter
        manager.@indexConfigurationManager = indexConfigurationManager
        manager.@indexLayout = indexLayout
        indexLayout.isSplitByTenants(indexConfig) >> true
        indexLayout.allIndexes(indexConfig) >> List.of(
                new IndexLayout.TenantIndex("tenant-1", "idx_tenant1"),
                new IndexLayout.TenantIndex("tenant-2", "idx_tenant2"))
        manager.initSessionResults["tenant-1"] = IndexManipulationResult.SUCCESS
        manager.initSessionResults["tenant-2"] = IndexManipulationResult.FAILURE

        when:
        def result = manager.initSession("test_Entity")

        then:
        manager.initSessionCalls as Set == ["tenant-1", "tenant-2"] as Set
        result.toList().toSet() == [
                new IndexOperationResult<>("test_Entity", "idx_tenant1", "tenant-1", IndexManipulationResult.SUCCESS),
                new IndexOperationResult<>("test_Entity", "idx_tenant2", "tenant-2", IndexManipulationResult.FAILURE)
        ].toSet()
    }

    def "initSession without tenantId initializes tenantless session when entity is tenantless"() {
        given:
        def manager = new TestableEnqueueingSessionManager()
        manager.@multitenancyAdapter = multitenancyAdapter
        manager.@indexConfigurationManager = indexConfigurationManager
        manager.@indexLayout = indexLayout
        indexLayout.isSplitByTenants(indexConfig) >> false
        indexLayout.allIndexes(indexConfig) >> List.of(new IndexLayout.TenantIndex(null, "idx_test_entity"))
        indexLayout.indexName(indexConfig, null) >> "idx_test_entity"
        manager.initSessionResults[null] = IndexManipulationResult.SUCCESS

        when:
        def result = manager.initSession("test_Entity")

        then:
        manager.initSessionCalls == [null]
        result == List.of(new IndexOperationResult<>("test_Entity", "idx_test_entity", null, IndexManipulationResult.SUCCESS))
    }

    def "suspendSession without tenantId suspends all tenants for tenant-aware entity"() {
        given:
        def manager = new TestableEnqueueingSessionManager()
        manager.@multitenancyAdapter = multitenancyAdapter
        manager.@indexConfigurationManager = indexConfigurationManager
        manager.@indexLayout = indexLayout
        indexLayout.isSplitByTenants(indexConfig) >> true
        indexLayout.allIndexes(indexConfig) >> List.of(
                new IndexLayout.TenantIndex("tenant-1", "idx_tenant1"),
                new IndexLayout.TenantIndex("tenant-2", "idx_tenant2"))
        manager.suspendSessionResults["tenant-1"] = IndexManipulationResult.SUCCESS
        manager.suspendSessionResults["tenant-2"] = IndexManipulationResult.FAILURE

        when:
        def result = manager.suspendSession("test_Entity")

        then:
        manager.suspendSessionCalls as Set == ["tenant-1", "tenant-2"] as Set
        result.toList().toSet() == [
                new IndexOperationResult<>("test_Entity", "idx_tenant1", "tenant-1", IndexManipulationResult.SUCCESS),
                new IndexOperationResult<>("test_Entity", "idx_tenant2", "tenant-2", IndexManipulationResult.FAILURE)
        ].toSet()
    }

    def "suspendSession without tenantId suspends tenantless session when entity is tenantless"() {
        given:
        def manager = new TestableEnqueueingSessionManager()
        manager.@multitenancyAdapter = multitenancyAdapter
        manager.@indexConfigurationManager = indexConfigurationManager
        manager.@indexLayout = indexLayout
        indexLayout.isSplitByTenants(indexConfig) >> false
        indexLayout.allIndexes(indexConfig) >> List.of(new IndexLayout.TenantIndex(null, "idx_test_entity"))
        indexLayout.indexName(indexConfig, null) >> "idx_test_entity"
        manager.suspendSessionResults[null] = IndexManipulationResult.SUCCESS

        when:
        def result = manager.suspendSession("test_Entity")

        then:
        manager.suspendSessionCalls == [null]
        result == List.of(new IndexOperationResult<>("test_Entity", "idx_test_entity", null, IndexManipulationResult.SUCCESS))
    }

    def "resumeSession without tenantId resumes all tenants for tenant-aware entity"() {
        given:
        def manager = new TestableEnqueueingSessionManager()
        manager.@multitenancyAdapter = multitenancyAdapter
        manager.@indexConfigurationManager = indexConfigurationManager
        manager.@indexLayout = indexLayout
        indexLayout.isSplitByTenants(indexConfig) >> true
        indexLayout.allIndexes(indexConfig) >> List.of(
                new IndexLayout.TenantIndex("tenant-1", "idx_tenant1"),
                new IndexLayout.TenantIndex("tenant-2", "idx_tenant2"))
        manager.resumeSessionResults["tenant-1"] = IndexManipulationResult.SUCCESS
        manager.resumeSessionResults["tenant-2"] = IndexManipulationResult.FAILURE

        when:
        def result = manager.resumeSession("test_Entity")

        then:
        manager.resumeSessionCalls as Set == ["tenant-1", "tenant-2"] as Set
        result.toList().toSet() == [
                new IndexOperationResult<>("test_Entity", "idx_tenant1", "tenant-1", IndexManipulationResult.SUCCESS),
                new IndexOperationResult<>("test_Entity", "idx_tenant2", "tenant-2", IndexManipulationResult.FAILURE)
        ].toSet()
    }

    def "resumeSession without tenantId resumes tenantless session when entity is tenantless"() {
        given:
        def manager = new TestableEnqueueingSessionManager()
        manager.@multitenancyAdapter = multitenancyAdapter
        manager.@indexConfigurationManager = indexConfigurationManager
        manager.@indexLayout = indexLayout
        indexLayout.isSplitByTenants(indexConfig) >> false
        indexLayout.allIndexes(indexConfig) >> List.of(new IndexLayout.TenantIndex(null, "idx_test_entity"))
        indexLayout.indexName(indexConfig, null) >> "idx_test_entity"
        manager.resumeSessionResults[null] = IndexManipulationResult.SUCCESS

        when:
        def result = manager.resumeSession("test_Entity")

        then:
        manager.resumeSessionCalls == [null]
        result == List.of(new IndexOperationResult<>("test_Entity", "idx_test_entity", null, IndexManipulationResult.SUCCESS))
    }

    def "removeSession without tenantId removes all tenants for tenant-aware entity"() {
        given:
        def manager = new TestableEnqueueingSessionManager()
        manager.@multitenancyAdapter = multitenancyAdapter
        manager.@indexConfigurationManager = indexConfigurationManager
        manager.@indexLayout = indexLayout
        indexLayout.isSplitByTenants(indexConfig) >> true
        indexLayout.allIndexes(indexConfig) >> List.of(
                new IndexLayout.TenantIndex("tenant-1", "idx_tenant1"),
                new IndexLayout.TenantIndex("tenant-2", "idx_tenant2"))
        manager.removeSessionResults["tenant-1"] = IndexManipulationResult.SUCCESS
        manager.removeSessionResults["tenant-2"] = IndexManipulationResult.FAILURE

        when:
        def result = manager.removeSession("test_Entity")

        then:
        manager.removeSessionCalls as Set == ["tenant-1", "tenant-2"] as Set
        result.toList().toSet() == [
                new IndexOperationResult<>("test_Entity", "idx_tenant1", "tenant-1", IndexManipulationResult.SUCCESS),
                new IndexOperationResult<>("test_Entity", "idx_tenant2", "tenant-2", IndexManipulationResult.FAILURE)
        ].toSet()
    }

    def "removeSession without tenantId removes tenantless session when entity is tenantless"() {
        given:
        def manager = new TestableEnqueueingSessionManager()
        manager.@multitenancyAdapter = multitenancyAdapter
        manager.@indexConfigurationManager = indexConfigurationManager
        manager.@indexLayout = indexLayout
        indexLayout.isSplitByTenants(indexConfig) >> false
        indexLayout.allIndexes(indexConfig) >> List.of(new IndexLayout.TenantIndex(null, "idx_test_entity"))
        indexLayout.indexName(indexConfig, null) >> "idx_test_entity"
        manager.removeSessionResults[null] = IndexManipulationResult.SUCCESS

        when:
        def result = manager.removeSession("test_Entity")

        then:
        manager.removeSessionCalls == [null]
        result == List.of(new IndexOperationResult<>("test_Entity", "idx_test_entity", null, IndexManipulationResult.SUCCESS))
    }

    def "loadEntityNamesOfSessions returns entity names for all sessions"() {
        given:
        def valuesLoader = Mock(FluentValuesLoader)
        def first = Mock(KeyValueEntity)
        def second = Mock(KeyValueEntity)
        dataManager.loadValues("select distinct e.entityName from search_EnqueueingSession e") >> valuesLoader
        valuesLoader.properties("entityName") >> valuesLoader
        valuesLoader.list() >> [first, second]
        first.getValue("entityName") >> "test_Entity"
        second.getValue("entityName") >> "test_Entity2"

        when:
        def result = manager.loadEntityNamesOfSessions()

        then:
        result == ["test_Entity", "test_Entity2"]
    }

    def "loadEntityNamesOfSessions with null tenantId returns only tenantless sessions"() {
        given:
        def valuesLoader = Mock(FluentValuesLoader)
        def first = Mock(KeyValueEntity)
        dataManager.loadValues("select distinct e.entityName from search_EnqueueingSession e where e.tenantId is null") >> valuesLoader
        valuesLoader.properties("entityName") >> valuesLoader
        valuesLoader.list() >> [first]
        first.getValue("entityName") >> "test_Entity"

        when:
        def result = manager.loadEntityNamesOfSessions(null)

        then:
        result == ["test_Entity"]
    }

    def "loadEntityNamesOfSessions with tenantId returns tenant sessions"() {
        given:
        def valuesLoader = Mock(FluentValuesLoader)
        def first = Mock(KeyValueEntity)
        dataManager.loadValues("select distinct e.entityName from search_EnqueueingSession e where e.tenantId = :tenantId") >> valuesLoader
        valuesLoader.properties("entityName") >> valuesLoader
        valuesLoader.list() >> [first]
        first.getValue("entityName") >> "test_Entity"

        when:
        def result = manager.loadEntityNamesOfSessions("tenant-1")

        then:
        1 * valuesLoader.parameter("tenantId", "tenant-1")
        result == ["test_Entity"]
    }

    def "getNextActiveSession without tenantId loads next session across all tenants"() {
        given:
        def session = Mock(EnqueueingSession)
        def loader = Mock(FluentLoader)
        def loader2 = Mock(FluentLoader.ByQuery)
        dataManager.load(EnqueueingSession) >> loader
        loader.query("WHERE e.status = :status ORDER BY e.createdDate ASC") >> loader2
        loader2.parameter("status", EnqueueingSessionStatus.ACTIVE) >> loader2
        loader2.optional() >> Optional.of(session)

        when:
        def result = manager.getNextActiveSession()

        then:
        result == session
    }
}
