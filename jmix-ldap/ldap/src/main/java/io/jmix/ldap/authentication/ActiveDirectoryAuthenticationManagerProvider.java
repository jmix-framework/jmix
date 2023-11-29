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
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

import java.util.List;

public class ActiveDirectoryAuthenticationManagerProvider implements AddonAuthenticationManagerProvider {

    protected StandardAuthenticationProvidersProducer providersProducer;

    protected ApplicationEventPublisher publisher;

    protected LdapProperties ldapProperties;

    protected UserDetailsContextMapper ldapUserDetailsContextMapper;

    protected JmixLdapGrantedAuthoritiesMapper grantedAuthoritiesMapper;

    public ActiveDirectoryAuthenticationManagerProvider(StandardAuthenticationProvidersProducer providersProducer,
                                                        ApplicationEventPublisher publisher,
                                                        LdapProperties ldapProperties,
                                                        UserDetailsContextMapper ldapUserDetailsContextMapper,
                                                        JmixLdapGrantedAuthoritiesMapper grantedAuthoritiesMapper) {
        this.providersProducer = providersProducer;
        this.publisher = publisher;
        this.ldapProperties = ldapProperties;
        this.ldapUserDetailsContextMapper = ldapUserDetailsContextMapper;
        this.grantedAuthoritiesMapper = grantedAuthoritiesMapper;
    }

    @Override
    public AuthenticationManager getAuthenticationManager() {
        List<AuthenticationProvider> providers = providersProducer.getStandardProviders();
        providers.add(activeDirectoryLdapAuthenticationProvider());
        ProviderManager providerManager = new ProviderManager(providers);
        providerManager.setAuthenticationEventPublisher(new DefaultAuthenticationEventPublisher(publisher));
        return providerManager;
    }

    protected AuthenticationProvider activeDirectoryLdapAuthenticationProvider() {
        String urls = StringUtils.join(ldapProperties.getUrls(), StringUtils.SPACE);
        ActiveDirectoryLdapAuthenticationProvider authenticationProvider =
                new ActiveDirectoryLdapAuthenticationProvider(ldapProperties.getActiveDirectoryDomain(), urls,
                        ldapProperties.getUserSearchBase());
        authenticationProvider.setConvertSubErrorCodesToExceptions(true);
        authenticationProvider.setUserDetailsContextMapper(ldapUserDetailsContextMapper);
        authenticationProvider.setAuthoritiesMapper(grantedAuthoritiesMapper);
        authenticationProvider.setSearchFilter(ldapProperties.getUserSearchFilter());
        return authenticationProvider;
    }
}
