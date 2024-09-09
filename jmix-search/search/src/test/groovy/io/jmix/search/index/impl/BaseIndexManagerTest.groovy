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
import io.jmix.search.index.IndexSchemaManagementStrategy
import io.jmix.search.index.IndexSynchronizationStatus
import spock.lang.Specification

import static io.jmix.search.index.IndexSchemaManagementStrategy.CREATE_ONLY
import static io.jmix.search.index.IndexSchemaManagementStrategy.CREATE_OR_RECREATE
import static io.jmix.search.index.IndexSchemaManagementStrategy.CREATE_OR_UPDATE
import static io.jmix.search.index.IndexSynchronizationStatus.CREATED
import static io.jmix.search.index.IndexSynchronizationStatus.MISSING

class BaseIndexManagerTest extends Specification {

    public static final String INDEX_NAME = "some_index_name"
    public static final String ENTITY_NAME = "SomeEntityName"

    def "SynchronizeIndexSchema1"() {
        given:
        BaseIndexManager indexManager = new BaseIndexManagerTestImpl(null, null, null, null, null)

        when:
        indexManager.synchronizeIndexSchema(null)

        then:
        thrown(IllegalArgumentException)
    }

    def "if index is missing and the strategy doesn't allow to create index the index should be marked as MISSED"() {
        given:
        SearchProperties searchPropertiesMock = Mock()
        searchPropertiesMock.getIndexSchemaManagementStrategy() >> strategy

        and:
        IndexStateRegistry indexStateRegistry = Mock()

        and:
        BaseIndexManager indexManager = new BaseIndexManagerTestImpl(null, indexStateRegistry, searchPropertiesMock, null, null)
        BaseIndexManager indexManagerSpy = Spy(indexManager)
        indexManagerSpy.isIndexExist(INDEX_NAME) >> false


        and:
        IndexConfiguration indexConfigurationMock = Mock()
        indexConfigurationMock.getIndexName() >> INDEX_NAME
        indexConfigurationMock.getEntityName() >> ENTITY_NAME


        when:
        IndexSynchronizationStatus status = indexManagerSpy.synchronizeIndexSchema(indexConfigurationMock)

        then:
        status == resultStatus

        0 * indexStateRegistry.markIndexAsAvailable(ENTITY_NAME)
        0 * indexStateRegistry.markIndexAsUnavailable(ENTITY_NAME)

        where:
        strategy                           | resultStatus
        IndexSchemaManagementStrategy.NONE | MISSING
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
        CREATE_ONLY        | true           | CREATED      | 1                       | 0
        CREATE_ONLY        | false          | MISSING      | 0                       | 1
        CREATE_OR_RECREATE | true           | CREATED      | 1                       | 0
        CREATE_OR_RECREATE | false          | MISSING      | 0                       | 1
        CREATE_OR_UPDATE   | true           | CREATED      | 1                       | 0
        CREATE_OR_UPDATE   | false          | MISSING      | 0                       | 1
    }

}
