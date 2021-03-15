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
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.List;

@ConfigurationProperties(prefix = "jmix.sec.oauth2")
@ConstructorBinding
public class SecurityOAuth2Properties {
    private final String clientId;
    private final String clientSecret;
    private final int clientTokenExpirationTimeSec;
    private final int clientRefreshTokenExpirationTimeSec;
    private final String[] clientAuthorizedGrantTypes;
    private final boolean supportRefreshToken;
    private final boolean reuseRefreshToken;
    boolean standardAuthenticationEnabled;
    List<String> standardAuthenticationUsers;
    boolean tokenMaskingEnabled;
    List<String> externalRestBypassPatterns;

    public SecurityOAuth2Properties(
            @DefaultValue("client") String clientId,
            @DefaultValue("{noop}secret") String clientSecret,
            @DefaultValue("43200") int clientTokenExpirationTimeSec, //12 hours
            @DefaultValue("31536000") int clientRefreshTokenExpirationTimeSec, //365 days
            @DefaultValue({"password", "external", "refresh_token"}) String[] clientAuthorizedGrantTypes,
            @DefaultValue("true") boolean supportRefreshToken,
            @DefaultValue("true") boolean reuseRefreshToken,
            @DefaultValue("true") boolean standardAuthenticationEnabled,
            List<String> standardAuthenticationUsers,
            @DefaultValue("true") boolean tokenMaskingEnabled,
            List<String> externalRestBypassPatterns) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.clientTokenExpirationTimeSec = clientTokenExpirationTimeSec;
        this.clientRefreshTokenExpirationTimeSec = clientRefreshTokenExpirationTimeSec;
        this.clientAuthorizedGrantTypes = clientAuthorizedGrantTypes;
        this.supportRefreshToken = supportRefreshToken;
        this.reuseRefreshToken = reuseRefreshToken;
        this.standardAuthenticationEnabled = standardAuthenticationEnabled;
        this.standardAuthenticationUsers = standardAuthenticationUsers;
        this.tokenMaskingEnabled = tokenMaskingEnabled;
        this.externalRestBypassPatterns = externalRestBypassPatterns;
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
     * @return list of users that are not allowed to use external authentication. They can use only standard
     * authentication. Empty list means that everyone is allowed to login using external authentication.
     */
    public List<String> getStandardAuthenticationUsers() {
        return standardAuthenticationUsers;
    }

    /**
     * @return token masking in application logs is enabled
     */
    public boolean isTokenMaskingEnabled() {
        return tokenMaskingEnabled;
    }

    /**
     * Application components can use this property to set URL patterns to bypass by REST API.
     * <p>
     * Patterns are trailing sub-paths of REST API base mapping: {@code /rest/v2} starting with "/".
     * <p>
     * Example for IDP REST Auth to bypass all {@code /rest/v2/idp/**} requests:
     * <pre>
     *    cuba.web.externalRestBypassPatterns = +/idp/,
     * </pre>
     *
     * @return Comma-separated list with trailing comma of patterns for REST {@link RequestMatcher} to bypass.
     */
    public List<String> getExternalRestBypassPatterns() {
        return externalRestBypassPatterns;
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
}
