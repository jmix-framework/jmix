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

package io.jmix.search.index.impl

import io.jmix.search.SearchProperties
import io.jmix.search.index.IndexOperationResult
import io.jmix.search.index.IndexManager
import io.jmix.search.index.queue.IndexingQueueManager
import spock.lang.Specification

import static io.jmix.search.index.IndexSynchronizationStatus.ACTUAL
import static io.jmix.search.index.IndexSynchronizationStatus.CREATED
import static io.jmix.search.index.IndexSynchronizationStatus.MISSING
import static io.jmix.search.index.IndexSynchronizationStatus.RECREATED

class StartupIndexSynchronizerTest extends Specification {

    def "nothing is synchronized when the add-on is disabled"() {
        given:
        IndexManager indexManager = Mock()
        IndexStateRegistry indexStateRegistry = Mock()

        and:
        SearchProperties searchProperties = Mock()
        searchProperties.isEnabled() >> false

        and:
        def synchronizer = createSynchronizer(indexManager, Mock(IndexingQueueManager))
        synchronizer.searchProperties = searchProperties

        when:
        synchronizer.synchronize()

        then:
        0 * indexManager._
        0 * indexStateRegistry._
    }

    def "an entity whose index has been created is enqueued for reindexing"() {
        given:
        IndexManager indexManager = Mock()
        indexManager.synchronizeIndexSchemas() >> [
                new IndexOperationResult<>("demo_Order", "index_order", null, CREATED),
                new IndexOperationResult<>("demo_Customer", "index_customer", null, ACTUAL)
        ]

        and:
        IndexingQueueManager indexingQueueManager = Mock()

        and:
        def synchronizer = createSynchronizer(indexManager, indexingQueueManager)

        when:
        synchronizer.synchronize()

        then:
        1 * indexingQueueManager.initAsyncEnqueueIndexAll("demo_Order")
        0 * indexingQueueManager.initAsyncEnqueueIndexAll("demo_Customer")
    }

    def "only the tenant whose index has been recreated is enqueued for reindexing"() {
        given: "one tenant index is recreated, the others are in place"
        IndexManager indexManager = Mock()
        indexManager.synchronizeIndexSchemas() >> [
                new IndexOperationResult<>("demo_Order", "index_tenant_1", "tenant1", ACTUAL),
                new IndexOperationResult<>("demo_Order", "index_tenant_2", "tenant2", RECREATED),
                new IndexOperationResult<>("demo_Order", "index_tenant_3", "tenant3", ACTUAL)
        ]

        and:
        IndexingQueueManager indexingQueueManager = Mock()

        and:
        def synchronizer = createSynchronizer(indexManager, indexingQueueManager)

        when:
        synchronizer.synchronize()

        then: "the data of the other tenants is not reread"
        1 * indexingQueueManager.initAsyncEnqueueIndexAll("demo_Order", "tenant2")
        0 * indexingQueueManager.initAsyncEnqueueIndexAll("demo_Order")
        0 * indexingQueueManager._
    }

    def "every recreated tenant index is enqueued on its own"() {
        given:
        IndexManager indexManager = Mock()
        indexManager.synchronizeIndexSchemas() >> [
                new IndexOperationResult<>("demo_Order", "index_tenant_1", "tenant1", CREATED),
                new IndexOperationResult<>("demo_Order", "index_tenant_2", "tenant2", RECREATED)
        ]

        and:
        IndexingQueueManager indexingQueueManager = Mock()

        and:
        def synchronizer = createSynchronizer(indexManager, indexingQueueManager)

        when:
        synchronizer.synchronize()

        then:
        1 * indexingQueueManager.initAsyncEnqueueIndexAll("demo_Order", "tenant1")
        1 * indexingQueueManager.initAsyncEnqueueIndexAll("demo_Order", "tenant2")
    }

    protected StartupIndexSynchronizer createSynchronizer(IndexManager indexManager,
                                                          IndexingQueueManager indexingQueueManager) {
        SearchProperties searchProperties = Mock()
        searchProperties.isEnabled() >> true
        searchProperties.isEnqueueIndexAllOnStartupIndexRecreationEnabled() >> true
        searchProperties.getEnqueueIndexAllOnStartupIndexRecreationEntities() >> List.of()

        def synchronizer = new StartupIndexSynchronizer()
        synchronizer.indexManager = indexManager
        synchronizer.indexingQueueManager = indexingQueueManager
        synchronizer.searchProperties = searchProperties
        return synchronizer
    }
}
