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

package io.jmix.rest.security;

import io.jmix.core.AccessManager;
import io.jmix.core.Messages;
import io.jmix.core.security.SecurityContextHelper;
import io.jmix.rest.context.RestEnabledContext;
import io.jmix.rest.exception.RestApiAccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.password.ResourceOwnerPasswordTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

public class JmixUserPasswordTokenGranter extends ResourceOwnerPasswordTokenGranter {
    protected final AccessManager accessManager;
    protected final Messages messages;

    public JmixUserPasswordTokenGranter(
            AccessManager accessManager,
            Messages messages,
            AuthenticationManager authenticationManager,
            AuthorizationServerTokenServices tokenServices,
            ClientDetailsService clientDetailsService,
            OAuth2RequestFactory requestFactory) {
        super(authenticationManager, tokenServices, clientDetailsService, requestFactory);
        this.accessManager = accessManager;
        this.messages = messages;
    }

    @Override
    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
        OAuth2Authentication auth2Authentication = super.getOAuth2Authentication(client, tokenRequest);

        RestEnabledContext restEnabledContext = new RestEnabledContext();

        //TODO: extract into authentication success event (after set client type)
        Authentication currentAuthentication = SecurityContextHelper.getAuthentication();
        try {
            SecurityContextHelper.setAuthentication(auth2Authentication);
            accessManager.applyRegisteredConstraints(restEnabledContext);
        } finally {
            SecurityContextHelper.setAuthentication(currentAuthentication);
        }

        if (!restEnabledContext.isPermitted()) {
            throw new RestApiAccessDeniedException(messages.getMessage("io.jmix.rest/restApiAccessDenied"));
        }

        return auth2Authentication;
    }
}
