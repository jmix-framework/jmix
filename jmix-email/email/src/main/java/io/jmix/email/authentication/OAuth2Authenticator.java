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

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;

/**
 * {@link Authenticator} implementation that uses access token provided by {@link OAuth2TokenProvider}.
 */
public class OAuth2Authenticator extends Authenticator {

    private final String username;
    private final OAuth2TokenProvider tokenProvider;

    public OAuth2Authenticator(String username, OAuth2TokenProvider tokenProvider) {
        this.username = username;
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, tokenProvider.getAccessToken());
    }
}
