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

package io.jmix.email.authentication.impl;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.UserCredentials;
import io.jmix.email.EmailerProperties;
import io.jmix.email.authentication.EmailRefreshTokenManager;
import io.jmix.email.authentication.OAuth2TokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

//TODO
public class GoogleOAuth2TokenProvider implements OAuth2TokenProvider {

    private static final Logger log = LoggerFactory.getLogger(GoogleOAuth2TokenProvider.class);

    protected final EmailerProperties emailerProperties;
    protected final EmailRefreshTokenManager refreshTokenManager;

    public GoogleOAuth2TokenProvider(EmailerProperties emailerProperties,
                                     EmailRefreshTokenManager refreshTokenManager) {
        this.emailerProperties = emailerProperties;
        this.refreshTokenManager = refreshTokenManager;
    }

    @Override
    public String getAccessToken() {
        GoogleCredentials credentials = createUserCredentials();

        log.info("[IVGA] refreshIfExpired: start");
        try {
            credentials.refreshIfExpired();
        } catch (IOException e) {
            throw new RuntimeException("Unable to refresh access token", e);
        }
        log.info("[IVGA] refreshIfExpired: end");

        AccessToken accessToken = credentials.getAccessToken();
        log.info("[IVGA] Access token: scopes={}, expiration={}", accessToken.getScopes(), accessToken.getExpirationTime());
        return accessToken.getTokenValue();
    }

    @Override
    public String getRefreshToken() {
        return refreshTokenManager.getDefaultRefreshTokenValue();
    }

    protected UserCredentials createUserCredentials() {
        return UserCredentials.newBuilder()
                .setClientId(emailerProperties.getOAuth2().getClientId())
                .setClientSecret(emailerProperties.getOAuth2().getSecret())
                .setRefreshToken(getRefreshToken())
                .build();
    }
}
