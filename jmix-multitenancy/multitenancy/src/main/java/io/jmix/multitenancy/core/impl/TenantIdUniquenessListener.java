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

package io.jmix.multitenancy.core.impl;

import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.event.EntitySavingEvent;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.data.PersistenceHints;
import io.jmix.multitenancy.entity.Tenant;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Prevents a tenant from taking the tenant id of an existing tenant, including a soft-deleted one.
 * Data and users of a deleted tenant still carry its tenant id, so reusing the id would expose that data
 * to the new tenant and let the users of the deleted tenant log in again.
 */
@Component("mten_TenantIdUniquenessListener")
public class TenantIdUniquenessListener {

    private final UnconstrainedDataManager unconstrainedDataManager;

    public TenantIdUniquenessListener(UnconstrainedDataManager unconstrainedDataManager) {
        this.unconstrainedDataManager = unconstrainedDataManager;
    }

    @EventListener
    public void beforeSave(EntitySavingEvent event) {
        if (!(event.getEntity() instanceof Tenant tenant)) {
            return;
        }
        String tenantId = tenant.getTenantId();
        if (tenantId == null) {
            return;
        }
        if (!event.isNewEntity() && isTenantIdUnchanged(tenant)) {
            return;
        }
        boolean taken = !unconstrainedDataManager.load(Tenant.class)
                .condition(PropertyCondition.equal("tenantId", tenantId))
                .hint(PersistenceHints.SOFT_DELETION, false)
                .maxResults(1)
                .list()
                .isEmpty();
        if (taken) {
            throw new IllegalArgumentException(
                    String.format("Tenant id '%s' is already used by another tenant, possibly a deleted one", tenantId));
        }
    }

    private boolean isTenantIdUnchanged(Tenant tenant) {
        if (tenant.getId() == null) {
            return false;
        }
        Tenant stored = unconstrainedDataManager.load(Tenant.class)
                .id(tenant.getId())
                .hint(PersistenceHints.SOFT_DELETION, false)
                .optional()
                .orElse(null);
        return stored != null && Objects.equals(tenant.getTenantId(), stored.getTenantId());
    }
}
