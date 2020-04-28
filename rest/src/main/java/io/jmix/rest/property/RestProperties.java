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

package io.jmix.rest.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.List;

@ConfigurationProperties(prefix = "jmix.rest")
@ConstructorBinding
public class RestProperties {

    String clientId;
    boolean standardAuthenticationEnabled;
    List<String> standardAuthenticationUsers;
    boolean anonymousEnabled;
    boolean tokenMaskingEnabled;
    boolean optimisticLockingEnabled;
    boolean checkPasswordOnClient;
    List<String> externalRestBypassPatterns;
    boolean responseViewEnabled;
    String securityScope;
    boolean storeTokensInDb;
    boolean syncTokenReplication;

    public RestProperties(String clientId,
                          @DefaultValue("true") boolean standardAuthenticationEnabled,
                          List<String> standardAuthenticationUsers,
                          @DefaultValue("false") boolean anonymousEnabled,
                          @DefaultValue("true") boolean tokenMaskingEnabled,
                          @DefaultValue("false") boolean optimisticLockingEnabled,
                          @DefaultValue("false") boolean checkPasswordOnClient,
                          List<String> externalRestBypassPatterns,
                          @DefaultValue("true") boolean responseViewEnabled,
                          @DefaultValue("REST") String securityScope,
                          @DefaultValue("false") boolean storeTokensInDb,
                          @DefaultValue("false") boolean syncTokenReplication) {
        this.clientId = clientId;
        this.standardAuthenticationEnabled = standardAuthenticationEnabled;
        this.standardAuthenticationUsers = standardAuthenticationUsers;
        this.anonymousEnabled = anonymousEnabled;
        this.tokenMaskingEnabled = tokenMaskingEnabled;
        this.optimisticLockingEnabled = optimisticLockingEnabled;
        this.checkPasswordOnClient = checkPasswordOnClient;
        this.externalRestBypassPatterns = externalRestBypassPatterns;
        this.responseViewEnabled = responseViewEnabled;
        this.securityScope = securityScope;
        this.storeTokensInDb = storeTokensInDb;
        this.syncTokenReplication = syncTokenReplication;
    }

    /**
     * @return authorized rest client id
     */
    public String getRestClientId() {
        return clientId;
    }

    public boolean getStandardAuthenticationEnabled() {
        return standardAuthenticationEnabled;
    }

    /**
     * @return list of users that are not allowed to use external authentication. They can use only standard authentication.
     * Empty list means that everyone is allowed to login using external authentication.
     */
    public List<String> getStandardAuthenticationUsers() {
        return standardAuthenticationUsers;
    }

    /**
     * @return anonymous access to REST API is allowed
     */
    public boolean getRestAnonymousEnabled() {
        return anonymousEnabled;
    }

    /**
     * @return token masking in application logs is enabled
     */
    public boolean getTokenMaskingEnabled() {
        return tokenMaskingEnabled;
    }

    /**
     * @return whether the passed entities versions should be validated before entities are persisted
     */
    public boolean getOptimisticLockingEnabled() {
        return optimisticLockingEnabled;
    }


    /**
     * @return Whether to use an login/password authentication on client
     * instead of login/password authentication on middleware.
     */
    public boolean getCheckPasswordOnClient() {
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
    public boolean getRestResponseViewEnabled() {
        return responseViewEnabled;
    }

    /**
     * Active security scope for a REST client.
     * Security scope specifies which roles will be loaded for user session
     */
    public String getSecurityScope() {
        return securityScope;
    }

    /**
     * @return whether to store REST API OAuth tokens in the database
     */
    boolean isRestStoreTokensInDb() {
        return storeTokensInDb;
    }

    /**
     * @return whether newly created tokens should be sent to the cluster synchronously
     */
    boolean isSyncTokenReplication() {
        return syncTokenReplication;
    }
}
