/*
 * Copyright 2019 Haulmont.
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

package io.jmix.security;

import io.jmix.core.JmixOrder;
import io.jmix.security.impl.StandardAuthenticationProvidersProducer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

import static io.jmix.security.SecurityConfigurers.uiSecurity;

public class StandardSecurityConfiguration {

    public static final String SECURITY_CONFIGURER_QUALIFIER = "standard-security";

    @Bean("sec_StandardSecurityFilterChain")
    @Order(JmixOrder.HIGHEST_PRECEDENCE + 300)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.apply(uiSecurity());
        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/"));
        SecurityConfigurers.applySecurityConfigurersWithQualifier(http, SECURITY_CONFIGURER_QUALIFIER);
        return http.build();
    }

    @Bean("sec_AuthenticationManager")
    public AuthenticationManager authenticationManager(StandardAuthenticationProvidersProducer providersProducer,
                                                       AuthenticationEventPublisher authenticationEventPublisher) {
        List<AuthenticationProvider> providers = providersProducer.getStandardProviders();
        ProviderManager providerManager = new ProviderManager(providers);
        providerManager.setAuthenticationEventPublisher(authenticationEventPublisher);
        return providerManager;
    }

    @Bean("sec_AuthenticationEventPublisher")
    public DefaultAuthenticationEventPublisher authenticationEventPublisher(ApplicationEventPublisher publisher) {
        return new DefaultAuthenticationEventPublisher(publisher);
    }
}
