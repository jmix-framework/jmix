/*
 * Copyright 2020 Haulmont.
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

package io.jmix.rest.security.oauth;

import io.jmix.core.security.ClientDetails;
import io.jmix.core.session.SessionData;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.session.web.http.HttpSessionIdResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A {@link HttpSessionIdResolver} that uses {@link OAuth2AccessToken} to search session id.
 * This class provides stateful sessions for the access token.
 **/
public class OAuth2AccessTokenSessionIdResolver implements HttpSessionIdResolver {

    public static final String SESSION_ID = OAuth2AccessTokenSessionIdResolver.class.getSimpleName() + ".SESSION_ID";

    public static final String ACCESS_TOKEN = OAuth2AccessTokenSessionIdResolver.class.getSimpleName() + ".ACCESS_TOKEN";

    @Autowired
    protected TokenStore tokenStore;

    @Autowired
    private ObjectProvider<SessionData> sessionDataProvider;

    @Override
    public List<String> resolveSessionIds(HttpServletRequest request) {
        String tokenValue = fromRequest(request);
        if (tokenValue != null) {
            OAuth2AccessToken token = tokenStore.readAccessToken(tokenValue);
            if (token != null) {
                String sessionId = (String) token.getAdditionalInformation().get(SESSION_ID);
                if (sessionId != null) {
                    return Collections.singletonList(sessionId);
                }
            }
        } else if (isRefreshTokenRequested(request)) {
            String refreshTokenValue = request.getParameter("refresh_token");
            if (refreshTokenValue != null) {
                OAuth2RefreshToken refreshToken = tokenStore.readRefreshToken(refreshTokenValue);
                if (refreshToken != null) {
                    OAuth2Authentication authentication = tokenStore.readAuthenticationForRefreshToken(refreshToken);
                    if (authentication.getDetails() != null && authentication.getDetails() instanceof ClientDetails) {
                        ClientDetails ClientDetails = (ClientDetails) authentication.getDetails();
                        return Collections.singletonList(ClientDetails.getSessionId());
                    }
                }
            }
        }

        return Collections.emptyList();
    }

    protected boolean isRefreshTokenRequested(HttpServletRequest request) {
        return "refresh_token".equals(request.getParameter("grant_type"));
    }

    private String fromRequest(HttpServletRequest request) {
        return (String) request.getAttribute(OAuth2AuthenticationDetails.ACCESS_TOKEN_VALUE);
    }

    @Override
    public void setSessionId(HttpServletRequest request, HttpServletResponse response, String sessionId) {
        String tokenValue = fromRequest(request);
        OAuth2AccessToken token;
        if (tokenValue == null) {
            SessionData sessionData = sessionDataProvider.getIfAvailable();
            if (sessionData != null) {
                tokenValue = (String) sessionData.getAttribute(ACCESS_TOKEN);
            }
        }
        if (tokenValue != null) {
            token = tokenStore.readAccessToken(tokenValue);
            if (token != null) {
                String originalSessionId = (String) token.getAdditionalInformation().get(SESSION_ID);
                if (!Objects.equals(originalSessionId, sessionId)) {
                    token.getAdditionalInformation().put(SESSION_ID, sessionId);
                    tokenStore.storeAccessToken(token, tokenStore.readAuthentication(token));
                }
            }
        }
    }

    @Override
    public void expireSession(HttpServletRequest request, HttpServletResponse response) {
        String tokenValue = fromRequest(request);
        if (tokenValue != null) {
            OAuth2AccessToken oAuth2AccessToken = tokenStore.readAccessToken(tokenValue);
            if (oAuth2AccessToken != null) {
                tokenStore.removeAccessToken(oAuth2AccessToken);
            }
        }
    }
}
