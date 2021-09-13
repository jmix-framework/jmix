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

import io.jmix.core.JmixOrder;
import io.jmix.core.security.PostAuthenticationChecks;
import io.jmix.core.security.PreAuthenticationChecks;
import io.jmix.core.security.UserRepository;
import io.jmix.core.security.impl.SubstitutitutedUserAuthenticationProvider;
import io.jmix.core.security.impl.SystemAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;

import static io.jmix.security.SecurityConfigurers.uiSecurity;

@Order(JmixOrder.HIGHEST_PRECEDENCE + 200)
public class StandardSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private PreAuthenticationChecks preAuthenticationChecks;
    @Autowired
    private PostAuthenticationChecks postAuthenticationChecks;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(new SystemAuthenticationProvider(userRepository));
        auth.authenticationProvider(new SubstitutitutedUserAuthenticationProvider(userRepository));

        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userRepository);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        daoAuthenticationProvider.setPreAuthenticationChecks(preAuthenticationChecks);
        daoAuthenticationProvider.setPostAuthenticationChecks(postAuthenticationChecks);

        auth.authenticationProvider(daoAuthenticationProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.apply(uiSecurity()).and()
                .logout().logoutUrl("/logout").logoutSuccessUrl("/");
    }

    @Bean(name = "sec_AuthenticationManager")
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
