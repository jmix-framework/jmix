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

package io.jmix.reportsrest.security.event;

import io.jmix.core.AccessManager;
import io.jmix.core.security.SecurityContextHelper;
import io.jmix.reportsrest.security.ReportsAuthorizedUrlsProvider;
import io.jmix.reportsrest.security.accesscontext.ReportRestAccessContext;
import io.jmix.securityoauth2.event.BeforeInvocationEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.util.AntPathMatcher;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

public class ReportBeforeInvocationEventListener {

    @Autowired
    protected ReportsAuthorizedUrlsProvider reportsAuthorizedUrlsProvider;

    @Autowired
    protected AccessManager accessManager;

    @EventListener(BeforeInvocationEvent.class)
    public void doListen(BeforeInvocationEvent event) {
        Collection<String> authenticatedUrlPatterns = reportsAuthorizedUrlsProvider.getAuthenticatedUrlPatterns();
        if (shouldCheckRequest(event.getRequest(), authenticatedUrlPatterns)) {
            ReportRestAccessContext reportRestAccessContext = new ReportRestAccessContext();
            Authentication currentAuthentication = SecurityContextHelper.getAuthentication();
            try {
                SecurityContextHelper.setAuthentication(event.getAuthentication());
                accessManager.applyRegisteredConstraints(reportRestAccessContext);
            } finally {
                SecurityContextHelper.setAuthentication(currentAuthentication);
            }

            if (!reportRestAccessContext.isPermitted()) {
                event.preventInvocation();
                event.setErrorCode(HttpStatus.FORBIDDEN.value());
            }
        }
    }

    protected boolean shouldCheckRequest(ServletRequest request, Collection<String> authenticatedUrlPatterns) {
        String requestURI = ((HttpServletRequest) request).getRequestURI();
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        for (String authenticatedUrlPattern : authenticatedUrlPatterns) {
            if (antPathMatcher.match(authenticatedUrlPattern, requestURI)) {
                return true;
            }
        }

        return false;
    }
}