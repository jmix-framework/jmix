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

package io.jmix.autoconfigure.authserver;

import io.jmix.authserver.AuthorizationServerConfiguration;
import io.jmix.authserver.DefaultRegisteredClientProperties;
import io.jmix.authserver.client.RegisteredClientProvider;
import io.jmix.authserver.client.impl.DefaultRegisteredClientProvider;
import io.jmix.authserver.introspection.AuthorizationServiceOpaqueTokenIntrospector;
import io.jmix.core.JmixSecurityFilterChainOrder;
import io.jmix.security.SecurityConfigurers;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@AutoConfiguration
@Import({AuthorizationServerConfiguration.class})
@ConditionalOnProperty(name = "jmix.authorization-server.use-default-configuration", matchIfMissing = true)
public class AuthorizationServerAutoConfiguration {

    @Configuration
    @EnableWebMvc
    public class AuthorizationServerLoginPageConfiguration implements WebMvcConfigurer {

        @Override
        public void addViewControllers(ViewControllerRegistry registry) {
            registry.addViewController("/as-login/**").setViewName("as-login.html");
        }
    }

    @Configuration(proxyBeanMethods = false)
    public static class AuthorizationServerSecurityConfiguration {

        public static final String SECURITY_CONFIGURER_QUALIFIER = "authorization-server";
        public static final String LOGIN_FORM_SECURITY_CONFIGURER_QUALIFIER = "authorization-server-login-form";

        @Bean("authsr_AuthorizationServerSecurityFilterChain")
        @Order(JmixSecurityFilterChainOrder.AUTHSERVER_AUTHORIZATION_SERVER)
        public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
                throws Exception {
            OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
            http
                    // Redirect to the login page when not authenticated from the
                    // authorization endpoint
                    .exceptionHandling((exceptions) -> exceptions
                            .authenticationEntryPoint(
                                    new LoginUrlAuthenticationEntryPoint("/as-login"))
                    );

            SecurityConfigurers.applySecurityConfigurersWithQualifier(http, SECURITY_CONFIGURER_QUALIFIER);
            return http.build();
        }

        @Bean("authsr_LoginFormSecurityFilterChain")
        @Order(JmixSecurityFilterChainOrder.AUTHSERVER_LOGIN_FORM)
        public SecurityFilterChain loginFormSecurityFilterChain(HttpSecurity http)
                throws Exception {
            http
                    .securityMatcher("/as-login")
                    .authorizeHttpRequests(authorize -> {
                        authorize.anyRequest().permitAll();
                    })
                    .formLogin(form -> {
                        form.loginPage("/as-login");
                    });

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
        public AuthorizationServerSettings authorizationServerSettings() {
            return AuthorizationServerSettings.builder().build();
        }
    }

    @Configuration(proxyBeanMethods = false)
    public static class ResourceServerSecurityConfiguration {

        public static final String SECURITY_CONFIGURER_QUALIFIER = "authorization-server-resource-server";

        @Bean("authsr_ResourceServerSecurityFilterChain")
        @Order(JmixSecurityFilterChainOrder.AUTHSERVER_RESOURCE_SERVER)
        public SecurityFilterChain resourceServerSecurityFilterChain(HttpSecurity http,
                                                                     OpaqueTokenIntrospector opaqueTokenIntrospector) throws Exception {
            http.apply(SecurityConfigurers.apiSecurity());
            http
                    .authorizeHttpRequests(authorize -> {
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
