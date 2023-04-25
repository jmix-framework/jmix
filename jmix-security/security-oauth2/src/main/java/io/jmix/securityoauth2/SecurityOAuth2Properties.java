/*
 * Copyright 2021 Haulmont.
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

package io.jmix.securityoauth2;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "jmix.security.oauth2")
public class SecurityOAuth2Properties {
    private final String clientId;
    private final String clientSecret;
    private final int clientTokenExpirationTimeSec;
    private final int clientRefreshTokenExpirationTimeSec;
    private final String[] clientAuthorizedGrantTypes;
    private final boolean supportRefreshToken;
    private final boolean reuseRefreshToken;
    private final boolean tokenMaskingEnabled;
    private final boolean devMode;
    private final String devUsername;

    public SecurityOAuth2Properties(
            @DefaultValue("client") String clientId,
            @DefaultValue("{noop}secret") String clientSecret,
            @DefaultValue("43200") int clientTokenExpirationTimeSec, //12 hours
            @DefaultValue("31536000") int clientRefreshTokenExpirationTimeSec, //365 days
            @DefaultValue({"password", "external", "refresh_token"}) String[] clientAuthorizedGrantTypes,
            @DefaultValue("true") boolean supportRefreshToken,
            @DefaultValue("true") boolean reuseRefreshToken,
            @DefaultValue("true") boolean tokenMaskingEnabled,
            @DefaultValue("false") boolean devMode,
            String devUsername) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.clientTokenExpirationTimeSec = clientTokenExpirationTimeSec;
        this.clientRefreshTokenExpirationTimeSec = clientRefreshTokenExpirationTimeSec;
        this.clientAuthorizedGrantTypes = clientAuthorizedGrantTypes;
        this.supportRefreshToken = supportRefreshToken;
        this.reuseRefreshToken = reuseRefreshToken;
        this.tokenMaskingEnabled = tokenMaskingEnabled;
        this.devMode = devMode;
        this.devUsername = devUsername;
    }

    /**
     * @return authorized rest client id
     */
    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * @return token masking in application logs is enabled
     */
    public boolean isTokenMaskingEnabled() {
        return tokenMaskingEnabled;
    }

    /**
     * @return access token expiration time in seconds for the default client
     */
    public int getClientTokenExpirationTimeSec() {
        return clientTokenExpirationTimeSec;
    }

    /**
     * @return refresh token expiration time in seconds for the default client
     */
    public int getClientRefreshTokenExpirationTimeSec() {
        return clientRefreshTokenExpirationTimeSec;
    }

    /**
     * @return authorized grant types for the default client
     */
    public String[] getClientAuthorizedGrantTypes() {
        return clientAuthorizedGrantTypes;
    }

    /**
     * @return whether to support the refresh token
     */
    public boolean isSupportRefreshToken() {
        return supportRefreshToken;
    }

    /**
     * @return whether to reuse refresh tokens (until expired)
     */
    public boolean isReuseRefreshToken() {
        return reuseRefreshToken;
    }

    /**
     * Development mode allows using of REST/GraphQL APIs without authentication token.
     * Default user that is used for all REST/GraphQL APIs invocation
     * in dev mode is specified by {@link SecurityOAuth2Properties#getDevUsername()}
     */
    public boolean isDevMode() {
        return devMode;
    }

    /**
     * @return - username that is used for development mode.
     * See {@link SecurityOAuth2Properties#isDevMode()}
     */
    public String getDevUsername() {
        return devUsername;
    }
}
