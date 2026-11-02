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

package io.jmix.sessions.endpoint;

import io.jmix.authserver.authentication.IssuedAuthorizationProcessor;
import io.jmix.authserver.service.cleanup.impl.UserInvalidationListener;
import io.jmix.core.session.SessionData;
import io.jmix.sessions.resolver.OAuth2AndCookieSessionIdResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.stereotype.Component;

/**
 * Binds the session of a token request to the issued authorization, so that the requests with its access or refresh
 * token use this session.
 */
@Component("sess_OAuth2SessionBindingProcessor")
public class OAuth2SessionBindingProcessor implements IssuedAuthorizationProcessor {

    @Autowired
    protected ObjectProvider<SessionData> sessionDataProvider;

    @Autowired
    protected OAuth2AuthorizationService authorizationService;

    @NullMarked
    @Override
    public OAuth2Authorization process(OAuth2Authorization authorization, HttpServletRequest request) {
        OAuth2Authorization.Token<OAuth2AccessToken> accessToken = authorization.getAccessToken();
        if (accessToken == null) {
            return authorization;
        }

        SessionData sessionData = sessionDataProvider.getObject();
        sessionData.setAttribute(OAuth2AndCookieSessionIdResolver.ACCESS_TOKEN, accessToken.getToken().getTokenValue());

        OAuth2Authorization boundAuthorization = OAuth2Authorization.from(authorization)
                .attribute(OAuth2AndCookieSessionIdResolver.SESSION_ID, sessionData.getSessionId())
                .attribute(UserInvalidationListener.AUTHORIZATION_ID, authorization.getId())
                .build();
        authorizationService.save(boundAuthorization);
        return boundAuthorization;
    }
}
