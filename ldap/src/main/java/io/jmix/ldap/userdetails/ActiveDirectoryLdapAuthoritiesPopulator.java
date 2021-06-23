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
