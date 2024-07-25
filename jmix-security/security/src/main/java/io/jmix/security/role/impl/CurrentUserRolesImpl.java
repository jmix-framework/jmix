/*
 * Copyright 2024 Haulmont.
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

package io.jmix.security.role.impl;

import io.jmix.core.security.CurrentAuthentication;
import io.jmix.security.model.ResourceRole;
import io.jmix.security.model.RowLevelRole;
import io.jmix.security.role.CurrentUserRoles;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import io.jmix.security.role.RowLevelRoleRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Component("sec_CurrentUserRoles")
public class CurrentUserRolesImpl implements CurrentUserRoles {

    private final CurrentAuthentication currentAuthentication;

    private final RoleGrantedAuthorityUtils roleGrantedAuthorityUtils;

    private final ResourceRoleRepository resourceRoleRepository;

    private final RowLevelRoleRepository rowLevelRoleRepository;

    public CurrentUserRolesImpl(CurrentAuthentication currentAuthentication,
                                RoleGrantedAuthorityUtils roleGrantedAuthorityUtils,
                                ResourceRoleRepository resourceRoleRepository,
                                RowLevelRoleRepository rowLevelRoleRepository) {
        this.currentAuthentication = currentAuthentication;
        this.roleGrantedAuthorityUtils = roleGrantedAuthorityUtils;
        this.resourceRoleRepository = resourceRoleRepository;
        this.rowLevelRoleRepository = rowLevelRoleRepository;
    }

    @Override
    public List<ResourceRole> getResourceRoles() {
        String resourceRolePrefix = roleGrantedAuthorityUtils.getDefaultRolePrefix();
        Collection<? extends GrantedAuthority> grantedAuthorities = currentAuthentication.getAuthentication().getAuthorities();
        return grantedAuthorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .filter(authority -> authority.startsWith(resourceRolePrefix))
                .map(authority -> authority.substring(resourceRolePrefix.length()))
                .map(resourceRoleRepository::getRoleByCode)
                .toList();
    }

    @Override
    public List<RowLevelRole> getRowLevelRoles() {
        String rowLevelRolePrefix = roleGrantedAuthorityUtils.getDefaultRowLevelRolePrefix();
        Collection<? extends GrantedAuthority> grantedAuthorities = currentAuthentication.getAuthentication().getAuthorities();
        return grantedAuthorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .filter(authority -> authority.startsWith(rowLevelRolePrefix))
                .map(authority -> authority.substring(rowLevelRolePrefix.length()))
                .map(rowLevelRoleRepository::getRoleByCode)
                .toList();
    }
}
