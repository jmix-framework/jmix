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

import io.jmix.core.common.util.Preconditions;
import org.jspecify.annotations.Nullable;

/**
 * Class that contains result of {@link AuthDetails} validation.
 * Validation is optional in general and can be provided by different implementations of {@link AuthDetailsValidator}.
 */
public class AuthDetailsValidationResult {

    protected final boolean valid;
    protected final String message;
    protected final Exception exception;

    private AuthDetailsValidationResult(boolean valid, @Nullable String message, @Nullable Exception exception) {
        this.valid = valid;
        this.message = message;
        this.exception = exception;
    }

    /**
     * Creates valid {@link AuthDetailsValidationResult} without violation message and exception.
     *
     * @return Valid {@link AuthDetailsValidationResult}
     */
    public static AuthDetailsValidationResult createValid() {
        return new AuthDetailsValidationResult(true, null, null);
    }

    /**
     * Creates invalid {@link AuthDetailsValidationResult} with provided message.
     *
     * @param message description of validation violation
     * @return invalid {@link AuthDetailsValidationResult}
     *
     * @see #createInvalid(String, Exception)
     */
    public static AuthDetailsValidationResult createInvalid(String message) {
        return createInvalid(message, null);
    }

    /**
     * Creates invalid {@link AuthDetailsValidationResult} with provided message and optional exception.
     *
     * @param message   description of validation violation
     * @param exception optional related exception
     * @return invalid {@link AuthDetailsValidationResult}
     */
    public static AuthDetailsValidationResult createInvalid(String message, @Nullable Exception exception) {
        Preconditions.checkNotNullArgument(message);
        return new AuthDetailsValidationResult(false, message, exception);
    }

    /**
     * @return true if validation has no violations
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * @return description of validation violation
     */
    @Nullable
    public String getMessage() {
        return message;
    }

    /**
     * @return exception related to validation violation
     */
    @Nullable
    public Exception getException() {
        return exception;
    }
}
