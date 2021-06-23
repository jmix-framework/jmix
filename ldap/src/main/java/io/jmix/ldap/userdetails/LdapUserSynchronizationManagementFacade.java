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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

@ManagedResource(description = "Synchronizes LDAP users from the predefined LDAP group", objectName = "jmix.ldap:type=LdapUserSynchronization")
@Component("ldap_LdapUserSynchronizationManagementFacade")
public class LdapUserSynchronizationManagementFacade {
    @Autowired
    protected LdapUserSynchronizationManager ldapUserSynchronizationManager;

    @ManagedOperation(description = "Synchronizes LDAP users from the predefined LDAP group")
    public String synchronizeUsersFromGroup() {
        ldapUserSynchronizationManager.synchronizeUsersFromGroup();
        return "Synchronized successfully";
    }
}
