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

package io.jmix.authserver.filter;

import io.jmix.authserver.AuthServerProperties;
import io.jmix.security.util.ClientDetailsSourceSupport;
import io.jmix.securityresourceserver.authentication.BaseResourceServerSecurityFilterChainCustomizer;

import java.util.List;

public class AuthServerResourceServerSecurityFilterChainCustomizer extends BaseResourceServerSecurityFilterChainCustomizer {

    protected final AuthServerProperties authServerProperties;

    public AuthServerResourceServerSecurityFilterChainCustomizer(ClientDetailsSourceSupport clientDetailsSourceSupport,
                                                                 AuthServerProperties authServerProperties) {
        super(clientDetailsSourceSupport);
        this.authServerProperties = authServerProperties;
    }

    @Override
    public List<String> getChainBeanNames() {
        return authServerProperties.getFilterChain().getApiScopeSecurityFilterChainNames();
    }

    @Override
    public boolean isEnabled() {
        return authServerProperties.getFilterChain().isForceApiScopeEnabled();
    }
}