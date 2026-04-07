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

package io.jmix.authserver.service;

import io.jmix.authserver.AuthServerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

public class JmixJdbcOAuth2AuthorizationService extends JdbcOAuth2AuthorizationService {

    @Autowired
    protected AuthServerProperties authServerProperties;

    public JmixJdbcOAuth2AuthorizationService(JdbcOperations jdbcOperations,
                                              RegisteredClientRepository registeredClientRepository) {
        super(jdbcOperations, registeredClientRepository);
    }

    public JmixJdbcOAuth2AuthorizationService(JdbcOperations jdbcOperations,
                                              RegisteredClientRepository registeredClientRepository,
                                              LobHandler lobHandler) {
        super(jdbcOperations, registeredClientRepository, lobHandler);
    }

    @Override
    public void save(OAuth2Authorization authorization) {
        if (shouldRemoveToken(authorization)) {
            this.remove(authorization);
        } else {
            super.save(authorization);
        }
    }

    protected boolean shouldRemoveToken(OAuth2Authorization authorization) {
        return isTokenRemovalEnabled() && isRevoked(authorization);
    }

    protected boolean isTokenRemovalEnabled() {
        return authServerProperties.isRemoveTokenOnRevoke();
    }

    protected boolean isRevoked(OAuth2Authorization authorization) {
        OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken = authorization.getRefreshToken();
        OAuth2Authorization.Token<OAuth2AccessToken> accessToken = authorization.getAccessToken();

        // Refresh Token was explicitly revoked - remove the record
        if (refreshToken != null && refreshToken.isInvalidated()) {
            return true;
        }

        // Access Token was explicitly revoked.
        if (accessToken != null && accessToken.isInvalidated()) {
            // Only remove the record if the Refresh Token is also "inactive" (invalidated, expired, or never existed)
            if (refreshToken == null || !refreshToken.isActive()) {
                return true;
            }
        }

        // Nothing was revoked, Refresh Token is active or both tokens are missing (Authorization Code case)
        return false;
    }
}