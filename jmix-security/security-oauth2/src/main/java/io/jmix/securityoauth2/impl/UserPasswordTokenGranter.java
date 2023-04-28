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

import io.jmix.core.security.SecurityContextHelper;
import io.jmix.core.session.SessionData;
import io.jmix.security.model.SecurityScope;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.password.ResourceOwnerPasswordTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;

public class UserPasswordTokenGranter extends ResourceOwnerPasswordTokenGranter {
    private final AuthenticationManager authenticationManager;
    private final ObjectFactory<SessionData> sessionDataFactory;
    private final RequestLocaleProvider localeProvider;
    private final ApplicationEventPublisher eventPublisher;

    public UserPasswordTokenGranter(AuthenticationManager authenticationManager,
                                    AuthorizationServerTokenServices tokenServices,
                                    ClientDetailsService clientDetailsService,
                                    OAuth2RequestFactory requestFactory,
                                    ObjectFactory<SessionData> sessionDataFactory,
                                    RequestLocaleProvider localeProvider,
                                    ApplicationEventPublisher eventPublisher) {
        super(authenticationManager, tokenServices, clientDetailsService, requestFactory);
        this.authenticationManager = authenticationManager;
        this.sessionDataFactory = sessionDataFactory;
        this.localeProvider = localeProvider;
        this.eventPublisher = eventPublisher;
    }

    @Override
    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
        String username = tokenRequest.getRequestParameters().get("username");
        String password = tokenRequest.getRequestParameters().get("password");

        Authentication userAuth = new UsernamePasswordAuthenticationToken(username, password);
        ((AbstractAuthenticationToken) userAuth).setDetails(buildClientDetails());
        try {
            userAuth = authenticationManager.authenticate(userAuth);
        } catch (AccountStatusException | UsernameNotFoundException | BadCredentialsException ase) {
            throw new InvalidGrantException(ase.getMessage());
        }

        if (userAuth == null || !userAuth.isAuthenticated()) {
            throw new InvalidGrantException("Could not authenticate user: " + username);
        }

        OAuth2Request storedOAuth2Request = getRequestFactory().createOAuth2Request(client, tokenRequest);
        OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(storedOAuth2Request, userAuth);

        if (userAuth.getDetails() instanceof io.jmix.core.security.ClientDetails) {
            oAuth2Authentication.setDetails(io.jmix.core.security.ClientDetails.builder()
                    .of((io.jmix.core.security.ClientDetails) userAuth.getDetails())
                    .sessionId(sessionDataFactory.getObject().getSessionId())
                    .build());
        }

        publishInteractiveAuthenticationSuccessEvent(oAuth2Authentication);

        return oAuth2Authentication;
    }

    protected void publishInteractiveAuthenticationSuccessEvent(OAuth2Authentication oAuth2Authentication) {
        Authentication currentAuthentication = SecurityContextHelper.getAuthentication();
        try {
            SecurityContextHelper.setAuthentication(oAuth2Authentication);
            eventPublisher.publishEvent(
                    new InteractiveAuthenticationSuccessEvent(oAuth2Authentication, UserPasswordTokenGranter.class));
        } finally {
            SecurityContextHelper.setAuthentication(currentAuthentication);
        }
    }

    protected io.jmix.core.security.ClientDetails buildClientDetails() {
        return io.jmix.core.security.ClientDetails.builder()
                .clientType("API")
                .scope(SecurityScope.API)
                .locale(requestLocale())
                .build();
    }

    protected Locale requestLocale() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        return localeProvider.getLocale(request);
    }
}
