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

package io.jmix.saml.filter;

import io.jmix.saml.SamlProperties;
import io.jmix.security.configurer.BaseSecurityFilterChainCustomizer;
import io.jmix.security.configurer.UiClientDetailsSource;
import io.jmix.security.util.ClientDetailsSourceSupport;
import jakarta.servlet.Filter;
import org.slf4j.Logger;
import org.springframework.security.saml2.provider.service.web.authentication.Saml2WebSsoAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class SamlVaadinSecurityFilterChainCustomizer extends BaseSecurityFilterChainCustomizer {

    private static final Logger log = getLogger(SamlVaadinSecurityFilterChainCustomizer.class);

    protected final SamlProperties samlProperties;

    public SamlVaadinSecurityFilterChainCustomizer(ClientDetailsSourceSupport clientDetailsSourceSupport,
                                                   SamlProperties samlProperties) {
        super(clientDetailsSourceSupport);
        this.samlProperties = samlProperties;
    }

    @Override
    public List<String> getChainBeanNames() {
        return samlProperties.getFilterChain().getUiScopeSecurityFilterChainNames();
    }

    @Override
    public boolean isEnabled() {
        return samlProperties.getFilterChain().isForceUiScopeEnabled();
    }

    @Override
    protected void customizeFilter(String chainName, SecurityFilterChain chain, Filter filter) {
        if (filter instanceof Saml2WebSsoAuthenticationFilter filterToCustomize) {
            customizeSaml2WebSsoAuthenticationFilter(filterToCustomize);
        }
    }

    protected void customizeSaml2WebSsoAuthenticationFilter(Saml2WebSsoAuthenticationFilter filter) {
        log.debug("Customize Saml2WebSsoAuthenticationFilter");
        filter.setAuthenticationDetailsSource(new UiClientDetailsSource(clientDetailsSourceSupport));
    }
}
