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

import io.jmix.authserver.event.AsResourceServerBeforeInvocationEvent;
import io.jmix.core.AccessManager;
import io.jmix.core.security.SecurityContextHelper;
import io.jmix.reportsrest.security.accesscontext.ReportRestAccessContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.util.AntPathMatcher;

/**
 * A listener for {@link AsResourceServerBeforeInvocationEvent} that checks "reports.rest.enabled" specific policy
 * for /rest/reports/** requests, managed by resource server of the Authorization Server add-on. If the current user
 * doesn't have this policy then the FORBIDDEN error is thrown.
 */
public class ReportAsResourceServerBeforeInvocationEventListener {

    private static final String REPORT_AUTHORIZED_URL = "/rest/reports/**";

    @Autowired
    protected AccessManager accessManager;

    @EventListener(AsResourceServerBeforeInvocationEvent.class)
    public void doListen(AsResourceServerBeforeInvocationEvent event) {
        if (shouldCheckRequest(event.getRequest())) {
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

    protected boolean shouldCheckRequest(ServletRequest request) {
        String requestURI = ((HttpServletRequest) request).getRequestURI();
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        return antPathMatcher.match(REPORT_AUTHORIZED_URL, requestURI);
    }
}