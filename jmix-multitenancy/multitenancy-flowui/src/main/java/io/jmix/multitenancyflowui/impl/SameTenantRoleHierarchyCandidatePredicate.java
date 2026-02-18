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

package io.jmix.multitenancyflowui.impl;

import io.jmix.multitenancy.core.TenantProvider;
import io.jmix.security.model.BaseRole;
import io.jmix.security.model.RoleSource;
import io.jmix.securityflowui.util.RoleHierarchyCandidatePredicate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

/**
 * Implementation of {@link RoleHierarchyCandidatePredicate}
 * that allows role to be selected as a base one only if it from the same tenant only.
 */
public class SameTenantRoleHierarchyCandidatePredicate implements RoleHierarchyCandidatePredicate {

    @Autowired
    protected TenantProvider tenantProvider;

    @Override
    public boolean test(BaseRole currentRole, BaseRole baseRoleCandidate) {
        if (RoleSource.ANNOTATED_CLASS.equals(baseRoleCandidate.getSource())) {
            // Design-time roles are always allowed
            return true;
        }
        if (baseRoleCandidate == null) {
            return false;
        }

        String currentRoleTenantId;
        if (currentRole == null) {
            // 'Null' current role means this role is during creation process - get tenant fron current user
            String currentUserTenant = tenantProvider.getCurrentUserTenantId();
            // Convert "NO_TENANT" to null to match null tenant of role
            currentRoleTenantId = TenantProvider.NO_TENANT.equals(currentUserTenant) ? null : currentUserTenant;
        } else {
            currentRoleTenantId = currentRole.getTenantId();
        }

        String baseRoleCandidateTenantId = baseRoleCandidate.getTenantId();

        return Objects.equals(baseRoleCandidateTenantId, currentRoleTenantId);
    }
}