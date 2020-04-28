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

import io.jmix.core.security.UserSessionManager;
import io.jmix.rest.api.auth.ClientProxyTokenStore;
import io.jmix.rest.api.auth.ExternalOAuthTokenGranter;
import io.jmix.rest.api.auth.UniqueAuthenticationKeyGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;

@Configuration
@EnableAuthorizationServer
@PropertySource("classpath:/io/jmix/rest/application.properties")
public class JmixRestAuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

    private static final String REST_API = "rest-api";

    @Value("${jmix.rest.client.id}")
    protected String client;

    @Value("${jmix.rest.client.secret}")
    protected String secret;

    @Value("${jmix.rest.client.authorizedGrantTypes}")
    protected String[] authorizedGrantTypes;

    @Value("${jmix.rest.client.tokenExpirationTimeSec}")
    protected int tokenExpirationTimeSec;

    @Value("${jmix.rest.client.refreshTokenExpirationTimeSec}")
    protected int refreshTokenExpirationTimeSec;

    @Autowired
    protected AuthenticationManager authenticationManager;

    @Autowired
    protected UserSessionManager userSessionManager;

    @Bean
    protected UniqueAuthenticationKeyGenerator authenticationKeyGenerator() {
        return new UniqueAuthenticationKeyGenerator();
    }

    @Bean(name = "jmix_tokenStore")
    protected TokenStore tokenStore() {
        ClientProxyTokenStore clientProxyTokenStore = new ClientProxyTokenStore();
        clientProxyTokenStore.setAuthenticationKeyGenerator(authenticationKeyGenerator());
        return clientProxyTokenStore;
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients
                .inMemory()
                .withClient(client)
                .secret(secret)
                .authorizedGrantTypes(authorizedGrantTypes)
                .accessTokenValiditySeconds(tokenExpirationTimeSec)
                .refreshTokenValiditySeconds(refreshTokenExpirationTimeSec)
                .scopes(REST_API);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints
                .tokenGranter(new JmixRestTokenGranter(
                        userSessionManager,
                        authenticationManager,
                        endpoints.getTokenServices(),
                        endpoints.getClientDetailsService(),
                        endpoints.getOAuth2RequestFactory()))
                .pathMapping("/oauth/token", "/v2/oauth/token")
                .authenticationManager(authenticationManager)
                .tokenStore(tokenStore());
    }
}
