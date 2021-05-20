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

package io.jmix.multitenancy.listener;

import com.google.common.base.Strings;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.event.EntitySavingEvent;
import io.jmix.core.security.UserRepository;
import io.jmix.multitenancy.MultitenancyProperties;
import io.jmix.multitenancy.entity.TenantAssigmentEntity;
import io.jmix.security.model.BaseRole;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.assignment.RoleAssignmentRoleType;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component(TenantAssigmentEntityEventListener.NAME)
public class TenantAssigmentEntityEventListener {
    public static final String NAME = "TenantAssigmentEntityEventListener";

    private final Metadata metadata;
    private final MultitenancyProperties multitenancyProperties;
    private final ResourceRoleRepository resourceRoleRepository;
    private final UserRepository userRepository;
    private final DataManager dataManager;

    public TenantAssigmentEntityEventListener(Metadata metadata,
                                              MultitenancyProperties multitenancyProperties,
                                              ResourceRoleRepository resourceRoleRepository,
                                              UserRepository userRepository,
                                              DataManager dataManager) {
        this.metadata = metadata;
        this.multitenancyProperties = multitenancyProperties;
        this.resourceRoleRepository = resourceRoleRepository;
        this.userRepository = userRepository;
        this.dataManager = dataManager;
    }

    @EventListener
    public void onTenantAssigmentEntitySaving(EntitySavingEvent<TenantAssigmentEntity> event) {
        UserDetails userDetails = userRepository.loadUserByUsername(event.getEntity().getUsername());
        BaseRole tenantDefaultRole = getDefaultTenantRole();

        if (!userHasRole(userDetails, tenantDefaultRole)) {
            RoleAssignmentEntity roleAssignmentEntity = metadata.create(RoleAssignmentEntity.class);
            roleAssignmentEntity.setRoleCode(tenantDefaultRole.getCode());
            roleAssignmentEntity.setUsername(userDetails.getUsername());
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