/*
 * Copyright 2022 Haulmont.
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

package io.jmix.oidc.resourceserver;

import com.google.common.base.Strings;
import io.jmix.core.security.AccessDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A copy of io.jmix.securityoauth2.impl.LastSecurityFilter. It's purpose is to throw before- and after- events for
 * resource server API invocations. These events are used for checking security constraints and preventing API access
 * for users with insufficient permissions.
 *
 * TODO get rid of code duplication
 */
public class OidcResourceServerLastSecurityFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(OidcResourceServerLastSecurityFilter.class);

    protected ApplicationEventPublisher applicationEventPublisher;

    public OidcResourceServerLastSecurityFilter(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (applicationEventPublisher != null && authentication != null) {
                BeforeResourceServerApiInvocationEvent beforeInvocationEvent = new BeforeResourceServerApiInvocationEvent(authentication, request, response);
                applicationEventPublisher.publishEvent(beforeInvocationEvent);

                boolean invocationPrevented = beforeInvocationEvent.isInvocationPrevented();

                try {
                    if (!invocationPrevented) {
                        filterChain.doFilter(request, response);
                    } else {
                        log.debug("Request invocation prevented by BeforeInvocationEvent handler");
                        int errorCode = beforeInvocationEvent.getErrorCode();
                        if (errorCode > 0) {
                            String errorMessage = beforeInvocationEvent.getErrorMessage();
                            if (Strings.isNullOrEmpty(errorMessage)) {
                                log.warn("Send an error response with error code: {}", errorCode);
                                response.sendError(errorCode);
                            } else {
                                log.warn("Send an error response with error code: {} and message: {}", errorCode, errorMessage);
                                response.sendError(errorCode, errorMessage);
                            }
                        }
                    }
                } finally {
                    applicationEventPublisher.publishEvent(new AfterResourceServerApiInvocationEvent(authentication, request, response, invocationPrevented));
                }
            } else {
                filterChain.doFilter(request, response);
            }
        } catch (AccessDeniedException e) {
            log.error("Access denied", e);
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } catch (Exception e) {
            log.error("Error during API call", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }
}
