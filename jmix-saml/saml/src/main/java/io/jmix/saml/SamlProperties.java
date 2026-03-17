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

import java.util.List;

@ConfigurationProperties(prefix = "jmix.saml")
public class SamlProperties {

    boolean forceRedirectBindingLogout;
    int maxConcurrentUserMapping;

    DefaultSamlAssertionRolesMapperConfig defaultSamlAssertionRolesMapper;
    FilterChain filterChain;

    public SamlProperties(@DefaultValue("true") boolean forceRedirectBindingLogout,
                          @DefaultValue("128") int maxConcurrentUserMapping,
                          @DefaultValue DefaultSamlAssertionRolesMapperConfig defaultSamlAssertionRolesMapper,
                          @DefaultValue FilterChain filterChain) {
        this.forceRedirectBindingLogout = forceRedirectBindingLogout;
        this.maxConcurrentUserMapping = maxConcurrentUserMapping;

        this.defaultSamlAssertionRolesMapper = defaultSamlAssertionRolesMapper;
        this.filterChain = filterChain;
    }

    public boolean isForceRedirectBindingLogout() {
        return forceRedirectBindingLogout;
    }

    public int getMaxConcurrentUserMapping() {
        return maxConcurrentUserMapping;
    }

    public DefaultSamlAssertionRolesMapperConfig getDefaultSamlAssertionRolesMapper() {
        return defaultSamlAssertionRolesMapper;
    }

    public FilterChain getFilterChain() {
        return filterChain;
    }

    public static class DefaultSamlAssertionRolesMapperConfig {

        String rolesAssertionAttribute;

        public DefaultSamlAssertionRolesMapperConfig(@DefaultValue("Role") String rolesAssertionAttribute) {
            this.rolesAssertionAttribute = rolesAssertionAttribute;
        }

        public String getRolesAssertionAttribute() {
            return rolesAssertionAttribute;
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
