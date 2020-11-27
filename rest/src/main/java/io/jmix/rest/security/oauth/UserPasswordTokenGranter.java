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

package io.jmix.rest.security.oauth;

import io.jmix.core.session.SessionData;
import io.jmix.rest.api.common.RestLocaleUtils;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.password.ResourceOwnerPasswordTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

public class UserPasswordTokenGranter extends ResourceOwnerPasswordTokenGranter {
    protected final AuthenticationManager authenticationManager;
    protected final RestLocaleUtils restLocaleUtils;
    private final ObjectFactory<SessionData> sessionDataFactory;

    public UserPasswordTokenGranter(
            RestLocaleUtils restLocaleUtils,
            AuthenticationManager authenticationManager,
            AuthorizationServerTokenServices tokenServices,
            ClientDetailsService clientDetailsService,
            OAuth2RequestFactory requestFactory,
            ObjectFactory<SessionData> sessionDataFactory) {
        super(authenticationManager, tokenServices, clientDetailsService, requestFactory);
        this.authenticationManager = authenticationManager;
        this.restLocaleUtils = restLocaleUtils;
        this.sessionDataFactory = sessionDataFactory;
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

        return oAuth2Authentication;
    }

    protected io.jmix.core.security.ClientDetails buildClientDetails() {
        return io.jmix.core.security.ClientDetails.builder()
                .clientType("REST")
                .locale(requestLocale())
                .build();
    }

    protected Locale requestLocale() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        return restLocaleUtils.extractLocaleFromRequestHeader(request);
    }
}
