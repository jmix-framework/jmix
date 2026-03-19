/*
 * Copyright 2026 Haulmont.
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

package io.jmix.saml.mapper.user;

import com.google.common.base.Strings;
import io.jmix.core.SaveContext;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.security.UserRepository;
import io.jmix.data.PersistenceHints;
import io.jmix.saml.SamlProperties;
import io.jmix.saml.mapper.role.SamlAssertionRolesMapper;
import io.jmix.saml.user.JmixSamlUserDetails;
import io.jmix.saml.util.SamlAssertionUtils;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import io.jmix.security.role.assignment.RoleAssignmentRoleType;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Implementation of the {@link SamlUserMapper} that maps the external user object to the persistent user
 * and also stores the user and optionally their role assignment to the database.
 *
 * @param <T> class of Jmix user
 */
public abstract class SynchronizingSamlUserMapper<T extends JmixSamlUserDetails> extends BaseSamlUserMapper<T> {

    private static final Logger log = getLogger(SynchronizingSamlUserMapper.class);

    @Autowired
    protected UnconstrainedDataManager dataManager;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected SamlAssertionRolesMapper rolesMapper;
    @Autowired
    protected RoleGrantedAuthorityUtils roleGrantedAuthorityUtils;

    protected boolean synchronizeRoleAssignments;

    /**
     * Returns a class of the user used by the application. This user is set to the security context.
     */
    protected abstract Class<T> getApplicationUserClass();

    @Override
    protected T initJmixUser(Assertion assertion) {
        String username = getSamlUsername(assertion);
        T jmixUserDetails;
        try {
            jmixUserDetails = (T) userRepository.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            log.debug("User with login {} wasn't found in user repository", username);
            jmixUserDetails = dataManager.create(getApplicationUserClass());
        }
        return jmixUserDetails;
    }

    @Override
    protected void populateUserAuthorities(Assertion assertion, T jmixUser) {
        Collection<? extends GrantedAuthority> grantedAuthorities = rolesMapper.toGrantedAuthorities(assertion);
        jmixUser.setAuthorities(grantedAuthorities);
    }

    @Override
    protected void performAdditionalModifications(Assertion assertion, OpenSaml4AuthenticationProvider.ResponseToken responseToken, T jmixUser) {
        super.performAdditionalModifications(assertion, responseToken, jmixUser);
        saveJmixUserAndRoleAssignments(assertion, jmixUser);
    }

    /**
     * Saves Jmix user and synchronizes role assignments
     */
    protected void saveJmixUserAndRoleAssignments(Assertion assertion, T jmixUser) {
        SaveContext saveContext = new SaveContext();

        if (isSynchronizeRoleAssignments()) {
            String username = getSamlUsername(assertion);

            //disable soft-deletion to completely remove role assignment records from the database
            saveContext.setHint(PersistenceHints.SOFT_DELETION, false);

            // Load existing role assignments
            List<RoleAssignmentEntity> existingRoleAssignments = dataManager.load(RoleAssignmentEntity.class)
                    .query("select e from sec_RoleAssignmentEntity e where e.username = :username")
                    .parameter("username", username)
                    .list();

            // Build new role assignments from current authorities
            List<RoleAssignmentEntity> newRoleAssignments = buildRoleAssignmentEntities(username, jmixUser.getAuthorities());

            // Perform differential sync
            updateRoleAssignmentsSaveContext(existingRoleAssignments, newRoleAssignments, saveContext);
        }
        saveContext.saving(jmixUser);

        dataManager.save(saveContext);
    }

    /**
     * Fills save context with role assignment operations.
     *
     * @param existingAssignments Current role assignments in database
     * @param actualAssignments   New role assignments from SAML assertion
     * @param saveContext         SaveContext to add remove/save operations
     */
    protected void updateRoleAssignmentsSaveContext(List<RoleAssignmentEntity> existingAssignments,
                                                    List<RoleAssignmentEntity> actualAssignments,
                                                    SaveContext saveContext) {

        // Create sets for comparison (using roleCode + roleType as key)
        Set<String> existingKeys = existingAssignments.stream()
                .map(assignment -> assignmentKey(assignment))
                .collect(java.util.stream.Collectors.toSet());

        Set<String> actualKeys = actualAssignments.stream()
                .map(assignment -> assignmentKey(assignment))
                .collect(Collectors.toSet());

        // Find assignments to remove/add
        List<RoleAssignmentEntity> assignmentsToRemove = existingAssignments.stream()
                .filter(assignment -> !actualKeys.contains(assignmentKey(assignment)))
                .toList();

        List<RoleAssignmentEntity> assignmentsToAdd = actualAssignments.stream()
                .filter(assignment -> !existingKeys.contains(assignmentKey(assignment)))
                .toList();

        if (!assignmentsToRemove.isEmpty()) {
            log.debug("Removing {} obsolete role assignments", assignmentsToRemove.size());
            saveContext.removing(assignmentsToRemove);
        }

        if (!assignmentsToAdd.isEmpty()) {
            log.debug("Adding {} new role assignments", assignmentsToAdd.size());
            saveContext.saving(assignmentsToAdd);
        }

        if (assignmentsToRemove.isEmpty() && assignmentsToAdd.isEmpty()) {
            log.debug("Role assignments are already in sync, no changes needed");
        }
    }

    protected List<RoleAssignmentEntity> buildRoleAssignmentEntities(String username, Collection<? extends GrantedAuthority> grantedAuthorities) {
        List<RoleAssignmentEntity> roleAssignmentEntities = new ArrayList<>();
        String defaultRolePrefix = roleGrantedAuthorityUtils.getDefaultRolePrefix();
        String defaultRowLevelRolePrefix = roleGrantedAuthorityUtils.getDefaultRowLevelRolePrefix();
        for (GrantedAuthority grantedAuthority : grantedAuthorities) {
            String roleCode = grantedAuthority.getAuthority();
            if (!Strings.isNullOrEmpty(roleCode)) {
                String roleType = RoleAssignmentRoleType.RESOURCE;
                if (roleCode.startsWith(defaultRolePrefix)) {
                    roleCode = roleCode.substring(defaultRolePrefix.length());
                } else if (roleCode.startsWith(defaultRowLevelRolePrefix)) {
                    roleCode = roleCode.substring(defaultRowLevelRolePrefix.length());
                    roleType = RoleAssignmentRoleType.ROW_LEVEL;
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

    /**
     * Whether role assignment synchronization is enabled.
     */
    public boolean isSynchronizeRoleAssignments() {
        return synchronizeRoleAssignments;
    }

    /**
     * Enables role assignment synchronization. If true then role assignment entities will be stored to the database.
     */
    public void setSynchronizeRoleAssignments(boolean synchronizeRoleAssignments) {
        this.synchronizeRoleAssignments = synchronizeRoleAssignments;
    }

    private String assignmentKey(RoleAssignmentEntity assignment) {
        return assignment.getRoleCode() + ":" + assignment.getRoleType();
    }
}
