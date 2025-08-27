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

import io.jmix.core.DataManager;
import io.jmix.email.EmailerProperties;
import io.jmix.email.authentication.EmailRefreshTokenManager;
import io.jmix.email.entity.RefreshToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component //todo
public class EmailRefreshTokenManagerImpl implements EmailRefreshTokenManager {

    private static final Logger log = LoggerFactory.getLogger(EmailRefreshTokenManagerImpl.class);

    protected static final UUID DEFAULT_REFRESH_TOKEN_ID = UUID.fromString("0198c7b9-4abc-77b6-9088-fb080c13200b");
    protected static final String DEFAULT_REFRESH_TOKEN_REGISTRATION_ID = "default";

    protected final DataManager dataManager;
    protected final EmailerProperties emailerProperties;

    public EmailRefreshTokenManagerImpl(DataManager dataManager,
                                        EmailerProperties emailerProperties) {
        this.dataManager = dataManager;
        this.emailerProperties = emailerProperties;
    }

    @Override
    public RefreshToken storeDefaultRefreshTokenValue(String refreshToken) {
        log.debug("Storing default refresh token in database...");

        RefreshToken defaultRefreshToken = loadDefaultRefreshToken();
        if (defaultRefreshToken == null) {
            log.debug("Refresh token was not found in database. Create new one");
            defaultRefreshToken = dataManager.create(RefreshToken.class);
            defaultRefreshToken.setId(DEFAULT_REFRESH_TOKEN_ID);
            defaultRefreshToken.setRegistrationId(DEFAULT_REFRESH_TOKEN_REGISTRATION_ID);
        }
        defaultRefreshToken.setTokenValue(refreshToken);
        return dataManager.save(defaultRefreshToken);
    }

    @Override
    public String getDefaultRefreshTokenValue() {
        RefreshToken defaultRefreshToken = loadDefaultRefreshToken();
        if (defaultRefreshToken != null) {
            log.debug("Refresh token was found in database");
            return defaultRefreshToken.getTokenValue();
        }

        log.debug("Refresh token was not found in database. Using value from properties");
        return emailerProperties.getOAuth2().getRefreshToken();
    }

    @Nullable
    public RefreshToken loadDefaultRefreshToken() {
        log.debug("Loading default refresh token from database...");
        return dataManager.load(RefreshToken.class)
                .id(DEFAULT_REFRESH_TOKEN_ID)
                .optional()
                .orElse(null);
    }
}