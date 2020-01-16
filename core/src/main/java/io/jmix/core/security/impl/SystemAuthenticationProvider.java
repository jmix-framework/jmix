/*
 * Copyright 2019 Haulmont.
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

import io.jmix.core.security.SystemAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collections;

public class SystemAuthenticationProvider implements AuthenticationProvider {

    private UserDetailsService userDetailsService;

    public SystemAuthenticationProvider(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!(authentication instanceof SystemAuthenticationToken)) {
            throw new IllegalArgumentException(String.format(
                    "%s does not support %s", getClass().getSimpleName(), authentication.getClass()));
        }

        String username = authentication.getName();
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        return new SystemAuthenticationToken(userDetails, Collections.emptyList());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SystemAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
