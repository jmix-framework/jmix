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

package io.jmix.ldap.userdetails;

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;

/**
 * Extension point for fetching additional user roles for LDAP user.
 */
public interface LdapUserAdditionalRoleProvider {

    /**
     * Obtains for given user additional roles which will be merged with those obtained by the group search.
     * <p>
     * Usage example:
     * <pre>{@code
     *     Set<GrantedAuthority> authorities = new HashSet<>();
     *     String customUserRole = user.getStringAttribute("uid");
     *     if (!Strings.isNullOrEmpty(customUserRole)) {
     *         authorities.add(new SimpleGrantedAuthority(customUserRole));
     *     }
     *     return authorities;
     * }</pre>
     *
     * @param user the user for which it is necessary to calculate additional roles
     * @return the extra roles for provided {@code user}
     */
    Set<GrantedAuthority> getAdditionalRoles(DirContextOperations user, String username);

}
