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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;

import java.util.Set;

/**
 * Extension of the default strategy for obtaining user role information from the directory. In case there is an implementation
 * of {@link LdapUserAdditionalRoleProvider} interface it delegates fetching extra user roles to this implementation.
 *
 * @see LdapUserAdditionalRoleProvider
 */
public class JmixLdapAuthoritiesPopulator extends DefaultLdapAuthoritiesPopulator {

    protected LdapUserAdditionalRoleProvider ldapUserAdditionalRoleProvider;

    public JmixLdapAuthoritiesPopulator(ContextSource contextSource, String groupSearchBase) {
        super(contextSource, groupSearchBase);
    }

    @Autowired(required = false)
    public void setUserAdditionalRoleProvider(LdapUserAdditionalRoleProvider ldapUserAdditionalRoleProvider) {
        this.ldapUserAdditionalRoleProvider = ldapUserAdditionalRoleProvider;
    }

    @Override
    protected Set<GrantedAuthority> getAdditionalRoles(DirContextOperations user, String username) {
        if (ldapUserAdditionalRoleProvider != null) {
            return ldapUserAdditionalRoleProvider.getAdditionalRoles(user, username);
        }

        return super.getAdditionalRoles(user, username);
    }

}
