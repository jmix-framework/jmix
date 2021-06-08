/*
 * Copyright 2021 Haulmont.
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

import io.jmix.core.event.EntitySavingEvent;
import io.jmix.multitenancy.core.TenantEntityOperation;
import io.jmix.multitenancy.core.TenantProvider;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component("mten_TenantPersistingListener")
public class TenantPersistingListener {

    private final TenantProvider tenantProvider;
    private final TenantEntityOperation tenantEntityOperation;

    public TenantPersistingListener(TenantProvider tenantProvider,
                                    TenantEntityOperation tenantEntityOperation) {
        this.tenantProvider = tenantProvider;
        this.tenantEntityOperation = tenantEntityOperation;
    }

    @EventListener
    public void beforePersist(EntitySavingEvent event) {
        Object entity = event.getEntity();
        String tenantId = tenantProvider.getCurrentUserTenantId();
        if (tenantEntityOperation.findTenantProperty(entity.getClass()) != null && !TenantProvider.NO_TENANT.equals(tenantId)) {
            tenantEntityOperation.setTenant(entity, tenantId);
        }
    }
}
