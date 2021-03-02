/*
 * Copyright 2020 Haulmont.
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

package io.jmix.rest;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "jmix.rest")
@ConstructorBinding
public class RestProperties {

    String clientId;
    private String clientSecret;
    private final int clientTokenExpirationTimeSec;
    private final int clientRefreshTokenExpirationTimeSec;
    private final String[] clientAuthorizedGrantTypes;
    private final boolean supportRefreshToken;
    private final boolean reuseRefreshToken;
    private final String[] allowedOrigins;
    private final int maxUploadSize;
    boolean standardAuthenticationEnabled;
    List<String> standardAuthenticationUsers;
    boolean tokenMaskingEnabled;
    boolean optimisticLockingEnabled;
    boolean checkPasswordOnClient;
    List<String> externalRestBypassPatterns;
    boolean responseViewEnabled;
    String securityScope;
    boolean storeTokensInDb;
    Integer defaultMaxFetchSize;
    Map<String, Integer> entityMaxFetchSize;

    public RestProperties(
            @DefaultValue("client") String clientId,
            @DefaultValue("{noop}secret") String clientSecret,
            @DefaultValue("43200") int clientTokenExpirationTimeSec, //12 hours
            @DefaultValue("31536000") int clientRefreshTokenExpirationTimeSec, //365 days
            @DefaultValue({"password", "external", "refresh_token"}) String[] clientAuthorizedGrantTypes,
            @DefaultValue("true") boolean supportRefreshToken,
            @DefaultValue("true") boolean reuseRefreshToken,
            @DefaultValue("*") String[] allowedOrigins,
            //todo DataSize type
            @DefaultValue("20971520") int maxUploadSize,
            @DefaultValue("true") boolean standardAuthenticationEnabled,
            List<String> standardAuthenticationUsers,
            @DefaultValue("true") boolean tokenMaskingEnabled,
            @DefaultValue("false") boolean optimisticLockingEnabled,
            @DefaultValue("false") boolean checkPasswordOnClient,
            List<String> externalRestBypassPatterns,
            @DefaultValue("true") boolean responseViewEnabled,
            @DefaultValue("REST") String securityScope,
            @DefaultValue("false") boolean storeTokensInDb,
            @DefaultValue("10000") Integer defaultMaxFetchSize,
            @Nullable Map<String, Integer> entityMaxFetchSize) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.clientTokenExpirationTimeSec = clientTokenExpirationTimeSec;
        this.clientRefreshTokenExpirationTimeSec = clientRefreshTokenExpirationTimeSec;
        this.clientAuthorizedGrantTypes = clientAuthorizedGrantTypes;
        this.supportRefreshToken = supportRefreshToken;
        this.reuseRefreshToken = reuseRefreshToken;
        this.allowedOrigins = allowedOrigins;
        this.maxUploadSize = maxUploadSize;
        this.standardAuthenticationEnabled = standardAuthenticationEnabled;
        this.standardAuthenticationUsers = standardAuthenticationUsers;
        this.tokenMaskingEnabled = tokenMaskingEnabled;
        this.optimisticLockingEnabled = optimisticLockingEnabled;
        this.checkPasswordOnClient = checkPasswordOnClient;
        this.externalRestBypassPatterns = externalRestBypassPatterns;
        this.responseViewEnabled = responseViewEnabled;
        this.securityScope = securityScope;
        this.storeTokensInDb = storeTokensInDb;
        this.defaultMaxFetchSize = defaultMaxFetchSize;
        this.entityMaxFetchSize = entityMaxFetchSize == null ? Collections.emptyMap() : entityMaxFetchSize;
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

    public boolean isStandardAuthenticationEnabled() {
        return standardAuthenticationEnabled;
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
     * @return whether the passed entities versions should be validated before entities are persisted
     */
    public boolean isOptimisticLockingEnabled() {
        return optimisticLockingEnabled;
    }


    /**
     * @return Whether to use an login/password authentication on client instead of login/password authentication on
     * middleware.
     */
    public boolean isCheckPasswordOnClient() {
        return checkPasswordOnClient;
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
     * @return whether "responseView" param is required
     */
    public boolean isResponseViewEnabled() {
        return responseViewEnabled;
    }

    /**
     * Active security scope for a REST client. Security scope specifies which roles will be loaded for user session
     */
    public String getSecurityScope() {
        return securityScope;
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
     * @return allowed origins for cross-domain requests
     */
    public String[] getAllowedOrigins() {
        return allowedOrigins;
    }

    /**
     * @return maximum size of the file that may be uploaded with REST API in bytes
     */
    public int getMaxUploadSize() {
        return maxUploadSize;
    }

    /**
     * @return hether to store REST API OAuth tokens in the database
     */
    public boolean isStoreTokensInDb() {
        return storeTokensInDb;
    }

    public int getDefaultMaxFetchSize() {
        return defaultMaxFetchSize;
    }

    public int getEntityMaxFetchSize(String entityName) {
        Integer forEntity = entityMaxFetchSize.get(entityName);
        if (forEntity != null)
            return forEntity;
        return defaultMaxFetchSize;
    }
}
