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

package io.jmix.search.listener.dynattr

import io.jmix.core.EntityStates
import io.jmix.core.MetadataTools
import io.jmix.core.metamodel.model.MetaClass
import io.jmix.dynattr.DynamicAttributes
import io.jmix.dynattr.impl.DynamicAttributeChangeEvent
import io.jmix.search.index.impl.MultitenancyAdapter
import io.jmix.search.index.mapping.IndexConfigurationManager
import io.jmix.search.index.queue.IndexingQueueManager
import spock.lang.Specification
import test_support.entity.TestRootEntity

/**
 * The tenant a change of dynamic attributes puts into the indexing queue.
 *
 * <p>An item that does not name its tenant waits whenever any index of its entity is unavailable: the queue cannot
 * tell whether that index is the one the item is addressed to. The ordinary save path has named the tenant since
 * indexes were split; this path is the other way the same record reaches the queue.
 */
class DynamicAttributeTenantTest extends Specification {

    def indexConfigurationManager = Stub(IndexConfigurationManager) {
        isDirectlyIndexed(_) >> true
    }
    def indexingQueueManager = Mock(IndexingQueueManager)
    def entityStates = Stub(EntityStates) {
        isNew(_) >> true
    }

    def entity = new TestRootEntity(id: UUID.randomUUID())
    def event = new DynamicAttributeChangeEvent<>(
            Stub(MetaClass) {
                getName() >> "test_TestRootEntity"
                getJavaClass() >> TestRootEntity
            },
            entity,
            Stub(DynamicAttributes) {
                getKeys() >> (["contractNo"] as Set)
            })

    def "the tenant of the record reaches the queue item"() {
        given:
        def listener = listenerWith(Stub(MultitenancyAdapter) {
            isTenantIdReadable(entity) >> true
            getTenantIdForInstance(entity) >> "acme"
        })

        when:
        listener.onDynamicAttributesChange(event)

        then: """without it the item of Acme waits for the index of every other tenant of this entity to be
                 repaired, although its own index is sound"""
        1 * indexingQueueManager.enqueueIndexByEntityId(_, "acme")
    }

    def "a tenant that cannot be read leaves the item without one, as before"() {
        given: "the tenant attribute is a system property and a narrowly loaded record may not carry it"
        def listener = listenerWith(Stub(MultitenancyAdapter) {
            isTenantIdReadable(entity) >> false
        })

        when:
        listener.onDynamicAttributesChange(event)

        then: "not knowing is allowed: the tenant is worked out from the record when the queue is processed"
        1 * indexingQueueManager.enqueueIndexByEntityId(_, null)
    }

    protected DynamicAttributesTrackingListener listenerWith(MultitenancyAdapter adapter) {
        return new DynamicAttributesTrackingListener(indexConfigurationManager, indexingQueueManager,
                entityStates, Stub(MetadataTools), adapter)
    }
}
