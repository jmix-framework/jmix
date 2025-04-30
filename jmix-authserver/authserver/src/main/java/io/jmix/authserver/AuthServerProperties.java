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

package io.jmix.authserver;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "jmix.authserver")
public class AuthServerProperties {

    /**
     * Whether a default auto-configuration should be applied
     */
    boolean useDefaultConfiguration;

    /**
     * Whether InMemoryAuthorizationService should be used instead of JdbcOAuth2AuthorizationService
     */
    boolean useInMemoryAuthorizationService;

    /**
     * A list of jmix-specific client configurations
     */
    Map<String, JmixClient> client;

    /**
     * URL to send users to if login is required
     */
    String loginPageUrl;

    /**
     * A Spring MVC view name for the login page
     */
    String loginPageViewName;

    /**
     * Actual 'authorize' endpoint. Default: /oauth2/authorize
     * <p>
     * NOTE: This property should be specified if default endpoint has been remapped via Spring Security configuration.
     * Changing this property itself doesn't change actual authorize endpoint.
     */
    String authorizeEndpoint;

    /**
     * Name of url parameter within logout request which contains url to redirect to after logout
     */
    String postLogoutUrlRedirectParameterName;

    /**
     * Whether referer header value can be used as a target URL after logout.
     * <p>
     * Note: URL specified within {@link #postLogoutUrlRedirectParameterName} parameter has more priority
     */
    boolean useRefererPostLogout;

    public AuthServerProperties(
            @DefaultValue("true") boolean useDefaultConfiguration,
            @DefaultValue("false") boolean useInMemoryAuthorizationService,
            @DefaultValue Map<String, JmixClient> client,
            @DefaultValue("/as-login") String loginPageUrl,
            @DefaultValue("as-login.html") String loginPageViewName,
            @DefaultValue("/oauth2/authorize") String authorizeEndpoint,
            @DefaultValue("false") boolean useRefererPostLogout,
            String postLogoutUrlRedirectParameterName
    ) {
        this.useDefaultConfiguration = useDefaultConfiguration;
        this.useInMemoryAuthorizationService = useInMemoryAuthorizationService;
        this.client = client;
        this.loginPageUrl = loginPageUrl;
        this.loginPageViewName = loginPageViewName;
        this.authorizeEndpoint = authorizeEndpoint;
        this.postLogoutUrlRedirectParameterName = postLogoutUrlRedirectParameterName;
        this.useRefererPostLogout = useRefererPostLogout;
    }

    public boolean isUseDefaultConfiguration() {
        return useDefaultConfiguration;
    }

    public boolean isUseInMemoryAuthorizationService() {
        return useInMemoryAuthorizationService;
    }

    public Map<String, JmixClient> getClient() {
        return client;
    }

    public String getLoginPageUrl() {
        return loginPageUrl;
    }

    public String getLoginPageViewName() {
        return loginPageViewName;
    }

    public String getAuthorizeEndpoint() {
        return authorizeEndpoint;
    }

    public String getPostLogoutUrlRedirectParameterName() {
        return postLogoutUrlRedirectParameterName;
    }

    public boolean isUseRefererPostLogout() {
        return useRefererPostLogout;
    }

    /**
     * Class stores Jmix-specific settings of Authorization Server client.
     */
    public static class JmixClient {
        String clientId;
        List<String> resourceRoles;
        List<String> rowLevelRoles;

        public JmixClient(String clientId,
                          @DefaultValue List<String> resourceRoles,
                          @DefaultValue List<String> rowLevelRoles) {
            this.clientId = clientId;
            this.resourceRoles = resourceRoles;
            this.rowLevelRoles = rowLevelRoles;
        }

        public String getClientId() {
            return clientId;
        }

        public List<String> getResourceRoles() {
            return resourceRoles;
        }

        public List<String> getRowLevelRoles() {
            return rowLevelRoles;
        }
    }

}
