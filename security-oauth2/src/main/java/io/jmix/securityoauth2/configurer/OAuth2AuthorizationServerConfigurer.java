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

import io.jmix.core.session.SessionData;
import io.jmix.securityoauth2.SecurityOAuth2Properties;
import io.jmix.securityoauth2.impl.RequestLocaleProvider;
import io.jmix.securityoauth2.impl.SessionTokenEnhancer;
import io.jmix.securityoauth2.impl.UserPasswordTokenGranter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.builders.ClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.CompositeTokenGranter;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.client.ClientDetailsUserDetailsService;
import org.springframework.security.oauth2.provider.refresh.RefreshTokenGranter;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenStore;

import java.util.ArrayList;
import java.util.List;

public class OAuth2AuthorizationServerConfigurer extends WebSecurityConfigurerAdapter
        implements AuthorizationServerConfigurer {
    private SecurityOAuth2Properties properties;
    private AuthenticationManager authenticationManager;
    private TokenStore tokenStore;
    private ObjectProvider<SessionData> sessionDataProvider;
    private RequestLocaleProvider localeProvider;

    private TokenEnhancer tokenEnhancer;
    private ClientDetailsService clientDetails;
    private AuthorizationServerTokenServices tokenServices;
    private OAuth2RequestFactory oauth2RequestFactory;
    private TokenGranter tokenGranter;

    @Autowired
    public void setProperties(SecurityOAuth2Properties properties) {
        this.properties = properties;
    }

    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Autowired
    public void setTokenStore(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @Autowired
    public void setLocaleProvider(RequestLocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    @Autowired
    public void setSessionDataProvider(ObjectProvider<SessionData> sessionDataProvider) {
        this.sessionDataProvider = sessionDataProvider;
    }

    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    public TokenStore getTokenStore() {
        return tokenStore;
    }

    public ObjectProvider<SessionData> getSessionDataProvider() {
        return sessionDataProvider;
    }

    public RequestLocaleProvider getLocaleProvider() {
        return localeProvider;
    }

    public TokenEnhancer getTokenEnhancer() {
        if (tokenEnhancer == null) {
            tokenEnhancer = getDefaultTokenEnhancer();
        }
        return tokenEnhancer;
    }

    public ClientDetailsService getClientDetails() {
        if (clientDetails == null) {
            clientDetails = getDefaultClientDetails();
        }
        return clientDetails;
    }

    public AuthorizationServerTokenServices getTokenServices() {
        if (tokenServices == null) {
            tokenServices = getDefaultTokenService();
        }
        return tokenServices;
    }

    public OAuth2RequestFactory getOAuth2RequestFactory() {
        if (oauth2RequestFactory == null) {
            oauth2RequestFactory = getDefaultOAuth2RequestFactory();
        }
        return oauth2RequestFactory;
    }

    public TokenGranter getTokenGranter() {
        if (tokenGranter == null) {
            tokenGranter = getDefaultTokenGranter();
        }
        return tokenGranter;
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.withClientDetails(getClientDetails());
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints.authenticationManager(getAuthenticationManager())
                .tokenEnhancer(getTokenEnhancer())
                .tokenServices(getTokenServices())
                .tokenGranter(getTokenGranter());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
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
    }

    private TokenEnhancer getDefaultTokenEnhancer() {
        return new SessionTokenEnhancer(getSessionDataProvider());
    }

    private ClientDetailsService getDefaultClientDetails() {
        try {
            return new ClientDetailsServiceBuilder<>().inMemory()
                    .withClient(properties.getClientId())
                    .secret(properties.getClientSecret())
                    .authorizedGrantTypes(properties.getClientAuthorizedGrantTypes())
                    .accessTokenValiditySeconds(properties.getClientTokenExpirationTimeSec())
                    .refreshTokenValiditySeconds(properties.getClientRefreshTokenExpirationTimeSec())
                    .scopes("api")
                    .and()
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Error on building ClientDetailsService", e);
        }
    }

    private AuthorizationServerTokenServices getDefaultTokenService() {
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(getTokenStore());
        defaultTokenServices.setSupportRefreshToken(properties.isSupportRefreshToken());
        defaultTokenServices.setReuseRefreshToken(properties.isReuseRefreshToken());
        defaultTokenServices.setTokenEnhancer(getTokenEnhancer());
        defaultTokenServices.setClientDetailsService(getClientDetails());
        return defaultTokenServices;
    }

    private OAuth2RequestFactory getDefaultOAuth2RequestFactory() {
        return new DefaultOAuth2RequestFactory(getClientDetails());
    }

    private TokenGranter getDefaultTokenGranter() {
        List<TokenGranter> tokenGranters = new ArrayList<>();

        tokenGranters.add(new RefreshTokenGranter(getTokenServices(), getClientDetails(), getOAuth2RequestFactory()));

        if (getAuthenticationManager() != null) {
            tokenGranters.add(new UserPasswordTokenGranter(getAuthenticationManager(), getTokenServices(), getClientDetails(),
                    getOAuth2RequestFactory(), getSessionDataProvider(), getLocaleProvider()));
        }
        return new CompositeTokenGranter(tokenGranters);
    }

    private AuthenticationProvider getAuthenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(new ClientDetailsUserDetailsService(getClientDetails()));
        return authenticationProvider;
    }
}
