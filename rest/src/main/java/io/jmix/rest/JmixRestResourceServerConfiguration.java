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

import io.jmix.core.Events;
import io.jmix.core.security.UserSessions;
import io.jmix.rest.api.auth.ClientProxyTokenStore;
import io.jmix.rest.api.auth.JmixAnonymousAuthenticationFilter;
import io.jmix.rest.api.auth.JmixRestLastSecurityFilter;
import io.jmix.rest.api.common.RestParseUtils;
import io.jmix.rest.api.common.RestTokenMasker;
import io.jmix.rest.api.config.RestQueriesConfiguration;
import io.jmix.rest.api.config.RestServicesConfiguration;
import io.jmix.rest.api.sys.JmixRestExceptionLoggingFilter;
import io.jmix.rest.property.RestProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter;

import javax.annotation.Resource;

@Configuration
@EnableResourceServer
public class JmixRestResourceServerConfiguration extends ResourceServerConfigurerAdapter {

    @Resource(name = "jmix_tokenStore")
    protected ClientProxyTokenStore tokenStore;

    @Autowired
    protected UserSessions userSessions;

    @Autowired
    protected Events events;

    @Autowired
    protected RestTokenMasker restTokenMasker;

    @Autowired
    protected RestProperties restProperties;

    @Autowired
    protected RestServicesConfiguration restServicesConfiguration;

    @Autowired
    protected RestQueriesConfiguration restQueriesConfiguration;

    @Autowired
    protected AuthenticationManager authenticationManager;

    @Autowired
    protected RestParseUtils restParseUtils;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        JmixRestAuthorizationFilter jmixRestAuthorizationFilter = new JmixRestAuthorizationFilter(tokenStore, userSessions);

        JmixRestExceptionLoggingFilter jmixRestExceptionLoggingFilter = new JmixRestExceptionLoggingFilter();

        JmixRestLastSecurityFilter jmixRestLastSecurityFilter = new JmixRestLastSecurityFilter(events, restTokenMasker);

        JmixAnonymousAuthenticationFilter jmixAnonymousAuthenticationFilter = new JmixAnonymousAuthenticationFilter(
                restProperties,
                restServicesConfiguration,
                restQueriesConfiguration,
                authenticationManager,
                restParseUtils);

        http
                .anonymous().disable()
                .authorizeRequests()
                .antMatchers("/**").authenticated()
                .and()
                .addFilterBefore(jmixRestExceptionLoggingFilter, WebAsyncManagerIntegrationFilter.class)
                .addFilterBefore(jmixAnonymousAuthenticationFilter, BasicAuthenticationFilter.class)
                .addFilterAfter(jmixRestAuthorizationFilter, BasicAuthenticationFilter.class)
                .addFilterAfter(jmixRestLastSecurityFilter, FilterSecurityInterceptor.class);
    }
}
