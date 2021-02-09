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
import io.jmix.core.rememberme.JmixRememberMeServices;
import io.jmix.core.rememberme.RememberMeProperties;
import io.jmix.core.security.UserRepository;
import io.jmix.core.security.impl.JmixSessionAuthenticationStrategy;
import io.jmix.core.security.impl.SystemAuthenticationProvider;
import io.jmix.core.session.SessionProperties;
import io.jmix.security.constraint.SecurityConstraintsRegistration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices.DEFAULT_PARAMETER;

@EnableWebSecurity
public class StandardSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private CoreProperties coreProperties;

    @Autowired
    private SessionProperties sessionProperties;

    @Autowired
    private RememberMeProperties rememberMeProperties;

    @Autowired
    private UserRepository userRepository;

    @Autowired(required = false)
    private DataSource dataSource;

    @Autowired
    private PersistentTokenRepository rememberMeTokenRepository;

    @Autowired
    private SessionRegistry sessionRegistry;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(new SystemAuthenticationProvider(userRepository));

        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userRepository);
        daoAuthenticationProvider.setPasswordEncoder(getPasswordEncoder());
        auth.authenticationProvider(daoAuthenticationProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.antMatcher("/**")
                .authorizeRequests().anyRequest().permitAll()
                .and()
                .logout().logoutUrl("/logout").logoutSuccessUrl("/")
                .and()
                .headers().frameOptions().sameOrigin()
                .and()
                .anonymous(anonymousConfigurer -> {
                    anonymousConfigurer.key(coreProperties.getAnonymousAuthenticationTokenKey());
                    anonymousConfigurer.principal(userRepository.getAnonymousUser());
                    Collection<? extends GrantedAuthority> anonymousAuthorities = userRepository.getAnonymousUser().getAuthorities();
                    if (!anonymousAuthorities.isEmpty()) {
                        anonymousConfigurer.authorities(new ArrayList<>(userRepository.getAnonymousUser().getAuthorities()));
                    }
                })
                .rememberMe().rememberMeServices(rememberMeServices())
                .and()
                .sessionManagement().sessionAuthenticationStrategy(sessionControlAuthenticationStrategy())
                .maximumSessions(sessionProperties.getMaximumSessionsPerUser()).sessionRegistry(sessionRegistry)
                .and().and()
                .csrf().disable()
                .headers().frameOptions().sameOrigin();
    }

    @Bean("sec_rememberMeServices")
    protected RememberMeServices rememberMeServices() {
        JmixRememberMeServices rememberMeServices =
                new JmixRememberMeServices(rememberMeProperties.getKey(), userRepository, rememberMeTokenRepository);
        rememberMeServices.setTokenValiditySeconds(rememberMeProperties.getTokenValiditySeconds());
        rememberMeServices.setParameter(DEFAULT_PARAMETER);
        return rememberMeServices;
    }

    @Primary
    @Bean
    protected SessionAuthenticationStrategy sessionControlAuthenticationStrategy() {
        return new CompositeSessionAuthenticationStrategy(strategies());
    }

    protected List<SessionAuthenticationStrategy> strategies() {
        RegisterSessionAuthenticationStrategy registerSessionAuthenticationStrategy
                = new RegisterSessionAuthenticationStrategy(sessionRegistry);
        ConcurrentSessionControlAuthenticationStrategy concurrentSessionControlStrategy
                = new ConcurrentSessionControlAuthenticationStrategy(sessionRegistry);
        concurrentSessionControlStrategy.setMaximumSessions(sessionProperties.getMaximumSessionsPerUser());

        List<SessionAuthenticationStrategy> strategies = new LinkedList<>();

        strategies.add(registerSessionAuthenticationStrategy);
        strategies.add(concurrentSessionControlStrategy);
        strategies.add(jmixSessionAuthenticationStrategy());
        return strategies;
    }

    @Bean
    protected SessionAuthenticationStrategy jmixSessionAuthenticationStrategy() {
        return new JmixSessionAuthenticationStrategy();
    }

    @Bean(name = "sec_SessionRegistry")
    protected SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean(name = "sec_AuthenticationManager")
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean(name = "sec_PasswordEncoder")
    public PasswordEncoder getPasswordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean(name = "sec_SecurityConstraintsRegistration")
    public SecurityConstraintsRegistration constraintsRegistration() {
        return new SecurityConstraintsRegistration();
    }

    @Bean("sec_rememberMeRepository")
    protected PersistentTokenRepository rememberMeRepository() {
        if (dataSource == null) {
            return new InMemoryTokenRepositoryImpl();
        }
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        return jdbcTokenRepository;
    }
}
