/*
 * Copyright (c) 2008-2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.multitenancy.listener;


import com.google.common.base.Strings;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.data.listener.BeforeInsertEntityListener;
import io.jmix.data.listener.BeforeUpdateEntityListener;
import io.jmix.multitenancy.MultitenancyProperties;
import io.jmix.multitenancy.entity.Tenant;
import io.jmix.security.model.BaseRole;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.assignment.RoleAssignmentRoleType;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;


@Component("mten_TenantListener")
public class TenantListener implements BeforeUpdateEntityListener<Tenant>, BeforeInsertEntityListener<Tenant> {

    private final MultitenancyProperties multitenancyProperties;
    private final Metadata metadata;
    private final ResourceRoleRepository resourceRoleRepository;
    private final UserDetailsService userDetailsService;
    private final DataManager dataManager;

    public TenantListener(MultitenancyProperties multitenancyProperties,
                          Metadata metadata,
                          ResourceRoleRepository resourceRoleRepository,
                          UserDetailsService userDetailsService,
                          DataManager dataManager) {
        this.multitenancyProperties = multitenancyProperties;
        this.metadata = metadata;
        this.resourceRoleRepository = resourceRoleRepository;
        this.userDetailsService = userDetailsService;
        this.dataManager = dataManager;
    }

    @Override
    public void onBeforeInsert(Tenant tenant) {
        updateTenantAdmin(tenant);
    }

    @Override
    public void onBeforeUpdate(Tenant tenant) {
        updateTenantAdmin(tenant);
    }

    protected void updateTenantAdmin(Tenant tenant) {
        UserDetails tenantAdmin = userDetailsService.loadUserByUsername(tenant.getAdminUsername());
        assignDefaultTenantRole(tenantAdmin);
    }

    protected void assignDefaultTenantRole(UserDetails user) {
        BaseRole tenantDefaultRole = getDefaultTenantRole();
        if (!userHasRole(user, tenantDefaultRole)) {
            RoleAssignmentEntity roleAssignmentEntity = metadata.create(RoleAssignmentEntity.class);
            roleAssignmentEntity.setRoleCode(tenantDefaultRole.getCode());
            roleAssignmentEntity.setUsername(user.getUsername());
            roleAssignmentEntity.setRoleType(RoleAssignmentRoleType.RESOURCE);
            dataManager.save(roleAssignmentEntity);
        }
    }

    private BaseRole getDefaultTenantRole() {
        String roleCode = multitenancyProperties.getDefaultTenantRoleCode();
        if (Strings.isNullOrEmpty(roleCode)) {
            throw new IllegalArgumentException("Properties defaultTenantRoleCode is not set");
        }
        return resourceRoleRepository.getRoleByCode(roleCode);
    }

    private boolean userHasRole(UserDetails user, BaseRole role) {
        return user.getAuthorities()
                .stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equalsIgnoreCase(role.getCode()));

    }
}
