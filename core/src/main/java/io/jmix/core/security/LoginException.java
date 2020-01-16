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

import io.jmix.core.Logging;
import io.jmix.core.compatibility.SupportedByClient;

/**
 * Login error. Contains message localized accordingly to the current user locale. 
 */
@SupportedByClient
@Logging(Logging.Type.BRIEF)
public class LoginException extends RuntimeException {

    private static final long serialVersionUID = 6144194102176774627L;

    public LoginException(String message) {
        super(message);
    }

    protected LoginException(Throwable t) {
        super(t);
    }

    protected LoginException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoginException(String template, Object... params) {
        super(String.format(template, params));
    }
}