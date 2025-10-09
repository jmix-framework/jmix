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
import org.springframework.lang.Nullable;

/**
 * Interface defining methods for management of refresh token used for OAuth2 authentication with SMTP server.
 */
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
     * Gets current value of refresh token.
     *
     * @return token value stored in database.
     * If no token value is stored - return token value from application property 'jmix.email.oauth2.refreshToken'
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
