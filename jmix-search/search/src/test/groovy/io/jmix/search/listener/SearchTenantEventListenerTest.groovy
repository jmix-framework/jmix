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

package io.jmix.search.listener

import io.jmix.multitenancy.event.TenantEvent
import io.jmix.search.SearchProperties
import io.jmix.search.index.IndexConfiguration
import io.jmix.search.index.mapping.IndexConfigurationManager
import io.jmix.search.index.IndexManager
import io.jmix.search.index.IndexSchemaManagementStrategy
import spock.lang.Specification

class SearchTenantEventListenerTest extends Specification {

    public static final String TENANT_ID = "tenant-01"

    def "created tenant is synchronized: the strategy is applied by the index manager, not by the listener"() {
        given:
        def searchProperties = Mock(SearchProperties)
        searchProperties.getIndexSchemaManagementStrategy() >> strategy
        def indexManager = Mock(IndexManager)
        def configurations = List.of(Mock(IndexConfiguration))
        def configurationManager = Mock(IndexConfigurationManager) {
            getAllIndexConfigurations() >> configurations
        }

        and:
        def listener = new SearchTenantEventListener(searchProperties, indexManager, configurationManager)
        def event = new TenantEvent(this, TenantEvent.Type.CREATED, TENANT_ID)

        when:
        listener.onTenantEventOccurred(event)

        then:
        1 * indexManager.synchronizeIndexSchemas(configurations, TENANT_ID)
        0 * indexManager.deleteIndexes(_, _)

        where:
        strategy << IndexSchemaManagementStrategy.values()
    }

    def "indexes of a deleted tenant are kept"() {
        given:
        def searchProperties = Mock(SearchProperties)
        def indexManager = Mock(IndexManager)
        def configurations = List.of(Mock(IndexConfiguration))
        def configurationManager = Mock(IndexConfigurationManager) {
            getAllIndexConfigurations() >> configurations
        }

        and:
        def listener = new SearchTenantEventListener(searchProperties, indexManager, configurationManager)
        def event = new TenantEvent(this, TenantEvent.Type.DELETED, TENANT_ID)

        when:
        listener.onTenantEventOccurred(event)

        then:
        0 * indexManager._
    }
}
