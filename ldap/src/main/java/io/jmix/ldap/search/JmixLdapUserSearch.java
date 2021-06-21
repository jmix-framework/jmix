package io.jmix.ldap.search;

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.search.LdapUserSearch;

import java.util.Set;

/**
 * Extension of LdapUserSearch interface which adds extra method required by Jmix.
 */
public interface JmixLdapUserSearch extends LdapUserSearch {
    /**
     * Locates multiple user in the directory by the given substring
     * and returns the LDAP information for those users.
     *
     * @param substring the substring of login name supplied to the authentication service.
     * @return a set of DirContextOperations objects containing the user's full DN and requested attributes.
     */
    Set<DirContextOperations> searchForUsersBySubstring(String substring);
}
