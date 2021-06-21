package io.jmix.ldap.userdetails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ActiveDirectoryLdapAuthoritiesPopulator implements LdapAuthoritiesPopulator {

    private static final Logger log = LoggerFactory.getLogger(ActiveDirectoryLdapAuthoritiesPopulator.class);

    @Override
    public Collection<? extends GrantedAuthority> getGrantedAuthorities(DirContextOperations userData, String username) {
        String[] groups = userData.getStringAttributes("memberOf");
        if (groups == null) {
            log.debug("No values for 'memberOf' attribute.");
            return AuthorityUtils.NO_AUTHORITIES;
        }
        if (log.isDebugEnabled()) {
            log.debug("'memberOf' attribute values: " + Arrays.asList(groups));
        }
        List<GrantedAuthority> authorities = new ArrayList<>(groups.length);
        for (String group : groups) {
            authorities.add(new SimpleGrantedAuthority(new DistinguishedName(group).removeLast().getValue()));
        }
        return authorities;
    }
}
