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

package io.jmix.core.security;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface UserSubstitutionManager {

    /**
     * @return users which can be substituted by current authenticated user
     */
    List<UserDetails> getCurrentSubstitutedUsers();

    /**
     * @return users which can be substituted by user with specified {@code userName}
     */
    List<UserDetails> getSubstitutedUsers(String userName);

    /**
     * Performs user substitution
     *
     * @throws IllegalArgumentException if current user isn't allowed to substitute user with specified name
     */
    void substituteUser(String substitutedUserName);
}
