/*
 * Copyright 2021 Haulmont.
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

package io.jmix.ldap;

import io.jmix.core.JmixOrder;
import io.jmix.core.security.event.PreAuthenticationCheckEvent;
import io.jmix.ldap.userdetails.JmixLdapGrantedAuthoritiesMapper;
import io.jmix.security.impl.StandardAuthenticationProvidersProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.ldap.LdapBindAuthenticationManagerFactory;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

import static io.jmix.security.SecurityConfigurers.uiSecurity;

public class LdapSecurityConfiguration {

    public static final String SECURITY_CONFIGURER_QUALIFIER = "ldap";

    @Autowired
    protected LdapProperties ldapProperties;

    @Autowired
    protected LdapContextSource ldapContextSource;

    @Autowired
    protected UserDetailsContextMapper ldapUserDetailsContextMapper;

    @Autowired
    protected LdapAuthoritiesPopulator ldapAuthoritiesPopulator;

    @Autowired
    protected JmixLdapGrantedAuthoritiesMapper grantedAuthoritiesMapper;

    @Bean("ldap_SecurityFilterChain")
    @Order(JmixOrder.HIGHEST_PRECEDENCE + 300)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.apply(uiSecurity());
        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/"));
        return http.build();
    }

    @Bean("ldap_AuthenticationManager")
    public AuthenticationManager ldapAuthenticationManager(StandardAuthenticationProvidersProducer providersProducer,
                                                           AuthenticationEventPublisher authenticationEventPublisher) {
        LdapBindAuthenticationManagerFactory factory = new LdapBindAuthenticationManagerFactory(ldapContextSource);
        factory.setUserSearchBase(ldapProperties.getUserSearchBase());
        factory.setUserSearchFilter(ldapProperties.getUserSearchFilter());
        factory.setLdapAuthoritiesPopulator(ldapAuthoritiesPopulator);
        factory.setUserDetailsContextMapper(ldapUserDetailsContextMapper);
        factory.setAuthoritiesMapper(grantedAuthoritiesMapper);

        List<AuthenticationProvider> providers = providersProducer.getStandardProviders();

        AuthenticationManager authenticationManager = factory.createAuthenticationManager();
        if (authenticationManager instanceof ProviderManager) {
            providers.addAll(((ProviderManager) authenticationManager).getProviders());
        } else {
            throw new IllegalStateException("Cannot get providers from default LDAP authentication manager");
        }
        ProviderManager providerManager = new ProviderManager(providers);
        providerManager.setAuthenticationEventPublisher(authenticationEventPublisher);
        return providerManager;
    }

    @Bean("ldap_AuthenticationEventPublisher")
    public DefaultAuthenticationEventPublisher authenticationEventPublisher(ApplicationEventPublisher publisher) {
        return new DefaultAuthenticationEventPublisher(publisher);
    }

    @EventListener
    @Order(JmixOrder.LOWEST_PRECEDENCE - 10)
    public void onPreAuthenticationCheckEvent(PreAuthenticationCheckEvent event) {
        if (!ldapProperties.getStandardAuthenticationUsers().contains(event.getUser().getUsername())) {
            throw new BadCredentialsException("Current user cannot be authenticated via standard authentication");
        }
    }
}
