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

import java.util.Date;
import java.util.List;

/**
 * Interface is used for getting possible substituted users and for performing the substitution operation.
 */
public interface UserSubstitutionManager {

    /**
     * @return users which can be substituted by the currently authenticated user at the current time
     */
    List<UserDetails> getCurrentSubstitutedUsers();

    /**
     * @return users which can be substituted by user with specified {@code username} at the given {@code date}
     */
    List<UserDetails> getSubstitutedUsers(String username, Date date);

    /**
     * Performs user substitution
     *
     * @throws IllegalArgumentException if current user isn't allowed to substitute user with specified name
     */
    void substituteUser(String substitutedUserName);
}
