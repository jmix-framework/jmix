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

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression test for the credentials check of {@link JmixDaoAuthenticationProvider}.
 * <p>
 * Reproduces the scenario that broke with Spring Security 6.5.10 (CVE-2026-22746 fix): a user without a
 * local password (e.g. an LDAP-synchronized user) is loaded by the {@code DaoAuthenticationProvider}, the
 * pre-authentication check fails (the LDAP add-on rejects standard authentication for such users), and the
 * additional credentials check is then performed anyway against a {@code null}/empty stored password.
 * <p>
 * With a plain {@link org.springframework.security.authentication.dao.DaoAuthenticationProvider} and a
 * delegating password encoder this raises an {@link IllegalArgumentException} that escapes the
 * {@code ProviderManager} and aborts the whole authentication. {@link JmixDaoAuthenticationProvider} must
 * instead fail with an {@link AuthenticationException}, so the {@code ProviderManager} can fall through to
 * the next provider (the LDAP bind provider).
 */
class JmixDaoAuthenticationProviderTest {

    private static final PasswordEncoder DELEGATING_ENCODER =
            PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private static final UserDetailsChecker REJECTING_PRE_CHECK = user -> {
        throw new BadCredentialsException("Current user cannot be authenticated via standard authentication");
    };

    private static final UserDetailsChecker PASSING_CHECK = user -> {
    };

    @Test
    void passwordlessUser_whenPreCheckFails_failsWithAuthenticationException() {
        // The reported case: stored password is null and the pre-check rejects standard authentication.
        JmixDaoAuthenticationProvider provider = newProvider(userService(null), REJECTING_PRE_CHECK);

        // Must be an AuthenticationException (not IllegalArgumentException from the password encoder),
        // otherwise the ProviderManager aborts instead of trying the next (LDAP) provider.
        assertThrows(AuthenticationException.class,
                () -> provider.authenticate(token("alice", "secret")));
    }

    @Test
    void passwordlessUser_withEmptyPassword_whenPreCheckFails_failsWithAuthenticationException() {
        JmixDaoAuthenticationProvider provider = newProvider(userService(""), REJECTING_PRE_CHECK);

        assertThrows(AuthenticationException.class,
                () -> provider.authenticate(token("alice", "secret")));
    }

    @Test
    void passwordlessUser_whenPreCheckPasses_failsWithBadCredentials() {
        // Even without the pre-check gate, a user with no local password cannot be authenticated here.
        JmixDaoAuthenticationProvider provider = newProvider(userService(null), PASSING_CHECK);

        assertThrows(BadCredentialsException.class,
                () -> provider.authenticate(token("alice", "secret")));
    }

    @Test
    void userWithValidPassword_authenticatesSuccessfully() {
        // The override must not break normal username/password authentication.
        String encoded = DELEGATING_ENCODER.encode("secret");
        JmixDaoAuthenticationProvider provider = newProvider(userService(encoded), PASSING_CHECK);

        Authentication result = provider.authenticate(token("bob", "secret"));

        assertTrue(result.isAuthenticated());
    }

    @Test
    void userWithValidPassword_andWrongCredentials_failsWithBadCredentials() {
        String encoded = DELEGATING_ENCODER.encode("secret");
        JmixDaoAuthenticationProvider provider = newProvider(userService(encoded), PASSING_CHECK);

        assertThrows(BadCredentialsException.class,
                () -> provider.authenticate(token("bob", "wrong")));
    }

    private JmixDaoAuthenticationProvider newProvider(UserDetailsService userDetailsService,
                                                      UserDetailsChecker preAuthenticationChecks) {
        JmixDaoAuthenticationProvider provider = new JmixDaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(DELEGATING_ENCODER);
        provider.setPreAuthenticationChecks(preAuthenticationChecks);
        return provider;
    }

    private UserDetailsService userService(String storedPassword) {
        return username -> new TestUser(username, storedPassword);
    }

    private UsernamePasswordAuthenticationToken token(String username, String password) {
        return new UsernamePasswordAuthenticationToken(username, password);
    }

    /**
     * Minimal {@link UserDetails} that allows a {@code null}/empty password, unlike
     * {@link org.springframework.security.core.userdetails.User}.
     */
    private static final class TestUser implements UserDetails {

        private final String username;
        private final String password;

        private TestUser(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return AuthorityUtils.NO_AUTHORITIES;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}
