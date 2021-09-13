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

import io.jmix.core.security.impl.SubstitutedUserAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Class that is used to get the information about the authenticated user.
 */
public interface CurrentAuthentication {

    @Nullable
    Authentication getAuthentication();

    /**
     * @return currently authenticated user
     * @throws RuntimeException if Authentication is not set to {@link org.springframework.security.core.context.SecurityContext}
     *                          or user information cannot be extracted from current authentication
     */
    UserDetails getUser();


    /**
     * @return substituted user if current authentication token is instance of
     * {@link SubstitutedUserAuthenticationToken}, logged-in user otherwise.
     * @throws RuntimeException if Authentication is not set to {@link org.springframework.security.core.context.SecurityContext}
     *                          or user information cannot be extracted from current authentication
     */
    UserDetails getCurrentOrSubstitutedUser();

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
