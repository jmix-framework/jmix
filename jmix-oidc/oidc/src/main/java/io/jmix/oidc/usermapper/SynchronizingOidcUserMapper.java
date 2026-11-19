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

package io.jmix.oidc.usermapper;

import com.google.common.base.Strings;
import io.jmix.core.SaveContext;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.security.UserRepository;
import io.jmix.data.PersistenceHints;
import io.jmix.oidc.claimsmapper.ClaimsRolesMapper;
import io.jmix.oidc.user.JmixOidcUser;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import io.jmix.security.role.assignment.RoleAssignment;
import io.jmix.security.role.assignment.RoleAssignmentRoleType;
import io.jmix.security.role.assignment.RoleAssignmentSource;
import io.jmix.security.role.assignment.RoleAssignmentSynchronizer;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of the {@link OidcUserMapper} that not only maps the external user object to the persistent user
 * entity, but also stores the user and optionally their role assignment to the database.
 *
 * <p>
 * The role assignments are stored with the {@link RoleAssignmentSource#OIDC} source. Role assignments made by hand
 * ({@link RoleAssignmentSource#MANUAL}) survive the synchronization and are added to the user's authorities.
 *
 * @param <T>
 */
@NullMarked
public abstract class SynchronizingOidcUserMapper<T extends JmixOidcUser> extends BaseOidcUserMapper<T> {

    private static final Logger log = LoggerFactory.getLogger(SynchronizingOidcUserMapper.class);

    protected UnconstrainedDataManager dataManager;

    protected UserRepository userRepository;

    protected ClaimsRolesMapper claimsRolesMapper;

    protected boolean synchronizeRoleAssignments;

    protected RoleGrantedAuthorityUtils roleGrantedAuthorityUtils;

    /**
     * Synchronizer of stored role assignments. Absent if the application has no jmix-security-data module.
     */
    @Autowired(required = false)
    @Nullable
    protected RoleAssignmentSynchronizer roleAssignmentSynchronizer;

    public SynchronizingOidcUserMapper(UnconstrainedDataManager dataManager,
                                       UserRepository userRepository,
                                       ClaimsRolesMapper claimsRolesMapper,
                                       RoleGrantedAuthorityUtils roleGrantedAuthorityUtils) {
        this.dataManager = dataManager;
        this.userRepository = userRepository;
        this.claimsRolesMapper = claimsRolesMapper;
        this.roleGrantedAuthorityUtils = roleGrantedAuthorityUtils;
    }

    /**
     * Returns a class of the user used by the application. This user is set to the security context.
     */
    protected abstract Class<T> getApplicationUserClass();

    /**
     * Extracts username from the {@code oidcUser}
     */
    @Override
    protected String getOidcUserUsername(OidcUser oidcUser) {
        return oidcUser.getName();
    }

    @Override
    protected T initJmixUser(OidcUser oidcUser) {
        String username = getOidcUserUsername(oidcUser);
        T jmixUserDetails;
        try {
            jmixUserDetails = (T) userRepository.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            log.info("User with login {} wasn't found in user repository", username);
            jmixUserDetails = dataManager.create(getApplicationUserClass());
        }
        return jmixUserDetails;
    }

    @Override
    protected void populateUserAuthorities(OidcUser oidcUser, T jmixUser) {
        Collection<? extends GrantedAuthority> grantedAuthorities = claimsRolesMapper.toGrantedAuthorities(oidcUser.getClaims());
        jmixUser.setAuthorities(grantedAuthorities);
    }

    @Override
    protected void performAdditionalModifications(OidcUser oidcUser, T jmixUser) {
        super.performAdditionalModifications(oidcUser, jmixUser);
        saveJmixUserAndRoleAssignments(oidcUser, jmixUser);
    }

    /**
     * Saves the user and, if {@link #setSynchronizeRoleAssignments(boolean) enabled}, synchronizes their stored role
     * assignments with the roles derived from the {@code oidcUser} (see {@link RoleAssignmentSynchronizer}). Then adds
     * the authorities of the user's {@link RoleAssignmentSource#MANUAL manual} role assignments to the user, whether
     * the synchronization is enabled or not.
     */
    protected void saveJmixUserAndRoleAssignments(OidcUser oidcUser, T jmixUser) {
        SaveContext saveContext = new SaveContext();
        String username = getOidcUserUsername(oidcUser);

        if (roleAssignmentSynchronizer == null) {
            if (synchronizeRoleAssignments) {
                throw new IllegalStateException("Role assignment synchronization requires the jmix-security-data module");
            }
        } else {
            Collection<GrantedAuthority> manualAuthorities;
            if (synchronizeRoleAssignments) {
                //disable soft-deletion to completely remove role assignment records from the database
                saveContext.setHint(PersistenceHints.SOFT_DELETION, false);
                List<RoleAssignment> assertedAssignments = new ArrayList<>();
                for (RoleAssignmentEntity entity : buildRoleAssignmentEntities(username, jmixUser.getAuthorities())) {
                    assertedAssignments.add(new RoleAssignment(username, entity.getRoleCode(), entity.getRoleType()));
                }
                manualAuthorities = roleAssignmentSynchronizer.synchronize(username, assertedAssignments,
                        getRoleAssignmentSource(), saveContext);
            } else {
                manualAuthorities = roleAssignmentSynchronizer.getManualAuthorities(username);
            }
            addManualAuthorities(jmixUser, manualAuthorities);
        }

        saveContext.saving(jmixUser);

        //persist user details and roles if needed
        dataManager.save(saveContext);
    }

    /**
     * Adds the authorities of the user's manual role assignments to the authorities derived from the
     * {@code oidcUser}, skipping those the user already has.
     */
    protected void addManualAuthorities(T jmixUser, Collection<GrantedAuthority> manualAuthorities) {
        if (manualAuthorities.isEmpty()) {
            return;
        }
        List<GrantedAuthority> authorities = new ArrayList<>(jmixUser.getAuthorities());
        Set<@Nullable String> authorityNames = new HashSet<>();
        for (GrantedAuthority authority : authorities) {
            authorityNames.add(authority.getAuthority());
        }
        for (GrantedAuthority authority : manualAuthorities) {
            if (authorityNames.add(authority.getAuthority())) {
                authorities.add(authority);
            }
        }
        jmixUser.setAuthorities(authorities);
    }

    /**
     * Returns the source that the synchronized role assignments are stored with.
     */
    protected String getRoleAssignmentSource() {
        return RoleAssignmentSource.OIDC;
    }

    protected Collection<RoleAssignmentEntity> buildRoleAssignmentEntities(String username, Collection<? extends GrantedAuthority> grantedAuthorities) {
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
                roleAssignmentEntity.setSource(getRoleAssignmentSource());
                roleAssignmentEntities.add(roleAssignmentEntity);
            }
        }
        return roleAssignmentEntities;
    }

    /**
     * Enables role assignment entities synchronization. If true then role assignment entities will be stored to the
     * database.
     * <p>
     * The synchronization removes only the role assignments it owns: those with the {@link RoleAssignmentSource#OIDC}
     * source and those stored before role assignments had a source. Role assignments made by hand are kept.
     */
    public void setSynchronizeRoleAssignments(boolean synchronizeRoleAssignments) {
        this.synchronizeRoleAssignments = synchronizeRoleAssignments;
    }

    public boolean isSynchronizeRoleAssignments() {
        return synchronizeRoleAssignments;
    }
}
