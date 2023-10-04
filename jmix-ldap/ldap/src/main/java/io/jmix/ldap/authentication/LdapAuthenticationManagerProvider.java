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

package io.jmix.ldap.authentication;

import io.jmix.core.security.AddonAuthenticationManagerProvider;
import io.jmix.ldap.LdapProperties;
import io.jmix.ldap.userdetails.JmixLdapGrantedAuthoritiesMapper;
import io.jmix.core.security.StandardAuthenticationProvidersProducer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.ldap.LdapBindAuthenticationManagerFactory;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

import java.util.List;

public class LdapAuthenticationManagerProvider implements AddonAuthenticationManagerProvider {

    protected StandardAuthenticationProvidersProducer providersProducer;

    protected ApplicationEventPublisher publisher;

    protected LdapProperties ldapProperties;

    protected LdapContextSource ldapContextSource;

    protected UserDetailsContextMapper ldapUserDetailsContextMapper;

    protected JmixLdapGrantedAuthoritiesMapper grantedAuthoritiesMapper;

    protected LdapAuthoritiesPopulator ldapAuthoritiesPopulator;

    public LdapAuthenticationManagerProvider(StandardAuthenticationProvidersProducer providersProducer,
                                             ApplicationEventPublisher publisher,
                                             LdapProperties ldapProperties,
                                             LdapContextSource ldapContextSource,
                                             UserDetailsContextMapper ldapUserDetailsContextMapper,
                                             JmixLdapGrantedAuthoritiesMapper grantedAuthoritiesMapper,
                                             LdapAuthoritiesPopulator ldapAuthoritiesPopulator) {
        this.providersProducer = providersProducer;
        this.publisher = publisher;
        this.ldapProperties = ldapProperties;
        this.ldapContextSource = ldapContextSource;
        this.ldapUserDetailsContextMapper = ldapUserDetailsContextMapper;
        this.grantedAuthoritiesMapper = grantedAuthoritiesMapper;
        this.ldapAuthoritiesPopulator = ldapAuthoritiesPopulator;
    }

    @Override
    public AuthenticationManager getAuthenticationManager() {
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
        providerManager.setAuthenticationEventPublisher(new DefaultAuthenticationEventPublisher(publisher));
        return providerManager;
    }
}
