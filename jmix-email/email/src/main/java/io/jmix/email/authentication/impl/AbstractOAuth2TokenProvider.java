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

package io.jmix.email.authentication.impl;

import io.jmix.email.EmailerProperties;
import io.jmix.email.authentication.EmailRefreshTokenManager;
import io.jmix.email.authentication.OAuth2TokenProvider;

public abstract class AbstractOAuth2TokenProvider implements OAuth2TokenProvider {

    protected final EmailerProperties emailerProperties;
    protected final EmailRefreshTokenManager refreshTokenManager;

    public AbstractOAuth2TokenProvider(EmailerProperties emailerProperties, EmailRefreshTokenManager refreshTokenManager) {
        this.emailerProperties = emailerProperties;
        this.refreshTokenManager = refreshTokenManager;
    }

    @Override
    public String getRefreshToken() {
        return refreshTokenManager.getRefreshTokenValue();
    }

    protected String getClientId() {
        return emailerProperties.getOAuth2().getClientId();
    }

    protected String getSecret() {
        return emailerProperties.getOAuth2().getSecret();
    }
}
