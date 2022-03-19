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

package io.jmix.securityoauth2.impl;

import com.google.common.base.Strings;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.core.security.ClientDetails;
import io.jmix.securityoauth2.event.AfterInvocationEvent;
import io.jmix.securityoauth2.event.BeforeInvocationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;

/**
 * The last filter in the security filters chain. It does the following:
 *
 * <ul>
 *     <li>logs all REST API methods access</li>
 *     <li>parses the request locale and sets it to the authentication</li>
 * </ul>
 */
public class LastSecurityFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(LastSecurityFilter.class);

    protected ApplicationEventPublisher applicationEventPublisher;
    protected TokenMasker tokenMasker;
    protected RequestLocaleProvider localeProvider;

    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void setTokenMasker(TokenMasker tokenMasker) {
        this.tokenMasker = tokenMasker;
    }

    public void setLocaleProvider(RequestLocaleProvider localeUtils) {
        this.localeProvider = localeUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException {
        logRequest(request);
        parseRequestLocale(request);

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (applicationEventPublisher != null && authentication != null) {
                BeforeInvocationEvent beforeInvocationEvent = new BeforeInvocationEvent(authentication, request, response);
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
                    applicationEventPublisher.publishEvent(new AfterInvocationEvent(authentication, request, response, invocationPrevented));
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

    /**
     * Method logs REST API method invocation
     */
    protected void logRequest(ServletRequest request) {
        if (log.isDebugEnabled()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                String tokenValue = "";
                if (authentication instanceof AnonymousAuthenticationToken) {
                    tokenValue = "anonymous";
                } else if (authentication.getDetails() instanceof OAuth2AuthenticationDetails) {
                    tokenValue = ((OAuth2AuthenticationDetails) authentication.getDetails()).getTokenValue();
                }
                log.debug("Request [{}] {} {} {}",
                        tokenMasker.maskToken(tokenValue),
                        ((HttpServletRequest) request).getMethod(),
                        getRequestURL((HttpServletRequest) request),
                        request.getRemoteAddr());
            }
        }
    }

    /**
     * Method parses the request locale and sets it to the authentication
     */
    protected void parseRequestLocale(ServletRequest request) {
        Locale locale = localeProvider.getLocale((HttpServletRequest) request);
        if (locale != null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof OAuth2Authentication) {
                Authentication userAuthentication = ((OAuth2Authentication) authentication).getUserAuthentication();
                if (userAuthentication.getDetails() instanceof ClientDetails
                        && userAuthentication instanceof AbstractAuthenticationToken) {
                    ClientDetails clientDetails = (ClientDetails) userAuthentication.getDetails();
                    ((AbstractAuthenticationToken) userAuthentication).setDetails(ClientDetails.builder()
                            .of(clientDetails)
                            .locale(locale)
                            .build());
                }
            }
        }
    }

    protected String getRequestURL(HttpServletRequest request) {
        return request.getRequestURL() +
                (!Strings.isNullOrEmpty(request.getQueryString()) ? "?" + request.getQueryString() : "");
    }
}
