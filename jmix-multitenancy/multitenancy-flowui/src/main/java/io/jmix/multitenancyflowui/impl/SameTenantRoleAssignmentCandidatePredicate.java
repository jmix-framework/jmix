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

package io.jmix.multitenancyflowui.impl;

import io.jmix.multitenancy.core.TenantProvider;
import io.jmix.security.model.BaseRole;
import io.jmix.security.model.RoleSource;
import io.jmix.securityflowui.util.RoleAssignmentCandidatePredicate;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Objects;

/**
 * Implementation of {@link RoleAssignmentCandidatePredicate}
 * that allows role to be assigned to the user from the same tenant only.
 */
public class SameTenantRoleAssignmentCandidatePredicate implements RoleAssignmentCandidatePredicate {

    protected final TenantProvider tenantProvider;

    public SameTenantRoleAssignmentCandidatePredicate(TenantProvider tenantProvider) {
        this.tenantProvider = tenantProvider;
    }

    @Override
    public boolean test(UserDetails userDetails, BaseRole baseRole) {
        if (RoleSource.ANNOTATED_CLASS.equals(baseRole.getSource())) {
            return true;
        }

        String userTenant = tenantProvider.getTenantIdForUser(userDetails);
        // Convert "NO_TENANT" to null to match null tenant of role
        userTenant = TenantProvider.NO_TENANT.equals(userTenant) ? null : userTenant;
        String roleTenant = baseRole.getTenantId();

        return Objects.equals(roleTenant, userTenant);
    }
}
