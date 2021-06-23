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

import io.jmix.ldap.LdapProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.LdapUtils;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;

import javax.naming.NamingException;
import java.util.Collection;
import java.util.Collections;

public class LdapUserSynchronizationManager {

    private static final Logger log = LoggerFactory.getLogger(LdapUserSynchronizationManager.class);

    protected String groupDn;

    protected LdapTemplate ldapTemplate;

    protected LdapUserSearch ldapUserSearch;

    protected LdapUserDetailsSynchronizationStrategy synchronizationStrategy;

    protected String memberAttribute = "uniqueMember";

    protected String usernameAttribute = "uid";

    protected LdapAuthoritiesPopulator authoritiesPopulator;

    @Autowired(required = false)
    public void setSynchronizationStrategy(LdapUserDetailsSynchronizationStrategy synchronizationStrategy) {
        this.synchronizationStrategy = synchronizationStrategy;
    }

    @Autowired
    public void setLdapProperties(LdapProperties ldapProperties) {
        this.groupDn = ldapProperties.getGroupForSynchronization();
        this.memberAttribute = ldapProperties.getMemberAttribute();
        this.usernameAttribute = ldapProperties.getUsernameAttribute();
    }

    @Autowired
    public void setAuthoritiesPopulator(LdapAuthoritiesPopulator authoritiesPopulator) {
        this.authoritiesPopulator = authoritiesPopulator;
    }

    /**
     * Obtains LDAP users from the given group and synchronize them using the {@link #synchronizationStrategy}.
     *
     * @throws IllegalArgumentException when LDAP group for synchronization is not set or
     *                                  group does not contain any users.
     * @throws IllegalStateException    when LdapUserDetailsSynchronizationStrategy bean is not found.
     */
    public void synchronizeUsersFromGroup() {
        if (groupDn == null) {
            throw new IllegalArgumentException("LDAP group for synchronization is not set in application properties");
        }
        if (synchronizationStrategy == null) {
            throw new IllegalStateException("LdapUserDetailsSynchronizationStrategy bean is not found");
        }

        String groupRelativeDn = getRelativeDn(groupDn);
        DirContextOperations groupDirContextOperations = ldapTemplate.lookupContext(groupRelativeDn);
        String[] groupMembers = groupDirContextOperations.getStringAttributes(memberAttribute);

        if (groupMembers == null || groupMembers.length == 0) {
            throw new IllegalArgumentException("No users found in the group: " + groupDn);
        } else {
            for (String userDn : groupMembers) {
                String relativeName = getRelativeDn(userDn);
                DirContextOperations dirContextOperations = ldapTemplate.lookupContext(relativeName);
                String username = dirContextOperations.getStringAttribute(usernameAttribute);
                Collection<? extends GrantedAuthority> authorities = Collections.emptyList();
                if (authoritiesPopulator != null) {
                    authorities = authoritiesPopulator.getGrantedAuthorities(dirContextOperations, username);
                }
                synchronizationStrategy.synchronizeUserDetails(dirContextOperations, username, authorities);
            }
        }
    }

    /**
     * Obtains the part of a DN relative to the base context.
     */
    protected String getRelativeDn(String dn) {
        try {
            return LdapUtils.getRelativeName(dn, ldapTemplate.getContextSource().getReadOnlyContext());
        } catch (NamingException e) {
            throw org.springframework.ldap.support.LdapUtils.convertLdapException(e);
        }
    }

    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public void setLdapUserSearch(LdapUserSearch ldapUserSearch) {
        this.ldapUserSearch = ldapUserSearch;
    }

    public void setMemberAttribute(String memberAttribute) {
        this.memberAttribute = memberAttribute;
    }

    public void setUsernameAttribute(String usernameAttribute) {
        this.usernameAttribute = usernameAttribute;
    }
}
