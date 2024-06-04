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

package io.jmix.superset;

import io.jmix.core.annotation.Internal;
import io.jmix.superset.schedule.SupersetTokenScheduleConfigurer;
import org.springframework.lang.Nullable;

/**
 * Provides Superset tokens management: access token, CSRF token (if is enabled). It stores tokens and cares about
 * refreshing tokens if it is needed.
 */
@Internal
public interface SupersetTokenManager {

    /**
     * Refreshes an access token. It sends a "login" request for the first time and then sends "refresh" requests. A
     * refresh request is sent only when the difference between expiration time and current time is less than or equal
     * to one minute.
     * <p>
     * This method is managed by {@link SupersetTokenScheduleConfigurer}. The Spring scheduler is configured to invoke
     * this method with {@link SupersetProperties#getAccessTokenRefreshSchedule()} delay.
     */
    void refreshAccessToken();

    /**
     * The access token is available after Spring context refresh when a "login" request is sent to Superset.
     *
     * @return access token or {@code null} if it's not initialized
     */
    @Nullable
    String getAccessToken();

    /**
     * The refresh token is available after Spring context refresh when a "login" request is sent to Superset.
     *
     * @return refresh token or {@code null} if it's not initialized
     */
    @Nullable
    String getRefreshToken();

    /**
     * Depends on {@link SupersetProperties#isCsrfProtectionEnabled()} application property. If it's enabled
     * a request will be sent to get a new CSRF token. Then the {@link #getCsrfToken()} method will return new CSRF
     * token.
     * <p>
     * This method is managed by {@link SupersetTokenScheduleConfigurer}. The Spring scheduler is configured to invoke
     * this method with {@link SupersetProperties#getCsrfTokenRefreshSchedule()} delay.
     * <p>
     * Note, that CSRF token does not encode the expiration time so the schedule delay duration is configured almost
     * equal to a default value of CSRF token expiration in Superset (WTF_CSRF_TIME_LIMIT property). If the value of
     * expiration time is changed in Superset, the {@link SupersetProperties#getCsrfTokenRefreshSchedule()} should be
     * changed too.
     */
    void refreshCsrfToken();

    /**
     * Depends on {@link SupersetProperties#isCsrfProtectionEnabled()} application property. If it's enabled a CSRF
     * token will be fetched on Spring context refresh. Otherwise, no CSRF token will be fetched and method will return
     * {@code null}.
     *
     * @return a CSRF token or {@code null} if CSRF protection is disabled
     */
    @Nullable
    String getCsrfToken();
}
