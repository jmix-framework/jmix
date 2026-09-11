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

package io.jmix.email;

import jakarta.mail.MessagingException;
import org.jspecify.annotations.NullMarked;

/**
 * Verifies connectivity and authentication with the configured mail server without sending
 * anything.
 */
@NullMarked
public interface EmailConnectionTester {

    /**
     * Connects the underlying mail transport, performs authentication and closes the connection.
     * When OAuth2 authentication is enabled, this includes obtaining an access token, so an
     * invalid or revoked refresh token is reported as a failure.
     *
     * @throws MessagingException    if the connection or authentication fails
     * @throws IllegalStateException if the configured mail sender does not support connection
     *                               testing, or a token cannot be obtained
     */
    void testConnection() throws MessagingException;
}
