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

package io.jmix.rest;

import io.jmix.rest.api.auth.UniqueAuthenticationKeyGenerator;
import io.jmix.rest.property.RestProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.builders.InMemoryClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

@Configuration
@EnableAuthorizationServer
public class JmixRestAuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

    private static final String REST_API = "rest-api";

    @Autowired
    protected RestProperties restProperties;

    @Autowired
    protected AuthenticationManager authenticationManager;

    @Bean
    protected UniqueAuthenticationKeyGenerator authenticationKeyGenerator() {
        return new UniqueAuthenticationKeyGenerator();
    }

    @Bean(name = "rest_tokenStore")
    protected TokenStore tokenStore() {
        //todo MG database token storage support
        InMemoryTokenStore tokenStore = new InMemoryTokenStore();
        tokenStore.setAuthenticationKeyGenerator(authenticationKeyGenerator());
        return tokenStore;
    }

    @Bean(name = "rest_tokenServices")
    public AuthorizationServerTokenServices tokenServices() {
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(tokenStore());
        defaultTokenServices.setSupportRefreshToken(restProperties.isSupportRefreshToken());
        defaultTokenServices.setReuseRefreshToken(restProperties.isReuseRefreshToken());
//        defaultTokenServices.setTokenEnhancer(accessTokenConverter());
        return defaultTokenServices;
    }

    @Bean("rest_clientDetailsService")
    public ClientDetailsService clientDetailsService() {
        InMemoryClientDetailsServiceBuilder builder = new InMemoryClientDetailsServiceBuilder();
        builder
                .withClient(restProperties.getClientId())
                .secret(restProperties.getClientSecret())
                .authorizedGrantTypes(restProperties.getClientAuthorizedGrantTypes())
                .accessTokenValiditySeconds(restProperties.getClientRefreshTokenExpirationTimeSec())
                .refreshTokenValiditySeconds(restProperties.getClientRefreshTokenExpirationTimeSec())
                .scopes(REST_API);
        try {
            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException("Error on building ClientDetailsService", e);
        }
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.withClientDetails(clientDetailsService());
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints
//                .tokenGranter(new JmixRestTokenGranter(
//                        authenticationManager,
//                        endpoints.getTokenServices(),
//                        endpoints.getClientDetailsService(),
//                        endpoints.getOAuth2RequestFactory()))
                .pathMapping("/oauth/token", "/rest/oauth/token")
                .authenticationManager(authenticationManager)
                .tokenServices(tokenServices())
                .tokenStore(tokenStore());
    }
}
