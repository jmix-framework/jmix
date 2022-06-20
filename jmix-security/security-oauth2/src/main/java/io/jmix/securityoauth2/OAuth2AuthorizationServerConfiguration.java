/*
 * Copyright 2021 Haulmont.
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

package io.jmix.securityoauth2;

import io.jmix.core.CorsProperties;
import io.jmix.core.session.SessionData;
import io.jmix.securityoauth2.impl.RequestLocaleProvider;
import io.jmix.securityoauth2.impl.SessionTokenEnhancer;
import io.jmix.securityoauth2.impl.UserPasswordTokenGranter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.CompositeTokenGranter;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.refresh.RefreshTokenGranter;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Declares bean definitions used by OAuth authorization services
 */
@Configuration
public class OAuth2AuthorizationServerConfiguration {


    @Autowired
    private ObjectProvider<SessionData> sessionDataProvider;

    @Autowired
    private SecurityOAuth2Properties properties;

    @Autowired
    private TokenStore tokenStore;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RequestLocaleProvider localeProvider;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ClientDetailsService clientDetailsService;

    @Bean
    public OAuth2RequestFactory oAuth2RequestFactory() {
        return new DefaultOAuth2RequestFactory(clientDetailsService);
    }

    @Bean
    public TokenGranter tokenGranter() {
        List<TokenGranter> tokenGranters = new ArrayList<>();

        tokenGranters.add(new RefreshTokenGranter(tokenServices(), clientDetailsService, oAuth2RequestFactory()));

        if (authenticationManager != null) {
            tokenGranters.add(new UserPasswordTokenGranter(authenticationManager, tokenServices(), clientDetailsService,
                    oAuth2RequestFactory(), sessionDataProvider, localeProvider, eventPublisher));
        }
        return new CompositeTokenGranter(tokenGranters);
    }

    @Bean
    public TokenEnhancer tokenEnhancer() {
        return new SessionTokenEnhancer(sessionDataProvider);
    }

    @Bean
    public AuthorizationServerTokenServices tokenServices() {
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(tokenStore);
        defaultTokenServices.setSupportRefreshToken(properties.isSupportRefreshToken());
        defaultTokenServices.setReuseRefreshToken(properties.isReuseRefreshToken());
        defaultTokenServices.setTokenEnhancer(tokenEnhancer());
        defaultTokenServices.setClientDetailsService(clientDetailsService);
        return defaultTokenServices;
    }

    /**
     * A CORS filter for /oauth/** endpoints. It is required because we cannot add CORS filter for that endpoint using
     * standard Spring Security approach. CORS settings for other endpoints are configured by the CorsConfiguration
     * provided by the core module auto-configuration.
     */
    @Bean("sec_OAuth2CorsFilterRegistrationBean")
    public FilterRegistrationBean<CorsFilter> oauthCorsFilter(CorsProperties corsProperties) {
        List<String> allowedOrigins = corsProperties.getAllowedOrigins();

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedHeaders(corsProperties.getAllowedHeaders());
        config.setAllowedMethods(corsProperties.getAllowedMethods());
        if (!allowedOrigins.contains(CorsConfiguration.ALL)) {
            config.setAllowCredentials(true);
        }
        source.registerCorsConfiguration("/oauth/**", config);

        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setUrlPatterns(Arrays.asList("/oauth/*"));

        //The filter must be loaded before OAuth2 security filters
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 100);
        return bean;
    }
}
