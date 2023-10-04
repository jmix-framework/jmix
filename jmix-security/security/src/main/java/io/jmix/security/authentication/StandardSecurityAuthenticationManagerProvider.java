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

package io.jmix.security.authentication;

import io.jmix.core.security.AddonAuthenticationManagerProvider;
import io.jmix.core.security.StandardAuthenticationProvidersProducer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.ProviderManager;

import java.util.List;

/**
 * The default AddonAuthenticationManagerProvider that provides AuthenticationManager used by basic
 * application.
 */
public class StandardSecurityAuthenticationManagerProvider implements AddonAuthenticationManagerProvider {

    protected StandardAuthenticationProvidersProducer providersProducer;

    protected ApplicationEventPublisher publisher;

    public StandardSecurityAuthenticationManagerProvider(StandardAuthenticationProvidersProducer providersProducer,
                                                         ApplicationEventPublisher publisher) {
        this.providersProducer = providersProducer;
        this.publisher = publisher;
    }

    @Override
    public AuthenticationManager getAuthenticationManager() {
        List<AuthenticationProvider> providers = providersProducer.getStandardProviders();
        ProviderManager providerManager = new ProviderManager(providers);
        providerManager.setAuthenticationEventPublisher(new DefaultAuthenticationEventPublisher(publisher));
        return providerManager;
    }
}
