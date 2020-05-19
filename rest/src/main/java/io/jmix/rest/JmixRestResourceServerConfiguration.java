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
import io.jmix.core.Events;
import io.jmix.core.JmixModules;
import io.jmix.core.security.UserRepository;
import io.jmix.rest.api.auth.JmixRestLastSecurityFilter;
import io.jmix.rest.api.common.RestTokenMasker;
import io.jmix.rest.api.sys.JmixRestExceptionLoggingFilter;
import io.jmix.rest.property.RestProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Configuration
@EnableResourceServer
public class JmixRestResourceServerConfiguration extends ResourceServerConfigurerAdapter {

    @Autowired
    @Qualifier("jmix_tokenStore")
    protected TokenStore tokenStore;

    @Autowired
    protected Events events;

    @Autowired
    protected RestTokenMasker restTokenMasker;

    @Autowired
    private CoreProperties coreProperties;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    protected JmixModules jmixModules;

    @Autowired
    protected RestProperties restProperties;

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources
//                .tokenServices(tokenServices)
                .tokenStore(tokenStore);
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

        JmixRestLastSecurityFilter jmixRestLastSecurityFilter = new JmixRestLastSecurityFilter(events, restTokenMasker);
        JmixRestExceptionLoggingFilter jmixRestExceptionLoggingFilter = new JmixRestExceptionLoggingFilter();

        String[] requestMatcherAntPatterns = Stream.of(anonymousUrlPatterns, authenticatedUrlPatterns)
                .flatMap(Stream::of)
                .toArray(String[]::new);

        http
                .requestMatchers()
                .antMatchers(requestMatcherAntPatterns)
                .and()
                .csrf().disable()
                .anonymous(anonymousConfigurer -> {
                    anonymousConfigurer.key(coreProperties.getAnonymousAuthenticationTokenKey());
                    anonymousConfigurer.principal(userRepository.getAnonymousUser());
                })
                .authorizeRequests()
                .antMatchers(anonymousUrlPatterns).permitAll()
                .antMatchers(authenticatedUrlPatterns).authenticated()
                .and()
                .addFilterBefore(jmixRestExceptionLoggingFilter, WebAsyncManagerIntegrationFilter.class)
                .addFilterAfter(jmixRestLastSecurityFilter, FilterSecurityInterceptor.class);
    }
}
