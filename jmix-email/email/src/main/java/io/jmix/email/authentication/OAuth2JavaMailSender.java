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

import jakarta.mail.MessagingException;
import jakarta.mail.Transport;
import org.jspecify.annotations.NullMarked;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * {@link JavaMailSenderImpl} that authenticates every SMTP connection with a current OAuth2
 * access token obtained from {@link OAuth2TokenProvider}.
 * <p>
 * The token is passed explicitly as the connection password. A {@link jakarta.mail.Authenticator}
 * must not be used for OAuth2: {@code jakarta.mail.Service} caches the password authentication in
 * the {@link jakarta.mail.Session} after the first successful connection and consults the cache
 * before the authenticator, so the first access token would be reused for the lifetime of the
 * session regardless of expiry or refresh token changes.
 */
@NullMarked
public class OAuth2JavaMailSender extends JavaMailSenderImpl {

    protected final OAuth2TokenProvider tokenProvider;

    public OAuth2JavaMailSender(OAuth2TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected Transport connectTransport() throws MessagingException {
        Transport transport = getTransport(getSession());
        transport.connect(getHost(), getPort(), getUsername(), tokenProvider.getAccessToken());
        return transport;
    }
}
