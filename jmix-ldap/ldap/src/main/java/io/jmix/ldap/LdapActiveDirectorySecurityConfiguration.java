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
import io.jmix.core.security.AddonAuthenticationManagerSupplier;
import io.jmix.core.security.event.PreAuthenticationCheckEvent;
import io.jmix.ldap.authentication.ActiveDirectoryAuthenticationManagerSupplier;
import io.jmix.ldap.userdetails.JmixLdapGrantedAuthoritiesMapper;
import io.jmix.security.authentication.StandardAuthenticationProvidersProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

public class LdapActiveDirectorySecurityConfiguration {

    @Autowired
    protected LdapProperties ldapProperties;

    @Autowired
    protected UserDetailsContextMapper ldapUserDetailsContextMapper;

    @Autowired
    protected JmixLdapGrantedAuthoritiesMapper grantedAuthoritiesMapper;

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

    @EventListener
    @Order(JmixOrder.LOWEST_PRECEDENCE - 10)
    public void onPreAuthenticationCheckEvent(PreAuthenticationCheckEvent event) {
        if (!ldapProperties.getStandardAuthenticationUsers().contains(event.getUser().getUsername())) {
            throw new BadCredentialsException("Current user cannot be authenticated via standard authentication");
        }
    }
}
