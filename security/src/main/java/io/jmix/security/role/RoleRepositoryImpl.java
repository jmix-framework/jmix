/*
 * Copyright 2020 Haulmont.
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

package io.jmix.security.role;

import io.jmix.core.common.datastruct.Pair;
import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.Role;
import io.jmix.security.model.RowLevelPolicy;
import io.jmix.security.role.provider.RoleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Component(RoleRepository.NAME)
public class RoleRepositoryImpl implements RoleRepository {

    private static final Logger log = LoggerFactory.getLogger(RoleRepositoryImpl.class);

    private Collection<RoleProvider> roleProviders;

    @Autowired
    public void setRoleProviders(Collection<RoleProvider> roleProviders) {
        this.roleProviders = roleProviders;
    }

    @Override
    @Nullable
    public Role getRoleByCode(String code) {
        for (RoleProvider roleProvider : roleProviders) {
            Role role = roleProvider.getRoleByCode(code);
            if (role != null) {
                if (role.getChildRoles() != null && !role.getChildRoles().isEmpty()) {
                    List<String> traversedRoles = new ArrayList<>();
                    traversedRoles.add(role.getCode());
                    Pair<List<ResourcePolicy>, List<RowLevelPolicy>> childPolicies =
                            getAggregatedRolePolicies(role.getCode(), role.getChildRoles(), traversedRoles);
                    role.getResourcePolicies().addAll(childPolicies.getFirst());
                    role.getRowLevelPolicies().addAll(childPolicies.getSecond());
                }
                return role;
            }
        }
        return null;
    }

    @Override
    public boolean deleteRole(String code) {
        for (RoleProvider roleProvider : roleProviders) {
            Role role = roleProvider.getRoleByCode(code);
            if (role != null) {
                return roleProvider.deleteRole(role);
            }
        }
        throw new IllegalArgumentException(String.format("Role with code \"%s\" was not found", code));
    }

    @Override
    public Collection<Role> getAllRoles() {
        Collection<Role> roles = new ArrayList<>();
        for (RoleProvider roleProvider : roleProviders) {
            roles.addAll(roleProvider.getAllRoles());
        }
        for (Role role : roles) {
            if (role.getChildRoles() != null && !role.getChildRoles().isEmpty()) {
                List<String> traversedRoles = new ArrayList<>();
                traversedRoles.add(role.getCode());
                Pair<List<ResourcePolicy>, List<RowLevelPolicy>> childPolicies =
                        getAggregatedRolePolicies(role.getCode(), role.getChildRoles(), traversedRoles);
                role.getResourcePolicies().addAll(childPolicies.getFirst());
                role.getRowLevelPolicies().addAll(childPolicies.getSecond());
            }
        }
        return roles;
    }

    protected Pair<List<ResourcePolicy>, List<RowLevelPolicy>> getAggregatedRolePolicies(String aggregatedRoleCode,
                                                                                         Set<String> childRoles,
                                                                                         List<String> traversedRoles) {
        Pair<List<ResourcePolicy>, List<RowLevelPolicy>> result = new Pair<>(new ArrayList<>(), new ArrayList<>());
        for (String childCode : childRoles) {
            if (!traversedRoles.contains(childCode)) {
                traversedRoles.add(childCode);
                boolean found = false;
                for (RoleProvider roleProvider : roleProviders) {
                    Role child = roleProvider.getRoleByCode(childCode);
                    if (child != null) {
                        found = true;
                        if (child.getChildRoles() != null && !child.getChildRoles().isEmpty()) {
                            Pair<List<ResourcePolicy>, List<RowLevelPolicy>> childPolicies =
                                    getAggregatedRolePolicies(childCode, child.getChildRoles(), traversedRoles);
                            result.getFirst().addAll(childPolicies.getFirst());
                            result.getSecond().addAll(childPolicies.getSecond());
                        }
                        result.getFirst().addAll(child.getResourcePolicies());
                        result.getSecond().addAll(child.getRowLevelPolicies());
                        break;
                    }
                }
                if (!found) {
                    log.warn("Role {} was not found while collecting child roles for aggregated role {}",
                            childCode, aggregatedRoleCode);
                }
            }
        }
        return result;
    }
}
