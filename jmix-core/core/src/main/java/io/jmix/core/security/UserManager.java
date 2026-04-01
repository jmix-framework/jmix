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

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Interface provides API for some actions with users
 */
public interface UserManager {

    /**
     * Changes the password for the specific user and saves changes to the database immediately.
     *
     * @param userName    user login
     * @param oldPassword non-encoded old user password
     * @param newPassword non-encoded new user password
     * @throws PasswordNotMatchException if the oldPassword is not {@code null} and
     *                                   the oldPassword does not equal to the current password or
     *                                   if the oldPassword is {@code null}
     *                                   and the newPassword does equal the current password
     */
    default void changePassword(String userName, @Nullable String oldPassword, @Nullable String newPassword)
            throws PasswordNotMatchException {
        changePassword(userName, oldPassword, newPassword, true);
    }

    /**
     * Changes the password for the specific user.
     *
     * @param userName    users login
     * @param oldPassword non-encoded old user password
     * @param newPassword non-encoded new user password
     * @param saveChanges whether to save changes to the database
     * @throws PasswordNotMatchException if the oldPassword is not {@code null}
     *                                   and the oldPassword does not equal to the current password or
     *                                   if the oldPassword is {@code null}
     *                                   and the newPassword does equal the current password
     */
    UserDetails changePassword(String userName, @Nullable String oldPassword, @Nullable String newPassword,
                               boolean saveChanges) throws PasswordNotMatchException;

    /**
     * Generates new passwords for passed users and saves changes to the database immediately.
     *
     * @param users users which need reset passwords
     * @return map which contains new passwords for the passed users
     */
    default Map<UserDetails, String> resetPasswords(Set<UserDetails> users) {
        return resetPasswords(users, true);
    }

    /**
     * Generates new passwords for passed users.
     *
     * @param users       users which need reset passwords
     * @param saveChanges whether to save changes to the database
     * @return map which contains new passwords for the passed users
     */
    Map<UserDetails, String> resetPasswords(Set<UserDetails> users, boolean saveChanges);

    /**
     * Resets 'remember me' token for the specific user.
     *
     * @param users - users which need reset 'remember me' token
     */
    void resetRememberMe(Collection<UserDetails> users);
}
