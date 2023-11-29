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
