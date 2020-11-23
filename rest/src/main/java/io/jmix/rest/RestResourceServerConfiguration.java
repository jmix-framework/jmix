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

import io.jmix.core.CoreProperties;
import io.jmix.core.JmixModules;
import io.jmix.core.annotation.Internal;
import io.jmix.core.security.UserRepository;
import io.jmix.core.session.SessionProperties;
import io.jmix.rest.security.filter.RestLastSecurityFilter;
import io.jmix.rest.api.common.RestAuthUtils;
import io.jmix.rest.api.common.RestTokenMasker;
import io.jmix.rest.security.filter.RestExceptionLoggingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.ClientDetailsUserDetailsService;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Internal
@Configuration
@EnableResourceServer
public class RestResourceServerConfiguration extends ResourceServerConfigurerAdapter {

    @Autowired
    @Qualifier("rest_tokenStore")
    protected TokenStore tokenStore;

    @Autowired
    protected ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    protected RestTokenMasker restTokenMasker;

    @Autowired
    private CoreProperties coreProperties;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected JmixModules jmixModules;

    @Autowired
    protected RestProperties restProperties;

    @Autowired
    protected SessionRegistry sessionRegistry;

    @Autowired
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy;

    @Autowired
    protected SessionProperties sessionProperties;

    @Autowired
    protected ClientDetailsService clientDetailsService;

    @Autowired
    protected RestAuthUtils restAuthUtils;

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.tokenStore(tokenStore);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        List<String> authenticatedUrlPatternsProperties = jmixModules.getPropertyValues("jmix.rest.authenticatedUrlPatterns");
        String[] authenticatedUrlPatterns = authenticatedUrlPatternsProperties.stream()
                .flatMap(s -> Arrays.stream(s.split(",")))
                .toArray(String[]::new);

        List<String> anonymousUrlPatternsProperties = jmixModules.getPropertyValues("jmix.rest.anonymousUrlPatterns");
        String[] anonymousUrlPatterns = anonymousUrlPatternsProperties.stream()
                .flatMap(s -> Arrays.stream(s.split(",")))
                .toArray(String[]::new);

        RestLastSecurityFilter jmixRestLastSecurityFilter = new RestLastSecurityFilter(
                applicationEventPublisher, restTokenMasker, restAuthUtils);
        RestExceptionLoggingFilter jmixRestExceptionLoggingFilter = new RestExceptionLoggingFilter();

        String[] requestMatcherAntPatterns = Stream.of(anonymousUrlPatterns, authenticatedUrlPatterns)
                .flatMap(Stream::of)
                .toArray(String[]::new);

        http.requestMatchers()
                .antMatchers(requestMatcherAntPatterns)
                .and()
                .csrf().disable()
                .sessionManagement().sessionAuthenticationStrategy(sessionAuthenticationStrategy)
                .maximumSessions(sessionProperties.getMaximumSessionsPerUser()).sessionRegistry(sessionRegistry)
                .and().and()
                .anonymous(anonymousConfigurer -> {
                    anonymousConfigurer.key(coreProperties.getAnonymousAuthenticationTokenKey());
                    anonymousConfigurer.principal(userRepository.getAnonymousUser());
                    Collection<? extends GrantedAuthority> anonymousAuthorities = userRepository.getAnonymousUser().getAuthorities();
                    if (!anonymousAuthorities.isEmpty()) {
                        anonymousConfigurer.authorities(new ArrayList<>(userRepository.getAnonymousUser().getAuthorities()));
                    }
                })
                .authorizeRequests()
                .antMatchers(anonymousUrlPatterns).permitAll()
                .antMatchers(authenticatedUrlPatterns).authenticated()
                .and()
                .addFilterBefore(jmixRestExceptionLoggingFilter, WebAsyncManagerIntegrationFilter.class)
                .addFilterAfter(jmixRestLastSecurityFilter, FilterSecurityInterceptor.class);

        http.requestMatchers()
                .antMatchers("/rest/oauth/revoke")
                .and()
                .httpBasic()
                .authenticationEntryPoint(new OAuth2AuthenticationEntryPoint())
                .and()
                .authorizeRequests()
                .antMatchers("/rest/oauth/revoke").authenticated()
                .and()
                .authenticationProvider(clientDetailsAuthenticationProvider());
    }

    protected AuthenticationProvider clientDetailsAuthenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(new ClientDetailsUserDetailsService(clientDetailsService));
        return authenticationProvider;
    }
}
