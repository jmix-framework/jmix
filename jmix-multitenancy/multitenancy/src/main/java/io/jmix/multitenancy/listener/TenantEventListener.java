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

package io.jmix.multitenancy.listener;

import io.jmix.core.Stores;
import io.jmix.core.annotation.Internal;
import org.springframework.beans.factory.annotation.Autowired;
import io.jmix.multitenancy.entity.Tenant;
import io.jmix.core.event.EntityChangedEvent;
import io.jmix.data.StoreAwareLocator;
import io.jmix.multitenancy.service.impl.TenantLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.jmix.core.cluster.ClusterApplicationEventPublisher;
import io.jmix.multitenancy.event.TenantEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;
import java.util.UUID;

@Internal
@Component("mten_TenantEventListener")
public class TenantEventListener {

    private static final Logger log = LoggerFactory.getLogger(TenantEventListener.class);
    public static final String TENANT_ID_FIELD_NAME = "tenantId";
    @Autowired
    protected ClusterApplicationEventPublisher publisher;
    @Autowired
    protected TenantLoader tenantLoader;
    @Autowired
    protected StoreAwareLocator storeAwareLocator;

    /**
     * Reads the created tenant in a transaction of its own.
     * <p>
     * The {@code AFTER_COMMIT} phase is not "once everything is over": the handler runs from the completion
     * callback of the transaction that has just committed, and that transaction's entity manager is still bound to
     * the thread. A plain {@code DataManager} load reuses it and then fails while detaching, because
     * {@code EclipselinkPersistenceSupport.detachAll} flushes and a flush without an active transaction throws
     * {@code TransactionRequiredException}. Spring logs it and carries on, so the announcement - and with it the
     * indexes of the new tenant - would be lost silently.
     * <p>
     * {@code BEFORE_COMMIT}, which listeners elsewhere use to avoid this, is not an option here: the announcement
     * makes the search module call the engine over the network, and holding a database transaction open across
     * that call would turn an unavailable engine into stuck transactions.
     */
    protected Optional<Tenant> loadInNewTransaction(UUID entityId) {
        TransactionTemplate transactionTemplate = storeAwareLocator.getTransactionTemplate(Stores.MAIN);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return transactionTemplate.execute(status -> tenantLoader.findWithTenantId(entityId));
    }

    @TransactionalEventListener
    public void onTenantChangedAfterCommit(final EntityChangedEvent<Tenant> event) {
        EntityChangedEvent.Type actionType = event.getType();
        if (actionType == EntityChangedEvent.Type.CREATED) {
            UUID entityId = (UUID) event.getEntityId().getValue();
            Optional<Tenant> tenantOpt = loadInNewTransaction(entityId);
            if (tenantOpt.isPresent()) {
                String tenantId = tenantOpt.get().getTenantId();
                if (tenantId != null) {
                    publisher.publish(new TenantEvent(this, TenantEvent.Type.CREATED, tenantId));
                } else {
                    log.warn("Tenant with id {} has no tenantId", entityId);
                }
            } else {
                log.warn("Tenant with id {} not found", entityId);
            }
        }

        if (actionType == EntityChangedEvent.Type.DELETED) {
            Object rawTenantId = event.getChanges().getOldValue(TENANT_ID_FIELD_NAME);
            if (rawTenantId != null) {
                publisher.publish(new TenantEvent(this, TenantEvent.Type.DELETED, (String) rawTenantId));
            } else {
                log.warn("Deleted tenant doesn't have tenantId. Deleted entity: {}", event.getEntityId().getValue());
            }
        }
    }
}
