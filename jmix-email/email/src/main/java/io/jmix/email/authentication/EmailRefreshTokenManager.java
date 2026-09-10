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

package io.jmix.email.authentication;

import io.jmix.email.entity.RefreshToken;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Interface defining methods for management of refresh token used for OAuth2 authentication with SMTP server.
 */
@NullMarked
public interface EmailRefreshTokenManager {

    /**
     * Stores provided token value to database.
     * It will override previous value of refresh token.
     *
     * @param refreshTokenValue token value
     * @return stored {@link RefreshToken} instance
     */
    RefreshToken storeRefreshTokenValue(String refreshTokenValue);

    /**
     * Stores provided token value and the OAuth2 client type the token was issued to.
     * It will override previous value of refresh token.
     *
     * @param refreshTokenValue token value
     * @param clientType        client type the token was issued to
     * @return stored {@link RefreshToken} instance
     */
    default RefreshToken storeRefreshTokenValue(String refreshTokenValue, OAuth2ClientType clientType) {
        return storeRefreshTokenValue(refreshTokenValue);
    }

    /**
     * Gets the OAuth2 client type the stored refresh token was issued to.
     *
     * @return stored client type, or {@link OAuth2ClientType#CONFIDENTIAL} when unknown
     */
    default OAuth2ClientType getRefreshTokenClientType() {
        return OAuth2ClientType.CONFIDENTIAL;
    }

    /**
     * Gets current value of refresh token.
     *
     * @return token value stored in database. If no token value is stored, the initial value from
     * the 'jmix.email.oauth2.refresh-token' application property is returned. Once a token is stored
     * in the database, the stored value always takes precedence over the property.
     * @throws IllegalStateException if no token is stored and the application property is not set
     */
    String getRefreshTokenValue();

    /**
     * Loads refresh token instance from database.
     *
     * @return {@link RefreshToken} instance stored in database or null if no token is stored
     */
    @Nullable
    RefreshToken loadRefreshToken();
}
