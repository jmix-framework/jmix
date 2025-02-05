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

package io.jmix.authserver.service.cleanup.impl;

import io.jmix.authserver.service.cleanup.OAuth2ExpiredTokenCleaner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcOperations;

import java.util.Date;

public class JdbcOAuth2ExpiredTokenCleaner implements OAuth2ExpiredTokenCleaner {

    private static final Logger log = LoggerFactory.getLogger(JdbcOAuth2ExpiredTokenCleaner.class);

    private static final String TABLE_NAME = "oauth2_authorization";

    private static final String ACCESS_TOKEN_EXISTS_AND_EXPIRED_FILTER = "(access_token_expires_at is not null AND access_token_expires_at < ?)";
    private static final String REFRESH_TOKEN_EXPIRED_IF_EXISTS_FILTER = "(refresh_token_expires_at is null or refresh_token_expires_at < ?)";

    private static final String DELETE_ACCESS_TOKEN_QUERY = "delete from " + TABLE_NAME
            + " where " + ACCESS_TOKEN_EXISTS_AND_EXPIRED_FILTER
            + " and " + REFRESH_TOKEN_EXPIRED_IF_EXISTS_FILTER;

    protected final JdbcOperations jdbcOperations;

    public JdbcOAuth2ExpiredTokenCleaner(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Override
    public void removeExpiredAccessTokens() {
        Date currentDate = new Date();
        int updated = jdbcOperations.update(DELETE_ACCESS_TOKEN_QUERY, currentDate, currentDate);
        log.info("Removed {} expired access tokens", updated);
    }
}
