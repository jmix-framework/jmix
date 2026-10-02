/*
 * Copyright 2024 Haulmont.
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

import io.jmix.search.index.IndexManipulationResult
import io.jmix.search.SearchProperties
import io.jmix.search.index.IndexConfiguration
import io.jmix.search.index.IndexNameGenerator
import io.jmix.search.index.IndexOperationResult
import io.jmix.search.index.IndexRecreationStatus
import io.jmix.search.index.IndexSynchronizationStatus
import io.jmix.search.index.IndexValidationStatus
import io.jmix.search.index.mapping.IndexConfigurationManager
import io.jmix.search.index.queue.IndexingQueueManager
import io.jmix.search.index.mapping.IndexMappingConfiguration
import spock.lang.Specification

import static io.jmix.search.index.IndexSchemaManagementStrategy.*
import static io.jmix.search.index.IndexSynchronizationStatus.*

class BaseIndexManagerTest extends Specification {

    public static final String INDEX_NAME = "some_index_name"
    public static final String ENTITY_NAME = "SomeEntityName"

    def "synchronizeIndexSchemas. The giving configurations couldn't be null"() {
        given:
        BaseIndexManager indexManager = new BaseIndexManagerTestImpl(null, null, null, null, null, null)
        indexManager.indexLayout = Mock(IndexLayout)

        when:
        indexManager.synchronizeIndexSchemas(null, null)

        then:
        thrown(IllegalArgumentException)
    }

    def "if index is missing and the strategy allows to create index the attempt to create index should be performed"() {
        given:
        SearchProperties searchPropertiesMock = Mock()
        searchPropertiesMock.getIndexSchemaManagementStrategy() >> strategy

        and:
        IndexConfiguration indexConfigurationMock = Mock()
        indexConfigurationMock.getEntityName() >> ENTITY_NAME

        and:
        IndexStateRegistry indexStateRegistry = Mock()

        and:
        IndexLayout indexLayout = Mock()
        indexLayout.allIndexes(indexConfigurationMock) >> List.of(new IndexLayout.TenantIndex(null, INDEX_NAME))
        indexLayout.indexName(indexConfigurationMock, null) >> INDEX_NAME

        and:
        BaseIndexManager indexManager = new BaseIndexManagerTestImpl(null, indexStateRegistry, searchPropertiesMock, Mock(IndexNameGenerator), null, null)
        indexManager.indexLayout = indexLayout
        BaseIndexManager indexManagerSpy = Spy(indexManager)
        indexManagerSpy.isIndexExist(INDEX_NAME) >> false
        indexManagerSpy.createIndex(indexConfigurationMock, INDEX_NAME) >> creationResult

        when:
        List<IndexOperationResult<IndexSynchronizationStatus>> result = indexManagerSpy.synchronizeIndexSchemas(List.of(indexConfigurationMock), null)

        then:
        result == List.of(new IndexOperationResult<>(ENTITY_NAME, INDEX_NAME, null, resultStatus))
        markAsAvailableExecutes * indexStateRegistry.markIndexAsAvailable(INDEX_NAME)
        markAsUnavailableExecutes * indexStateRegistry.markIndexAsUnavailable(INDEX_NAME)

        where:
        strategy           | creationResult                  | resultStatus | markAsAvailableExecutes | markAsUnavailableExecutes
        NONE               | null                            | MISSING      | 0                       | 1
        CREATE_ONLY        | IndexManipulationResult.SUCCESS | CREATED      | 1                       | 0
        CREATE_ONLY        | IndexManipulationResult.FAILURE | MISSING      | 0                       | 1
        CREATE_OR_RECREATE | IndexManipulationResult.SUCCESS | CREATED      | 1                       | 0
        CREATE_OR_RECREATE | IndexManipulationResult.FAILURE | MISSING      | 0                       | 1
        CREATE_OR_UPDATE   | IndexManipulationResult.SUCCESS | CREATED      | 1                       | 0
        CREATE_OR_UPDATE   | IndexManipulationResult.FAILURE | MISSING      | 0                       | 1
    }

    def "an index another node has just created counts as created here too"() {
        given: """two nodes are told about a new tenant at the same moment and both go to create its indexes.
                  One wins; the engine answers the other with an error the client raises as a runtime exception"""
        SearchProperties searchPropertiesMock = Mock()
        searchPropertiesMock.getIndexSchemaManagementStrategy() >> CREATE_ONLY

        and:
        IndexConfiguration indexConfigurationMock = Mock()
        indexConfigurationMock.getEntityName() >> ENTITY_NAME

        and:
        IndexStateRegistry indexStateRegistry = Mock()

        and:
        IndexLayout indexLayout = Mock()
        indexLayout.allIndexes(indexConfigurationMock) >> List.of(new IndexLayout.TenantIndex(null, INDEX_NAME))
        indexLayout.indexName(indexConfigurationMock, null) >> INDEX_NAME

        and:
        BaseIndexManager indexManager = new BaseIndexManagerTestImpl(null, indexStateRegistry, searchPropertiesMock, Mock(IndexNameGenerator), null, null)
        indexManager.indexLayout = indexLayout
        BaseIndexManager indexManagerSpy = Spy(indexManager)
        indexManagerSpy.createIndex(indexConfigurationMock, INDEX_NAME) >> { throw creationOutcome }
        // asked twice: once to find the index missing, and once more after creation failed
        indexManagerSpy.isIndexExist(INDEX_NAME) >>> [false, existsAfterwards]

        when:
        List<IndexOperationResult<IndexSynchronizationStatus>> result =
                indexManagerSpy.synchronizeIndexSchemas(List.of(indexConfigurationMock), null)

        then: """the index is there, this node just did not put it there - leaving it unmarked would make the node
                 treat it as unavailable and stop indexing that tenant until a restart"""
        result == List.of(new IndexOperationResult<>(ENTITY_NAME, INDEX_NAME, null, expectedStatus))
        markAsAvailable * indexStateRegistry.markIndexAsAvailable(INDEX_NAME)
        markAsUnavailable * indexStateRegistry.markIndexAsUnavailable(INDEX_NAME)

        where:
        creationOutcome                            | existsAfterwards || expectedStatus | markAsAvailable | markAsUnavailable
        new RuntimeException("already exists")     | true             || CREATED        | 1               | 0
        new RuntimeException("mapping is rubbish") | false            || MISSING        | 0               | 1
    }

    def "index exists but index recreating required"() {
        given:
        SearchProperties searchPropertiesMock = Mock()
        searchPropertiesMock.getIndexSchemaManagementStrategy() >> strategy

        and:
        IndexConfiguration indexConfigurationMock = Mock()
        indexConfigurationMock.getEntityName() >> ENTITY_NAME

        and:
        IndexStateRegistry indexStateRegistry = Mock()

        and:
        ConfigurationComparingResult comparingResult = Mock()
        comparingResult.isIndexRecreatingRequired() >> true

        and:
        IndexConfigurationComparator configurationComparator = Mock()
        configurationComparator.compareConfigurations(indexConfigurationMock, INDEX_NAME) >> comparingResult

        and:
        IndexLayout indexLayout = Mock()
        indexLayout.allIndexes(indexConfigurationMock) >> List.of(new IndexLayout.TenantIndex(null, INDEX_NAME))
        indexLayout.indexName(indexConfigurationMock, null) >> INDEX_NAME

        and:
        BaseIndexManager indexManager = new BaseIndexManagerTestImpl(null, indexStateRegistry, searchPropertiesMock, Mock(IndexNameGenerator), configurationComparator, null)
        indexManager.indexLayout = indexLayout
        BaseIndexManager indexManagerSpy = Spy(indexManager)
        indexManagerSpy.isIndexExist(INDEX_NAME) >> true
        indexManagerSpy.dropIndex(INDEX_NAME) >> droppingResult
        indexManagerSpy.createIndex(indexConfigurationMock, INDEX_NAME) >> creatingResult

        when:
        List<IndexOperationResult<IndexSynchronizationStatus>> result = indexManagerSpy.synchronizeIndexSchemas(List.of(indexConfigurationMock), null)

        then:
        result == List.of(new IndexOperationResult<>(ENTITY_NAME, INDEX_NAME, null, resultStatus))
        markAsAvailableExecutes * indexStateRegistry.markIndexAsAvailable(INDEX_NAME)
        markAsUnavailableExecutes * indexStateRegistry.markIndexAsUnavailable(INDEX_NAME)

        where:
        strategy           | droppingResult | creatingResult                  | resultStatus | markAsAvailableExecutes | markAsUnavailableExecutes
        NONE               | null           | null                            | IRRELEVANT   | 0                       | 1
        CREATE_ONLY        | null           | null                            | IRRELEVANT   | 0                       | 1
        CREATE_OR_UPDATE   | null           | null                            | IRRELEVANT   | 0                       | 1
        CREATE_OR_RECREATE | true           | IndexManipulationResult.SUCCESS | RECREATED    | 1                       | 0
        CREATE_OR_RECREATE | true           | IndexManipulationResult.FAILURE | IRRELEVANT   | 0                       | 1
        CREATE_OR_RECREATE | false          | null                            | IRRELEVANT   | 0                       | 1
    }

    def "index exists but index update required"() {
        given:
        SearchProperties searchPropertiesMock = Mock()
        searchPropertiesMock.getIndexSchemaManagementStrategy() >> strategy

        and:
        IndexMappingConfiguration mappingConfiguration = Mock()
        IndexConfiguration indexConfigurationMock = Mock()
        indexConfigurationMock.getMapping() >> mappingConfiguration
        indexConfigurationMock.getEntityName() >> ENTITY_NAME

        and:
        IndexStateRegistry indexStateRegistry = Mock()

        and:
        ConfigurationComparingResult comparingResult = Mock()
        comparingResult.isIndexRecreatingRequired() >> false
        comparingResult.isConfigurationUpdateRequired() >> true
        comparingResult.isMappingUpdateRequired() >> isMappingUpdateRequired

        and:
        IndexConfigurationComparator configurationComparator = Mock()
        configurationComparator.compareConfigurations(indexConfigurationMock, INDEX_NAME) >> comparingResult

        and:
        IndexLayout indexLayout = Mock()
        indexLayout.allIndexes(indexConfigurationMock) >> List.of(new IndexLayout.TenantIndex(null, INDEX_NAME))
        indexLayout.indexName(indexConfigurationMock, null) >> INDEX_NAME

        and:
        BaseIndexManager indexManager = new BaseIndexManagerTestImpl(null, indexStateRegistry, searchPropertiesMock, Mock(IndexNameGenerator), configurationComparator, null)
        indexManager.indexLayout = indexLayout
        BaseIndexManager indexManagerSpy = Spy(indexManager)
        indexManagerSpy.isIndexExist(INDEX_NAME) >> true

        putMappingExecutes * indexManagerSpy.putMapping(INDEX_NAME, mappingConfiguration) >> putMappingResult

        when:
        List<IndexOperationResult<IndexSynchronizationStatus>> result = indexManagerSpy.synchronizeIndexSchemas(List.of(indexConfigurationMock), null)

        then:
        result == List.of(new IndexOperationResult<>(ENTITY_NAME, INDEX_NAME, null, resultStatus))
        markAsAvailableExecutes * indexStateRegistry.markIndexAsAvailable(INDEX_NAME)
        markAsUnavailableExecutes * indexStateRegistry.markIndexAsUnavailable(INDEX_NAME)

        where:
        strategy           | isMappingUpdateRequired | putMappingResult | resultStatus | putMappingExecutes | markAsAvailableExecutes | markAsUnavailableExecutes
        NONE               | null                    | null             | IRRELEVANT   | 0                  | 0                       | 1
        CREATE_ONLY        | null                    | null             | IRRELEVANT   | 0                  | 0                       | 1
        CREATE_OR_UPDATE   | true                    | true             | UPDATED      | 1                  | 1                       | 0
        CREATE_OR_UPDATE   | true                    | false            | IRRELEVANT   | 1                  | 0                       | 1
        CREATE_OR_RECREATE | true                    | true             | UPDATED      | 1                  | 1                       | 0
        CREATE_OR_RECREATE | true                    | false            | IRRELEVANT   | 1                  | 0                       | 1
//These cases are not supported yet. The exception trowing is checked in the next test.
//        CREATE_OR_UPDATE | false
//        CREATE_OR_RECREATE | false
    }

    def "synchronizeIndexSchema returns tenant specific results for tenant-aware configuration"() {
        given:
        SearchProperties searchPropertiesMock = Mock()
        searchPropertiesMock.getIndexSchemaManagementStrategy() >> NONE

        and:
        IndexConfiguration indexConfigurationMock = Mock()
        indexConfigurationMock.getEntityName() >> ENTITY_NAME
        indexConfigurationMock.isTenantAware() >> true

        and:
        IndexStateRegistry indexStateRegistry = Mock()

        and:
        IndexLayout indexLayout = Mock()
        indexLayout.isSplitByTenants(indexConfigurationMock) >> true
        indexLayout.allIndexes(indexConfigurationMock) >> List.of(
                new IndexLayout.TenantIndex("tenant1", "index_tenant_1"),
                new IndexLayout.TenantIndex("tenant2", "index_tenant_2"))

        and:
        BaseIndexManager indexManager = new BaseIndexManagerTestImpl(null, indexStateRegistry, searchPropertiesMock, Mock(IndexNameGenerator), null, null)
        indexManager.indexLayout = indexLayout
        BaseIndexManager indexManagerSpy = Spy(indexManager)
        indexManagerSpy.isIndexExist("index_tenant_1") >> false
        indexManagerSpy.isIndexExist("index_tenant_2") >> false

        when:
        List<IndexOperationResult<IndexSynchronizationStatus>> result = indexManagerSpy.synchronizeIndexSchemas(List.of(indexConfigurationMock), null)

        then:
        result == [
                new IndexOperationResult<>(ENTITY_NAME, "index_tenant_1", "tenant1", MISSING),
                new IndexOperationResult<>(ENTITY_NAME, "index_tenant_2", "tenant2", MISSING)
        ]
        0 * indexStateRegistry.markIndexAsAvailable(_)
        1 * indexStateRegistry.markIndexAsUnavailable("index_tenant_1")
        1 * indexStateRegistry.markIndexAsUnavailable("index_tenant_2")
    }

    def "validateIndexes returns tenant specific results for tenant-aware configuration"() {
        given:
        IndexConfiguration indexConfigurationMock = Mock()
        indexConfigurationMock.getEntityName() >> ENTITY_NAME
        indexConfigurationMock.isTenantAware() >> true

        and:
        IndexConfigurationComparator configurationComparator = Mock()
        configurationComparator.compareConfigurations(indexConfigurationMock, "index_tenant_1") >>
                new ConfigurationComparingResult(MappingComparingResult.EQUAL, SettingsComparingResult.EQUAL)
        configurationComparator.compareConfigurations(indexConfigurationMock, "index_tenant_2") >>
                new ConfigurationComparingResult(MappingComparingResult.NOT_COMPATIBLE, SettingsComparingResult.EQUAL)

        and:
        IndexStateRegistry indexStateRegistry = Mock()

        and:
        IndexLayout indexLayout = Mock()
        indexLayout.isSplitByTenants(indexConfigurationMock) >> true
        indexLayout.allIndexes(indexConfigurationMock) >> List.of(
                new IndexLayout.TenantIndex("tenant1", "index_tenant_1"),
                new IndexLayout.TenantIndex("tenant2", "index_tenant_2"))

        and:
        BaseIndexManager indexManager = new BaseIndexManagerTestImpl(null, indexStateRegistry, Mock(SearchProperties), Mock(IndexNameGenerator), configurationComparator, null)
        indexManager.indexLayout = indexLayout
        BaseIndexManager indexManagerSpy = Spy(indexManager)
        indexManagerSpy.isIndexExist("index_tenant_1") >> true
        indexManagerSpy.isIndexExist("index_tenant_2") >> true

        when:
        List<IndexOperationResult<IndexValidationStatus>> result = indexManagerSpy.validateIndexes(List.of(indexConfigurationMock), null)

        then:
        result == [
                new IndexOperationResult<>(ENTITY_NAME, "index_tenant_1", "tenant1", IndexValidationStatus.ACTUAL),
                new IndexOperationResult<>(ENTITY_NAME, "index_tenant_2", "tenant2", IndexValidationStatus.IRRELEVANT)
        ]
        1 * indexStateRegistry.markIndexAsAvailable("index_tenant_1")
        1 * indexStateRegistry.markIndexAsUnavailable("index_tenant_2")
    }

    def "recreateIndexes returns tenant specific results for tenant-aware configuration"() {
        given:
        IndexConfiguration indexConfigurationMock = Mock()
        indexConfigurationMock.getEntityName() >> ENTITY_NAME
        indexConfigurationMock.isTenantAware() >> true

        and:
        IndexLayout indexLayout = Mock()
        indexLayout.isSplitByTenants(indexConfigurationMock) >> true
        indexLayout.allIndexes(indexConfigurationMock) >> List.of(
                new IndexLayout.TenantIndex("tenant1", "index_tenant_1"),
                new IndexLayout.TenantIndex("tenant2", "index_tenant_2"))

        and:
        BaseIndexManager indexManager = new BaseIndexManagerTestImpl(null, Mock(IndexStateRegistry), Mock(SearchProperties), Mock(IndexNameGenerator), null, null)
        indexManager.indexLayout = indexLayout
        BaseIndexManager indexManagerSpy = Spy(indexManager)
        indexManagerSpy.dropIndex("index_tenant_1") >> true
        indexManagerSpy.dropIndex("index_tenant_2") >> true
        indexManagerSpy.createIndex(indexConfigurationMock, "index_tenant_1") >> IndexManipulationResult.SUCCESS
        indexManagerSpy.createIndex(indexConfigurationMock, "index_tenant_2") >> IndexManipulationResult.FAILURE

        when:
        List<IndexOperationResult<IndexRecreationStatus>> result = indexManagerSpy.recreateIndexes(List.of(indexConfigurationMock), null)

        then:
        result == [
                new IndexOperationResult<>(ENTITY_NAME, "index_tenant_1", "tenant1", IndexRecreationStatus.SUCCESS),
                new IndexOperationResult<>(ENTITY_NAME, "index_tenant_2", "tenant2", IndexRecreationStatus.PROBLEM_WITH_INDEX_CREATING)
        ]
    }

    def "synchronizeIndexSchemas for a tenant touches only the configurations split by tenants"() {
        given:
        SearchProperties searchPropertiesMock = Mock()
        searchPropertiesMock.getIndexSchemaManagementStrategy() >> CREATE_ONLY

        and:
        IndexConfiguration splitConfiguration = Mock()
        splitConfiguration.getEntityName() >> ENTITY_NAME
        IndexConfiguration sharedConfiguration = Mock()
        sharedConfiguration.getEntityName() >> "SharedEntity"

        and:
        IndexConfigurationManager configurationManager = Mock()
        configurationManager.getAllIndexConfigurations() >> List.of(splitConfiguration, sharedConfiguration)

        and:
        IndexLayout indexLayout = Mock()
        indexLayout.isSplitByTenants(splitConfiguration) >> true
        indexLayout.isSplitByTenants(sharedConfiguration) >> false
        indexLayout.indexName(splitConfiguration, "tenant1") >> "index_tenant_1"

        and:
        BaseIndexManager indexManager = new BaseIndexManagerTestImpl(
                configurationManager, Mock(IndexStateRegistry), searchPropertiesMock, Mock(IndexNameGenerator), null, null)
        indexManager.indexLayout = indexLayout
        BaseIndexManager indexManagerSpy = Spy(indexManager)
        indexManagerSpy.isIndexExist("index_tenant_1") >> false
        indexManagerSpy.createIndex(splitConfiguration, "index_tenant_1") >> IndexManipulationResult.SUCCESS

        when:
        def result = indexManagerSpy.synchronizeIndexSchemas(configurationManager.getAllIndexConfigurations(), "tenant1")

        then:
        result == List.of(new IndexOperationResult<>(ENTITY_NAME, "index_tenant_1", "tenant1", CREATED))
    }

    def "synchronizeIndexSchemas for a tenant follows the schema management strategy"() {
        given:
        SearchProperties searchPropertiesMock = Mock()
        searchPropertiesMock.getIndexSchemaManagementStrategy() >> strategy

        and:
        IndexConfiguration indexConfigurationMock = Mock()
        indexConfigurationMock.getEntityName() >> ENTITY_NAME

        and:
        IndexConfigurationManager configurationManager = Mock()
        configurationManager.getAllIndexConfigurations() >> List.of(indexConfigurationMock)

        and:
        IndexStateRegistry indexStateRegistry = Mock()

        and:
        IndexLayout indexLayout = Mock()
        indexLayout.isSplitByTenants(indexConfigurationMock) >> true
        indexLayout.indexName(indexConfigurationMock, "tenant1") >> "index_tenant_1"

        and:
        BaseIndexManager indexManager = new BaseIndexManagerTestImpl(
                configurationManager, indexStateRegistry, searchPropertiesMock, Mock(IndexNameGenerator), null, null)
        indexManager.indexLayout = indexLayout
        BaseIndexManager indexManagerSpy = Spy(indexManager)
        indexManagerSpy.isIndexExist("index_tenant_1") >> false
        indexManagerSpy.createIndex(indexConfigurationMock, "index_tenant_1") >> IndexManipulationResult.SUCCESS

        when:
        def result = indexManagerSpy.synchronizeIndexSchemas(configurationManager.getAllIndexConfigurations(), "tenant1")

        then:
        result == List.of(new IndexOperationResult<>(ENTITY_NAME, "index_tenant_1", "tenant1", expectedStatus))

        where:
        strategy           || expectedStatus
        NONE               || MISSING
        CREATE_ONLY        || CREATED
        CREATE_OR_UPDATE   || CREATED
        CREATE_OR_RECREATE || CREATED
    }

    def "synchronizeIndexSchemas for a tenant handles an index that already exists"() {
        given:
        SearchProperties searchPropertiesMock = Mock()
        searchPropertiesMock.getIndexSchemaManagementStrategy() >> CREATE_ONLY

        and:
        IndexConfiguration indexConfigurationMock = Mock()
        indexConfigurationMock.getEntityName() >> ENTITY_NAME

        and:
        IndexConfigurationManager configurationManager = Mock()
        configurationManager.getAllIndexConfigurations() >> List.of(indexConfigurationMock)

        and: "the index left from a previously deleted tenant matches the configuration"
        IndexConfigurationComparator configurationComparator = Mock()
        configurationComparator.compareConfigurations(indexConfigurationMock, "index_tenant_1") >>
                new ConfigurationComparingResult(MappingComparingResult.EQUAL, SettingsComparingResult.EQUAL)

        and:
        IndexStateRegistry indexStateRegistry = Mock()

        and:
        IndexLayout indexLayout = Mock()
        indexLayout.isSplitByTenants(indexConfigurationMock) >> true
        indexLayout.indexName(indexConfigurationMock, "tenant1") >> "index_tenant_1"

        and:
        BaseIndexManager indexManager = new BaseIndexManagerTestImpl(
                configurationManager, indexStateRegistry, searchPropertiesMock, Mock(IndexNameGenerator), configurationComparator, null)
        indexManager.indexLayout = indexLayout
        BaseIndexManager indexManagerSpy = Spy(indexManager)
        indexManagerSpy.isIndexExist("index_tenant_1") >> true

        when:
        def result = indexManagerSpy.synchronizeIndexSchemas(configurationManager.getAllIndexConfigurations(), "tenant1")

        then: "the existing index is accepted instead of being reported as a failed creation"
        result == List.of(new IndexOperationResult<>(ENTITY_NAME, "index_tenant_1", "tenant1", ACTUAL))
        1 * indexStateRegistry.markIndexAsAvailable("index_tenant_1")
    }

    def "settings update is not supported yet"() {
        given:
        SearchProperties searchPropertiesMock = Mock()
        searchPropertiesMock.getIndexSchemaManagementStrategy() >> strategy

        and:
        IndexMappingConfiguration mappingConfiguration = Mock()
        IndexConfiguration indexConfigurationMock = Mock()
        indexConfigurationMock.getMapping() >> mappingConfiguration
        indexConfigurationMock.getEntityName() >> ENTITY_NAME

        and:
        IndexStateRegistry indexStateRegistry = Mock()

        and:
        ConfigurationComparingResult comparingResult = Mock()
        comparingResult.isIndexRecreatingRequired() >> false
        comparingResult.isConfigurationUpdateRequired() >> true
        comparingResult.isMappingUpdateRequired() >> isMappingUpdateRequired

        and:
        IndexConfigurationComparator configurationComparator = Mock()
        configurationComparator.compareConfigurations(indexConfigurationMock, INDEX_NAME) >> comparingResult

        and:
        IndexLayout indexLayout = Mock()
        indexLayout.allIndexes(indexConfigurationMock) >> List.of(new IndexLayout.TenantIndex(null, INDEX_NAME))
        indexLayout.indexName(indexConfigurationMock, null) >> INDEX_NAME

        and:
        BaseIndexManager indexManager = new BaseIndexManagerTestImpl(null, indexStateRegistry, searchPropertiesMock, Mock(IndexNameGenerator), configurationComparator, null)
        indexManager.indexLayout = indexLayout
        BaseIndexManager indexManagerSpy = Spy(indexManager)
        indexManagerSpy.isIndexExist(INDEX_NAME) >> true

        when:
        indexManagerSpy.synchronizeIndexSchemas(List.of(indexConfigurationMock), null)

        then: "an operation that is impossible for every index is not reported per index"
        def exception = thrown(UnsupportedOperationException)
        exception.getMessage() == "An index settings update is not supported yet. Only index recreating is supported."

        where:
        strategy           | isMappingUpdateRequired
        CREATE_OR_UPDATE   | false
        CREATE_OR_RECREATE | false
    }

    def "getIndexValidationStatus work if index not exists"() {
        given:
        BaseIndexManager indexManager = new BaseIndexManagerTestImpl(
                Mock(IndexConfigurationManager),
                Mock(IndexStateRegistry),
                Mock(SearchProperties),
                Mock(IndexNameGenerator),
                Mock(IndexConfigurationComparator),
                Mock(IndexStateResolver))
        indexManager.indexLayout = Mock(IndexLayout)

        when:
        def validationStatus = indexManager.getIndexValidationStatus(
                Mock(IndexConfiguration),
                INDEX_NAME,
                false)

        then:
        validationStatus == IndexValidationStatus.MISSING
    }

    def "getIndexValidationStatus work if index exists"() {
        given:
        def indexConfigurationMock = Mock(IndexConfiguration)

        and:
        def indexConfigurationComparatorMock = Mock(IndexConfigurationComparator)
        indexConfigurationComparatorMock.compareConfigurations(indexConfigurationMock, INDEX_NAME)
                >> new ConfigurationComparingResult(mappingComparingResult, settingsComparingResult)

        and:
        BaseIndexManager indexManager = new BaseIndexManagerTestImpl(
                Mock(IndexConfigurationManager),
                Mock(IndexStateRegistry),
                Mock(SearchProperties),
                Mock(IndexNameGenerator),
                indexConfigurationComparatorMock,
                Mock(IndexStateResolver))
        indexManager.indexLayout = Mock(IndexLayout)

        when:
        def validationStatus = indexManager.getIndexValidationStatus(
                indexConfigurationMock,
                INDEX_NAME,
                true)

        then:
        validationStatus == expectedStatus

        where:
        settingsComparingResult                | mappingComparingResult                || expectedStatus
        SettingsComparingResult.EQUAL          | MappingComparingResult.EQUAL          || IndexValidationStatus.ACTUAL
        SettingsComparingResult.NOT_COMPATIBLE | MappingComparingResult.EQUAL          || IndexValidationStatus.IRRELEVANT
        SettingsComparingResult.EQUAL          | MappingComparingResult.UPDATABLE      || IndexValidationStatus.IRRELEVANT
        SettingsComparingResult.EQUAL          | MappingComparingResult.NOT_COMPATIBLE || IndexValidationStatus.IRRELEVANT
    }

    def "deleting indexes discards what the queue was holding for them"() {
        given:
        IndexConfiguration splitConfiguration = Mock()
        splitConfiguration.getEntityName() >> ENTITY_NAME
        IndexConfiguration sharedConfiguration = Mock()
        sharedConfiguration.getEntityName() >> "SharedEntity"

        and:
        IndexConfigurationManager configurationManager = Mock()
        configurationManager.getAllIndexConfigurations() >> List.of(splitConfiguration, sharedConfiguration)

        and:
        IndexLayout indexLayout = Mock()
        indexLayout.isSplitByTenants(splitConfiguration) >> true
        indexLayout.isSplitByTenants(sharedConfiguration) >> false
        indexLayout.indexName(splitConfiguration, "tenant1") >> "index_tenant_1"

        and:
        IndexingQueueManager queueManager = Mock()

        and:
        BaseIndexManager indexManager = new BaseIndexManagerTestImpl(
                configurationManager, Mock(IndexStateRegistry), Mock(SearchProperties),
                Mock(IndexNameGenerator), null, null)
        indexManager.indexLayout = indexLayout
        indexManager.indexingQueueManager = queueManager

        when:
        indexManager.deleteIndexes(configurationManager.getAllIndexConfigurations(), "tenant1")

        then: """the queue of the deleted index is emptied, and the entity shared by every tenant is left alone:
                 its index was not deleted, so its items are still processable"""
        1 * queueManager.emptyQueue(ENTITY_NAME, "tenant1")
        0 * queueManager.emptyQueue("SharedEntity", _)
    }

}
