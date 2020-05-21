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

package io.jmix.security;

import io.jmix.core.CoreProperties;
import io.jmix.core.security.UserAuthenticationProvider;
import io.jmix.core.security.UserRepository;
import io.jmix.core.security.impl.SystemAuthenticationProvider;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.beans.factory.annotation.Autowired;

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@Conditional(OnStandardSecurityImplementation.class)
@EnableWebSecurity
public class StandardSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CoreProperties coreProperties;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(new SystemAuthenticationProvider(userRepository));

        UserAuthenticationProvider userAuthenticationProvider = new UserAuthenticationProvider();
        userAuthenticationProvider.setUserDetailsService(userRepository);
        userAuthenticationProvider.setPasswordEncoder(getPasswordEncoder());
        auth.authenticationProvider(userAuthenticationProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.antMatcher("/**")
                .authorizeRequests().anyRequest().permitAll()
                .and()
                .anonymous(anonymousConfigurer -> {
                    anonymousConfigurer.key(coreProperties.getAnonymousAuthenticationTokenKey());
                    anonymousConfigurer.principal(userRepository.getAnonymousUser());
                })
                .csrf().disable();
    }

    @Bean(name = "jmix_authenticationManager")
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    //todo MG
//    @Bean(name = "jmix_userDetailsService")
//    @Override
//    public UserDetailsService userDetailsServiceBean() throws Exception {
//        return super.userDetailsServiceBean();
//    }

    @Bean(name = "jmix_PasswordEncoder")
    public PasswordEncoder getPasswordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
