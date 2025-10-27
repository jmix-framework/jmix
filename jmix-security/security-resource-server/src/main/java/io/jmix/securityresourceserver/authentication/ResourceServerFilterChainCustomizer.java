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

package io.jmix.securityresourceserver.authentication;

import io.jmix.security.util.RequestLocaleProvider;
import jakarta.servlet.Filter;
import org.slf4j.Logger;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Customizes {@link SecurityFilterChain} provided by {@link ForceApiSecurityScopePropertiesProvider} -
 * sets {@link ApiClientDetailsSource} as AuthenticationDetailsSource to {@link BearerTokenAuthenticationFilter}
 * within provided list of {@link SecurityFilterChain}
 */
public class ResourceServerFilterChainCustomizer implements SmartInitializingSingleton {

    private static final Logger log = getLogger(ResourceServerFilterChainCustomizer.class);

    protected final ForceApiSecurityScopePropertiesProvider forceApiSecurityScopePropertiesProvider;
    protected final RequestLocaleProvider requestLocaleProvider;

    protected final Map<String, SecurityFilterChain> chains;

    public ResourceServerFilterChainCustomizer(ForceApiSecurityScopePropertiesProvider forceApiSecurityScopePropertiesProvider,
                                               RequestLocaleProvider requestLocaleProvider,
                                               Map<String, SecurityFilterChain> chains) {
        this.forceApiSecurityScopePropertiesProvider = forceApiSecurityScopePropertiesProvider;
        this.requestLocaleProvider = requestLocaleProvider;
        this.chains = chains;
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (forceApiSecurityScopePropertiesProvider.isEnabled()) {
            List<String> chainNames = getChainNames();
            log.debug("Customize security filter chains: {}", chainNames);
            for (String targetChainName : chainNames) {
                SecurityFilterChain chain = chains.get(targetChainName);
                if (chain != null) {
                    log.debug("Chain with name '{}' is found: {}", targetChainName, chain);
                    for (Filter filter : chain.getFilters()) {
                        if (filter instanceof BearerTokenAuthenticationFilter filterToCustomize) {
                            customizeBearerTokenAuthenticationFilter(filterToCustomize);
                        }
                    }
                } else {
                    log.debug("Chain with name '{}' is not found", targetChainName);
                }
            }
        } else {
            log.debug("Security filter chains customization is disabled");
        }
    }

    protected List<String> getChainNames() {
        return forceApiSecurityScopePropertiesProvider.getSecurityFilterChainNames();
    }

    protected void customizeBearerTokenAuthenticationFilter(BearerTokenAuthenticationFilter filter) {
        log.debug("Customize BearerTokenAuthenticationFilter");
        filter.setAuthenticationDetailsSource(new ApiClientDetailsSource(requestLocaleProvider));
    }
}