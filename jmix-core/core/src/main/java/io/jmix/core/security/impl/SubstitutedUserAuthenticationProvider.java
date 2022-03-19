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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public class SubstitutedUserAuthenticationProvider implements AuthenticationProvider {

    private UserDetailsService userDetailsService;

    public SubstitutedUserAuthenticationProvider(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SubstitutedUserAuthenticationToken substitutedAuthentication = (SubstitutedUserAuthenticationToken) authentication;

        String substitutedUserName = (String) substitutedAuthentication.getSubstitutedPrincipal();
        UserDetails substitutedUser = userDetailsService.loadUserByUsername(substitutedUserName);

        SubstitutedUserAuthenticationToken authenticated = new SubstitutedUserAuthenticationToken(
                substitutedAuthentication,
                substitutedUser,
                substitutedUser.getAuthorities());

        return authenticated;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SubstitutedUserAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
