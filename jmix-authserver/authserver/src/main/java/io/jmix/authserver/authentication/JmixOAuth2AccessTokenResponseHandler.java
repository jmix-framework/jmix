/*
 * Copyright 2026 Haulmont.
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

package io.jmix.authserver.authentication;

import io.jmix.authserver.event.AsUserAuthenticationSuccessEvent;
import io.jmix.core.security.SecurityContextHelper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2AccessTokenResponseAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.security.Principal;

/**
 * Handles a successful token request: runs the {@link IssuedAuthorizationProcessor}s, publishes
 * {@link AsUserAuthenticationSuccessEvent} for the grants that authenticate a user and writes the access token
 * response.
 */
@NullMarked
public class JmixOAuth2AccessTokenResponseHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(JmixOAuth2AccessTokenResponseHandler.class);

    protected OAuth2AuthorizationService authorizationService;
    protected ApplicationEventPublisher eventPublisher;
    protected ObjectProvider<IssuedAuthorizationProcessor> issuedAuthorizationProcessors;
    protected AuthenticationSuccessHandler accessTokenResponseHandler =
            new OAuth2AccessTokenResponseAuthenticationSuccessHandler();

    public JmixOAuth2AccessTokenResponseHandler(
            OAuth2AuthorizationService authorizationService,
            ApplicationEventPublisher eventPublisher,
            ObjectProvider<IssuedAuthorizationProcessor> issuedAuthorizationProcessors) {
        this.authorizationService = authorizationService;
        this.eventPublisher = eventPublisher;
        this.issuedAuthorizationProcessors = issuedAuthorizationProcessors;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2AccessTokenAuthenticationToken accessTokenAuthentication =
                (OAuth2AccessTokenAuthenticationToken) authentication;
        OAuth2Authorization authorization = authorizationService.findByToken(
                accessTokenAuthentication.getAccessToken().getTokenValue(), OAuth2TokenType.ACCESS_TOKEN);

        if (authorization != null) {
            for (IssuedAuthorizationProcessor processor : issuedAuthorizationProcessors.orderedStream().toList()) {
                authorization = processor.process(authorization, request);
            }

            Authentication userAuthentication = getUserAuthentication(authorization);
            if (userAuthentication != null && isUserAuthenticationGrant(request)) {
                publishAuthenticationSuccessEvent(userAuthentication, accessTokenAuthentication, authorization,
                        request);
            }
        }

        accessTokenResponseHandler.onAuthenticationSuccess(request, response, authentication);
    }

    @Nullable
    protected Authentication getUserAuthentication(OAuth2Authorization authorization) {
        return authorization.getAttribute(Principal.class.getName()) instanceof Authentication userAuthentication
                ? userAuthentication
                : null;
    }

    /**
     * Checks the grant type of the token request rather than of the authorization: refreshing the tokens keeps the
     * grant type of the authorization they were first issued for.
     */
    protected boolean isUserAuthenticationGrant(HttpServletRequest request) {
        String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
        return PasswordAuthorizationGrantTypeHelper.PASSWORD_GRANT_TYPE.getValue().equals(grantType)
                || AuthorizationGrantType.AUTHORIZATION_CODE.getValue().equals(grantType);
    }

    protected void publishAuthenticationSuccessEvent(Authentication userAuthentication,
                                                     OAuth2AccessTokenAuthenticationToken accessTokenAuthentication,
                                                     OAuth2Authorization authorization,
                                                     HttpServletRequest request) {
        SecurityContext previousContext = SecurityContextHolder.getContext();
        SecurityContext userContext = SecurityContextHolder.createEmptyContext();
        userContext.setAuthentication(userAuthentication);
        SecurityContextHelper.installContext(userContext);
        try {
            eventPublisher.publishEvent(new AsUserAuthenticationSuccessEvent(userAuthentication,
                    accessTokenAuthentication.getRegisteredClient(), authorization, request));
        } catch (AuthenticationException e) {
            rejectAuthorization(authorization, request);
            throw e instanceof OAuth2AuthenticationException oauth2Exception
                    ? oauth2Exception
                    : new OAuth2AuthenticationException(
                    new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT, e.getMessage(), null), e);
        } catch (RuntimeException e) {
            log.error("AsUserAuthenticationSuccessEvent listener failed for user '{}', the tokens are issued",
                    userAuthentication.getName(), e);
        } finally {
            SecurityContextHelper.restoreContext(previousContext);
        }
    }

    /**
     * Removes the authorization rejected by an event listener, together with the session created for the token
     * request.
     */
    protected void rejectAuthorization(OAuth2Authorization authorization, HttpServletRequest request) {
        authorizationService.remove(authorization);

        HttpSession session = request.getSession(false);
        if (session != null && session.isNew()) {
            session.invalidate();
        }
    }
}
