/*
 * Copyright 2021 Haulmont.
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

package io.jmix.core.usersubstitution;

import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.lang.Nullable;

/**
 * Interface is used to get the information about current user substitution
 */
public interface CurrentUserSubstitution {

    /**
     * Method returns currently authenticated user (the user who actually logged in). The fact of user substitution is
     * not taken into account.
     *
     * @return currently authenticated user
     * @throws RuntimeException if Authentication is not set to
     *                          {@link org.springframework.security.core.context.SecurityContext} or user information
     *                          cannot be extracted from current authentication
     */
    UserDetails getAuthenticatedUser();

    /**
     * Method returns the substituted user or null if user substitution didn't happen.
     *
     * @return substituted user or null if user substitution didn't happen
     */
    @Nullable
    UserDetails getSubstitutedUser();

    /**
     * Method returns the substituted user if the substitution happened or authenticated (logged in) user otherwise
     *
     * @return substituted user if the substitution happened or authenticated (logged in) user otherwise
     */
    UserDetails getEffectiveUser();
}
