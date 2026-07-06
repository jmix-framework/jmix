/*
 * Copyright 2026 Haulmont.
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

package io.jmix.saml;

import org.apache.commons.collections4.ListUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.lang.Nullable;

import java.util.List;

@ConfigurationProperties(prefix = "jmix.saml")
public class SamlProperties {

    /**
     * Whether to force 'redirect' binding for logout requests.
     * If true, logout requests will always use the 'redirect' binding. Otherwise, the binding will be determined
     * based on the RelyingPartyRegistration
     */
    boolean forceRedirectBindingLogout;
    /**
     * Number of lock stripes used by {@link io.jmix.saml.mapper.user.BaseSamlUserMapper} to serialize concurrent
     * mapping of the same username within this JVM. Mappings of the same username always share one lock; mappings
     * of different usernames may occasionally share a stripe and briefly wait for each other. The locks do not
     * work across cluster nodes — the unique database constraint on the username is the actual guard against
     * duplicate user creation.
     */
    int maxConcurrentUserMapping;

    /**
     * URL to redirect to when SAML single logout completes or cannot be performed.
     */
    String logoutSuccessUrl;

    /**
     * Whether to expose the SAML service provider metadata endpoint ('/saml2/metadata' and
     * '/saml2/metadata/{registrationId}'). The metadata XML is used to configure the identity provider.
     */
    boolean exposeMetadata;

    /**
     * Name of the SAML assertion attribute to take the username from. By default, the username is taken from
     * the subject NameID. Set this property when the identity provider releases a 'transient' NameID (its value
     * changes on every login, which breaks user synchronization) or when a specific attribute (e.g. email)
     * should identify the user.
     */
    String usernameAttribute;

    /**
     * DefaultSamlAssertionRolesMapper configuration.
     */
    DefaultSamlAssertionRolesMapperConfig defaultSamlAssertionRolesMapper;

    /**
     * Set of properties to configure Security filter chains.
     */
    FilterChain filterChain;

    public SamlProperties(@DefaultValue("true") boolean forceRedirectBindingLogout,
                          @DefaultValue("128") int maxConcurrentUserMapping,
                          @DefaultValue("/") String logoutSuccessUrl,
                          @DefaultValue("true") boolean exposeMetadata,
                          @Nullable String usernameAttribute,
                          @DefaultValue DefaultSamlAssertionRolesMapperConfig defaultSamlAssertionRolesMapper,
                          @DefaultValue FilterChain filterChain) {
        this.forceRedirectBindingLogout = forceRedirectBindingLogout;
        this.maxConcurrentUserMapping = maxConcurrentUserMapping;
        this.logoutSuccessUrl = logoutSuccessUrl;
        this.exposeMetadata = exposeMetadata;
        this.usernameAttribute = usernameAttribute;

        this.defaultSamlAssertionRolesMapper = defaultSamlAssertionRolesMapper;
        this.filterChain = filterChain;
    }

    /**
     * @see #forceRedirectBindingLogout
     */
    public boolean isForceRedirectBindingLogout() {
        return forceRedirectBindingLogout;
    }

    /**
     * @see #maxConcurrentUserMapping
     */
    public int getMaxConcurrentUserMapping() {
        return maxConcurrentUserMapping;
    }

    /**
     * @see #logoutSuccessUrl
     */
    public String getLogoutSuccessUrl() {
        return logoutSuccessUrl;
    }

    /**
     * @see #exposeMetadata
     */
    public boolean isExposeMetadata() {
        return exposeMetadata;
    }

    /**
     * @see #usernameAttribute
     */
    @Nullable
    public String getUsernameAttribute() {
        return usernameAttribute;
    }

    /**
     * @see #defaultSamlAssertionRolesMapper
     */
    public DefaultSamlAssertionRolesMapperConfig getDefaultSamlAssertionRolesMapper() {
        return defaultSamlAssertionRolesMapper;
    }

    /**
     * @see #filterChain
     */
    public FilterChain getFilterChain() {
        return filterChain;
    }

    public static class DefaultSamlAssertionRolesMapperConfig {

        /**
         * Name of SAML assertion attribute that contains roles.
         */
        String rolesAssertionAttribute;

        /**
         * Prefix that can be used to recognize a resource role from the authorization server
         */
        String resourceRolePrefix;

        /**
         * Prefix that can be used to recognize a row-level role from the authorization server
         */
        String rowLevelRolePrefix;

        public DefaultSamlAssertionRolesMapperConfig(@DefaultValue("Role") String rolesAssertionAttribute,
                                                     @DefaultValue("") String resourceRolePrefix,
                                                     @DefaultValue("") String rowLevelRolePrefix) {
            this.rolesAssertionAttribute = rolesAssertionAttribute;
            this.resourceRolePrefix = resourceRolePrefix;
            this.rowLevelRolePrefix = rowLevelRolePrefix;
        }

        /**
         * @see #rolesAssertionAttribute
         */
        public String getRolesAssertionAttribute() {
            return rolesAssertionAttribute;
        }

        /**
         * @see #resourceRolePrefix
         */
        public String getResourceRolePrefix() {
            return resourceRolePrefix;
        }

        /**
         * @see #rowLevelRolePrefix
         */
        public String getRowLevelRolePrefix() {
            return rowLevelRolePrefix;
        }
    }

    public static class FilterChain {

        /**
         * Whether the forced UI scope is enabled for Security filter chains provided via {@link #uiScopeSecurityFilterChainNames}.
         */
        boolean forceUiScopeEnabled;

        /**
         * Represents a list of security filter chain names which should be customized
         * by {@link io.jmix.saml.filter.SamlVaadinSecurityFilterChainCustomizer}.
         *
         * @see #forceUiScopeEnabled
         */
        List<String> uiScopeSecurityFilterChainNames;

        public FilterChain(@DefaultValue("true") boolean forceUiScopeEnabled,
                           List<String> uiScopeSecurityFilterChainNames) {
            this.forceUiScopeEnabled = forceUiScopeEnabled;
            this.uiScopeSecurityFilterChainNames = ListUtils.emptyIfNull(uiScopeSecurityFilterChainNames);
        }

        /**
         * @see #forceUiScopeEnabled
         */
        public boolean isForceUiScopeEnabled() {
            return forceUiScopeEnabled;
        }

        /**
         * @see #uiScopeSecurityFilterChainNames
         */
        public List<String> getUiScopeSecurityFilterChainNames() {
            return uiScopeSecurityFilterChainNames;
        }
    }
}
