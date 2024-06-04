/*
 * Copyright 2024 Haulmont.
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

package io.jmix.superset.client;

import io.jmix.superset.SupersetProperties;
import io.jmix.superset.SupersetTokenManager;
import io.jmix.superset.schedule.SupersetTokenScheduleConfigurer;
import io.jmix.superset.client.model.*;
import org.springframework.lang.Nullable;

import java.io.IOException;

/**
 * Service for communicating with Superset API.
 * <p>
 * See <a href="https://superset.apache.org/docs/api/">Apache Superset API</a>
 */
public interface SupersetClient {

    /**
     * Performs POST login request {@code /api/v1/security/login} with default body configuration.
     * The request blocks current thread.
     * <p>
     * Note, that failed request will return response with {@code message} property.
     *
     * @return response with JWT access token and refresh token
     * @throws IOException           if an I/ O error occurs when sending or receiving
     * @throws InterruptedException  if the operation is interrupted
     * @throws IllegalStateException if it cannot write or read JSON value
     */
    LoginResponse login() throws IOException, InterruptedException;

    /**
     * Performs POST login request {@code /api/v1/security/login}. The request blocks current thread.
     * <p>
     * Note, that failed request will return response with {@code message} property.
     *
     * @param body request body
     * @return response with JWT access token and refresh token
     * @throws IOException           if an I/ O error occurs when sending or receiving
     * @throws InterruptedException  if the operation is interrupted
     * @throws IllegalStateException if it cannot write or read JSON value
     */
    LoginResponse login(LoginBody body) throws IOException, InterruptedException;

    /**
     * Performs refresh access token request {@code /api/v1/security/refresh}. The request blocks current thread.
     * <p>
     * Note, that failed request will return response with {@code systemMessage} property.
     *
     * @param refreshToken refresh token to send
     * @return response with new JWT access token
     * @throws IOException           if an I/ O error occurs when sending or receiving
     * @throws InterruptedException  if the operation is interrupted
     * @throws IllegalStateException if it cannot write or read JSON value
     */
    RefreshResponse refresh(String refreshToken) throws IOException, InterruptedException;

    /**
     * Performs a guest token request {@code /api/v1/security/guest_token}. The request blocks current thread. The
     * guest token can be used to embed a dashboard.
     * <p>
     * Note, that failed request will return response with {@code message} or {@code systemMessage} property.
     *
     * @param body        the body to send
     * @param accessToken access token that can be taken from {@link #login(LoginBody)}
     * @param csrfToken   CSRF token should be passed if {@link SupersetProperties#isCsrfProtectionEnabled()} is enabled
     * @return response with guest token
     * @throws IOException           if an I/ O error occurs when sending or receiving
     * @throws InterruptedException  if the operation is interrupted
     * @throws IllegalStateException if it cannot write or read JSON value
     */
    GuestTokenResponse fetchGuestToken(GuestTokenBody body, String accessToken, @Nullable String csrfToken)
            throws IOException, InterruptedException;

    /**
     * Performs a request to get a CSRF token {@code /api/v1/security/csrf_token}. The request blocks
     * current thread. CSRF token is required when Superset configures CSRF protection.
     * <p>
     * When {@link SupersetProperties#isCsrfProtectionEnabled()} is enabled a Spring scheduler gets CSRF token on
     * Spring refresh context. See {@link SupersetTokenScheduleConfigurer} and {@link SupersetTokenManager}.
     *
     * @param accessToken access token to send
     * @return response with CSRF token
     * @throws IOException           if an I/ O error occurs when sending or receiving
     * @throws InterruptedException  if the operation is interrupted
     * @throws IllegalStateException if it cannot read JSON value
     */
    CsrfTokenResponse fetchCsrfToken(String accessToken) throws IOException, InterruptedException;
}
