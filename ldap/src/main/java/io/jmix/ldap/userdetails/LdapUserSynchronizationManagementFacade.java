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
