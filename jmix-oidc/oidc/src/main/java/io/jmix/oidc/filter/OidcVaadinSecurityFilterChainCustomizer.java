/*
 * Copyright 2025 Haulmont.
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

package io.jmix.oidc.filter;

import io.jmix.oidc.OidcProperties;
import io.jmix.security.configurer.BaseSecurityFilterChainCustomizer;
import io.jmix.security.configurer.UiClientDetailsSource;
import io.jmix.security.util.ClientDetailsSourceSupport;
import jakarta.servlet.Filter;
import org.slf4j.Logger;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class OidcVaadinSecurityFilterChainCustomizer extends BaseSecurityFilterChainCustomizer {

    private static final Logger log = getLogger(OidcVaadinSecurityFilterChainCustomizer.class);

    protected final OidcProperties oidcProperties;

    public OidcVaadinSecurityFilterChainCustomizer(ClientDetailsSourceSupport clientDetailsSourceSupport,
                                                   OidcProperties oidcProperties) {
        super(clientDetailsSourceSupport);
        this.oidcProperties = oidcProperties;
    }

    @Override
    public List<String> getChainBeanNames() {
        return oidcProperties.getFilterChain().getUiScopeSecurityFilterChainNames();
    }

    @Override
    public boolean isEnabled() {
        return oidcProperties.getFilterChain().isForceUiScopeEnabled();
    }

    @Override
    protected void customizeFilter(String chainName, SecurityFilterChain chain, Filter filter) {
        if (filter instanceof OAuth2LoginAuthenticationFilter filterToCustomize) {
            customizeOAuth2LoginAuthenticationFilter(filterToCustomize);
        }
    }

    protected void customizeOAuth2LoginAuthenticationFilter(OAuth2LoginAuthenticationFilter filter) {
        log.debug("Customize OAuth2LoginAuthenticationFilter");
        filter.setAuthenticationDetailsSource(new UiClientDetailsSource(clientDetailsSourceSupport));
    }
}