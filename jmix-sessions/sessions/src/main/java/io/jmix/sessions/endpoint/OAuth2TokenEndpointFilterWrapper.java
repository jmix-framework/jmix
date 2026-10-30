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

package io.jmix.sessions.endpoint;

import io.jmix.core.security.ClientDetails;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.util.RequestLocaleProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.web.OAuth2TokenEndpointFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class OAuth2TokenEndpointFilterWrapper extends OncePerRequestFilter {
    protected OAuth2TokenEndpointFilter delegate;
    protected RequestLocaleProvider requestLocaleProvider;

    public OAuth2TokenEndpointFilterWrapper(OAuth2TokenEndpointFilter delegate,
                                            RequestLocaleProvider requestLocaleProvider) {
        this.delegate = delegate;
        this.requestLocaleProvider = requestLocaleProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication instanceof OAuth2ClientAuthenticationToken t) {
                ClientDetails details = ClientDetails.builder()
                        .clientType("API")
                        .scope(SecurityScope.API)
                        .locale(requestLocaleProvider.getLocale(request))
                        .sessionId(request.getSession().getId())
                        .build();
                t.setDetails(details);

            }
        }
        delegate.doFilter(request, response, filterChain);
    }
}
