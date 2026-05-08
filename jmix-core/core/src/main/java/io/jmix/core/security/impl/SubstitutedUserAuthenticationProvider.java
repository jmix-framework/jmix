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

package io.jmix.core.security.impl;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.ArrayList;
import java.util.Collection;

public class SubstitutedUserAuthenticationProvider implements AuthenticationProvider {

    private static final String SUBSTITUTION_AUTHORITY = "FACTOR_SUBSTITUTION";

    private UserDetailsService userDetailsService;

    public SubstitutedUserAuthenticationProvider(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SubstitutedUserAuthenticationToken substitutedAuthentication = (SubstitutedUserAuthenticationToken) authentication;

        String substitutedUserName = (String) substitutedAuthentication.getSubstitutedPrincipal();
        UserDetails substitutedUser = userDetailsService.loadUserByUsername(substitutedUserName);

        Collection<? extends GrantedAuthority> authorities = getEffectiveAuthorities(substitutedAuthentication, substitutedUser);

        SubstitutedUserAuthenticationToken authenticated = new SubstitutedUserAuthenticationToken(
                substitutedAuthentication,
                substitutedUser,
                authorities
        );

        return authenticated;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SubstitutedUserAuthenticationToken.class.isAssignableFrom(authentication);
    }

    /**
     * Merge authorities:
     * <ul>
     *     <li>Substituted user authorities</li>
     *     <li>Original user factor authorities</li>
     *     <li>Substitution factor authority</li>
     * </ul>
     *
     * @param substitutedAuthentication token with original authorities
     * @param substitutedUser           user details with substituted authorities
     * @return merged authorities
     */
    protected Collection<? extends GrantedAuthority> getEffectiveAuthorities(SubstitutedUserAuthenticationToken substitutedAuthentication,
                                                                             UserDetails substitutedUser) {
        Collection<GrantedAuthority> mergedAuthorities = new ArrayList<>(substitutedUser.getAuthorities());
        Collection<GrantedAuthority> originalAuthorities = substitutedAuthentication.getAuthorities();
        substitutedAuthentication.getAuthorities().stream()
                .filter(authority -> authority instanceof FactorGrantedAuthority)
                .forEach(autority -> mergedAuthorities.add(autority));

        mergedAuthorities.add(FactorGrantedAuthority.fromAuthority(SUBSTITUTION_AUTHORITY));
        return mergedAuthorities;
    }
}
