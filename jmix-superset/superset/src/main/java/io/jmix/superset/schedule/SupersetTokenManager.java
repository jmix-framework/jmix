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

package io.jmix.superset.schedule;

import io.jmix.superset.SupersetProperties;
import jakarta.annotation.Nullable;

public interface SupersetTokenManager {

    void refreshAccessToken();

    String getAccessToken();

    String getRefreshToken();

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
