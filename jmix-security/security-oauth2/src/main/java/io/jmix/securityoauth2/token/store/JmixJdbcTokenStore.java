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

package io.jmix.securityoauth2.token.store;

import io.jmix.core.impl.StandardSerialization;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.security.oauth2.common.ExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.Date;

/**
 * {@link JdbcTokenStore} that can properly deserialize Jmix entities in such a way that they support lazy loading after
 * deserialization.
 */
public class JmixJdbcTokenStore extends JdbcTokenStore {

    private static final String ACCESS_TOKEN_INSERT_STATEMENT = "insert into oauth_access_token " +
            "(token_id, token, authentication_id, user_name, client_id, authentication, refresh_token, expires_at) " +
            "values (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String REFRESH_TOKEN_INSERT_STATEMENT = "insert into oauth_refresh_token " +
            "(token_id, token, authentication, expires_at) " +
            "values (?, ?, ?, ?)";

    protected StandardSerialization standardSerialization;
    protected AuthenticationKeyGenerator authenticationKeyGenerator = new DefaultAuthenticationKeyGenerator();

    protected final JdbcTemplate jdbcTemplate;

    public JmixJdbcTokenStore(DataSource dataSource, StandardSerialization standardSerialization) {
        super(dataSource);
        this.standardSerialization = standardSerialization;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void setAuthenticationKeyGenerator(AuthenticationKeyGenerator authenticationKeyGenerator) {
        super.setAuthenticationKeyGenerator(authenticationKeyGenerator);
        this.authenticationKeyGenerator = authenticationKeyGenerator;
    }

    @Override
    public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
        String refreshToken = null;
        if (token.getRefreshToken() != null) {
            refreshToken = token.getRefreshToken().getValue();
        }

        if (readAccessToken(token.getValue()) != null) {
            removeAccessToken(token.getValue());
        }

        jdbcTemplate.update(ACCESS_TOKEN_INSERT_STATEMENT,
                new Object[]{
                        extractTokenKey(token.getValue()), // token_id
                        new SqlLobValue(serializeAccessToken(token)), // token
                        authenticationKeyGenerator.extractKey(authentication), // authentication_id
                        authentication.isClientOnly() ? null : authentication.getName(), // user_name
                        authentication.getOAuth2Request().getClientId(), // client_id
                        new SqlLobValue(serializeAuthentication(authentication)), // authentication
                        extractTokenKey(refreshToken), // refresh_token
                        token.getExpiration() // expires_at
                },
                new int[]{
                        Types.VARCHAR, Types.BLOB, Types.VARCHAR, Types.VARCHAR,
                        Types.VARCHAR, Types.BLOB, Types.VARCHAR, Types.TIMESTAMP
                });
    }

    @Override
    public void storeRefreshToken(OAuth2RefreshToken refreshToken, OAuth2Authentication authentication) {
        Date expiration = null;
        if (refreshToken instanceof ExpiringOAuth2RefreshToken) {
            ExpiringOAuth2RefreshToken expiringRefreshToken = (ExpiringOAuth2RefreshToken) refreshToken;
            expiration = expiringRefreshToken.getExpiration();
        }
        jdbcTemplate.update(REFRESH_TOKEN_INSERT_STATEMENT,
                new Object[]{
                        extractTokenKey(refreshToken.getValue()),
                        new SqlLobValue(serializeRefreshToken(refreshToken)),
                        new SqlLobValue(serializeAuthentication(authentication)),
                        expiration
                },
                new int[]{Types.VARCHAR, Types.BLOB, Types.BLOB, Types.TIMESTAMP});
    }

    @Override
    protected OAuth2AccessToken deserializeAccessToken(byte[] token) {
        return (OAuth2AccessToken) standardSerialization.deserialize(token);
    }

    @Override
    protected OAuth2RefreshToken deserializeRefreshToken(byte[] token) {
        return (OAuth2RefreshToken) standardSerialization.deserialize(token);
    }

    @Override
    protected OAuth2Authentication deserializeAuthentication(byte[] authentication) {
        return (OAuth2Authentication) standardSerialization.deserialize(authentication);
    }
}
