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

import io.jmix.core.CoreConfiguration;
import io.jmix.core.CoreProperties;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.rememberme.JmixRememberMeServices;
import io.jmix.core.rememberme.RememberMeProperties;
import io.jmix.core.security.*;
import io.jmix.core.security.impl.AuthenticationManagerSupplierImpl;
import io.jmix.core.security.impl.JmixSessionAuthenticationStrategy;
import io.jmix.core.session.SessionProperties;
import io.jmix.security.authentication.StandardAuthenticationManagerSupplier;
import io.jmix.security.authentication.StandardAuthenticationProvidersProducer;
import io.jmix.security.impl.constraint.SecurityConstraintsRegistration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import java.util.LinkedList;
import java.util.List;

import static org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices.DEFAULT_PARAMETER;

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@JmixModule(dependsOn = CoreConfiguration.class)
@PropertySource(name = "io.jmix.security", value = "classpath:/io/jmix/security/module.properties")
public class SecurityConfiguration {

    @Autowired
    private CoreProperties coreProperties;

    @Autowired
    private SessionProperties sessionProperties;

    @Autowired
    private RememberMeProperties rememberMeProperties;

    @Autowired
    private PersistentTokenRepository rememberMeTokenRepository;

    @Autowired
    private SessionRegistry sessionRegistry;

    @Autowired
    private UserRepository userRepository;

    @Bean(name = "sec_PasswordEncoder")
    public PasswordEncoder getPasswordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean(name = "sec_SecurityConstraintsRegistration")
    public SecurityConstraintsRegistration constraintsRegistration() {
        return new SecurityConstraintsRegistration();
    }

    @Bean
    protected PersistentTokenRepository inMemoryRememberMeRepository() {
        return new InMemoryTokenRepositoryImpl();
    }

    @Bean("sec_rememberMeServices")
    public RememberMeServices rememberMeServices() {
        JmixRememberMeServices rememberMeServices =
                new JmixRememberMeServices(rememberMeProperties.getKey(), userRepository, rememberMeTokenRepository);
        rememberMeServices.setTokenValiditySeconds(rememberMeProperties.getTokenValiditySeconds());
        rememberMeServices.setParameter(DEFAULT_PARAMETER);
        return rememberMeServices;
    }

    @Primary
    @Bean
    public SessionAuthenticationStrategy sessionControlAuthenticationStrategy() {
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
    public SessionAuthenticationStrategy jmixSessionAuthenticationStrategy() {
        return new JmixSessionAuthenticationStrategy();
    }

    @Bean(name = "sec_SessionRegistry")
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean(name = "sec_HttpSessionEventPublisher")
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean(name = "sec_PreAuthenticationChecks")
    public PreAuthenticationChecks preAuthenticationChecks() {
        return new PreAuthenticationChecks();
    }

    @Bean(name = "sec_PostAuthenticationChecks")
    public PostAuthenticationChecks postAuthenticationChecks() {
        return new PostAuthenticationChecks();
    }

    @Bean("sec_StandardAuthenticationManagerSupplier")
    @Order(200)
    public AddonAuthenticationManagerSupplier standardAuthenticationManagerSupplier(StandardAuthenticationProvidersProducer providersProducer,
                                                                            ApplicationEventPublisher publisher) {
        return new StandardAuthenticationManagerSupplier(providersProducer, publisher);
    }

    @Bean("sec_AuthenticationManagerSupplier")
    public AuthenticationManagerSupplier authenticationManagerSupplier(List<AddonAuthenticationManagerSupplier> suppliers) {
        return new AuthenticationManagerSupplierImpl(suppliers);
    }

    /**
     * Global AuthenticationManager
     */
    @Bean("sec_AuthenticationManager")
    public AuthenticationManager authenticationManager(AuthenticationManagerSupplier authenticationManagerSupplier) {
        return authenticationManagerSupplier.getAuthenticationManager();
    }

}
