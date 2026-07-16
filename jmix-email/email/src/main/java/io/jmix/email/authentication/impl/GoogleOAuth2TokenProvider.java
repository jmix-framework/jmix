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
import com.google.auth.oauth2.UserCredentials;
import io.jmix.email.EmailerProperties;
import io.jmix.email.authentication.EmailRefreshTokenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class GoogleOAuth2TokenProvider extends AbstractOAuth2TokenProvider {

    private static final Logger log = LoggerFactory.getLogger(GoogleOAuth2TokenProvider.class);

    protected UserCredentials credentials;
    protected String currentRefreshToken;

    public GoogleOAuth2TokenProvider(EmailerProperties emailerProperties,
                                     EmailRefreshTokenManager refreshTokenManager) {
        super(emailerProperties, refreshTokenManager);
    }

    @Override
    public synchronized String getAccessToken() {
        String storedRefreshToken = getRefreshToken();
        if (credentials == null || !storedRefreshToken.equals(currentRefreshToken)) {
            log.debug("Initializing Google user credentials");
            credentials = createUserCredentials();
            currentRefreshToken = storedRefreshToken;
        }

        try {
            // UserCredentials caches the access token internally and refreshes it only when it is close to expiry.
            credentials.refreshIfExpired();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to refresh Google access token", e);
        }

        AccessToken accessToken = credentials.getAccessToken();
        log.debug("Access token has been acquired with scopes: {} (expiration date = {})",
                accessToken.getScopes(), accessToken.getExpirationTime());
        return accessToken.getTokenValue();
    }

    protected UserCredentials createUserCredentials() {
        return UserCredentials.newBuilder()
                .setClientId(getClientId())
                .setClientSecret(getSecret())
                .setRefreshToken(getRefreshToken())
                .build();
    }
}
