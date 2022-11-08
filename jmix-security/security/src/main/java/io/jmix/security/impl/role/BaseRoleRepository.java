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

package io.jmix.security.impl.role;

import com.google.common.collect.Sets;
import io.jmix.security.model.BaseRole;
import io.jmix.security.role.RoleProvider;
import io.jmix.security.role.RoleRepository;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public abstract class BaseRoleRepository<T extends BaseRole> implements RoleRepository<T> {

    protected abstract Collection<? extends RoleProvider<T>> getRoleProviders();

    protected abstract void mergeChildRoleState(T role, T childRole);

    @Override
    public T findRoleByCode(String code) {
        return findRoleByCodeExcludeVisited(code, Sets.newHashSet());
    }

    @Override
    public boolean deleteRole(String code) {
        for (RoleProvider<T> roleProvider : getRoleProviders()) {
            T role = roleProvider.findRoleByCode(code);
            if (role != null) {
                return roleProvider.deleteRole(role);
            }
        }
        throw new IllegalArgumentException(String.format("Role with code \"%s\" was not found", code));
    }

    @Override
    public Collection<T> getAllRoles() {
        Collection<T> roles = new ArrayList<>();
        for (RoleProvider<T> roleProvider : getRoleProviders()) {
            roles.addAll(roleProvider.getAllRoles());
        }
        return roles;
    }

    @Nullable
    protected T findRoleByCodeExcludeVisited(String code, Set<String> visited) {
        visited.add(code);

        for (RoleProvider<T> roleProvider : getRoleProviders()) {
            T role = roleProvider.findRoleByCode(code);
            if (role != null) {
                if (role.getChildRoles() != null) {
                    for (String childCode : role.getChildRoles()) {
                        if (!visited.contains(childCode)) {
                            T childRole = findRoleByCodeExcludeVisited(childCode, visited);
                            if (childRole != null) {
                                mergeChildRoleState(role, childRole);
                            }
                        }
                    }
                }
                return role;
            }
        }
        return null;
    }
}
