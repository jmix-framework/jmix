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

package io.jmix.rest;

import io.jmix.core.security.UserSession;
import io.jmix.core.security.UserSessions;
import io.jmix.rest.api.auth.ClientProxyTokenStore;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class JmixRestAuthorizationFilter extends OncePerRequestFilter {

    private static final String ACCESS_TOKEN_VALUE = "OAuth2AuthenticationDetails.ACCESS_TOKEN_VALUE";
    private static final String SESSION_ID = "sessionId";

    protected ClientProxyTokenStore clientProxyTokenStore;
    protected UserSessions userSessions;

    public JmixRestAuthorizationFilter(ClientProxyTokenStore clientProxyTokenStore, UserSessions userSessions) {
        this.clientProxyTokenStore = clientProxyTokenStore;
        this.userSessions = userSessions;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Object accessToken = request.getAttribute(ACCESS_TOKEN_VALUE);
        if (accessToken != null) {
            OAuth2Authentication oAuth2Authentication = clientProxyTokenStore.readAuthentication(accessToken.toString());
            //noinspection unchecked
            Map<String, Object> details = (Map<String, Object>) oAuth2Authentication.getUserAuthentication().getDetails();

            UUID sessionId = UUID.fromString(details.get(SESSION_ID).toString());
            UserSession userSession = userSessions.getAndRefresh(sessionId);
            SecurityContextHolder.getContext().setAuthentication(userSession);
        }
        filterChain.doFilter(request, response);
    }
}
