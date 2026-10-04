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
import io.jmix.search.index.IndexConfiguration
import io.jmix.search.index.IndexRecreationStatus
import io.jmix.search.index.mapping.IndexConfigurationManager
import spock.lang.Specification

/**
 * What the console operation "recreate indexes" does when the index is not there.
 *
 * <p>This is the usual reason to run it: a tenant that has just appeared, an index someone removed by hand, an
 * engine started from scratch. Both engines answer a deletion of a missing index with an error, so recreating has
 * to look before it drops - otherwise the operation reports a failure and leaves the administrator with no index.
 */
class RecreateIndexTest extends Specification {

    static final String INDEX = "search_index_test_order"

    def configuration = Stub(IndexConfiguration) {
        getEntityName() >> "test_Order"
    }

    def "an index that is not there is created, not reported as a failure"() {
        given:
        def manager = Spy(manager())
        manager.isIndexExist(INDEX) >> false

        when:
        def status = manager.recreateIndex(configuration, INDEX)

        then: "a drop would come back as an error from the engine and sink the whole operation"
        0 * manager.dropIndex(_)
        status == IndexRecreationStatus.SUCCESS
    }

    def "an index that is there is dropped first"() {
        given:
        def manager = Spy(manager())
        manager.isIndexExist(INDEX) >> true
        manager.dropIndex(INDEX) >> true

        when:
        def status = manager.recreateIndex(configuration, INDEX)

        then:
        1 * manager.dropIndex(INDEX) >> true
        status == IndexRecreationStatus.SUCCESS
    }

    def "a drop that fails is reported as a drop problem, and nothing is created"() {
        given:
        def manager = Spy(manager())
        manager.isIndexExist(INDEX) >> true

        when:
        def status = manager.recreateIndex(configuration, INDEX)

        then:
        1 * manager.dropIndex(INDEX) >> false
        0 * manager.createIndex(_, _)
        status == IndexRecreationStatus.PROBLEM_WITH_INDEX_DELETING
    }

    protected BaseIndexManagerTestImpl manager() {
        return new BaseIndexManagerTestImpl(
                Stub(IndexConfigurationManager),
                new IndexStateRegistry(),
                Stub(SearchProperties),
                Stub(IndexConfigurationComparator),
                Stub(IndexStateResolver))
    }
}
