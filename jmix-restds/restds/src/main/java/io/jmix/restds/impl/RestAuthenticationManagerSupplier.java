/*
 * Copyright 2024 Haulmont.
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

package io.jmix.restds.impl;

import io.jmix.core.security.AddonAuthenticationManagerSupplier;
import io.jmix.security.authentication.StandardAuthenticationProvidersProducer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public class RestAuthenticationManagerSupplier implements AddonAuthenticationManagerSupplier {

    protected StandardAuthenticationProvidersProducer providersProducer;

    protected ApplicationEventPublisher publisher;
    private final RestPasswordAuthenticator restAuthenticator;
    private final UserDetailsService userDetailsService;

    public RestAuthenticationManagerSupplier(StandardAuthenticationProvidersProducer providersProducer,
                                             ApplicationEventPublisher publisher,
                                             RestPasswordAuthenticator restAuthenticator,
                                             UserDetailsService userDetailsService) {
        this.providersProducer = providersProducer;
        this.publisher = publisher;
        this.restAuthenticator = restAuthenticator;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public AuthenticationManager getAuthenticationManager() {
        List<AuthenticationProvider> providers = providersProducer.getStandardProviders();
        providers.add(new RestAuthenticationProvider(restAuthenticator, userDetailsService));
        ProviderManager providerManager = new ProviderManager(providers);
        providerManager.setAuthenticationEventPublisher(new DefaultAuthenticationEventPublisher(publisher));
        return providerManager;
    }
}
