/*
 * Copyright 2025 Haulmont.
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

package io.jmix.multitenancy.service.impl;

import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.annotation.Internal;
import io.jmix.multitenancy.entity.Tenant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Loads tenants with the attribute holding the tenant id.
 * <p>
 * Reads without access constraints on purpose. The list of tenants is infrastructure, not data of the current
 * user: {@code Tenant} is itself annotated with {@code @TenantId}, so a constrained read under a user session
 * returns that user's own tenant alone, and a user without read permission on the entity gets an empty list with
 * no error at all. Callers - index layout above all - would then silently work with a fraction of the tenants.
 */
@Internal
@Component("mten_TenantLoader")
public class TenantLoader {

    protected static final String TENANT_ID_PROPERTY = "tenantId";

    @Autowired
    protected UnconstrainedDataManager dataManager;

    public List<Tenant> findAllWithTenantId() {
        return dataManager
                .load(Tenant.class)
                .all()
                .fetchPlan(fpb -> fpb.add(TENANT_ID_PROPERTY))
                .list();
    }

    public Optional<Tenant> findWithTenantId(UUID id) {
        return dataManager
                .load(Tenant.class)
                .id(id)
                .fetchPlan(fpb -> fpb.add(TENANT_ID_PROPERTY))
                .optional();
    }
}
