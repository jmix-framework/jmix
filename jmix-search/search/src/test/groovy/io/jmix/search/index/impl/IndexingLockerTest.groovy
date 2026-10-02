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

import java.util.concurrent.CompletableFuture

/**
 * An enqueueing session belongs to one entity of one tenant, so the lock that guards it is keyed by the same pair.
 */
class IndexingLockerTest extends Specification {

    static final String ENTITY = "demo_Order"

    def indexConfigurationManager = Stub(IndexConfigurationManager) {
        isDirectlyIndexed(_) >> true
        getAllIndexedEntities() >> [ENTITY]
    }

    def locker = new IndexingLocker(indexConfigurationManager)

    def "sessions of different tenants of one entity do not exclude each other"() {
        when:
        def first = locker.tryLockEnqueueingSession(ENTITY, "tenant-a")

        then:
        first
        lockedInAnotherThread(ENTITY, "tenant-b")
    }

    def "a session is locked against itself"() {
        when:
        locker.tryLockEnqueueingSession(ENTITY, "tenant-a")

        then:
        !lockedInAnotherThread(ENTITY, "tenant-a")
    }

    def "a tenantless session and a tenant session are independent"() {
        when:
        locker.tryLockEnqueueingSession(ENTITY, null)

        then:
        lockedInAnotherThread(ENTITY, "tenant-a")
    }

    def "the same entity of two tenants is unlocked independently"() {
        given:
        locker.tryLockEnqueueingSession(ENTITY, "tenant-a")
        locker.tryLockEnqueueingSession(ENTITY, "tenant-b")

        when:
        locker.unlockEnqueueingSession(ENTITY, "tenant-a")

        then: "the released one is free again"
        lockedInAnotherThread(ENTITY, "tenant-a")

        and: "the other one is still held"
        !lockedInAnotherThread(ENTITY, "tenant-b")
    }

    def "a bulk enqueueing excludes only the work over the same records"() {
        when: "the records of one tenant are being enqueued"
        locker.tryLockEntityForEnqueueIndexAll(ENTITY, "tenant-a")

        then: "nobody else may enqueue the same ones"
        !CompletableFuture.supplyAsync { locker.tryLockEntityForEnqueueIndexAll(ENTITY, "tenant-a") }.get()

        and: """the records of another tenant are other records, in another index: an enqueueing asked without a
                tenant walks the tenants one at a time, so there is no pass that covers them all at once"""
        CompletableFuture.supplyAsync { locker.tryLockEntityForEnqueueIndexAll(ENTITY, "tenant-b") }.get()
    }

    def "an entity that is not split by tenants locks by its name alone, as it did before tenants existed"() {
        when:
        locker.tryLockEntityForEnqueueIndexAll(ENTITY, null)

        then:
        !CompletableFuture.supplyAsync { locker.tryLockEntityForEnqueueIndexAll(ENTITY, null) }.get()
    }

    protected boolean lockedInAnotherThread(String entityName, String tenantId) {
        return CompletableFuture.supplyAsync {
            locker.tryLockEnqueueingSession(entityName, tenantId)
        }.get()
    }
}
