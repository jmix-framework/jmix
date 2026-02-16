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

import io.jmix.security.model.BaseRole;
import io.jmix.security.model.RoleSource;
import io.jmix.securityflowui.util.RoleHierarchyCandidatePredicate;

import java.util.Objects;

/**
 * Implementation of {@link RoleHierarchyCandidatePredicate}
 * that allows role to be selected as a base one only if it from the same tenant only.
 */
public class SameTenantRoleHierarchyCandidatePredicate implements RoleHierarchyCandidatePredicate {

    @Override
    public boolean test(BaseRole currentRole, BaseRole baseRoleCandidate) {
        if (RoleSource.ANNOTATED_CLASS.equals(baseRoleCandidate.getSource())) {
            return true;
        }
        if (currentRole == null || baseRoleCandidate == null) {
            return false;
        }

        String childRoleTenantId = currentRole.getTenantId();
        String baseRoleCandidateTenantId = baseRoleCandidate.getTenantId();

        return Objects.equals(baseRoleCandidateTenantId, childRoleTenantId);
    }
}