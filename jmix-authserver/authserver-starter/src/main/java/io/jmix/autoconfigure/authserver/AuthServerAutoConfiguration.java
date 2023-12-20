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

import io.jmix.authserver.AuthServerConfiguration;
import io.jmix.authserver.AuthServerProperties;
import io.jmix.authserver.filter.AsResourceServerEventSecurityFilter;
import io.jmix.authserver.introspection.AuthorizationServiceOpaqueTokenIntrospector;
import io.jmix.authserver.introspection.TokenIntrospectorRolesHelper;
import io.jmix.authserver.roleassignment.InMemoryRegisteredClientRoleAssignmentRepository;
import io.jmix.authserver.roleassignment.RegisteredClientRoleAssignment;
import io.jmix.authserver.roleassignment.RegisteredClientRoleAssignmentPropertiesMapper;
import io.jmix.authserver.roleassignment.RegisteredClientRoleAssignmentRepository;
import io.jmix.core.JmixSecurityFilterChainOrder;
import io.jmix.security.SecurityConfigurers;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collection;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@AutoConfiguration
@Import({AuthServerConfiguration.class})
@ConditionalOnProperty(name = "jmix.authserver.use-default-configuration", matchIfMissing = true)
public class AuthServerAutoConfiguration {

    @Configuration
    @Order(AuthorizationServerLoginPageConfiguration.ORDER)
    public static class AuthorizationServerLoginPageConfiguration implements WebMvcConfigurer {

        private final AuthServerProperties authServerProperties;

        public AuthorizationServerLoginPageConfiguration(AuthServerProperties authServerProperties) {
            this.authServerProperties = authServerProperties;
        }

        public static final int ORDER = 100;

        @Override
        public void addViewControllers(ViewControllerRegistry registry) {
            registry.addViewController(authServerProperties.getLoginPageUrl())
                    .setViewName(authServerProperties.getLoginPageViewName());
        }
    }

    @Configuration(proxyBeanMethods = false)
    public static class AuthorizationServerSecurityConfiguration {

        public static final String SECURITY_CONFIGURER_QUALIFIER = "authorization-server";
        public static final String LOGIN_FORM_SECURITY_CONFIGURER_QUALIFIER = "authorization-server-login-form";

        private final AuthServerProperties authServerProperties;

        public AuthorizationServerSecurityConfiguration(AuthServerProperties authServerProperties) {
            this.authServerProperties = authServerProperties;
        }

        /**
         * Enables CORS for pre-flight requests to OAuth2 endpoints
         */
        @Bean("authsr_AuthorizationServerCorsSecurityFilterChain")
        @Order(JmixSecurityFilterChainOrder.AUTHSERVER_AUTHORIZATION_SERVER + 5)
        public SecurityFilterChain authorizationServerCorsSecurityFilterChain(HttpSecurity http)
                throws Exception {
            http.securityMatcher(antMatcher(HttpMethod.OPTIONS, "/oauth2/**"));
            http.cors(Customizer.withDefaults());
            return http.build();
        }

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
                                    new LoginUrlAuthenticationEntryPoint(authServerProperties.getLoginPageUrl()))
                    )
                    .cors(Customizer.withDefaults());
            SecurityConfigurers.applySecurityConfigurersWithQualifier(http, SECURITY_CONFIGURER_QUALIFIER);
            return http.build();
        }

        @Bean("authsr_LoginFormSecurityFilterChain")
        @Order(JmixSecurityFilterChainOrder.AUTHSERVER_LOGIN_FORM)
        public SecurityFilterChain loginFormSecurityFilterChain(HttpSecurity http)
                throws Exception {
            http
                    .securityMatcher(authServerProperties.getLoginPageUrl(), "/aslogin/styles/**")
                    .authorizeHttpRequests(authorize -> {
                        authorize.anyRequest().permitAll();
                    })
                    .formLogin(form -> {
                        form.loginPage(authServerProperties.getLoginPageUrl());
                    });

            SecurityConfigurers.applySecurityConfigurersWithQualifier(http, LOGIN_FORM_SECURITY_CONFIGURER_QUALIFIER);
            return http.build();
        }

        @Bean
        @ConditionalOnMissingBean
        public OAuth2AuthorizationService oAuth2AuthorizationService() {
            return new InMemoryOAuth2AuthorizationService();
        }

        @Bean
        public AuthorizationServerSettings authorizationServerSettings() {
            return AuthorizationServerSettings.builder().build();
        }

        @Bean
        @ConditionalOnMissingBean(RegisteredClientRoleAssignmentRepository.class)
        public InMemoryRegisteredClientRoleAssignmentRepository inMemoryRegisteredClientRoleAssignmentRepository() {
            Collection<RegisteredClientRoleAssignment> registeredClientRoleAssignments =
                    new RegisteredClientRoleAssignmentPropertiesMapper(authServerProperties).asRegisteredClientRoleAssignments();
            return new InMemoryRegisteredClientRoleAssignmentRepository(registeredClientRoleAssignments);
        }
    }

    @Configuration(proxyBeanMethods = false)
    public static class ResourceServerSecurityConfiguration {

        public static final String SECURITY_CONFIGURER_QUALIFIER = "authorization-server-resource-server";

        @Bean("authsr_ResourceServerSecurityFilterChain")
        @Order(JmixSecurityFilterChainOrder.AUTHSERVER_RESOURCE_SERVER)
        public SecurityFilterChain resourceServerSecurityFilterChain(HttpSecurity http,
                                                                     OpaqueTokenIntrospector opaqueTokenIntrospector,
                                                                     ApplicationEventPublisher applicationEventPublisher) throws Exception {
            http.apply(SecurityConfigurers.apiSecurity());
            http
                    .oauth2ResourceServer(oauth2 -> oauth2
                            .opaqueToken(opaqueToken -> opaqueToken
                                    .introspector(opaqueTokenIntrospector)))
                    .cors(Customizer.withDefaults());
            AsResourceServerEventSecurityFilter asResourceServerEventSecurityFilter = new AsResourceServerEventSecurityFilter(applicationEventPublisher);
            http.addFilterBefore(asResourceServerEventSecurityFilter, AuthorizationFilter.class);
            SecurityConfigurers.applySecurityConfigurersWithQualifier(http, SECURITY_CONFIGURER_QUALIFIER);
            return http.build();
        }

        @ConditionalOnMissingBean
        @Bean("authsr_OpaqueTokenIntrospector")
        public OpaqueTokenIntrospector opaqueTokenIntrospector(OAuth2AuthorizationService authorizationService,
                                                               TokenIntrospectorRolesHelper tokenIntrospectorRolesHelper) {
            return new AuthorizationServiceOpaqueTokenIntrospector(authorizationService, tokenIntrospectorRolesHelper);
        }
    }
}
