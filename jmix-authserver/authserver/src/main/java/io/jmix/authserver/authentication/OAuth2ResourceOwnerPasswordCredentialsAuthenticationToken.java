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

package io.jmix.authserver.authentication;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An {@link Authentication} implementation used for the OAuth 2.0 Resource Owner Password Credentials Grant.
 *
 * @see OAuth2ResourceOwnerPasswordCredentialsAuthenticationProvider
 */
public class OAuth2ResourceOwnerPasswordCredentialsAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {

    private final String username;

    private final String password;

    private final Set<String> scopes;

    protected OAuth2ResourceOwnerPasswordCredentialsAuthenticationToken(String username,
                                                                        String password,
                                                                        Authentication clientPrincipal,
                                                                        @Nullable Set<String> scopes,
                                                                        @Nullable Map<String, Object> additionalParameters) {
        super(PasswordAuthorizationGrantTypeHelper.PASSWORD_GRANT_TYPE, clientPrincipal, additionalParameters);
        this.username = username;
        this.password = password;
        this.scopes = Collections.unmodifiableSet(
                scopes != null ? new HashSet<>(scopes) : Collections.emptySet());
    }

    /**
     * Returns the resource owner username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the resource owner password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the requested scope(s).
     *
     * @return the requested scope(s), or an empty {@code Set} if not available
     */
    public Set<String> getScopes() {
        return scopes;
    }
}