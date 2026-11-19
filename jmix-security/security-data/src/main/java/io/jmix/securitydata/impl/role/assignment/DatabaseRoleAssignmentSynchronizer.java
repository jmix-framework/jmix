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

package io.jmix.securitydata.impl.role.assignment;

import io.jmix.core.SaveContext;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.common.util.Preconditions;
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
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Role assignment synchronizer that works with the role assignments stored in the database.
 */
@NullMarked
@Component("sec_DatabaseRoleAssignmentSynchronizer")
public class DatabaseRoleAssignmentSynchronizer implements RoleAssignmentSynchronizer {

    private static final Logger log = LoggerFactory.getLogger(DatabaseRoleAssignmentSynchronizer.class);

    @Autowired
    protected UnconstrainedDataManager dataManager;

    @Autowired
    protected RoleGrantedAuthorityUtils roleGrantedAuthorityUtils;

    @Override
    public Collection<GrantedAuthority> synchronize(String username, Collection<RoleAssignment> assertedAssignments,
                                                    String source, SaveContext saveContext) {
        Preconditions.checkNotNullArgument(username, "username is null");
        Preconditions.checkNotNullArgument(assertedAssignments, "assertedAssignments is null");
        Preconditions.checkNotNullArgument(source, "source is null");
        Preconditions.checkNotNullArgument(saveContext, "saveContext is null");
        if (RoleAssignmentSource.MANUAL.equals(source)) {
            throw new IllegalArgumentException("Role assignments cannot be synchronized as manual ones");
        }

        Set<String> assertedKeys = new HashSet<>();
        for (RoleAssignment assignment : assertedAssignments) {
            assertedKeys.add(assignmentKey(assignment.getRoleCode(), assignment.getRoleType()));
        }

        List<RoleAssignmentEntity> storedAssignments = loadRoleAssignments(username);
        Set<String> storedKeys = new HashSet<>();
        for (RoleAssignmentEntity stored : storedAssignments) {
            String key = assignmentKey(stored.getRoleCode(), stored.getRoleType());
            storedKeys.add(key);
            boolean asserted = assertedKeys.contains(key);
            String storedSource = stored.getSource();
            if (storedSource == null && asserted) {
                log.debug("Taking over role assignment {} of user '{}' as {}", key, username, source);
                stored.setSource(source);
                saveContext.saving(stored);
            } else if ((storedSource == null || source.equals(storedSource)) && !asserted) {
                log.debug("Removing role assignment {} of user '{}'", key, username);
                saveContext.removing(stored);
            }
        }

        for (RoleAssignment assignment : assertedAssignments) {
            String key = assignmentKey(assignment.getRoleCode(), assignment.getRoleType());
            if (storedKeys.add(key)) {
                log.debug("Adding role assignment {} to user '{}' as {}", key, username, source);
                RoleAssignmentEntity entity = dataManager.create(RoleAssignmentEntity.class);
                entity.setUsername(username);
                entity.setRoleCode(assignment.getRoleCode());
                entity.setRoleType(assignment.getRoleType());
                entity.setSource(source);
                saveContext.saving(entity);
            }
        }

        return createManualAuthorities(storedAssignments);
    }

    @Override
    public Collection<GrantedAuthority> getManualAuthorities(String username) {
        Preconditions.checkNotNullArgument(username, "username is null");
        return createManualAuthorities(loadRoleAssignments(username));
    }

    protected List<RoleAssignmentEntity> loadRoleAssignments(String username) {
        return dataManager.load(RoleAssignmentEntity.class)
                .query("e.username = :username")
                .parameter("username", username)
                .list();
    }

    protected Collection<GrantedAuthority> createManualAuthorities(Collection<RoleAssignmentEntity> assignments) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (RoleAssignmentEntity assignment : assignments) {
            if (RoleAssignmentSource.MANUAL.equals(assignment.getSource())) {
                GrantedAuthority authority = createAuthority(assignment);
                if (authority != null) {
                    authorities.add(authority);
                }
            }
        }
        return authorities;
    }

    @Nullable
    protected GrantedAuthority createAuthority(RoleAssignmentEntity assignment) {
        if (RoleAssignmentRoleType.RESOURCE.equals(assignment.getRoleType())) {
            return roleGrantedAuthorityUtils.createResourceRoleGrantedAuthority(assignment.getRoleCode());
        } else if (RoleAssignmentRoleType.ROW_LEVEL.equals(assignment.getRoleType())) {
            return roleGrantedAuthorityUtils.createRowLevelRoleGrantedAuthority(assignment.getRoleCode());
        }
        return null;
    }

    private String assignmentKey(String roleCode, String roleType) {
        return roleCode + ":" + roleType;
    }
}
