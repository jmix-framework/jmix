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

package io.jmix.simplesecurity.role;

import io.jmix.simplesecurity.SimpleSecurityProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * Utility class for working with granted authorities in simple-security module.
 */
@Component("simsec_GrantedAuthorityUtils")
public class GrantedAuthorityUtils {

    protected SimpleSecurityProperties simpleSecurityProperties;

    protected GrantedAuthorityDefaults grantedAuthorityDefaults;

    protected String defaultRolePrefix = "ROLE_";

    public GrantedAuthorityUtils(SimpleSecurityProperties simpleSecurityProperties) {
        this.simpleSecurityProperties = simpleSecurityProperties;
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
     * Returns the role prefix for the resource role. It is taken from the {@link GrantedAuthorityDefaults} if the bean
     * of this type is defined. Otherwise, the default ROLE_ value is returned.
     */
    public String getDefaultRolePrefix() {
        return defaultRolePrefix;
    }

    /**
     * Prefixes role with defaultRolePrefix if role does not already start with defaultRolePrefix.
     *
     * @return role name prefixed by defaultRolePrefix
     */
    public String getRoleWithDefaultPrefix(String role) {
        if (role.startsWith(defaultRolePrefix)) {
            return role;
        }
        return defaultRolePrefix + role;
    }

    /**
     * Creates {@link GrantedAuthority} for the given {@code roleName}. The roleName may start with a default role
     * prefix (ROLE_) and may not. Both cases are supported.
     */
    public GrantedAuthority createRoleGrantedAuthority(String roleName) {
        return new SimpleGrantedAuthority(getRoleWithDefaultPrefix(roleName));
    }
}