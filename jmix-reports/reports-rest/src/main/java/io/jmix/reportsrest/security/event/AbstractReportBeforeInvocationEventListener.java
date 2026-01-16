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
import io.jmix.reportsrest.security.accesscontext.ReportRestAccessContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.util.AntPathMatcher;

/**
 * Base class for report REST API access listeners that checks "reports.rest.enabled" specific policy
 * for /rest/reports/** requests. If the current user doesn't have this policy then the FORBIDDEN error is thrown.
 */
public abstract class AbstractReportBeforeInvocationEventListener {

    protected static final String REPORT_AUTHORIZED_URL = "/rest/reports/**";
    protected static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

    @Autowired
    protected AccessManager accessManager;

    /**
     * Checks if the request should be validated and applies access constraints.
     *
     * @param request        the servlet request
     * @param authentication the authentication to check permissions for
     * @return {@code true} if access is permitted, {@code false} otherwise
     */
    protected boolean checkAccess(ServletRequest request, Authentication authentication) {
        if (!shouldCheckRequest(request)) {
            return true;
        }

        ReportRestAccessContext reportRestAccessContext = new ReportRestAccessContext();
        Authentication currentAuthentication = SecurityContextHelper.getAuthentication();
        try {
            SecurityContextHelper.setAuthentication(authentication);
            accessManager.applyRegisteredConstraints(reportRestAccessContext);
        } finally {
            SecurityContextHelper.setAuthentication(currentAuthentication);
        }

        return reportRestAccessContext.isPermitted();
    }

    /**
     * Returns the HTTP status code for forbidden access.
     *
     * @return the forbidden status code
     */
    protected int getForbiddenErrorCode() {
        return HttpStatus.FORBIDDEN.value();
    }

    /**
     * Checks if the request URI matches the report REST API pattern.
     *
     * @param request the servlet request
     * @return {@code true} if the request should be checked, {@code false} otherwise
     */
    protected boolean shouldCheckRequest(ServletRequest request) {
        String requestURI = ((HttpServletRequest) request).getRequestURI();
        return ANT_PATH_MATCHER.match(REPORT_AUTHORIZED_URL, requestURI);
    }
}
