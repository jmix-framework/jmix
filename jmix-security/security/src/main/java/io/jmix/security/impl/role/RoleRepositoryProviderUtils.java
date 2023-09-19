/*
 * Copyright 2022 Haulmont.
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

import io.jmix.security.model.BaseRole;
import io.jmix.security.role.RoleProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Different implementations of {@link io.jmix.security.role.RoleRepository} delegate working with {@link RoleProvider}
 * to this class. The class is responsible for searching for roles in multiple role providers, for deleting roles from
 * providers.
 */
@Component("sec_RoleRepositoryProviderUtils")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RoleRepositoryProviderUtils<T extends BaseRole> {

    private final Collection<RoleProvider<T>> roleProviders;

    public RoleRepositoryProviderUtils(Collection<RoleProvider<T>> roleProviders) {
        this.roleProviders = roleProviders;
    }

    public boolean deleteRole(String roleCode) {
        for (RoleProvider<T> roleProvider : roleProviders) {
            T role = roleProvider.findRoleByCode(roleCode);
            if (role != null) {
                return roleProvider.deleteRole(role);
            }
        }
        throw new IllegalArgumentException(String.format("Role with code \"%s\" was not found", roleCode));
    }

    public Collection<T> getAllRoles() {
        Collection<T> roles = new ArrayList<>();
        for (RoleProvider<T> roleProvider : roleProviders) {
            roles.addAll(roleProvider.getAllRoles());
        }
        return roles;
    }

    @Nullable
    public T findRoleByCodeExcludeVisited(String roleCode, Set<String> visited, BiConsumer<T, T> roleMergingOperation) {
        visited.add(roleCode);

        for (RoleProvider<T> roleProvider : roleProviders) {
            T role = roleProvider.findRoleByCode(roleCode);
            if (role != null) {
                if (role.getChildRoles() != null) {
                    for (String childCode : role.getChildRoles()) {
                        if (!visited.contains(childCode)) {
                            T childRole = findRoleByCodeExcludeVisited(childCode, visited, roleMergingOperation);
                            if (childRole != null) {
                                roleMergingOperation.accept(role, childRole);
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
