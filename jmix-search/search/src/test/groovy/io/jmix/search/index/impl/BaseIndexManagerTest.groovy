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

import io.jmix.search.SearchProperties
import io.jmix.search.index.IndexConfiguration
import io.jmix.search.index.IndexSynchronizationStatus
import io.jmix.search.index.mapping.IndexMappingConfiguration
import spock.lang.Specification

import static io.jmix.search.index.IndexSchemaManagementStrategy.*
import static io.jmix.search.index.IndexSynchronizationStatus.*

class BaseIndexManagerTest extends Specification {

    public static final String INDEX_NAME = "some_index_name"
    public static final String ENTITY_NAME = "SomeEntityName"

    def "synchronizeIndexSchema. The giving configuration couldn't be null"() {
        given:
        BaseIndexManager indexManager = new BaseIndexManagerTestImpl(null, null, null, null, null)

        when:
        indexManager.synchronizeIndexSchema(null)

        then:
        thrown(IllegalArgumentException)
    }

    def "if index is missing and the strategy allows to create index the attempt to create index should be performed"() {
        given:
        SearchProperties searchPropertiesMock = Mock()
        searchPropertiesMock.getIndexSchemaManagementStrategy() >> strategy

        and:
        IndexConfiguration indexConfigurationMock = Mock()
        indexConfigurationMock.getIndexName() >> INDEX_NAME
        indexConfigurationMock.getEntityName() >> ENTITY_NAME

        and:
        IndexStateRegistry indexStateRegistry = Mock()

        and:
        BaseIndexManager indexManager = new BaseIndexManagerTestImpl(null, indexStateRegistry, searchPropertiesMock, null, null)
        BaseIndexManager indexManagerSpy = Spy(indexManager)
        indexManagerSpy.isIndexExist(INDEX_NAME) >> false
        indexManagerSpy.createIndex(indexConfigurationMock) >> creationResult

        when:
        IndexSynchronizationStatus status = indexManagerSpy.synchronizeIndexSchema(indexConfigurationMock)

        then:
        status == resultStatus
        markAsAvailableExecutes * indexStateRegistry.markIndexAsAvailable(ENTITY_NAME)
        markAsUnavailableExecutes * indexStateRegistry.markIndexAsUnavailable(ENTITY_NAME)

        where:
        strategy           | creationResult | resultStatus | markAsAvailableExecutes | markAsUnavailableExecutes
        NONE               | null           | MISSING      | 0                       | 0
        CREATE_ONLY        | true           | CREATED      | 1                       | 0
        CREATE_ONLY        | false          | MISSING      | 0                       | 1
        CREATE_OR_RECREATE | true           | CREATED      | 1                       | 0
        CREATE_OR_RECREATE | false          | MISSING      | 0                       | 1
        CREATE_OR_UPDATE   | true           | CREATED      | 1                       | 0
        CREATE_OR_UPDATE   | false          | MISSING      | 0                       | 1
    }

    def "index exists but index recreating required"() {
        given:
        SearchProperties searchPropertiesMock = Mock()
        searchPropertiesMock.getIndexSchemaManagementStrategy() >> strategy

        and:
        IndexConfiguration indexConfigurationMock = Mock()
        indexConfigurationMock.getIndexName() >> INDEX_NAME
        indexConfigurationMock.getEntityName() >> ENTITY_NAME

        and:
        IndexStateRegistry indexStateRegistry = Mock()

        and:
        ConfigurationComparingResult comparingResult = Mock()
        comparingResult.isIndexRecreatingRequired() >> true

        and:
        IndexConfigurationComparator configurationComparator = Mock()
        configurationComparator.compareConfigurations(indexConfigurationMock) >> comparingResult

        and:
        BaseIndexManager indexManager = new BaseIndexManagerTestImpl(null, indexStateRegistry, searchPropertiesMock, configurationComparator, null)
        BaseIndexManager indexManagerSpy = Spy(indexManager)
        indexManagerSpy.isIndexExist(INDEX_NAME) >> true
        indexManagerSpy.dropIndex(INDEX_NAME) >> droppingResult
        indexManagerSpy.createIndex(indexConfigurationMock) >> creatingResult

        when:
        IndexSynchronizationStatus status = indexManagerSpy.synchronizeIndexSchema(indexConfigurationMock)

        then:
        status == resultStatus
        markAsAvailableExecutes * indexStateRegistry.markIndexAsAvailable(ENTITY_NAME)
        markAsUnavailableExecutes * indexStateRegistry.markIndexAsUnavailable(ENTITY_NAME)
        recreatingExecutes * indexManagerSpy.recreateIndex(indexConfigurationMock)

        where:
        strategy           | droppingResult | creatingResult | resultStatus | recreatingExecutes | markAsAvailableExecutes | markAsUnavailableExecutes
        NONE               | null           | null           | IRRELEVANT   | 0                  | 0                       | 1
        CREATE_ONLY        | null           | null           | IRRELEVANT   | 0                  | 0                       | 1
        CREATE_OR_UPDATE   | null           | null           | IRRELEVANT   | 0                  | 0                       | 1
        CREATE_OR_RECREATE | true           | true           | RECREATED    | 1                  | 1                       | 0
        CREATE_OR_RECREATE | true           | false          | IRRELEVANT   | 1                  | 0                       | 1
        CREATE_OR_RECREATE | false          | null           | IRRELEVANT   | 1                  | 0                       | 1
    }

