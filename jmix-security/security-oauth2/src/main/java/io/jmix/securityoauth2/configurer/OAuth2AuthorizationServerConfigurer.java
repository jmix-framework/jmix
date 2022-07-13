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

package io.jmix.securityoauth2.configurer;

import io.jmix.core.JmixOrder;
import io.jmix.securityoauth2.SecurityOAuth2Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.client.ClientDetailsUserDetailsService;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.List;

public class OAuth2AuthorizationServerConfigurer implements AuthorizationServerConfigurer {

    @Autowired
    private SecurityOAuth2Properties properties;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenEnhancer tokenEnhancer;

    @Autowired
    private ClientDetailsService clientDetailsService;

    @Autowired
    private AuthorizationServerTokenServices tokenServices;

    @Autowired
    private TokenGranter tokenGranter;

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients
                .inMemory()
                .withClient(properties.getClientId())
                .secret(properties.getClientSecret())
                .authorizedGrantTypes(properties.getClientAuthorizedGrantTypes())
                .accessTokenValiditySeconds(properties.getClientTokenExpirationTimeSec())
                .refreshTokenValiditySeconds(properties.getClientRefreshTokenExpirationTimeSec())
                .scopes("api");
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints.authenticationManager(authenticationManager)
                .tokenEnhancer(tokenEnhancer)
                .tokenServices(tokenServices)
                .tokenGranter(tokenGranter);
    }

    @Bean("sec_OAuthAuthorizationServerSecurityFilterChain")
    @Order(JmixOrder.HIGHEST_PRECEDENCE + 100)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.requestMatchers()
                .antMatchers("/oauth/revoke")
                .and()
                .csrf().disable()
                .httpBasic()
                .and()
                .authorizeRequests()
                .antMatchers("/oauth/revoke").authenticated()
                .and()
                .authenticationProvider(getAuthenticationProvider());
        return http.build();
    }

    private AuthenticationProvider getAuthenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(new ClientDetailsUserDetailsService(clientDetailsService));
        return authenticationProvider;
    }
}
