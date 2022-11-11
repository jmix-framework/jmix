/*
 * Copyright 2022 Haulmont.
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

package io.jmix.securityui.password;

/**
 * Contains parameters of password validation.
 *
 * @param <E> type of the user entity
 * @see PasswordValidator
 */
public class PasswordValidationContext<E> {

    private final E user;
    private final String password;

    public PasswordValidationContext(E user, String password) {
        this.user = user;
        this.password = password;
    }

    /**
     * @return user instance for which the password is validated
     */
    public E getUser() {
        return user;
    }

    /**
     * @return validated password
     */
    public String getPassword() {
        return password;
    }
}
