package io.jmix.ldap;

import io.jmix.core.JmixOrder;
import io.jmix.core.security.event.PreAuthenticationCheckEvent;
import io.jmix.ldap.userdetails.JmixLdapGrantedAuthoritiesMapper;
import io.jmix.security.StandardSecurityConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

public class LdapSecurityConfiguration extends StandardSecurityConfiguration {

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

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        super.configure(auth);
        addLdapAuthenticationProvider(auth);
    }

    protected void addLdapAuthenticationProvider(AuthenticationManagerBuilder auth) throws Exception {
        auth.ldapAuthentication()
                .contextSource(ldapContextSource)
                .userSearchBase(ldapProperties.getUserSearchBase())
                .userSearchFilter(ldapProperties.getUserSearchFilter())
                .ldapAuthoritiesPopulator(ldapAuthoritiesPopulator)
                .rolePrefix(StringUtils.EMPTY)
                .userDetailsContextMapper(ldapUserDetailsContextMapper)
                .authoritiesMapper(grantedAuthoritiesMapper);
    }

    @EventListener
    @Order(JmixOrder.LOWEST_PRECEDENCE - 10)
    public void onPreAuthenticationCheckEvent(PreAuthenticationCheckEvent event) {
        if (!ldapProperties.getStandardAuthenticationUsers().contains(event.getUser().getUsername())) {
            throw new BadCredentialsException("Current user cannot be authenticated via standard authentication");
        }
    }
}
