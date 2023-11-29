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

import com.google.common.base.Strings;
import io.jmix.core.security.ServiceUserProvider;
import io.jmix.core.security.SystemAuthenticationToken;
import io.jmix.core.security.UserRepository;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public class SystemAuthenticationProvider implements AuthenticationProvider {

    private UserRepository userRepository;

    private ServiceUserProvider serviceUserProvider;

    public SystemAuthenticationProvider(UserRepository userRepository, ServiceUserProvider serviceUserProvider) {
        this.userRepository = userRepository;
        this.serviceUserProvider = serviceUserProvider;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!(authentication instanceof SystemAuthenticationToken)) {
            throw new IllegalArgumentException(String.format(
                    "%s does not support %s", getClass().getSimpleName(), authentication.getClass()));
        }

        UserDetails userDetails;
        String username = authentication.getName();
        //todo MG check null or 'system'
        if (Strings.isNullOrEmpty(username)) {
            userDetails = serviceUserProvider.getSystemUser();
        } else {
            userDetails = userRepository.loadUserByUsername(username);
        }

        return new SystemAuthenticationToken(userDetails, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SystemAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
