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
import io.jmix.core.JmixSecurityFilterChainOrder;
import io.jmix.core.security.AddonAuthenticationManagerSupplier;
import io.jmix.core.security.event.PreAuthenticationCheckEvent;
import io.jmix.ldap.authentication.ActiveDirectoryAuthenticationManagerSupplier;
import io.jmix.ldap.userdetails.JmixLdapGrantedAuthoritiesMapper;
import io.jmix.security.SecurityConfigurers;
import io.jmix.core.security.StandardAuthenticationProvidersProducer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.security.web.SecurityFilterChain;

import static io.jmix.security.SecurityConfigurers.uiSecurity;

public class LdapActiveDirectorySecurityConfiguration {

    public static final String SECURITY_CONFIGURER_QUALIFIER = "ldap-active-directory";

    @Autowired
    protected LdapProperties ldapProperties;

    @Autowired
    protected UserDetailsContextMapper ldapUserDetailsContextMapper;

    @Autowired
    protected JmixLdapGrantedAuthoritiesMapper grantedAuthoritiesMapper;

    @Bean("ldap_SecurityFilterChain")
    @Order(JmixSecurityFilterChainOrder.LDAP_ACTIVE_DIRECTORY)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.apply(uiSecurity());
        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/"));
        SecurityConfigurers.applySecurityConfigurersWithQualifier(http, SECURITY_CONFIGURER_QUALIFIER);
        return http.build();
    }

    @Bean("ldap_ActiveDirectoryAuthenticationManagerSupplier")
    @Order(100)
    public AddonAuthenticationManagerSupplier ldapActiveDirectoryAuthenticationManagerSupplier(StandardAuthenticationProvidersProducer providersProducer,
                                                                                               ApplicationEventPublisher publisher,
                                                                                               LdapProperties ldapProperties,
                                                                                               UserDetailsContextMapper ldapUserDetailsContextMapper,
                                                                                               JmixLdapGrantedAuthoritiesMapper grantedAuthoritiesMapper) {
        return new ActiveDirectoryAuthenticationManagerSupplier(providersProducer, publisher, ldapProperties,
                ldapUserDetailsContextMapper, grantedAuthoritiesMapper);
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

    @EventListener
    @Order(JmixOrder.LOWEST_PRECEDENCE - 10)
    public void onPreAuthenticationCheckEvent(PreAuthenticationCheckEvent event) {
        if (!ldapProperties.getStandardAuthenticationUsers().contains(event.getUser().getUsername())) {
            throw new BadCredentialsException("Current user cannot be authenticated via standard authentication");
        }
    }
}
