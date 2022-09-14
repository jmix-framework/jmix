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

package io.jmix.authorizationserver.client.impl;

import io.jmix.authorizationserver.DefaultRegisteredClientProperties;
import io.jmix.authorizationserver.client.RegisteredClientProvider;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.config.TokenSettings;

import java.util.List;
import java.util.UUID;

/**
 * A default implementation of {@link RegisteredClientProvider} that creates a single client registration and configures
 * it using information from the {@link DefaultRegisteredClientProperties}.
 */
public class DefaultRegisteredClientProvider implements RegisteredClientProvider {

    private DefaultRegisteredClientProperties defaultClientProperties;

    public DefaultRegisteredClientProvider(DefaultRegisteredClientProperties defaultClientProperties) {
        this.defaultClientProperties = defaultClientProperties;
    }

    @Override
    public List<RegisteredClient> getRegisteredClients() {
        RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(defaultClientProperties.getClientId())
                .clientSecret(defaultClientProperties.getClientSecret())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .redirectUris(uris -> {
                    uris.addAll(defaultClientProperties.getRedirectUris());
                })
                .scope("jmix")
                .tokenSettings(TokenSettings.builder()
                        .accessTokenFormat(OAuth2TokenFormat.REFERENCE)
                        .accessTokenTimeToLive(defaultClientProperties.getAccessTokenTimeToLive())
                        .refreshTokenTimeToLive(defaultClientProperties.getRefreshTokenTimeToLive())
                        .build())
                .build();
        return List.of(client);
    }
}
