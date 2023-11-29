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

import io.jmix.security.model.ResourceRole;
import io.jmix.security.model.RowLevelRole;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import io.jmix.security.role.RowLevelRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.util.Assert;

import java.util.*;

/**
 * GrantedAuthoritiesMapper that maps authorities to {@link GrantedAuthority}s.
 * <p>
 * First, it tries to map provided authorities to Jmix role codes if implementation of
 * {@link LdapAuthorityToJmixRoleCodesMapper} is provided. After that, it searches resource and row-level roles with
 * such codes, in case both resource and row-level roles with the same code exist both will be returned.
 *
 * @see LdapAuthorityToJmixRoleCodesMapper
 */
public class JmixLdapGrantedAuthoritiesMapper implements GrantedAuthoritiesMapper {

    private ResourceRoleRepository resourceRoleRepository;
    private RowLevelRoleRepository rowLevelRoleRepository;
    private LdapAuthorityToJmixRoleCodesMapper authorityToJmixRoleCodeMapper;
    private RoleGrantedAuthorityUtils roleGrantedAuthorityUtils;

    private List<String> defaultRoles;

    @Autowired
    public void setResourceRoleRepository(ResourceRoleRepository resourceRoleRepository) {
        this.resourceRoleRepository = resourceRoleRepository;
    }

    @Autowired
    public void setRowLevelRoleRepository(RowLevelRoleRepository rowLevelRoleRepository) {
        this.rowLevelRoleRepository = rowLevelRoleRepository;
    }

    @Autowired(required = false)
    public void setLdapAuthorityToJmixRoleCodeMapper(LdapAuthorityToJmixRoleCodesMapper authorityToJmixRoleCodeMapper) {
        this.authorityToJmixRoleCodeMapper = authorityToJmixRoleCodeMapper;
    }

    @Autowired
    public void setRoleGrantedAuthorityUtils(RoleGrantedAuthorityUtils roleGrantedAuthorityUtils) {
        this.roleGrantedAuthorityUtils = roleGrantedAuthorityUtils;
    }

    public void setDefaultRoles(List<String> roles) {
        Assert.notNull(roles, "roles list cannot be null");
        this.defaultRoles = roles;
    }

    @Override
    public Set<GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Set<GrantedAuthority> mapped = new HashSet<>(authorities.size());
        for (GrantedAuthority grantedAuthority : authorities) {
            String authority = grantedAuthority.getAuthority();
            if (authority != null) {
                mapped.addAll(mapAuthority(authority));
                if (authority.startsWith(roleGrantedAuthorityUtils.getDefaultRolePrefix())) {
                    //there may be an authority like ROLE_manager, we need to extract the role code (manager)
                    String roleCode = authority.substring(roleGrantedAuthorityUtils.getDefaultRolePrefix().length());
                    mapped.addAll(mapAuthority(roleCode));
                } else if (authority.startsWith(roleGrantedAuthorityUtils.getDefaultRowLevelRolePrefix())) {
                    //there may be an authority like ROW_LEVEL_ROLE_manager, we need to extract the role code (manager)
                    String roleCode = authority.substring(roleGrantedAuthorityUtils.getDefaultRowLevelRolePrefix().length());
                    mapped.addAll(mapAuthority(roleCode));
                }
            }
        }

        if (this.defaultRoles != null) {
            mapped.addAll(mapRoleCodesToAuthority(defaultRoles));
        }

        return mapped;
    }

    protected List<GrantedAuthority> mapAuthority(String authority) {
        Collection<String> roleCodes = new HashSet<>();
        if (authorityToJmixRoleCodeMapper != null) {
            roleCodes.addAll(authorityToJmixRoleCodeMapper.mapAuthorityToJmixRoleCodes(authority));
        } else {
            roleCodes.add(authority);
        }

        return mapRoleCodesToAuthority(roleCodes);
    }

    protected List<GrantedAuthority> mapRoleCodesToAuthority(Collection<String> roleCodes) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String roleCode : roleCodes) {
            ResourceRole resourceRole = resourceRoleRepository.findRoleByCode(roleCode);
            if (resourceRole != null) {
                authorities.add(roleGrantedAuthorityUtils.createResourceRoleGrantedAuthority(resourceRole));
            }

            RowLevelRole rowLevelRole = rowLevelRoleRepository.findRoleByCode(roleCode);
            if (rowLevelRole != null) {
                authorities.add(roleGrantedAuthorityUtils.createRowLevelRoleGrantedAuthority(rowLevelRole));
            }
        }

        return authorities;
    }

}
