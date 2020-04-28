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

package io.jmix.core.security;

import io.jmix.core.security.impl.AnonymousAuthenticationProvider;
import io.jmix.core.security.impl.CoreUserDetailsService;
import io.jmix.core.security.impl.SystemAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.inject.Inject;

@Configuration
@Conditional(OnCoreSecurityImplementation.class)
@EnableWebSecurity
@Order(100)
public class JmixCoreSecurityConfiguration extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {

    @Inject
    protected UserSessionCleanupInterceptor userSessionCleanupInterceptor;

//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        UserDetailsService userDetailsService = new CoreUserDetailsService();
//        auth.userDetailsService(userDetailsService);
//    }

    @Autowired
    protected AuthenticationManagerBuilder configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        UserDetailsService userDetailsService = new CoreUserDetailsService();
        auth.userDetailsService(userDetailsService);

        auth.authenticationProvider(new SystemAuthenticationProvider(userDetailsService));
        auth.authenticationProvider(new AnonymousAuthenticationProvider(userDetailsService));

        return auth;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.antMatcher("/**")
                .authorizeRequests().anyRequest().permitAll()
                .and()
                .csrf().disable();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userSessionCleanupInterceptor);
    }

    @Bean(name = "jmix_authenticationManager")
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean(name = "jmix_userDetailsService")
    @Override
    public UserDetailsService userDetailsServiceBean() throws Exception {
        return super.userDetailsServiceBean();
    }
}