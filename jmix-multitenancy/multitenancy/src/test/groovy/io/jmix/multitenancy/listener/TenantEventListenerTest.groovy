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

package io.jmix.multitenancy.listener

import io.jmix.core.Id
import io.jmix.core.event.AttributeChanges
import io.jmix.core.event.EntityChangedEvent
import io.jmix.multitenancy.entity.Tenant
import io.jmix.multitenancy.event.TenantEvent
import io.jmix.multitenancy.service.impl.TenantLoader
import io.jmix.core.cluster.ClusterApplicationEventPublisher
import io.jmix.data.StoreAwareLocator
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionTemplate
import spock.lang.Specification

class TenantEventListenerTest extends Specification {

    public static final String SPECIFIC_TENANT_ID = "specificTenantId"

    /**
     * The listener reads the tenant in a transaction of its own, the original one being over by then. Nothing here
     * needs a real transaction: the template is only asked to run what it is given.
     */
    private StoreAwareLocator storeAwareLocator() {
        def transactionManager = Mock(PlatformTransactionManager)
        transactionManager.getTransaction(_) >> Mock(TransactionStatus)
        def locator = Mock(StoreAwareLocator)
        locator.getTransactionTemplate(_) >> new TransactionTemplate(transactionManager)
        return locator
    }

    def "tenant creation"() {
        given:
        def publisher = Mock(ClusterApplicationEventPublisher)

        and:
        UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
        Tenant tenant = Mock(Tenant)
        tenant.getTenantId() >> SPECIFIC_TENANT_ID

        def tenantLoader = Mock(TenantLoader)
        tenantLoader.findWithTenantId(id) >> Optional.of(tenant)

        and:
        EntityChangedEvent<Tenant> entityChangedEvent = new EntityChangedEvent(
                Mock(Object),
                Id.of(id, Tenant),
                EntityChangedEvent.Type.CREATED,
                null,
                null)

        and:
        def listener = new TenantEventListener(
                publisher: publisher, tenantLoader: tenantLoader, storeAwareLocator: storeAwareLocator())

        when:
        listener.onTenantChangedAfterCommit(entityChangedEvent)

        then:
        1 * publisher.publish({ TenantEvent event ->
            event.tenantId == SPECIFIC_TENANT_ID && event.type == TenantEvent.Type.CREATED
        })
    }

    def "tenant deleting"() {
        given:
        def publisher = Mock(ClusterApplicationEventPublisher)

        and:
        def tenantLoader = Mock(TenantLoader)

        and:
        AttributeChanges changes = AttributeChanges.Builder.create().withChange("tenantId", SPECIFIC_TENANT_ID).build()

        EntityChangedEvent<Tenant> entityChangedEvent = new EntityChangedEvent(
                Mock(Object),
                Id.of(UUID.randomUUID(), Tenant),
                EntityChangedEvent.Type.DELETED,
                changes,
                null)

        and:
        def listener = new TenantEventListener(
                publisher: publisher, tenantLoader: tenantLoader, storeAwareLocator: storeAwareLocator())

        when:
        listener.onTenantChangedAfterCommit(entityChangedEvent)

        then:
        0 * tenantLoader._
                1 * publisher.publish({ TenantEvent event ->
            event.tenantId == SPECIFIC_TENANT_ID && event.type == TenantEvent.Type.DELETED
        })
    }

    def "tenant creation does not publish event when tenant not found"() {
        given:
        def publisher = Mock(ClusterApplicationEventPublisher)

        and:
        UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440001")
        def tenantLoader = Mock(TenantLoader)
        tenantLoader.findWithTenantId(id) >> Optional.empty()

        and:
        EntityChangedEvent<Tenant> entityChangedEvent = new EntityChangedEvent(
                Mock(Object),
                Id.of(id, Tenant),
                EntityChangedEvent.Type.CREATED,
                null,
                null)

        and:
        def listener = new TenantEventListener(
                publisher: publisher, tenantLoader: tenantLoader, storeAwareLocator: storeAwareLocator())

        when:
        listener.onTenantChangedAfterCommit(entityChangedEvent)

        then:
        0 * publisher._
    }

    def "tenant creation does not publish event when tenantId is null"() {
        given:
        def publisher = Mock(ClusterApplicationEventPublisher)

        and:
        UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440002")
        Tenant tenant = Mock(Tenant)
        tenant.getTenantId() >> null

        def tenantLoader = Mock(TenantLoader)
        tenantLoader.findWithTenantId(id) >> Optional.of(tenant)

        and:
        EntityChangedEvent<Tenant> entityChangedEvent = new EntityChangedEvent(
                Mock(Object),
                Id.of(id, Tenant),
                EntityChangedEvent.Type.CREATED,
                null,
                null)

        and:
        def listener = new TenantEventListener(
                publisher: publisher, tenantLoader: tenantLoader, storeAwareLocator: storeAwareLocator())

        when:
        listener.onTenantChangedAfterCommit(entityChangedEvent)

        then:
        0 * publisher._
    }

    def "tenant deleting does not publish event when tenantId missing"() {
        given:
        def publisher = Mock(ClusterApplicationEventPublisher)

        and:
        def tenantLoader = Mock(TenantLoader)

        and:
        AttributeChanges changes = AttributeChanges.Builder.create().build()

        EntityChangedEvent<Tenant> entityChangedEvent = new EntityChangedEvent(
                Mock(Object),
                Id.of(UUID.randomUUID(), Tenant),
                EntityChangedEvent.Type.DELETED,
                changes,
                null)

        and:
        def listener = new TenantEventListener(
                publisher: publisher, tenantLoader: tenantLoader, storeAwareLocator: storeAwareLocator())

        when:
        listener.onTenantChangedAfterCommit(entityChangedEvent)

        then:
        0 * publisher._
    }
}
