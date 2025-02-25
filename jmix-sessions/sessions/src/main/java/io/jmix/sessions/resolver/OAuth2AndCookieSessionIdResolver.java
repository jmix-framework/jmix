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

package io.jmix.sessions.resolver;

import com.google.common.base.Strings;
import io.jmix.core.session.SessionData;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;

import java.util.*;

/**
 * Uses oauth2 access token and cookies to search session id.
 **/
public class OAuth2AndCookieSessionIdResolver implements HttpSessionIdResolver {

    public static final String SESSION_ID = "OAuth2.SESSION_ID";
    public static final String ACCESS_TOKEN = "OAuth2.ACCESS_TOKEN";

    protected OAuth2AuthorizationService oAuth2AuthorizationService;

    protected HttpSessionIdResolver cookieHttpSessionIdResolver = new CookieHttpSessionIdResolver();
    protected ObjectProvider<SessionData> sessionDataProvider;

    @Override
    public List<String> resolveSessionIds(HttpServletRequest request) {
        if (isOAuth2Request(request)) {
            return resolveOAuth2SessionIds(request);
        } else {
            return cookieHttpSessionIdResolver.resolveSessionIds(request);
        }
    }

    @Override
    public void setSessionId(HttpServletRequest request, HttpServletResponse response, String sessionId) {
        if (isOAuth2Request(request)) {
            setOAuth2SessionId(request, sessionId);
        } else {
            cookieHttpSessionIdResolver.setSessionId(request, response, sessionId);
        }
    }

    @Override
    public void expireSession(HttpServletRequest request, HttpServletResponse response) {
        if (isOAuth2Request(request)) {
            expireOAuth2Session(request);
        } else {
            cookieHttpSessionIdResolver.expireSession(request, response);
        }
    }

    protected List<String> resolveOAuth2SessionIds(HttpServletRequest request) {
        String sessionId = null;
        OAuth2Authorization auth = null;
        if (isAccessTokenRequest(request)) {
            auth = oAuth2AuthorizationService.findByToken(getAccessToken(request), OAuth2TokenType.ACCESS_TOKEN);
        } else if (isRefreshTokenRequest(request)) {
            auth = oAuth2AuthorizationService.findByToken(getRefreshToken(request), OAuth2TokenType.REFRESH_TOKEN);
        }
        if (auth != null) {
            sessionId = (String) auth.getAttributes().get(SESSION_ID);
        }

        return sessionId != null ? Collections.singletonList(sessionId) : Collections.emptyList();
    }

    protected void setOAuth2SessionId(HttpServletRequest request, String sessionId) {
        String tokenValue = getAccessToken(request);

        if (tokenValue == null) {
            SessionData sessionData = sessionDataProvider.getIfAvailable();
            if (sessionData != null) {
                tokenValue = (String) sessionData.getAttribute(ACCESS_TOKEN);
            }
        }

        if (tokenValue != null) {
            OAuth2Authorization auth = oAuth2AuthorizationService.findByToken(tokenValue, OAuth2TokenType.ACCESS_TOKEN);

            if (auth != null) {
                String originalSessionId = (String) auth.getAttributes().get(SESSION_ID);
                if (!Objects.equals(originalSessionId, sessionId)) {
                    OAuth2Authorization updated = OAuth2Authorization.from(auth).attributes(c -> c.put(SESSION_ID, sessionId)).build();
                    oAuth2AuthorizationService.save(updated);
                }
            }
        }
    }

    protected void expireOAuth2Session(HttpServletRequest request) {
        String tokenValue = getAccessToken(request);
        if (tokenValue != null) {
            OAuth2Authorization auth = oAuth2AuthorizationService.findByToken(tokenValue, OAuth2TokenType.ACCESS_TOKEN);
            if (auth != null) {
                oAuth2AuthorizationService.remove(auth);
            }
        }
    }

    protected boolean isOAuth2Request(HttpServletRequest request) {
        return isAccessTokenRequest(request) || isRefreshTokenRequest(request);
    }

    protected boolean isRefreshTokenRequest(HttpServletRequest request) {
        return AuthorizationGrantType.REFRESH_TOKEN.getValue().equals(request.getParameter("grant_type")) &&
                !Strings.isNullOrEmpty(getRefreshToken(request));
    }

    protected boolean isAccessTokenRequest(HttpServletRequest request) {
        return !Strings.isNullOrEmpty(getAccessToken(request));
    }

    protected String getAccessToken(HttpServletRequest request) {
        String header = request.getHeader("authorization");
        if (header != null && header.startsWith("Bearer")) {
            return header.substring(7);
        }

        return null;
    }

    protected String getRefreshToken(HttpServletRequest request) {
        return request.getParameter(OAuth2ParameterNames.REFRESH_TOKEN);
    }

    @Autowired
    public void setOauth2AuthorizationService(OAuth2AuthorizationService oAuth2AuthorizationService) {
        this.oAuth2AuthorizationService = oAuth2AuthorizationService;
    }

    @Autowired
    public void setSessionDataProvider(ObjectProvider<SessionData> sessionDataProvider) {
        this.sessionDataProvider = sessionDataProvider;
    }
}
