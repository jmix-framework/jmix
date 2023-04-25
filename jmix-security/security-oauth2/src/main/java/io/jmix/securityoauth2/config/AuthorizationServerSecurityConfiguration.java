/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jmix.securityoauth2.config;

import io.jmix.core.JmixOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.configuration.ClientDetailsServiceConfiguration;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpointHandlerMapping;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;

import java.util.Collections;
import java.util.List;

/**
 * A copy of
 * {@link
 * org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerSecurityConfiguration}
 * that doesn't extend {@code WebSecurityConfigurerAdapter}.
 */
@Configuration
@Import({ClientDetailsServiceConfiguration.class, AuthorizationServerEndpointsConfiguration.class})
public class AuthorizationServerSecurityConfiguration {

    @Autowired
    private List<AuthorizationServerConfigurer> configurers = Collections.emptyList();

    @Autowired
    private ClientDetailsService clientDetailsService;

    @Autowired
    private AuthorizationServerEndpointsConfiguration endpoints;

    @Autowired
    public void configure(ClientDetailsServiceConfigurer clientDetails) throws Exception {
        for (AuthorizationServerConfigurer configurer : configurers) {
            configurer.configure(clientDetails);
        }
    }

    @Bean("sec_AuthorizationServerSecurityFilterChain")
    @Order(JmixOrder.HIGHEST_PRECEDENCE + 50)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        AuthorizationServerSecurityConfigurer configurer = new AuthorizationServerSecurityConfigurer();
        FrameworkEndpointHandlerMapping handlerMapping = endpoints.oauth2EndpointHandlerMapping();
        http.setSharedObject(FrameworkEndpointHandlerMapping.class, handlerMapping);
        configure(configurer);
        http.apply(configurer);
        String tokenEndpointPath = handlerMapping.getServletPath("/oauth/token");
        String tokenKeyPath = handlerMapping.getServletPath("/oauth/token_key");
        String checkTokenPath = handlerMapping.getServletPath("/oauth/check_token");
        if (!endpoints.getEndpointsConfigurer().isUserDetailsServiceOverride()) {
            UserDetailsService userDetailsService = http.getSharedObject(UserDetailsService.class);
            endpoints.getEndpointsConfigurer().userDetailsService(userDetailsService);
        }
        //todo SB3 check token endpoints
        // @formatter:off
        http
                .securityMatcher(tokenEndpointPath, tokenKeyPath, checkTokenPath)
                .authorizeHttpRequests(authorize -> {
                    authorize.requestMatchers(tokenEndpointPath).fullyAuthenticated();
                    authorize.requestMatchers(tokenKeyPath).access(new WebExpressionAuthorizationManager(configurer.getTokenKeyAccess()));
                    authorize.requestMatchers(checkTokenPath).access(new WebExpressionAuthorizationManager(configurer.getCheckTokenAccess()));
                })
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER);
        // @formatter:on
        http.setSharedObject(ClientDetailsService.class, clientDetailsService);

        return http.build();
    }

    protected void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
        for (AuthorizationServerConfigurer configurer : configurers) {
            configurer.configure(oauthServer);
        }
    }
}
