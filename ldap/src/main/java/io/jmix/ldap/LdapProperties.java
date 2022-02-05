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
    boolean enabled;
    String userDetailsSource;
    List<String> urls;
    String baseDn;
    String managerDn;
    String managerPassword;
    String managerReferral;
    String userSearchBase;
    String userSearchFilter;
    String usernameAttribute;
    String memberAttribute;

    String groupRoleAttribute;
    String groupSearchBase;
    boolean groupSearchSubtree;
    String groupSearchFilter;

    //Active Directory
    Boolean useActiveDirectoryConfiguration;
    String activeDirectoryDomain;

    String groupForSynchronization;
    Boolean synchronizeRoleAssignments;
    Boolean synchronizeUserOnLogin;
    List<String> defaultRoles;
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

