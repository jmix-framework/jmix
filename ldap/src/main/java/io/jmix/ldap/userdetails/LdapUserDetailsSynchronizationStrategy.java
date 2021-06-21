package io.jmix.ldap.userdetails;

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public interface LdapUserDetailsSynchronizationStrategy {
    UserDetails synchronizeUserDetails(DirContextOperations ctx, String username,
                                       Collection<? extends GrantedAuthority> authorities);
}
