/*
 * Copyright 2025 Haulmont.
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

import io.jmix.authserver.AuthServerProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.web.authentication.logout.LogoutHandler;

/**
 * {@link LogoutHandler} that tries to get bearer token request and remove it from token storage.
 */
public class TokenRevocationLogoutHandler implements LogoutHandler {

    private static final Logger log = LoggerFactory.getLogger(TokenRevocationLogoutHandler.class);

    private final OAuth2AuthorizationService authorizationService;

    private final TokenValueResolver tokenValueResolver;

    public TokenRevocationLogoutHandler(OAuth2AuthorizationService authorizationService,
                                        AuthServerProperties authServerProperties) {
        this.authorizationService = authorizationService;
        this.tokenValueResolver = createTokenValueResolver(authServerProperties);
    }

    protected TokenValueResolver createTokenValueResolver(AuthServerProperties authServerProperties) {
        return TokenValueResolver.builder()
                .withHeaderName(authServerProperties.getLogoutAccessTokenHeaderName())
                .withBodyFormParameterSupportEnabled(authServerProperties.isLogoutBodyFormParameterCheckForTokenEnabled())
                .withBodyFormParameterName(authServerProperties.getLogoutAccessTokenBodyFormParameterName())
                .withUrlParameterSupportEnabled(authServerProperties.isLogoutUrlParameterCheckForTokenEnabled())
                .withUrlParameterName(authServerProperties.getLogoutAccessTokenUrlParameterName())
                .build();
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String token = tokenValueResolver.resolve(request);
        if (token == null) {
            log.debug("Logout: No token found in request");
            return;
        }

        OAuth2Authorization authorization = authorizationService.findByToken(token, OAuth2TokenType.ACCESS_TOKEN);
        if (authorization != null) {
            log.debug("Logout: Remove OAuth2Authorization found by token value");
            authorizationService.remove(authorization);
        } else {
            log.debug("Logout: OAuth2Authorization was not found by token value");
        }
    }
}
