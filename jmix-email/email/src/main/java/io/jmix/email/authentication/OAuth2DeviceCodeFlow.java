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

/**
 * Interface defining the OAuth2 device authorization grant ("device code flow") used to connect
 * a mailbox account without a redirect URI: the user opens the provider verification page on any
 * device and enters the displayed code.
 */
@NullMarked
public interface OAuth2DeviceCodeFlow {

    /**
     * Starts the device code flow and returns a session carrying the user instructions
     * (verification URI and user code).
     * <p>
     * The flow completes asynchronously — track it via {@link OAuth2DeviceCodeSession#getStatus()}.
     * On successful completion the obtained refresh token is stored via {@link EmailRefreshTokenManager}.
     *
     * @return started session
     * @throws IllegalStateException if the flow cannot be started
     */
    OAuth2DeviceCodeSession start();
}
