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

package io.jmix.core.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Provides information about the currently authenticated user.
 */
public interface CurrentAuthentication {

    /**
     * Returns current authentication object.
     * @throws IllegalStateException if Authentication is not set to {@link org.springframework.security.core.context.SecurityContext}
     */
    Authentication getAuthentication();

    /**
     * Returns the authenticated user. If you need to get the user substitution information use the {@link
     * io.jmix.core.usersubstitution.CurrentUserSubstitution}.
     *
     * @return currently authenticated user
     * @throws RuntimeException if Authentication is not set to {@link org.springframework.security.core.context.SecurityContext}
     *                          or user information cannot be extracted from current authentication
     */
    UserDetails getUser();

    /**
     * Returns the authenticated user using hints. If you need to get the user substitution information use the {@link
     * io.jmix.core.usersubstitution.CurrentUserSubstitution}. See supported hints in the {@link CurrentUserHints}.
     *
     * @param hints hints with user retrieval instructions
     * @return currently authenticated user
     * @throws RuntimeException if Authentication is not set to {@link org.springframework.security.core.context.SecurityContext}
     *                          or user information cannot be extracted from current authentication
     * @see CurrentUserHints
     */
    UserDetails getUser(Map<String, Object> hints);
    /**
     * @return locale of the current authentication or default locale if current authentication doesn't contain locale
     * information
     * @throws RuntimeException if Authentication is not set to {@link org.springframework.security.core.context.SecurityContext}
     */
    Locale getLocale();

    /**
     * @return time zone of the current authentication or default time zone if current authentication doesn't contain
     * time zone information
     * @throws RuntimeException if Authentication is not set to {@link org.springframework.security.core.context.SecurityContext}
     */
    TimeZone getTimeZone();

    /**
     * @return true if authentication is set to {@link org.springframework.security.core.context.SecurityContext}
     */
    boolean isSet();
}
