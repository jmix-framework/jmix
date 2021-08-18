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

import org.springframework.context.ApplicationEvent;

import java.util.Map;
import java.util.Set;

/**
 * Base class for user password changed event. It includes user names and new passwords.
 */
public class UserPasswordChangedEvent extends ApplicationEvent {
    private static final long serialVersionUID = -3492496743097384401L;

    private final Map<String, String> passwordByUser;

    public UserPasswordChangedEvent(Map<String, String> passwordByUser) {
        super(passwordByUser);
        this.passwordByUser = passwordByUser;
    }

    public Set<String> getUsernames() {
        return passwordByUser.keySet();
    }

    public String getPasswordByUsername(String userName) {
        return passwordByUser.get(userName);
    }
}
