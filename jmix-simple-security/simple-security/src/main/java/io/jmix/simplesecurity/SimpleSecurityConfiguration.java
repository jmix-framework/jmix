/*
 * Copyright 2022 Haulmont.
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

package io.jmix.simplesecurity;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.rememberme.JmixRememberMeServices;
import io.jmix.core.rememberme.RememberMeProperties;
import io.jmix.core.security.*;
import io.jmix.core.security.impl.AuthenticationManagerProviderImpl;
import io.jmix.core.session.SessionProperties;
import io.jmix.simplesecurity.authentication.SimpleSecurityAddonAuthenticationManagerProvider;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import java.util.LinkedList;
import java.util.List;

import static org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices.DEFAULT_PARAMETER;

/**
 * Main configuration class of the simple-security module.
 */
@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@JmixModule(dependsOn = {CoreConfiguration.class})
public class SimpleSecurityConfiguration {

    //todo MG move to auto-configuration
    //todo MG register JDBC in-memory remember-me repository?
    @Bean
    public PersistentTokenRepository inMemoryRememberMeRepository() {
        return new InMemoryTokenRepositoryImpl();
    }

    @Bean("simsec_rememberMeServices")
    public RememberMeServices rememberMeServices(RememberMeProperties rememberMeProperties,
                                                 UserDetailsService userDetailsService,
                                                 PersistentTokenRepository persistentTokenRepository) {
        JmixRememberMeServices rememberMeServices =
                new JmixRememberMeServices(rememberMeProperties.getKey(), userDetailsService, persistentTokenRepository);
        rememberMeServices.setTokenValiditySeconds(rememberMeProperties.getTokenValiditySeconds());
        rememberMeServices.setParameter(DEFAULT_PARAMETER);
        return rememberMeServices;
    }

    //todo MG move to auto configuration?
    @Bean(name = "simsec_PasswordEncoder")
    public PasswordEncoder getPasswordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean(name = "simsec_PreAuthenticationChecks")
    public PreAuthenticationChecks preAuthenticationChecks() {
        return new PreAuthenticationChecks();
    }

    @Bean(name = "simsec_PostAuthenticationChecks")
    public PostAuthenticationChecks postAuthenticationChecks() {
        return new PostAuthenticationChecks();
    }

    @Bean("simsec_StandardAuthenticationProvidersProducer")
    public StandardAuthenticationProvidersProducer standardAuthenticationProvidersProducer(
            UserRepository userRepository,
            ServiceUserProvider serviceUserProvider,
            PasswordEncoder passwordEncoder,
            PreAuthenticationChecks preAuthenticationChecks,
            PostAuthenticationChecks postAuthenticationChecks
    ) {
        return new StandardAuthenticationProvidersProducer(userRepository, serviceUserProvider, passwordEncoder, preAuthenticationChecks, postAuthenticationChecks);
    }

    @Bean("simsec_SimpleSecurityAddonAuthenticationManagerProvider")
    public SimpleSecurityAddonAuthenticationManagerProvider simpleSecurityAuthenticationManagerProvider(
            StandardAuthenticationProvidersProducer standardAuthenticationProvidersProducer,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        return new SimpleSecurityAddonAuthenticationManagerProvider(standardAuthenticationProvidersProducer, applicationEventPublisher);
    }

    @Bean("simsec_AuthenticationManagerProvider")
    public AuthenticationManagerProvider authenticationManagerProvider(List<AddonAuthenticationManagerProvider> suppliers) {
        return new AuthenticationManagerProviderImpl(suppliers);
    }

    /**
     * Global AuthenticationManager
     */
    @Bean("simsec_AuthenticationManager")
    public AuthenticationManager authenticationManager(AuthenticationManagerProvider authenticationManagerProvider) {
        return authenticationManagerProvider.getAuthenticationManager();
    }

//    ---------------------------

    @Primary
    @Bean
    public SessionAuthenticationStrategy sessionControlAuthenticationStrategy(SessionRegistry sessionRegistry,
                                                                              SessionProperties sessionProperties) {
        return new CompositeSessionAuthenticationStrategy(strategies(sessionRegistry, sessionProperties));
    }

    protected List<SessionAuthenticationStrategy> strategies(SessionRegistry sessionRegistry, SessionProperties sessionProperties) {
        RegisterSessionAuthenticationStrategy registerSessionAuthenticationStrategy
                = new RegisterSessionAuthenticationStrategy(sessionRegistry);
        ConcurrentSessionControlAuthenticationStrategy concurrentSessionControlStrategy
                = new ConcurrentSessionControlAuthenticationStrategy(sessionRegistry);
        concurrentSessionControlStrategy.setMaximumSessions(sessionProperties.getMaximumSessionsPerUser());

        List<SessionAuthenticationStrategy> strategies = new LinkedList<>();

        strategies.add(registerSessionAuthenticationStrategy);
        strategies.add(concurrentSessionControlStrategy);
        //todo MG do we need this jmixSessionAuthenticationStrategy ?
//        strategies.add(jmixSessionAuthenticationStrategy());
        return strategies;
    }

//    @Bean
//    public SessionAuthenticationStrategy jmixSessionAuthenticationStrategy() {
//        return new JmixSessionAuthenticationStrategy();
//    }

    @Bean(name = "simsec_SessionRegistry")
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean(name = "simsec_HttpSessionEventPublisher")
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean("simsec_SecurityContextRepository")
    public SecurityContextRepository securityContextRepository() {
        return new DelegatingSecurityContextRepository(
                new RequestAttributeSecurityContextRepository(),
                new HttpSessionSecurityContextRepository()
        );
    }
}
