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
import io.jmix.core.JmixOrder;
import io.jmix.core.security.impl.SubstitutedUserAuthenticationProvider;
import io.jmix.core.security.impl.SystemAuthenticationProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.List;

/**
 * This security configuration can be used in test or simple projects, for example:
 * <pre>
 * &#64;SpringBootApplication
 * public class SampleApplication {
 *    // ...
 *
 *    &#64;EnableWebSecurity
 *    static class SecurityConfiguration extends CoreSecurityConfiguration {
 *
 *        &#64;Override
 *        public UserRepository userRepository() {
 * 	        InMemoryCoreUserRepository repository = new InMemoryCoreUserRepository();
 * 	        repository.addUser(new CoreUser("admin", "{noop}admin", "Administrator"));
 * 	        return repository;
 *        }
 *    }
 * }
 * </pre>
 */
public class CoreSecurityConfiguration {

    @Bean("core_SecurityFilterChain")
    @Order(JmixOrder.HIGHEST_PRECEDENCE + 300)
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   UserRepository userRepository,
                                                   CoreProperties coreProperties) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll()
                )
                .anonymous(anonymous -> anonymous
                        .principal(userRepository.getAnonymousUser())
                        .key(coreProperties.getAnonymousAuthenticationTokenKey())
                )
                .logout(logout -> logout.logoutSuccessUrl("/#login"))
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions().sameOrigin());
        return http.build();
    }

    @Bean(name = "core_authenticationManager")
    public AuthenticationManager authenticationManager(UserRepository userRepository,
                                                       AuthenticationEventPublisher authenticationEventPublisher,
                                                       @Qualifier("core_PreAuthenticationChecks") PreAuthenticationChecks preAuthenticationChecks,
                                                       @Qualifier("core_PostAuthenticationChecks") PostAuthenticationChecks postAuthenticationChecks) throws Exception {
        List<AuthenticationProvider> providers = new ArrayList<>();

        providers.add(new SystemAuthenticationProvider(userRepository));
        providers.add(new SubstitutedUserAuthenticationProvider(userRepository));

        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userRepository);
        daoAuthenticationProvider.setPreAuthenticationChecks(preAuthenticationChecks);
        daoAuthenticationProvider.setPostAuthenticationChecks(postAuthenticationChecks);

        providers.add(daoAuthenticationProvider);

        ProviderManager providerManager = new ProviderManager(providers);
        providerManager.setAuthenticationEventPublisher(authenticationEventPublisher);
        return providerManager;
    }

    @Bean(name = "core_UserRepository")
    public UserRepository userRepository() {
        return new InMemoryUserRepository();
    }

    @Bean(name = "core_PreAuthenticationChecks")
    public PreAuthenticationChecks preAuthenticationChecks() {
        return new PreAuthenticationChecks();
    }

    @Bean(name = "core_PostAuthenticationChecks")
    public PostAuthenticationChecks postAuthenticationChecks() {
        return new PostAuthenticationChecks();
    }

    @Bean("core_AuthenticationEventPublisher")
    public DefaultAuthenticationEventPublisher authenticationEventPublisher(ApplicationEventPublisher publisher) {
        return new DefaultAuthenticationEventPublisher(publisher);
    }
}