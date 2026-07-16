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
import io.jmix.core.entity.EntityValues;
import io.jmix.core.security.UserRepository;
import io.jmix.data.PersistenceHints;
import io.jmix.saml.SamlProperties;
import io.jmix.saml.mapper.role.SamlAssertionRolesMapper;
import io.jmix.saml.user.JmixSamlUserDetails;
import io.jmix.saml.util.SamlAssertionUtils;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import io.jmix.security.role.assignment.RoleAssignmentRoleType;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import org.jspecify.annotations.NullMarked;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml2.Saml2Exception;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml5AuthenticationProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Implementation of the {@link SamlUserMapper} that maps the external user object to the persistent user
 * and also stores the user and optionally their role assignment to the database.
 * <p>
 * If role assignment synchronization is enabled (see {@link #setSynchronizeRoleAssignments(boolean)}), the
 * identity provider owns <b>all</b> role assignments of the synchronized user: on every login the stored
 * assignments are replaced with the roles derived from the SAML assertion. Role assignments granted manually
 * (e.g. by an administrator in the UI) are removed by the next login and therefore must not be combined with
 * the synchronization mode.
 *
 * @param <T> class of Jmix user
 */
@NullMarked
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
        checkUsernameIsNotReserved(username);
        T jmixUserDetails;
        try {
            UserDetails userDetails = userRepository.loadUserByUsername(username);
            Class<T> applicationUserClass = getApplicationUserClass();
            if (!applicationUserClass.isInstance(userDetails)) {
                throw new Saml2Exception("User '" + username + "' loaded from the user repository is an instance "
                        + "of " + userDetails.getClass().getName() + " which is not compatible with the application "
                        + "user class " + applicationUserClass.getName());
            }
            jmixUserDetails = applicationUserClass.cast(userDetails);
        } catch (UsernameNotFoundException e) {
            log.debug("User with login {} wasn't found in user repository", username);
            jmixUserDetails = dataManager.create(getApplicationUserClass());
            setUsernameToNewUser(jmixUserDetails, username);
        }
        return jmixUserDetails;
    }

    /**
     * Throws an exception if the given username belongs to the built-in system or anonymous user. Otherwise, an
     * identity provider asserting such a username would map the external user onto a built-in one and persist it.
     */
    protected void checkUsernameIsNotReserved(String username) {
        if (username.equals(userRepository.getSystemUser().getUsername())
                || username.equals(userRepository.getAnonymousUser().getUsername())) {
            throw new Saml2Exception("Username '" + username + "' asserted by the identity provider is reserved "
                    + "for a built-in user");
        }
    }

    /**
     * Sets the username to a newly created user instance. The default implementation writes the {@code username}
     * entity attribute. Override this method if the application user class stores the login differently.
     */
    protected void setUsernameToNewUser(T jmixUser, String username) {
        EntityValues.setValue(jmixUser, "username", username);
    }

    @Override
    protected void populateUserAuthorities(Assertion assertion, T jmixUser) {
        Collection<? extends GrantedAuthority> grantedAuthorities = rolesMapper.toGrantedAuthorities(assertion);
        jmixUser.setAuthorities(grantedAuthorities);
    }

    @Override
    protected void performAdditionalModifications(Assertion assertion, OpenSaml5AuthenticationProvider.ResponseToken responseToken, T jmixUser) {
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
     * <p>
     * When enabled, the identity provider becomes the single source of truth for <b>all</b> role assignments of
     * the user: on every login the assignments stored in the database are replaced with the roles derived from
     * the SAML assertion. Any assignment granted through other means (e.g. manually by an administrator) is
     * removed, because the {@code SEC_ROLE_ASSIGNMENT} table has no marker distinguishing the origin of an
     * assignment. Do not assign roles manually to users synchronized with this mode.
     */
    public void setSynchronizeRoleAssignments(boolean synchronizeRoleAssignments) {
        this.synchronizeRoleAssignments = synchronizeRoleAssignments;
    }

    private String assignmentKey(RoleAssignmentEntity assignment) {
        return assignment.getRoleCode() + ":" + assignment.getRoleType();
    }
}
