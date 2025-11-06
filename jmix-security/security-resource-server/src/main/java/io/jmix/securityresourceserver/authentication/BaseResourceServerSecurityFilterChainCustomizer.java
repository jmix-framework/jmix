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

import io.jmix.security.configurer.ApiClientDetailsSource;
import io.jmix.security.configurer.BaseSecurityFilterChainCustomizer;
import io.jmix.security.util.ClientDetailsSourceSupport;
import jakarta.servlet.Filter;
import org.slf4j.Logger;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import static org.slf4j.LoggerFactory.getLogger;

public abstract class BaseResourceServerSecurityFilterChainCustomizer extends BaseSecurityFilterChainCustomizer {

    private static final Logger log = getLogger(BaseResourceServerSecurityFilterChainCustomizer.class);

    public BaseResourceServerSecurityFilterChainCustomizer(ClientDetailsSourceSupport clientDetailsSourceSupport) {
        super(clientDetailsSourceSupport);
    }

    @Override
    protected void customizeFilter(String chainName, SecurityFilterChain chain, Filter filter) {
        if (filter instanceof BearerTokenAuthenticationFilter filterToCustomize) {
            customizeBearerTokenAuthenticationFilter(filterToCustomize);
        }
    }

    protected void customizeBearerTokenAuthenticationFilter(BearerTokenAuthenticationFilter filter) {
        log.debug("Customize BearerTokenAuthenticationFilter");
        filter.setAuthenticationDetailsSource(new ApiClientDetailsSource(clientDetailsSourceSupport));
    }
}