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

import io.jmix.security.model.Role;
import io.jmix.security.role.provider.RoleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;

@Component(RoleRepository.NAME)
public class RoleRepositoryImpl implements RoleRepository {

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
            if (role != null) return role;
        }
        return null;
    }

    @Override
    public Collection<Role> getAllRoles() {
        Collection<Role> roles = new ArrayList<>();
        for (RoleProvider roleProvider : roleProviders) {
            roles.addAll(roleProvider.getAllRoles());
        }
        return roles;
    }
}
