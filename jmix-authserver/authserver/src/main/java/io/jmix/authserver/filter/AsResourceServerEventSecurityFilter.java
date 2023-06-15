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

package io.jmix.authserver.filter;

import com.google.common.base.Strings;
import io.jmix.authserver.event.AsResourceServerAfterInvocationEvent;
import io.jmix.authserver.event.AsResourceServerBeforeInvocationEvent;
import io.jmix.core.security.AccessDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The purpose of the filter is to throw before- and after- events for resource server API invocations. These events are
 * used for checking security constraints and preventing API access for users with insufficient permissions.
 */
public class AsResourceServerEventSecurityFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AsResourceServerEventSecurityFilter.class);

    protected ApplicationEventPublisher applicationEventPublisher;

    public AsResourceServerEventSecurityFilter(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException {
        //todo enable REST request logging and locale parsing
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (applicationEventPublisher != null && authentication != null) {
                AsResourceServerBeforeInvocationEvent asResourceServerBeforeInvocationEvent = new AsResourceServerBeforeInvocationEvent(authentication, request, response);
                applicationEventPublisher.publishEvent(asResourceServerBeforeInvocationEvent);

                boolean invocationPrevented = asResourceServerBeforeInvocationEvent.isInvocationPrevented();

                try {
                    if (!invocationPrevented) {
                        filterChain.doFilter(request, response);
                    } else {
                        log.debug("Request invocation prevented by BeforeInvocationEvent handler");
                        int errorCode = asResourceServerBeforeInvocationEvent.getErrorCode();
                        if (errorCode > 0) {
                            String errorMessage = asResourceServerBeforeInvocationEvent.getErrorMessage();
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
                    applicationEventPublisher.publishEvent(new AsResourceServerAfterInvocationEvent(authentication, request, response, invocationPrevented));
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
