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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

@ConfigurationProperties(prefix = "jmix.ldap")
@ConstructorBinding
public class LdapProperties {

    /**
     * Whether LDAP authentication is enabled.
     */
    boolean enabled;

    /**
     * The source of the UserDetails objects returned after successful authentication (app or ldap).
     */
    String userDetailsSource;

    /**
     * LDAP server URLs.
     */
    List<String> urls;

    /**
     * Common base DN for provided LDAP servers.
     */
    String baseDn;

    /**
     * User distinguished name (principal) to use for getting authenticated contexts.
     */
    String managerDn;

    /**
     * Password (credentials) to use for getting authenticated contexts.
     */
    String managerPassword;

    /**
     * Method to handle referrals.
     */
    String managerReferral;

    /**
     * Search base for user searches.
     */
    String userSearchBase;

    /**
     * LDAP filter used to search for users, e.g. (uid={0}).
     */
    String userSearchFilter;

    /**
     * LDAP attribute corresponding to the username.
     */
    String usernameAttribute;

    /**
     * LDAP group attribute to specify group membership.
     */
    String memberAttribute;

    /**
     * The ID of the attribute which contains the role name for a group (used by authorities populator).
     */
    String groupRoleAttribute;

    /**
     * The base DN from which the search for group membership should be performed (used by authorities populator).
     */
    String groupSearchBase;

    /**
     * Whether a subtree scope search should be performed while searching for user groups.
     */
    boolean groupSearchSubtree;

    /**
     * Group search filter configured for authorities populator.
     */
    String groupSearchFilter;

    //Active Directory

    /**
     * Whether Active Directory specific security configuration should be used instead of the default one.
     */
    Boolean useActiveDirectoryConfiguration;

    /**
     * Active Directory domain name.
     */
    String activeDirectoryDomain;

    /**
     * DN of the group containing users to be synchronized in the application.
     */
    String groupForSynchronization;

    /**
     * Whether to save role assignments during user synchronization.
     */
    Boolean synchronizeRoleAssignments;

    /**
     * Whether users are synchronized on every login.
     */
    Boolean synchronizeUserOnLogin;

    /**
     * List of Jmix roles codes that will be assigned to every user after successful authentication.
     */
    List<String> defaultRoles;

    /**
     * List of users that should always be authenticated over the database, not LDAP directory.
     */
    List<String> standardAuthenticationUsers;

    public LdapProperties(@DefaultValue("true") boolean enabled,
                          @DefaultValue("app") String userDetailsSource,
                          List<String> urls,
                          String baseDn,
                          String managerDn,
                          String managerPassword,
                          @DefaultValue("") String userSearchBase,
                          String userSearchFilter,
                          @DefaultValue("uid") String usernameAttribute,
                          @DefaultValue("uniqueMember") String memberAttribute,
                          @DefaultValue("cn") String groupRoleAttribute,
                          @DefaultValue("") String groupSearchBase,
                          @DefaultValue("false") boolean groupSearchSubtree,
                          @DefaultValue("(uniqueMember={0})") String groupSearchFilter,
                          @DefaultValue("false") Boolean useActiveDirectoryConfiguration,
                          String activeDirectoryDomain,
                          String groupForSynchronization,
                          @DefaultValue("true") Boolean synchronizeRoleAssignments,
                          @DefaultValue("true") Boolean synchronizeUserOnLogin,
                          @Nullable List<String> defaultRoles,
                          @DefaultValue({"admin", "system"}) List<String> standardAuthenticationUsers,
                          String managerReferral) {
        this.enabled = enabled;
        this.userDetailsSource = userDetailsSource;
        this.urls = urls;
        this.baseDn = baseDn;
        this.managerDn = managerDn;
        this.managerPassword = managerPassword;
        this.userSearchBase = userSearchBase;
        this.userSearchFilter = userSearchFilter;
        this.usernameAttribute = usernameAttribute;
        this.memberAttribute = memberAttribute;
        this.groupRoleAttribute = groupRoleAttribute;
        this.groupSearchBase = groupSearchBase;
        this.groupSearchSubtree = groupSearchSubtree;
        this.groupSearchFilter = groupSearchFilter;
        this.useActiveDirectoryConfiguration = useActiveDirectoryConfiguration;
        this.activeDirectoryDomain = activeDirectoryDomain;
        this.groupForSynchronization = groupForSynchronization;
        this.synchronizeRoleAssignments = synchronizeRoleAssignments;
        this.synchronizeUserOnLogin = synchronizeUserOnLogin;
        this.defaultRoles = defaultRoles == null ? Collections.emptyList() : defaultRoles;
        this.standardAuthenticationUsers = standardAuthenticationUsers;
        this.managerReferral = managerReferral;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getUserDetailsSource() {
        return userDetailsSource;
    }

    public List<String> getUrls() {
        return urls;
    }

    public String getBaseDn() {
        return baseDn;
    }

    public String getManagerDn() {
        return managerDn;
    }

    public String getManagerPassword() {
        return managerPassword;
    }

    public String getUserSearchBase() {
        return userSearchBase;
    }

    public String getUserSearchFilter() {
        return userSearchFilter;
    }

    public String getUsernameAttribute() {
        return usernameAttribute;
    }

    public String getMemberAttribute() {
        return memberAttribute;
    }

    public String getGroupRoleAttribute() {
        return groupRoleAttribute;
    }

    public String getGroupSearchBase() {
        return groupSearchBase;
    }

    public boolean isGroupSearchSubtree() {
        return groupSearchSubtree;
    }

    public String getGroupSearchFilter() {
        return groupSearchFilter;
    }

    public String getActiveDirectoryDomain() {
        return activeDirectoryDomain;
    }

    public Boolean getUseActiveDirectoryConfiguration() {
        return useActiveDirectoryConfiguration;
    }

    public String getGroupForSynchronization() {
        return groupForSynchronization;
    }

    public Boolean getSynchronizeRoleAssignments() {
        return synchronizeRoleAssignments;
    }

    public Boolean getSynchronizeUserOnLogin() {
        return synchronizeUserOnLogin;
    }

    public List<String> getDefaultRoles() {
        return defaultRoles;
    }

    public List<String> getStandardAuthenticationUsers() {
        return standardAuthenticationUsers;
    }

    public String getManagerReferral() {
        return managerReferral;
    }
}

