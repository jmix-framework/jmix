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

import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.lang.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Interface provides API for some actions with users
 */
public interface UserManager {

    /**
     * Changes the password for the specific user.
     *
     * @param userName    - users login
     * @param oldPassword - non encoded old users password
     * @param newPassword - non encoded new users password
     * @throws PasswordNotMatchException if the oldPassword is not null and the oldPassword does not equal to the current password or
     *                                   if the oldPassword is null and the newPassword does equal the current password
     */
    void changePassword(String userName, @Nullable String oldPassword, @Nullable String newPassword) throws PasswordNotMatchException;


    /**
     * Changes the password for the specific user.
     *
     * @param users - users which need reset passwords
     * @return map which contains new password for specific user
     */
    Map<UserDetails, String> resetPasswords(Set<UserDetails> users);


    /**
     * Resets 'remember me' token for the specific user.
     *
     * @param users - users which need reset 'remember me' token
     */
    void resetRememberMe(Collection<UserDetails> users);
}
