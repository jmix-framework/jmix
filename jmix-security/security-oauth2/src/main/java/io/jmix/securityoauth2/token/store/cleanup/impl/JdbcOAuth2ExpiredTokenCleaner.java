/*
 * Copyright 2022 Haulmont.
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

package io.jmix.securityoauth2.token.store.cleanup.impl;

import io.jmix.securityoauth2.token.store.cleanup.OAuth2ExpiredTokenCleaner;
import org.springframework.jdbc.core.JdbcOperations;

import java.util.Date;

public class JdbcOAuth2ExpiredTokenCleaner implements OAuth2ExpiredTokenCleaner {

    private static final String ACCESS_TOKEN_TABLE_NAME = "oauth_access_token";
    private static final String REFRESH_TOKEN_TABLE_NAME = "oauth_refresh_token";

    private static final String TOKEN_EXPIRED_FILTER = "expires_at is not null AND expires_at < ?";

    private static final String DELETE_ACCESS_TOKEN_QUERY = "delete from " + ACCESS_TOKEN_TABLE_NAME
            + " where " + TOKEN_EXPIRED_FILTER;

    private static final String DELETE_REFRESH_TOKEN_QUERY = "delete from " + REFRESH_TOKEN_TABLE_NAME
            + " where " + TOKEN_EXPIRED_FILTER;

    protected final JdbcOperations jdbcOperations;

    public JdbcOAuth2ExpiredTokenCleaner(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Override
    public int removeExpiredAccessTokens() {
        Date currentDate = new Date();
        return jdbcOperations.update(getDeleteAccessTokenQuery(), currentDate);
    }

    @Override
    public int removeExpiredRefreshTokens() {
        Date currentDate = new Date();
        return jdbcOperations.update(getDeleteRefreshTokenQuery(), currentDate);
    }

    protected String getDeleteAccessTokenQuery() {
        return DELETE_ACCESS_TOKEN_QUERY;
    }

    protected String getDeleteRefreshTokenQuery() {
        return DELETE_REFRESH_TOKEN_QUERY;
    }
}
