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

import io.jmix.core.CoreProperties;
import io.jmix.core.entity.BaseUser;
import io.jmix.core.security.impl.CoreUser;
import io.jmix.core.security.impl.InMemoryUserRepository;
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
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Conditional(OnCoreSecurityImplementation.class)
@EnableWebSecurity
@Order(100)
public class CoreSecurityConfiguration extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {

//    @Autowired
//    protected UserSessionCleanupInterceptor userSessionCleanupInterceptor;

    @Autowired
    private CoreProperties coreProperties;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(new SystemAuthenticationProvider(userRepository()));

        CoreAuthenticationProvider userAuthenticationProvider = new CoreAuthenticationProvider();
        userAuthenticationProvider.setUserDetailsService(userRepository());
        userAuthenticationProvider.setPasswordEncoder(getPasswordEncoder());
        auth.authenticationProvider(userAuthenticationProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.antMatcher("/**")
                .authorizeRequests().anyRequest().permitAll()
                .and()
                .anonymous(anonymousConfigurer -> {
                    BaseUser anonymousUser = userRepository().getAnonymousUser();
                    anonymousConfigurer.principal(anonymousUser);
                    anonymousConfigurer.key(coreProperties.getAnonymousAuthenticationTokenKey());
                })
                .csrf().disable()
                .headers().frameOptions().sameOrigin();
    }

    //todo MG why?
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(userSessionCleanupInterceptor);
//    }

    @Bean(name = "core_authenticationManager")
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean(UserRepository.NAME)
    public UserRepository userRepository() {
        CoreUser systemUser = new CoreUser("system", "{noop}", "System");
        CoreUser anonymousUser = new CoreUser("anonymous", "{noop}", "Anonymous");
        return new InMemoryUserRepository(systemUser, anonymousUser);
    }
//    @Bean(name = "core_userDetailsService")
//    @Override
//    public UserDetailsService userDetailsServiceBean() throws Exception {
//        return super.userDetailsServiceBean();
//    }

    @Bean(name = "core_PasswordEncoder")
    public PasswordEncoder getPasswordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}