/*
 * Copyright 2019 Haulmont.
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

import java.util.Locale;
import java.util.UUID;

/**
 * Central infrastructure interface to provide access to a current user session.
 */
public interface UserSessionSource {

    String NAME = "jmix_UserSessionSource";

    /**
     * @return true if the current user session is valid and calling {@link #getUserSession()} is safe
     */
    boolean checkCurrentUserSession();

    /**
     * @return current user session, never null
     * @throws NoUserSessionException if there is no active user session
     */
    UserSession getUserSession() throws NoUserSessionException;

    /**
     * @return effective user ID. This is either the logged in user, or substituted user if a substitution was performed
     * in this user session.
     * @throws RuntimeException if there is no active user session
     */
    UUID currentOrSubstitutedUserId();

    /**
     * @return current user session locale
     * @throws RuntimeException if there is no active user session
     */
    Locale getLocale();
}
