/*
 * Copyright 2026 Haulmont.
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

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;

/**
 * A {@link DaoAuthenticationProvider} that never passes a {@code null} or blank stored password to the
 * configured {@link org.springframework.security.crypto.password.PasswordEncoder}.
 * <p>
 * Users without a local password (for example, users synchronized from an external identity provider
 * such as LDAP) cannot be authenticated by this provider. For such users the credentials check fails
 * with a regular {@link BadCredentialsException} instead of letting a delegating password encoder raise
 * an {@link IllegalArgumentException} ("The name of the password encoder is improperly formatted or
 * incomplete...") on a {@code null}/empty stored password.
 * <p>
 * This matters since Spring Security 6.5.10 (fix for CVE-2026-22746): the additional credentials check is
 * now performed even when pre-authentication checks fail (see
 * {@link org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider#setAlwaysPerformAdditionalChecksOnUser(boolean)}),
 * and an {@link IllegalArgumentException} raised there is not treated as an authentication failure, so it
 * aborts the whole {@link org.springframework.security.authentication.ProviderManager} chain instead of
 * letting the next provider (e.g. the LDAP bind provider) authenticate the user.
 */
public class JmixDaoAuthenticationProvider extends DaoAuthenticationProvider {

    public JmixDaoAuthenticationProvider(UserDetailsService userDetailsService) {
        super(userDetailsService);
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {
        if (!StringUtils.hasText(userDetails.getPassword())) {
            throw new BadCredentialsException(messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }
        super.additionalAuthenticationChecks(userDetails, authentication);
    }
}
