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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jmix.authserver.AuthServerConfiguration;
import io.jmix.authserver.AuthServerProperties;
import io.jmix.authserver.authentication.OAuth2ResourceOwnerPasswordTokenEndpointConfigurer;
import io.jmix.authserver.filter.AsResourceServerEventSecurityFilter;
import io.jmix.authserver.introspection.AuthorizationServiceOpaqueTokenIntrospector;
import io.jmix.authserver.introspection.TokenIntrospectorRolesHelper;
import io.jmix.authserver.principal.AuthServerAuthenticationPrincipalResolver;
import io.jmix.authserver.roleassignment.InMemoryRegisteredClientRoleAssignmentRepository;
import io.jmix.authserver.roleassignment.RegisteredClientRoleAssignment;
import io.jmix.authserver.roleassignment.RegisteredClientRoleAssignmentPropertiesMapper;
import io.jmix.authserver.roleassignment.RegisteredClientRoleAssignmentRepository;
import io.jmix.authserver.service.OracleJdbcOAuth2AuthorizationService;
import io.jmix.authserver.service.cleanup.OAuth2ExpiredTokenCleaner;
import io.jmix.authserver.service.cleanup.impl.InMemoryOAuth2ExpiredTokenCleaner;
import io.jmix.authserver.service.cleanup.impl.JdbcOAuth2ExpiredTokenCleaner;
import io.jmix.authserver.service.mapper.JdbcOAuth2AuthorizationServiceObjectMapperCustomizer;
import io.jmix.core.JmixSecurityFilterChainOrder;
import io.jmix.data.persistence.DbmsType;
import io.jmix.security.SecurityConfigurers;
import io.jmix.security.util.JmixHttpSecurityUtils;
import io.jmix.securityresourceserver.requestmatcher.CompositeResourceServerRequestMatcherProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatchers;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collection;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@AutoConfiguration
@Import({AuthServerConfiguration.class})
@ConditionalOnProperty(name = "jmix.authserver.use-default-configuration", matchIfMissing = true)
public class AuthServerAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AuthServerAutoConfiguration.class);

    @Configuration(proxyBeanMethods = false)
    @Order(LoginPageConfiguration.ORDER)
    @ConditionalOnProperty(name = "jmix.authserver.use-default-login-page-configuration", matchIfMissing = true)
    public static class LoginPageConfiguration implements WebMvcConfigurer {

        public static final String SECURITY_CONFIGURER_QUALIFIER = "authorization-server-login-form";

        private final AuthServerProperties authServerProperties;

        public LoginPageConfiguration(AuthServerProperties authServerProperties) {
            this.authServerProperties = authServerProperties;
        }

        public static final int ORDER = 100;

        @Override
        public void addViewControllers(ViewControllerRegistry registry) {
            registry.addViewController(authServerProperties.getLoginPageUrl())
                    .setViewName(authServerProperties.getLoginPageViewName());
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

            SecurityConfigurers.applySecurityConfigurersWithQualifier(http, SECURITY_CONFIGURER_QUALIFIER);
            return http.build();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = "jmix.authserver.use-default-authorization-server-configuration", matchIfMissing = true)
    public static class AuthorizationServerConfiguration {

        public static final String SECURITY_CONFIGURER_QUALIFIER = "authorization-server";
        private final AuthServerProperties authServerProperties;

        public AuthorizationServerConfiguration(AuthServerProperties authServerProperties) {
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
            http.with(new OAuth2ResourceOwnerPasswordTokenEndpointConfigurer(), Customizer.withDefaults());
            SecurityConfigurers.applySecurityConfigurersWithQualifier(http, SECURITY_CONFIGURER_QUALIFIER);
            return http.build();
        }

        @Bean("authsr_AuthorizationServerLogoutSecurityFilterChain")
        @Order(JmixSecurityFilterChainOrder.AUTHSERVER_AUTHORIZATION_SERVER + 5)
        public SecurityFilterChain logoutSecurityFilterChain(HttpSecurity http, ServerProperties serverProperties) throws Exception {
            String sessionCookieName = getSessionCookieName(serverProperties);

            http.securityMatcher("/logout")
                    .csrf(AbstractHttpConfigurer::disable)
                    .cors(Customizer.withDefaults())
                    .authorizeHttpRequests(authorize -> authorize
                            .requestMatchers("/logout").authenticated()
                    ).logout(logout -> logout
                            .logoutUrl("/logout")
                            .logoutSuccessHandler(createLogoutSuccessHandler())
                            .invalidateHttpSession(true)
                            .clearAuthentication(true)
                            .deleteCookies(sessionCookieName)
                            .logoutRequestMatcher(createLogoutRequestMatcher("/logout"))
                    );
            return http.build();
        }

        protected LogoutSuccessHandler createLogoutSuccessHandler() {
            SimpleUrlLogoutSuccessHandler successHandler = new SimpleUrlLogoutSuccessHandler();
            if (StringUtils.isNotBlank(authServerProperties.getPostLogoutUrlRedirectParameterName())) {
                successHandler.setTargetUrlParameter(authServerProperties.getPostLogoutUrlRedirectParameterName());
            }
            successHandler.setUseReferer(authServerProperties.isUseRefererPostLogout());
            return successHandler;
        }

        protected String getSessionCookieName(ServerProperties serverProperties) {
            String sessionCookieName = serverProperties.getServlet().getSession().getCookie().getName();
            if (StringUtils.isBlank(sessionCookieName)) {
                sessionCookieName = "JSESSIONID";
            }
            return sessionCookieName;
        }

        protected RequestMatcher createLogoutRequestMatcher(String logoutUrl) {
            return RequestMatchers.anyOf(
                    new AntPathRequestMatcher(logoutUrl, "GET"),
                    new AntPathRequestMatcher(logoutUrl, "POST")
            );
        }

        @Bean
        @ConditionalOnMissingBean
        public OAuth2AuthorizationService oAuth2AuthorizationService(JdbcOperations jdbcOperations,
                                                                     RegisteredClientRepository registeredClientRepository,
                                                                     ObjectProvider<JdbcOAuth2AuthorizationServiceObjectMapperCustomizer> objectMapperCustomizers,
                                                                     DbmsType dbmsType) {
            if (authServerProperties.isUseInMemoryAuthorizationService()) {
                log.debug("Use {}", InMemoryOAuth2AuthorizationService.class);
                return new InMemoryOAuth2AuthorizationService();
            } else {
                JdbcOAuth2AuthorizationService authorizationService = createJdbcOAuth2AuthorizationService(
                        jdbcOperations, registeredClientRepository, dbmsType
                );
                log.debug("Use {}", authorizationService.getClass());

                ObjectMapper objectMapper = createObjectMapper(objectMapperCustomizers);

                JdbcOAuth2AuthorizationService.OAuth2AuthorizationRowMapper rowMapper =
                        new JdbcOAuth2AuthorizationService.OAuth2AuthorizationRowMapper(registeredClientRepository);
                rowMapper.setObjectMapper(objectMapper);
                authorizationService.setAuthorizationRowMapper(rowMapper);

                JdbcOAuth2AuthorizationService.OAuth2AuthorizationParametersMapper parametersMapper =
                        new JdbcOAuth2AuthorizationService.OAuth2AuthorizationParametersMapper();
                parametersMapper.setObjectMapper(objectMapper);
                authorizationService.setAuthorizationParametersMapper(parametersMapper);

                return authorizationService;
            }
        }

        @Bean("authsr_OAuth2ExpiredTokenCleaner")
        @ConditionalOnMissingBean
        public OAuth2ExpiredTokenCleaner OAuth2ExpiredTokenCleaner(JdbcOperations jdbcOperations) {
            if (authServerProperties.isUseInMemoryAuthorizationService()) {
                return new InMemoryOAuth2ExpiredTokenCleaner();
            } else {
                return new JdbcOAuth2ExpiredTokenCleaner(jdbcOperations);
            }
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

        @Bean("authsr_AuthServerAuthenticationPrincipalResolver")
        @Order(100)
        AuthServerAuthenticationPrincipalResolver authServerAuthenticationPrincipalResolver() {
            return new AuthServerAuthenticationPrincipalResolver();
        }

        protected JdbcOAuth2AuthorizationService createJdbcOAuth2AuthorizationService(JdbcOperations jdbcOperations,
                                                                                      RegisteredClientRepository registeredClientRepository,
                                                                                      DbmsType dbmsType) {
            if ("ORACLE".equals(dbmsType.getType())) {
                return new OracleJdbcOAuth2AuthorizationService(jdbcOperations, registeredClientRepository);
            } else {
                return new JdbcOAuth2AuthorizationService(jdbcOperations, registeredClientRepository);
            }
        }

        protected ObjectMapper createObjectMapper(ObjectProvider<JdbcOAuth2AuthorizationServiceObjectMapperCustomizer> objectMapperCustomizers) {
            ObjectMapper objectMapper = new ObjectMapper();
            ClassLoader classLoader = JdbcOAuth2AuthorizationService.class.getClassLoader();
            objectMapper.registerModules(SecurityJackson2Modules.getModules(classLoader));
            objectMapper.registerModule(new OAuth2AuthorizationServerJackson2Module());

            objectMapperCustomizers.orderedStream().forEach(customizer -> customizer.customize(objectMapper));

            return objectMapper;
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = "jmix.authserver.use-default-resource-server-configuration", matchIfMissing = true)
    public static class ResourceServerConfiguration {

        public static final String SECURITY_CONFIGURER_QUALIFIER = "authorization-server-resource-server";

        @Bean("authsr_ResourceServerSecurityFilterChain")
        @Order(JmixSecurityFilterChainOrder.AUTHSERVER_RESOURCE_SERVER)
        public SecurityFilterChain resourceServerSecurityFilterChain(HttpSecurity http,
                                                                     OpaqueTokenIntrospector opaqueTokenIntrospector,
                                                                     ApplicationEventPublisher applicationEventPublisher,
                                                                     CompositeResourceServerRequestMatcherProvider securityMatcherProvider) throws Exception {
            RequestMatcher authenticatedRequestMatcher = securityMatcherProvider.getAuthenticatedRequestMatcher();
            RequestMatcher anonymousRequestMatcher = securityMatcherProvider.getAnonymousRequestMatcher();
            RequestMatcher securityMatcher = new OrRequestMatcher(authenticatedRequestMatcher, anonymousRequestMatcher);
            http
                    .securityMatcher(securityMatcher)
                    .authorizeHttpRequests(authorize -> {
                        authorize
                                .requestMatchers(anonymousRequestMatcher).permitAll()
                                .requestMatchers(authenticatedRequestMatcher).authenticated();
                    })
                    .oauth2ResourceServer(oauth2 -> oauth2
                            .opaqueToken(opaqueToken -> opaqueToken
                                    .introspector(opaqueTokenIntrospector)))
                    .csrf(csrf -> csrf.disable())
                    .cors(Customizer.withDefaults());

            JmixHttpSecurityUtils.configureAnonymous(http);
            JmixHttpSecurityUtils.configureFrameOptions(http);
            JmixHttpSecurityUtils.configureSessionManagement(http);

            AsResourceServerEventSecurityFilter asResourceServerEventSecurityFilter = new AsResourceServerEventSecurityFilter(applicationEventPublisher);
            http.addFilterAfter(asResourceServerEventSecurityFilter, AuthorizationFilter.class);
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
