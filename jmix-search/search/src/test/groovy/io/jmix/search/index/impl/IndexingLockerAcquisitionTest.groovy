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

import io.jmix.search.index.mapping.IndexConfigurationManager
import spock.lang.Specification
import spock.lang.Timeout

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Taking and releasing the locks of an entity.
 *
 * <p>The maps hold no lock until one is asked for, so each of these starts from an empty map. A lock taken from
 * an empty map has to be the same object the next caller gets, or exclusion would be an illusion: two callers
 * would each hold a lock of their own and both believe they may proceed.
 *
 * <p>Exclusion is checked from another thread on purpose. {@code ReentrantLock} lets the thread that holds it
 * take it again, so a second {@code tryLock} on the test thread would succeed whatever the map contained.
 */
class IndexingLockerAcquisitionTest extends Specification {

    static final String ENTITY = "test_Order"
    static final String OTHER_ENTITY = "test_Customer"

    def locker = new IndexingLocker(Stub(IndexConfigurationManager) {
        isDirectlyIndexed(_) >> true
    })

    @Timeout(10)
    def "a bulk enqueueing lock is taken, holds against another thread, and is free again after release"() {
        expect: "nothing prepared the lock of this entity"
        locker.tryLockEntityForEnqueueIndexAll(ENTITY)

        when: "another thread asks for the lock of the same entity"
        def takenElsewhere = inAnotherThread { locker.tryLockEntityForEnqueueIndexAll(ENTITY) }

        then: "it is refused - both callers were handed the same lock"
        !takenElsewhere

        when:
        locker.unlockEntityForEnqueueIndexAll(ENTITY)

        then: "the release reached that same lock"
        inAnotherThread { locker.tryLockEntityForEnqueueIndexAll(ENTITY) }
    }

    @Timeout(10)
    def "the bulk enqueueing locks of two entities are independent"() {
        given:
        locker.tryLockEntityForEnqueueIndexAll(ENTITY)

        expect: "a lock is per entity, so work on one entity does not wait for work on another"
        inAnotherThread { locker.tryLockEntityForEnqueueIndexAll(OTHER_ENTITY) }
    }

    @Timeout(10)
    def "an enqueueing session lock is taken, holds against another thread, and is free again after release"() {
        expect:
        locker.tryLockEnqueueingSession(ENTITY)

        when:
        def takenElsewhere = inAnotherThread { locker.tryLockEnqueueingSession(ENTITY) }

        then:
        !takenElsewhere

        when:
        locker.unlockEnqueueingSession(ENTITY)

        then:
        inAnotherThread { locker.tryLockEnqueueingSession(ENTITY) }
    }

    protected boolean inAnotherThread(Closure<Boolean> action) {
        def executor = Executors.newSingleThreadExecutor()
        try {
            return executor.submit(action as java.util.concurrent.Callable<Boolean>).get(5, TimeUnit.SECONDS)
        } finally {
            executor.shutdownNow()
        }
    }
}
