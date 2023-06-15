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

package io.jmix.core.security.event;

import org.springframework.lang.Nullable;
import java.util.Collections;

/**
 * Class for password change event for a single user (typically, as a result of manual change).
 * It includes user name and a new password.
 * <p>
 * Note: for the event of password reset use {@link UserPasswordResetEvent}.
 * If you need to track all password changes, consider using the base class: {@link UserPasswordChangedEvent}.
 */
public class SingleUserPasswordChangeEvent extends UserPasswordChangedEvent {
    private static final long serialVersionUID = -2101650558061585608L;

    private final String username;
    private final String newPassword;

    public SingleUserPasswordChangeEvent(String username, @Nullable String newPassword) {
        super(Collections.singletonMap(username, newPassword));
        this.username = username;
        this.newPassword = newPassword;
    }

    public String getUsername() {
        return username;
    }

    @Nullable
    public String getNewPassword() {
        return newPassword;
    }
}
