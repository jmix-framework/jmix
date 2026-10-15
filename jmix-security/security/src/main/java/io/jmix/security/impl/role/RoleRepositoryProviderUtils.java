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
import io.jmix.security.model.RoleSource;
import io.jmix.security.role.RoleProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.jspecify.annotations.Nullable;
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

    public Collection<T> getAllRoles(boolean includePolicies) {
        Collection<T> roles = new ArrayList<>();
        for (RoleProvider<T> roleProvider : roleProviders) {
            roles.addAll(roleProvider.getAllRoles(includePolicies));
        }
        return roles;
    }

    /**
     * Finds a role by its code and merges its child roles into it, at all levels.
     * <p>
     * The role is taken from the first provider that has it. Then each of its child roles is found the same way, with
     * its own child roles already merged, and is passed together with the role to {@code roleMergingOperation}. The
     * operation changes the role object returned by the provider, usually by adding the policies of the child role to
     * the role's policies of all levels.
     * <p>
     * The child roles of a role with the {@link RoleSource#ANNOTATED_CLASS} source are not merged, because its
     * policies already include the policies of the role interfaces it extends.
     * <p>
     * {@code visited} collects the codes of the roles found during the call, including {@code roleCode}. A child role
     * whose code is already in it is skipped, so a cycle of child roles ends, and a child role that is reachable by
     * several paths is merged only on the first path.
     *
     * @param roleCode             the code of the role to find
     * @param visited              the codes of the roles that must not be merged again; the method adds codes to it
     * @param roleMergingOperation the operation that merges a child role (second argument) into a role (first argument)
     * @return the role with its child roles merged, or null if no provider has a role with this code
     */
    @Nullable
    public T findRoleByCodeExcludeVisited(String roleCode, Set<String> visited, BiConsumer<T, T> roleMergingOperation) {
        visited.add(roleCode);

        for (RoleProvider<T> roleProvider : roleProviders) {
            T role = roleProvider.findRoleByCode(roleCode);
            if (role != null) {
                if (role.getChildRoles() != null && !RoleSource.ANNOTATED_CLASS.equals(role.getSource())) {
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
