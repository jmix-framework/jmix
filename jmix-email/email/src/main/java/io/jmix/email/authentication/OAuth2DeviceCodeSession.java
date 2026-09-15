/*
 * Copyright 2026 Haulmont.
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

package io.jmix.email.authentication;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * State of a running {@link OAuth2DeviceCodeFlow}. Instances are thread-safe: the flow
 * implementation updates the state from background threads, the UI polls it.
 */
@NullMarked
public class OAuth2DeviceCodeSession {

    public enum Status {
        /**
         * Waiting for the user to complete verification on the provider page.
         */
        PENDING,
        /**
         * The flow has completed and the refresh token has been stored.
         */
        COMPLETED,
        /**
         * The flow has failed or expired, see {@link #getErrorMessage()}.
         */
        FAILED
    }

    protected volatile Status status = Status.PENDING;

    @Nullable
    protected volatile String userCode;
    @Nullable
    protected volatile String verificationUri;
    @Nullable
    protected volatile String message;
    @Nullable
    protected volatile String errorMessage;

    public Status getStatus() {
        return status;
    }

    /**
     * @return code the user must enter on the provider verification page
     */
    @Nullable
    public String getUserCode() {
        return userCode;
    }

    /**
     * @return provider verification page URI
     */
    @Nullable
    public String getVerificationUri() {
        return verificationUri;
    }

    /**
     * @return provider-supplied instruction message, if any
     */
    @Nullable
    public String getMessage() {
        return message;
    }

    @Nullable
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Intended to be called by {@link OAuth2DeviceCodeFlow} implementations.
     */
    public void init(String userCode, String verificationUri, @Nullable String message) {
        this.userCode = userCode;
        this.verificationUri = verificationUri;
        this.message = message;
    }

    /**
     * Intended to be called by {@link OAuth2DeviceCodeFlow} implementations.
     */
    public void complete() {
        this.status = Status.COMPLETED;
    }

    /**
     * Intended to be called by {@link OAuth2DeviceCodeFlow} implementations.
     */
    public void fail(String errorMessage) {
        this.errorMessage = errorMessage;
        this.status = Status.FAILED;
    }
}
