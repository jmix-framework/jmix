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

import io.jmix.ldap.LdapProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

import java.util.Collection;

/**
 * Maps LDAP user to user details obtained from {@link UserDetailsService}.
 */
public class UserDetailsServiceLdapUserDetailsMapper implements UserDetailsContextMapper {

    protected UserDetailsService userDetailsService;
    protected LdapUserDetailsSynchronizationStrategy synchronizationStrategy;
    protected LdapProperties ldapProperties;

    @Autowired
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Autowired(required = false)
    public void setSynchronizationStrategy(LdapUserDetailsSynchronizationStrategy synchronizationStrategy) {
        this.synchronizationStrategy = synchronizationStrategy;
    }

    @Autowired
    public void setLdapProperties(LdapProperties ldapProperties) {
        this.ldapProperties = ldapProperties;
    }

    @Override
    public UserDetails mapUserFromContext(DirContextOperations ctx, String username,
                                          Collection<? extends GrantedAuthority> authorities) {
        if (ldapProperties.getSynchronizeUserOnLogin() && synchronizationStrategy != null) {
            synchronizationStrategy.synchronizeUserDetails(ctx, username, authorities);
        }
        try {
            return userDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            throw new BadCredentialsException("Bad credentials", e);
        }
    }

    @Override
    public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
        throw new UnsupportedOperationException("UserDetailsServiceLdapUserDetailsMapper only supports reading from " +
                "a context. Please use a subclass if mapUserToContext() is required.");
    }
}
