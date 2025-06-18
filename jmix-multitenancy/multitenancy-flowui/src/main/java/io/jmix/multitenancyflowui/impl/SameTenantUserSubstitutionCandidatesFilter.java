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
import io.jmix.securityflowui.view.usersubstitution.UserSubstitutionCandidatesFilter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Implementation of {@link UserSubstitutionCandidatesFilter}
 * that allows user to be substituted by user from the same tenant only.
 */
@Component("mten_SameTenantUserSubstitutionCandidatesFilter")
public class SameTenantUserSubstitutionCandidatesFilter implements UserSubstitutionCandidatesFilter {

    protected final TenantProvider tenantProvider;

    public SameTenantUserSubstitutionCandidatesFilter(TenantProvider tenantProvider) {
        this.tenantProvider = tenantProvider;
    }

    @Override
    public boolean test(UserDetails firstUser, UserDetails secondUser) {
        String firstUserTenantId = tenantProvider.getTenantIdForUser(firstUser);
        String secondUserTenantId = tenantProvider.getTenantIdForUser(secondUser);
        return Objects.equals(firstUserTenantId, secondUserTenantId);
    }
}