    def "index exists but index update required"() {
        given:
        SearchProperties searchPropertiesMock = Mock()
        searchPropertiesMock.getIndexSchemaManagementStrategy() >> strategy

        and:
        IndexMappingConfiguration mappingConfiguration = Mock()
        IndexConfiguration indexConfigurationMock = Mock()
        indexConfigurationMock.getMapping() >> mappingConfiguration
        indexConfigurationMock.getIndexName() >> INDEX_NAME
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
        configurationComparator.compareConfigurations(indexConfigurationMock) >> comparingResult

        and:
        BaseIndexManager indexManager = new BaseIndexManagerTestImpl(null, indexStateRegistry, searchPropertiesMock, configurationComparator, null)
        BaseIndexManager indexManagerSpy = Spy(indexManager)
        indexManagerSpy.isIndexExist(INDEX_NAME) >> true

        indexManagerSpy.putMapping(INDEX_NAME, mappingConfiguration) >> putMappingResult

        when:
        IndexSynchronizationStatus status = indexManagerSpy.synchronizeIndexSchema(indexConfigurationMock)

        then:
        status == resultStatus
        markAsAvailableExecutes * indexStateRegistry.markIndexAsAvailable(ENTITY_NAME)
        markAsUnavailableExecutes * indexStateRegistry.markIndexAsUnavailable(ENTITY_NAME)

        where:
        strategy           | isMappingUpdateRequired | putMappingResult | resultStatus | markAsAvailableExecutes | markAsUnavailableExecutes
        NONE               | null                    | null             | IRRELEVANT   | 0                       | 1
        CREATE_ONLY        | null                    | null             | IRRELEVANT   | 0                       | 1
        CREATE_OR_UPDATE   | true                    | true             | UPDATED      | 1                       | 0
        CREATE_OR_UPDATE   | true                    | false            | IRRELEVANT   | 0                       | 1
        CREATE_OR_RECREATE | true                    | true             | UPDATED      | 1                       | 0
        CREATE_OR_RECREATE | true                    | false            | IRRELEVANT   | 0                       | 1
//These cases are not supported yet. The exception trowing is checked in the next test.
//        CREATE_OR_UPDATE | false
//        CREATE_OR_RECREATE | false
    }

    def "settings update don't supported"() {
        given:
        SearchProperties searchPropertiesMock = Mock()
        searchPropertiesMock.getIndexSchemaManagementStrategy() >> strategy

        and:
        IndexMappingConfiguration mappingConfiguration = Mock()
        IndexConfiguration indexConfigurationMock = Mock()
        indexConfigurationMock.getMapping() >> mappingConfiguration
        indexConfigurationMock.getIndexName() >> INDEX_NAME
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
        configurationComparator.compareConfigurations(indexConfigurationMock) >> comparingResult

        and:
        BaseIndexManager indexManager = new BaseIndexManagerTestImpl(null, indexStateRegistry, searchPropertiesMock, configurationComparator, null)
        BaseIndexManager indexManagerSpy = Spy(indexManager)
        indexManagerSpy.isIndexExist(INDEX_NAME) >> true

        when:
        indexManagerSpy.synchronizeIndexSchema(indexConfigurationMock)

        then:
        def exception = thrown(IllegalStateException)
        exception.getMessage() == "Only index mapping update is already supported."

        where:
        strategy           | isMappingUpdateRequired
        CREATE_OR_UPDATE   | false
        CREATE_OR_RECREATE | false
    }

}
