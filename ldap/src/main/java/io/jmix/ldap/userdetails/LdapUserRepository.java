package io.jmix.ldap.userdetails;

import io.jmix.core.security.UserRepository;
import io.jmix.ldap.search.JmixLdapUserSearch;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Implementation of UserRepository that searches for users in LDAP.
 */
public class LdapUserRepository implements UserRepository {

    private final JmixLdapUserSearch userSearch;

    private final LdapAuthoritiesPopulator authoritiesPopulator;

    private UserDetailsContextMapper userDetailsMapper = new LdapUserDetailsMapper();

    private String usernameAttribute = "uid";

    public LdapUserRepository(JmixLdapUserSearch userSearch) {
        this(userSearch, new NullLdapAuthoritiesPopulator());
    }

    public LdapUserRepository(JmixLdapUserSearch userSearch, LdapAuthoritiesPopulator authoritiesPopulator) {
        Assert.notNull(userSearch, "userSearch must not be null");
        Assert.notNull(authoritiesPopulator, "authoritiesPopulator must not be null");
        this.userSearch = userSearch;
        this.authoritiesPopulator = authoritiesPopulator;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        DirContextOperations userData = this.userSearch.searchForUser(username);
        return this.userDetailsMapper.mapUserFromContext(userData, username,
                this.authoritiesPopulator.getGrantedAuthorities(userData, username));
    }

    public void setUserDetailsMapper(UserDetailsContextMapper userDetailsMapper) {
        Assert.notNull(userDetailsMapper, "userDetailsMapper must not be null");
        this.userDetailsMapper = userDetailsMapper;
    }

    @Override
    public UserDetails getSystemUser() {
        throw new UnsupportedOperationException("LdapUserRepository does not include the system user");
    }

    @Override
    public UserDetails getAnonymousUser() {
        throw new UnsupportedOperationException("LdapUserRepository does not include the anonymous user");
    }

    @Override
    public List<? extends UserDetails> getByUsernameLike(String substring) {
        Set<DirContextOperations> userData = this.userSearch.searchForUsersBySubstring(substring);
        List<UserDetails> result = new ArrayList<>();
        for (DirContextOperations userDatum : userData) {
            String username = userDatum.getStringAttribute(usernameAttribute);
            result.add(this.userDetailsMapper.mapUserFromContext(userDatum, username,
                    this.authoritiesPopulator.getGrantedAuthorities(userDatum, username)));
        }
        return result;
    }

    public void setUsernameAttribute(String usernameAttribute) {
        this.usernameAttribute = usernameAttribute;
    }

    private static final class NullLdapAuthoritiesPopulator implements LdapAuthoritiesPopulator {

        @Override
        public Collection<GrantedAuthority> getGrantedAuthorities(DirContextOperations userDetails, String username) {
            return AuthorityUtils.NO_AUTHORITIES;
        }

    }
}
