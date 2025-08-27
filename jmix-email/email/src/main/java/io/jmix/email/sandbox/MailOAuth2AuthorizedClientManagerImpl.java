/*
 * Copyright 2025 Haulmont.
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

package io.jmix.email.sandbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;

//todo remove
//@Component("email_MailOAuth2AuthorizedClientManager")
public class MailOAuth2AuthorizedClientManagerImpl implements MailOAuth2AuthorizedClientManager {

    private static final Logger log = LoggerFactory.getLogger(MailOAuth2AuthorizedClientManagerImpl.class);

    private final AuthorizedClientServiceOAuth2AuthorizedClientManager delegate;

    public MailOAuth2AuthorizedClientManagerImpl(ClientRegistrationRepository clientRepo,
                                                 OAuth2AuthorizedClientService clientService) {
        log.info("[IVGA] INIT MailOAuth2AuthorizedClientManagerImpl: clientService class = {}", clientService.getClass());

        this.delegate = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRepo, clientService);

        this.delegate.setAuthorizedClientProvider(
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .refreshToken()
                        .build()
        );
    }

    @Override
    public OAuth2AuthorizedClient authorize(OAuth2AuthorizeRequest request) {
        return delegate.authorize(request);
    }
}
