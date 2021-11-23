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

import io.jmix.core.SaveContext;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.security.UserRepository;
import io.jmix.ldap.LdapProperties;
import io.jmix.security.authentication.RoleGrantedAuthority;
import io.jmix.security.role.assignment.RoleAssignmentRoleType;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A basic implementation of {@link LdapUserDetailsSynchronizationStrategy}, which provides
 * a general functionality for user synchronization with {@link UserRepository}.
 *
 * @param <T> user details class
 */
public abstract class AbstractLdapUserDetailsSynchronizationStrategy<T extends UserDetails>
        implements LdapUserDetailsSynchronizationStrategy {

    protected static final String ROW_LEVEL_ROLE_PREFIX = "row_level_role:";

    private static final Logger log = LoggerFactory.getLogger(AbstractLdapUserDetailsSynchronizationStrategy.class);

    @Autowired
    protected UnconstrainedDataManager dataManager;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected JmixLdapGrantedAuthoritiesMapper authoritiesMapper;

    @Autowired
    protected LdapProperties ldapProperties;

    @Override
    @SuppressWarnings("unchecked")
    public UserDetails synchronizeUserDetails(DirContextOperations ctx, String username,
                                              Collection<? extends GrantedAuthority> authorities) {
        T jmixUserDetails;
        try {
            jmixUserDetails = (T) userRepository.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            log.info("User with login {} wasn't found in user repository", username);
            jmixUserDetails = createUserDetails(username, ctx);
        }

        //copy ldap attributes to UserDetails
        mapUserDetailsAttributes(jmixUserDetails, ctx);

        SaveContext saveContext = new SaveContext();
        if (ldapProperties.getSynchronizeRoleAssignments()) {
            Set<GrantedAuthority> grantedAuthorities = authoritiesMapper.mapAuthorities(authorities);
            grantedAuthorities.addAll(getAdditionalRoles(ctx, username));

            //clean previous role assignments
            List<RoleAssignmentEntity> existingRoleAssignments = dataManager.load(RoleAssignmentEntity.class)
                    .query("select e from sec_RoleAssignmentEntity e where e.username = :username")
                    .parameter("username", username)
                    .list();
            saveContext.removing(existingRoleAssignments);

            saveContext.saving(buildRoleAssignments(grantedAuthorities, username));
        }
        saveContext.saving(jmixUserDetails);

        //persist user details and roles if needed
        dataManager.save(saveContext);

        return jmixUserDetails;
    }

    protected Collection<RoleAssignmentEntity> buildRoleAssignments(Collection<GrantedAuthority> grantedAuthorities,
                                                                    String username) {
        List<RoleAssignmentEntity> roleAssignmentEntities = new ArrayList<>();
        for (GrantedAuthority grantedAuthority : grantedAuthorities) {
            if (grantedAuthority instanceof RoleGrantedAuthority) {
                RoleGrantedAuthority roleGrantedAuthority = (RoleGrantedAuthority) grantedAuthority;
                String roleCode = roleGrantedAuthority.getAuthority();
                String roleType;
                if(roleCode.startsWith(ROW_LEVEL_ROLE_PREFIX)) {
                    roleType = RoleAssignmentRoleType.ROW_LEVEL;
                    roleCode = roleCode.substring(ROW_LEVEL_ROLE_PREFIX.length());
                } else {
                    roleType = RoleAssignmentRoleType.RESOURCE;
                }
                RoleAssignmentEntity roleAssignmentEntity = dataManager.create(RoleAssignmentEntity.class);
                roleAssignmentEntity.setRoleCode(roleCode);
                roleAssignmentEntity.setUsername(username);
                roleAssignmentEntity.setRoleType(roleType);
                roleAssignmentEntities.add(roleAssignmentEntity);
            }
        }
        return roleAssignmentEntities;
    }

    protected abstract Class<T> getUserClass();

    protected T createUserDetails(String username, DirContextOperations ctx) {
        T userDetails = dataManager.create(getUserClass());
        EntityValues.setValue(userDetails, "username", username);
        return userDetails;
    }

    /**
     * This method should be overridden to define how attributes mapping is to be performed.
     *
     * @param userDetails a UserDetails which should be populated with attributes from LDAP.
     * @param ctx         a DirContextOperations object containing the user's full DN and attributes.
     */
    protected abstract void mapUserDetailsAttributes(T userDetails, DirContextOperations ctx);

    /**
     * This method should be overridden if required to obtain any additional roles for the
     * given user (on top of those obtained from the users groups).
     *
     * @param user the context representing the user who's roles are required
     * @return the extra roles which will be merged with those returned by the group search
     */
    protected Set<GrantedAuthority> getAdditionalRoles(DirContextOperations user, String username) {
        return Collections.emptySet();
    }
}
