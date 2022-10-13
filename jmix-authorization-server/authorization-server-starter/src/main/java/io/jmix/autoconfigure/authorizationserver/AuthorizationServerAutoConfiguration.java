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

package io.jmix.autoconfigure.authorizationserver;

import io.jmix.authorizationserver.AuthorizationServerConfiguration;
import io.jmix.authorizationserver.DefaultRegisteredClientProperties;
import io.jmix.authorizationserver.client.RegisteredClientProvider;
import io.jmix.authorizationserver.client.impl.DefaultRegisteredClientProvider;
import io.jmix.authorizationserver.introspection.AuthorizationServiceOpaqueTokenIntrospector;
import io.jmix.core.JmixOrder;
import io.jmix.security.SecurityConfigurers;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@AutoConfiguration
@Import({AuthorizationServerConfiguration.class})
@ConditionalOnProperty(name = "jmix.authorization-server.use-default-configuration", matchIfMissing = true)
public class AuthorizationServerAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    public static class AuthorizationServerSecurityConfiguration {

        public static final String SECURITY_CONFIGURER_QUALIFIER = "authorization-server";
        public static final String LOGIN_FORM_SECURITY_CONFIGURER_QUALIFIER = "authorization-server-login-form";

        @Bean("authsr_AuthorizationServerSecurityFilterChain")
        @Order(JmixOrder.HIGHEST_PRECEDENCE + 100)
        public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
                throws Exception {
            OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
            http
                    // Redirect to the login page when not authenticated from the
                    // authorization endpoint
                    .exceptionHandling((exceptions) -> exceptions
                            .authenticationEntryPoint(
                                    new LoginUrlAuthenticationEntryPoint("/login"))
                    );

            SecurityConfigurers.applySecurityConfigurersWithQualifier(http, SECURITY_CONFIGURER_QUALIFIER);
            return http.build();
        }

        @Bean("authsr_LoginFormSecurityFilterChain")
        @Order(JmixOrder.HIGHEST_PRECEDENCE + 110)
        public SecurityFilterChain loginFormSecurityFilterChain(HttpSecurity http)
                throws Exception {
            http
                    .mvcMatcher("/login")
                    .authorizeRequests(authorize -> {
                        authorize.anyRequest().permitAll();
                    })
                    .formLogin();

            SecurityConfigurers.applySecurityConfigurersWithQualifier(http, LOGIN_FORM_SECURITY_CONFIGURER_QUALIFIER);
            return http.build();
        }

        @Bean
        public DefaultRegisteredClientProvider defaultRegisteredClientProvider(DefaultRegisteredClientProperties defaultClientProperties) {
            return new DefaultRegisteredClientProvider(defaultClientProperties);
        }

        @Bean
        @ConditionalOnMissingBean
        public RegisteredClientRepository registeredClientRepository(Collection<RegisteredClientProvider> clientProviders) {
            List<RegisteredClient> clients = clientProviders.stream()
                    .flatMap(provider -> provider.getRegisteredClients().stream())
                    .collect(Collectors.toList());
            return new InMemoryRegisteredClientRepository(clients);
        }

        @Bean
        public OAuth2AuthorizationService oAuth2AuthorizationService() {
            return new InMemoryOAuth2AuthorizationService();
        }

        @Bean
        public ProviderSettings providerSettings() {
            return ProviderSettings.builder().build();
        }
    }

    @Configuration(proxyBeanMethods = false)
    public static class ResourceServerSecurityConfiguration {

        public static final String SECURITY_CONFIGURER_QUALIFIER = "authorization-server-resource-server";

        @Bean("authsr_ResourceServerSecurityFilterChain")
        @Order(JmixOrder.HIGHEST_PRECEDENCE + 150)
        public SecurityFilterChain resourceServerSecurityFilterChain(HttpSecurity http,
                                                                     OpaqueTokenIntrospector opaqueTokenIntrospector) throws Exception {
            http.apply(SecurityConfigurers.apiSecurity());
            http
                    .authorizeRequests(authorize -> {
                        authorize.anyRequest().authenticated();
                    })
                    .oauth2ResourceServer(oauth2 -> oauth2
                            .opaqueToken(opaqueToken -> opaqueToken
                                    .introspector(opaqueTokenIntrospector)));
            SecurityConfigurers.applySecurityConfigurersWithQualifier(http, SECURITY_CONFIGURER_QUALIFIER);
            return http.build();
        }

        @ConditionalOnMissingBean
        @Bean("authsr_OpaqueTokenIntrospector")
        public OpaqueTokenIntrospector opaqueTokenIntrospector(OAuth2AuthorizationService authorizationService,
                                                               UserDetailsService userDetailsService) {
            return new AuthorizationServiceOpaqueTokenIntrospector(authorizationService, userDetailsService);
        }
    }
}
