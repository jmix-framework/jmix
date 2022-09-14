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

package io.jmix.authorizationserver;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.Set;

@ConfigurationProperties(prefix = "jmix.authorization-server.default-client")
@ConstructorBinding
public class DefaultRegisteredClientProperties {

    /**
     * Client id
     */
    String clientId;

    /**
     * Client secret
     */
    String clientSecret;

    /**
     * Redirect URI the client may use in a redirect-based flow
     */
    Set<String> redirectUris;

    /**
     * Access token time-to-live
     */
    Duration accessTokenTimeToLive;

    /**
     * Refresh token time-to-live
     */
    Duration refreshTokenTimeToLive;

    public DefaultRegisteredClientProperties(
            @DefaultValue("client") String clientId,
            @DefaultValue("{noop}secret") String clientSecret,
            @DefaultValue("http://localhost:8080/authorized") Set<String> redirectUris,
            @DefaultValue("5m") Duration accessTokenTimeToLive,
            @DefaultValue("60m") Duration refreshTokenTimeToLive) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUris = redirectUris;
        this.accessTokenTimeToLive = accessTokenTimeToLive;
        this.refreshTokenTimeToLive = refreshTokenTimeToLive;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public Set<String> getRedirectUris() {
        return redirectUris;
    }

    public Duration getAccessTokenTimeToLive() {
        return accessTokenTimeToLive;
    }

    public Duration getRefreshTokenTimeToLive() {
        return refreshTokenTimeToLive;
    }
}
