/*
 * Copyright 2021 Haulmont.
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

package io.jmix.rest.security.impl;

import io.jmix.core.AccessManager;
import io.jmix.core.Messages;
import io.jmix.core.security.SecurityContextHelper;
import io.jmix.oidc.resourceserver.BeforeResourceServerApiInvocationEvent;
import io.jmix.rest.accesscontext.RestAccessContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.util.AntPathMatcher;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collection;

/**
 * A copy of {@link RestBeforeInvocationEventListener} that works with OIDC add-on events.
 *
 * TODO get rid of code duplication
 */
public class RestBeforeResourceServerApiInvocationEventListener {

    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected RestAuthorizedUrlsProvider restAuthorizedUrlsProvider;
    @Autowired
    protected Messages messages;

    private static final Collection<String> REST_AUTHORIZED_URLS = Arrays.asList(
            "/rest/entities/**", "/rest/services/**", "/rest/queries/**",
            "/rest/messages/**", "/rest/metadata/**", "/rest/files/**",
            "/rest/userInfo", "/rest/permissions", "/rest/user-session/locale");

    @EventListener(BeforeResourceServerApiInvocationEvent.class)
    public void doListen(BeforeResourceServerApiInvocationEvent event) {
        if (shouldCheckRequest(event.getRequest())) {
            RestAccessContext restAccessContext = new RestAccessContext();
            Authentication currentAuthentication = SecurityContextHelper.getAuthentication();
            try {
                SecurityContextHelper.setAuthentication(event.getAuthentication());
                accessManager.applyRegisteredConstraints(restAccessContext);
            } finally {
                SecurityContextHelper.setAuthentication(currentAuthentication);
            }

            if (!restAccessContext.isPermitted()) {
                event.preventInvocation();
                event.setErrorCode(HttpStatus.FORBIDDEN.value());
                event.setErrorMessage(messages.getMessage("io.jmix.rest/restApiAccessDenied"));
            }
        }
    }

    protected boolean shouldCheckRequest(ServletRequest request) {
        String requestURI = ((HttpServletRequest) request).getRequestURI();
        AntPathMatcher antPathMatcher = new AntPathMatcher();

        for (String urlPattern : restAuthorizedUrlsProvider.getAnonymousUrlPatterns()) {
            if (antPathMatcher.match(urlPattern, requestURI)) {
                return false;
            }
        }

        for (String urlPattern : REST_AUTHORIZED_URLS) {
            if (antPathMatcher.match(urlPattern, requestURI)) {
                return true;
            }
        }

        for (String urlPattern : restAuthorizedUrlsProvider.getAuthenticatedUrlPatterns()) {
            if (antPathMatcher.match(urlPattern, requestURI)) {
                return true;
            }
        }

        return false;
    }
}