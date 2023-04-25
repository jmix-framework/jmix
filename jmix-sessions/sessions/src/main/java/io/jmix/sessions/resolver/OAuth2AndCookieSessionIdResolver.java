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
import io.jmix.core.security.ClientDetails;
import io.jmix.core.session.SessionData;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Uses oauth2 access token and cookies to search session id.
 **/
public class OAuth2AndCookieSessionIdResolver implements HttpSessionIdResolver {

    private static final String SESSION_ID = "OAuth2.SESSION_ID";
    private static final String ACCESS_TOKEN = "OAuth2.ACCESS_TOKEN";

    protected TokenStore tokenStore;
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
        if (isAccessTokenRequest(request)) {
            OAuth2AccessToken token = tokenStore.readAccessToken(getAccessToken(request));
            if (token != null) {
                sessionId = (String) token.getAdditionalInformation().get(SESSION_ID);
            }
        } else if (isRefreshTokenRequest(request)) {
            OAuth2RefreshToken token = tokenStore.readRefreshToken(getRefreshToken(request));
            if (token != null) {
                OAuth2Authentication authentication = tokenStore.readAuthenticationForRefreshToken(token);
                if (authentication.getDetails() instanceof ClientDetails) {
                    sessionId = ((ClientDetails) authentication.getDetails()).getSessionId();
                }
            }
        }

        return sessionId != null ? Collections.singletonList(sessionId) : Collections.emptyList();
    }

    protected void setOAuth2SessionId(HttpServletRequest request, String sessionId) {
        String tokenValue = getAccessToken(request);
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

    protected void expireOAuth2Session(HttpServletRequest request) {
        String tokenValue = getAccessToken(request);
        if (tokenValue != null) {
            OAuth2AccessToken oAuth2AccessToken = tokenStore.readAccessToken(tokenValue);
            if (oAuth2AccessToken != null) {
                tokenStore.removeAccessToken(oAuth2AccessToken);
            }
        }
    }

    protected boolean isOAuth2Request(HttpServletRequest request) {
        return isAccessTokenRequest(request) || isRefreshTokenRequest(request);
    }

    protected boolean isRefreshTokenRequest(HttpServletRequest request) {
        return OAuth2AccessToken.REFRESH_TOKEN.equals(request.getParameter("grant_type")) &&
                !Strings.isNullOrEmpty(getRefreshToken(request));
    }

    protected boolean isAccessTokenRequest(HttpServletRequest request) {
        return !Strings.isNullOrEmpty(getAccessToken(request));
    }

    protected String getAccessToken(HttpServletRequest request) {
        return (String) request.getAttribute(OAuth2AuthenticationDetails.ACCESS_TOKEN_VALUE);
    }

    protected String getRefreshToken(HttpServletRequest request) {
        return request.getParameter(OAuth2AccessToken.REFRESH_TOKEN);
    }

    @Autowired
    public void setTokenStore(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @Autowired
    public void setSessionDataProvider(ObjectProvider<SessionData> sessionDataProvider) {
        this.sessionDataProvider = sessionDataProvider;
    }
}
