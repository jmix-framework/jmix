/*
 * Copyright 2025 Haulmont.
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

package io.jmix.securityflowui.authentication;

import org.springframework.lang.Nullable;

public class AuthDetailsValidationResult {

    protected final boolean valid;
    @Nullable
    protected final String message;
    @Nullable
    protected final Exception exception;

    private AuthDetailsValidationResult(boolean valid, @Nullable String message, @Nullable Exception exception) {
        this.valid = valid;
        this.message = message;
        this.exception = exception;
    }

    public static AuthDetailsValidationResult createValid() {
        return new AuthDetailsValidationResult(true, null, null);
    }

    public static AuthDetailsValidationResult createInvalid(String message) {
        return createInvalid(message, null);
    }

    public static AuthDetailsValidationResult createInvalid(String message, @Nullable Exception exception) {
        return new AuthDetailsValidationResult(false, message, exception);
    }

    public boolean isValid() {
        return valid;
    }

    @Nullable
    public String getMessage() {
        return message;
    }

    @Nullable
    public Exception getException() {
        return exception;
    }
}
