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

package io.jmix.security.role;

import io.jmix.security.SecurityProperties;
import io.jmix.security.model.ResourceRole;
import io.jmix.security.model.RowLevelRole;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * Utility class for working with Jmix-related {@link GrantedAuthority}
 */
@Component("sec_RoleGrantedAuthorityUtils")
public class RoleGrantedAuthorityUtils {

    private GrantedAuthorityDefaults grantedAuthorityDefaults;
    private SecurityProperties securityProperties;
    private String defaultRolePrefix = "ROLE_";

    public RoleGrantedAuthorityUtils(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Autowired(required = false)
    public void setGrantedAuthorityDefaults(GrantedAuthorityDefaults grantedAuthorityDefaults) {
        this.grantedAuthorityDefaults = grantedAuthorityDefaults;
    }

    @PostConstruct
    public void init() {
        if (grantedAuthorityDefaults != null) {
            defaultRolePrefix = grantedAuthorityDefaults.getRolePrefix();
        }
    }

    /**
     * Creates {@link GrantedAuthority} for the given {@link ResourceRole}
     */
    public GrantedAuthority createResourceRoleGrantedAuthority(ResourceRole resourceRole) {
        return createResourceRoleGrantedAuthority(resourceRole.getCode());
    }

    /**
     * Creates {@link GrantedAuthority} for the {@link ResourceRole} with the given code
     */
    public GrantedAuthority createResourceRoleGrantedAuthority(String resourceRoleCode) {
        return new SimpleGrantedAuthority(defaultRolePrefix + resourceRoleCode);
    }

    /**
     * Creates {@link GrantedAuthority} for the given {@link RowLevelRole}
     */
    public GrantedAuthority createRowLevelRoleGrantedAuthority(RowLevelRole rowLevelRole) {
        return createRowLevelRoleGrantedAuthority(rowLevelRole.getCode());
    }

    /**
     * Creates {@link GrantedAuthority} for the {@link RowLevelRole} with the given code
     */
    public GrantedAuthority createRowLevelRoleGrantedAuthority(String rowLevelRoleCode) {
        return new SimpleGrantedAuthority(getDefaultRowLevelRolePrefix() + rowLevelRoleCode);
    }

    /**
     * Returns the role prefix for the resource role. It is taken from the {@link GrantedAuthorityDefaults} if the
     * bean of this type is defined. Otherwise, the default ROLE_ value is returned.
     */
    public String getDefaultRolePrefix() {
        return defaultRolePrefix;
    }

    /**
     * Returns the role prefix for the row-level role (ROW_LEVEL_ROLE_ by default)
     */
    public String getDefaultRowLevelRolePrefix() {
        return securityProperties.getDefaultRowLevelRolePrefix();
    }
}
