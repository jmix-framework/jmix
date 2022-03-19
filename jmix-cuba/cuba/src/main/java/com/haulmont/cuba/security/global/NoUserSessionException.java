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
package com.haulmont.cuba.security.global;

import java.util.UUID;

/**
 * Thrown if there is no {@link UserSession} on the current thread or if it is expired.
 */
public class NoUserSessionException extends RuntimeException {

    private static final long serialVersionUID = 4820628023682230319L;

    public NoUserSessionException() {
        super("No UserSession bound to the current thread");
    }

    public NoUserSessionException(UUID sessionId) {
        super(String.format("User session not found: %s", sessionId.toString()));
    }
}
