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

package io.jmix.ldap.userdetails;

import io.jmix.security.authentication.RoleGrantedAuthority;
import io.jmix.security.model.ResourceRole;
import io.jmix.security.model.RowLevelRole;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.RowLevelRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.util.Assert;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * GrantedAuthoritiesMapper that maps authorities to {@link RoleGrantedAuthority}s.
 * <p>
 * First, it tries to find a resource role with the same code. If it haven't been found,
 * it searches for a row-level role with the same code.
 */
public class JmixLdapGrantedAuthoritiesMapper implements GrantedAuthoritiesMapper {

    private ResourceRoleRepository resourceRoleRepository;
    private RowLevelRoleRepository rowLevelRoleRepository;

    private List<String> defaultRoles;
    private Function<String, String> authorityToRoleCodeMapper;

    @Autowired
    public void setResourceRoleRepository(ResourceRoleRepository resourceRoleRepository) {
        this.resourceRoleRepository = resourceRoleRepository;
    }

    @Autowired
    public void setRowLevelRoleRepository(RowLevelRoleRepository rowLevelRoleRepository) {
        this.rowLevelRoleRepository = rowLevelRoleRepository;
    }

    @Override
    public Set<GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
        HashSet<GrantedAuthority> mapped = new HashSet<>(authorities.size());
        for (GrantedAuthority authority : authorities) {
            if (authority instanceof RoleGrantedAuthority) {
                mapped.add(authority);
            } else {
                GrantedAuthority mappedAuthority = mapAuthority(authority.getAuthority());
                if (mappedAuthority != null) {
                    mapped.add(mappedAuthority);
                }
            }
        }
        if (this.defaultRoles != null) {
            List<GrantedAuthority> defaultAuthorities = this.defaultRoles.stream()
                    .map(this::mapAuthority)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            mapped.addAll(defaultAuthorities);
        }
        return mapped;
    }

    @Nullable
    protected GrantedAuthority mapAuthority(String authority) {
        if (authorityToRoleCodeMapper != null) {
            authority = authorityToRoleCodeMapper.apply(authority);
        }
        ResourceRole resourceRole = resourceRoleRepository.findRoleByCode(authority);
        if (resourceRole != null) {
            return RoleGrantedAuthority.ofResourceRole(resourceRole);
        } else {
            RowLevelRole rowLevelRole = rowLevelRoleRepository.findRoleByCode(authority);
            if (rowLevelRole != null) {
                return RoleGrantedAuthority.ofRowLevelRole(rowLevelRole);
            }
        }
        return null;
    }

    public void setDefaultRoles(List<String> roles) {
        Assert.notNull(roles, "roles list cannot be null");
        this.defaultRoles = roles;
    }

    /**
     * Sets the mapping function which will be used to convert an authority name
     * to role code which will be used to obtain a resource role or row-level role.
     *
     * @param authorityToRoleCodeMapper the mapping function
     */
    public void setAuthorityToRoleCodeMapper(Function<String, String> authorityToRoleCodeMapper) {
        this.authorityToRoleCodeMapper = authorityToRoleCodeMapper;
    }
}
