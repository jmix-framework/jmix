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
import io.jmix.search.index.impl.IndexLayout
import io.jmix.search.index.impl.IndexingLocker
import io.jmix.search.index.mapping.IndexConfigurationManager
import io.jmix.search.index.queue.entity.EnqueueingSession
import spock.lang.Specification

import java.util.concurrent.TimeUnit

/**
 * Removing what an entity left behind after it stopped being indexed.
 *
 * <p>An entity drops out of the indexed set when its index definition is removed and the application is
 * redeployed. Its enqueueing session stays in the table, the queue writes a warning on every tick and says to
 * terminate the session, and terminating it is the only way the row can be got rid of. Every step of that
 * removal has to accept an entity that no longer has an index configuration: the lock it takes, the enumeration
 * of the indexes to report against, and the row that reports the outcome.
 */
class RetiredEntitySessionTest extends Specification {

    static final String RETIRED = "demo_Retired"

    def "the lock of an enqueueing session is taken for an entity that is no longer indexed"() {
        given:
        def indexConfigurationManager = Mock(IndexConfigurationManager) {
            isDirectlyIndexed(RETIRED) >> false
        }
        def locker = new IndexingLocker(indexConfigurationManager)

        when:
        def locked = locker.tryLockEnqueueingSession(RETIRED, null, 1, TimeUnit.SECONDS)

        then: "the caller decides whether the entity must be indexed; the lock registry does not"
        locked

        cleanup:
        locker.unlockEnqueueingSession(RETIRED, null)
    }

    def "a session of an entity that is no longer indexed is removed"() {
        given:
        def session = new EnqueueingSession(entityName: RETIRED)
        def manager = managerHolding(session)

        when:
        def results = manager.removeSession(RETIRED)

        then: "the row is gone"
        1 * manager.dataManager.remove(session)

        and: "its entity has no index definition left, so there is no index to name the outcome against"
        results.isEmpty()
    }

    def "a session named together with its tenant is removed for an entity that is no longer indexed"() {
        given:
        def session = new EnqueueingSession(entityName: RETIRED, tenantId: "acme")
        def manager = managerHolding(session)

        when:
        def results = manager.removeSession(RETIRED, "acme")

        then:
        1 * manager.dataManager.remove(session)
        results.isEmpty()
    }

    /**
     * A session manager whose entity has no index configuration, and whose table holds exactly the given session.
     */
    protected EnqueueingSessionManager managerHolding(EnqueueingSession session) {
        def manager = new EnqueueingSessionManager()
        manager.indexConfigurationManager = Mock(IndexConfigurationManager) {
            isDirectlyIndexed(RETIRED) >> false
            getIndexConfigurationByEntityNameOpt(RETIRED) >> Optional.empty()
            // What the real one does for an entity it has no configuration for. A mock that answered null
            // instead would let the asking code look harmless.
            getIndexConfigurationByEntityName(RETIRED) >> {
                throw new IllegalArgumentException("Entity '$RETIRED' is not configured for indexing")
            }
        }
        manager.indexLayout = Mock(IndexLayout)
        manager.locker = new IndexingLocker(manager.indexConfigurationManager)

        def loader = Mock(FluentLoader)
        def byQuery = Mock(FluentLoader.ByQuery)
        manager.dataManager = Mock(DataManager)
        manager.dataManager.load(EnqueueingSession) >> loader
        loader.query(*_) >> byQuery
        byQuery.optional() >> Optional.of(session)
        byQuery.list() >> [session]
        return manager
    }
}
