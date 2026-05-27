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

package io.jmix.ldap.userdetails;

import io.jmix.core.FluentLoader;
import io.jmix.core.SaveContext;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.security.UserRepository;
import io.jmix.ldap.LdapProperties;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import io.jmix.security.role.assignment.RoleAssignmentRoleType;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AbstractLdapUserDetailsSynchronizationStrategyTest {

    private static final String USERNAME = "alice";
    private static final String SHARED_CODE = "qa-shared-code";
    private static final String ROLE_PREFIX = "ROLE_";
    private static final String ROW_LEVEL_ROLE_PREFIX = "ROW_LEVEL_ROLE_";

    private UnconstrainedDataManager dataManager;
    private UserRepository userRepository;
    private JmixLdapGrantedAuthoritiesMapper authoritiesMapper;
    private LdapProperties ldapProperties;
    private RoleGrantedAuthorityUtils roleGrantedAuthorityUtils;

    private TestStrategy strategy;

    @BeforeEach
    void setUp() {
        dataManager = mock(UnconstrainedDataManager.class);
        userRepository = mock(UserRepository.class);
        authoritiesMapper = mock(JmixLdapGrantedAuthoritiesMapper.class);
        ldapProperties = mock(LdapProperties.class);
        roleGrantedAuthorityUtils = mock(RoleGrantedAuthorityUtils.class);

        when(ldapProperties.getSynchronizeRoleAssignments()).thenReturn(true);
        when(roleGrantedAuthorityUtils.getDefaultRolePrefix()).thenReturn(ROLE_PREFIX);
        when(roleGrantedAuthorityUtils.getDefaultRowLevelRolePrefix()).thenReturn(ROW_LEVEL_ROLE_PREFIX);

        when(authoritiesMapper.mapAuthorities(anyCollection())).thenAnswer(invocation -> {
            Collection<? extends GrantedAuthority> authorities = invocation.getArgument(0);
            return new HashSet<>(authorities);
        });

        UserDetails existingUser = User.withUsername(USERNAME)
                .password("{noop}password")
                .authorities(Collections.emptyList())
                .build();
        when(userRepository.loadUserByUsername(USERNAME)).thenReturn(existingUser);

        when(dataManager.create(RoleAssignmentEntity.class))
                .thenAnswer(invocation -> {
                    RoleAssignmentEntity entity = new RoleAssignmentEntity();
                    entity.setId(UUID.randomUUID());
                    return entity;
                });

        strategy = new TestStrategy();
        strategy.dataManager = dataManager;
        strategy.userRepository = userRepository;
        strategy.authoritiesMapper = authoritiesMapper;
        strategy.ldapProperties = ldapProperties;
        strategy.roleGrantedAuthorityUtils = roleGrantedAuthorityUtils;
    }

    @Test
    void testMissingTypedAssignmentWhenBothRolesShareCode() {
        // Existing assignment: (qa-shared-code, resource).
        RoleAssignmentEntity existing = roleAssignment(SHARED_CODE, RoleAssignmentRoleType.RESOURCE);
        mockExistingAssignments(List.of(existing));

        // LDAP grants both authorities for the shared code (resource + row-level roles both exist in the app).
        Set<GrantedAuthority> granted = Set.of(
                new SimpleGrantedAuthority(ROLE_PREFIX + SHARED_CODE),
                new SimpleGrantedAuthority(ROW_LEVEL_ROLE_PREFIX + SHARED_CODE));

        strategy.synchronizeUserDetails(null, USERNAME, granted);

        SaveContext saveContext = captureSaveContext();

        List<RoleAssignmentEntity> toRemove = filterRoleAssignments(saveContext.getEntitiesToRemove());
        List<RoleAssignmentEntity> toSave = filterRoleAssignments(saveContext.getEntitiesToSave());

        assertTrue(toRemove.isEmpty(),
                "Existing resource-role assignment must not be removed when LDAP still grants the same code as a resource role");

        Map<String, String> savedByCodeType = toSave.stream()
                .collect(Collectors.toMap(this::assignmentKey, RoleAssignmentEntity::getRoleType));
        assertEquals(1, toSave.size(),
                "Exactly one new row-level assignment must be created for the shared code");
        assertEquals(RoleAssignmentRoleType.ROW_LEVEL,
                savedByCodeType.get(assignmentKey(SHARED_CODE, RoleAssignmentRoleType.ROW_LEVEL)),
                "Created assignment must have row_level type");
    }

    @Test
    void testStaleResourceRoleRetainedWhenOnlyRowLevelGranted() {
        // Existing assignment: (qa-shared-code, resource).
        RoleAssignmentEntity existing = roleAssignment(SHARED_CODE, RoleAssignmentRoleType.RESOURCE);
        mockExistingAssignments(List.of(existing));

        // LDAP grants only the row-level authority for the shared code.
        Set<GrantedAuthority> granted = Set.of(
                new SimpleGrantedAuthority(ROW_LEVEL_ROLE_PREFIX + SHARED_CODE));

        strategy.synchronizeUserDetails(null, USERNAME, granted);

        SaveContext saveContext = captureSaveContext();

        List<RoleAssignmentEntity> toRemove = filterRoleAssignments(saveContext.getEntitiesToRemove());
        List<RoleAssignmentEntity> toSave = filterRoleAssignments(saveContext.getEntitiesToSave());

        assertEquals(1, toRemove.size(),
                "Stale resource-role assignment must be removed when LDAP no longer grants it as a resource role");
        assertEquals(SHARED_CODE, toRemove.get(0).getRoleCode());
        assertEquals(RoleAssignmentRoleType.RESOURCE, toRemove.get(0).getRoleType());

        assertEquals(1, toSave.size(),
                "Row-level assignment with the same code must be created");
        assertEquals(SHARED_CODE, toSave.get(0).getRoleCode());
        assertEquals(RoleAssignmentRoleType.ROW_LEVEL, toSave.get(0).getRoleType());
    }

    @Test
    void testHappyPathDifferentCodesAndTypes() {
        // Existing assignments: (manager, resource) and (region-eu, row_level).
        RoleAssignmentEntity existingManager = roleAssignment("manager", RoleAssignmentRoleType.RESOURCE);
        RoleAssignmentEntity existingRegion = roleAssignment("region-eu", RoleAssignmentRoleType.ROW_LEVEL);
        mockExistingAssignments(List.of(existingManager, existingRegion));

        // LDAP grants: keep manager (resource), drop region-eu (row-level), add accountant (resource) and region-us (row-level).
        Set<GrantedAuthority> granted = Set.of(
                new SimpleGrantedAuthority(ROLE_PREFIX + "manager"),
                new SimpleGrantedAuthority(ROLE_PREFIX + "accountant"),
                new SimpleGrantedAuthority(ROW_LEVEL_ROLE_PREFIX + "region-us"));

        strategy.synchronizeUserDetails(null, USERNAME, granted);

        SaveContext saveContext = captureSaveContext();

        Set<String> removedKeys = filterRoleAssignments(saveContext.getEntitiesToRemove()).stream()
                .map(this::assignmentKey)
                .collect(Collectors.toSet());
        Set<String> savedKeys = filterRoleAssignments(saveContext.getEntitiesToSave()).stream()
                .map(this::assignmentKey)
                .collect(Collectors.toSet());

        assertEquals(Set.of(assignmentKey("region-eu", RoleAssignmentRoleType.ROW_LEVEL)), removedKeys);
        assertEquals(Set.of(
                        assignmentKey("accountant", RoleAssignmentRoleType.RESOURCE),
                        assignmentKey("region-us", RoleAssignmentRoleType.ROW_LEVEL)),
                savedKeys);
    }

    @SuppressWarnings("unchecked")
    private void mockExistingAssignments(List<RoleAssignmentEntity> existing) {
        FluentLoader<RoleAssignmentEntity> loader = mock(FluentLoader.class);
        FluentLoader.ByQuery<RoleAssignmentEntity> byQuery = mock(FluentLoader.ByQuery.class);

        when(dataManager.load(RoleAssignmentEntity.class)).thenReturn(loader);
        when(loader.query(anyString())).thenReturn(byQuery);
        when(byQuery.parameter(anyString(), any())).thenReturn(byQuery);
        when(byQuery.list()).thenReturn(existing);
    }

    private SaveContext captureSaveContext() {
        ArgumentCaptor<SaveContext> captor = ArgumentCaptor.forClass(SaveContext.class);
        verify(dataManager).save(captor.capture());
        return captor.getValue();
    }

    private List<RoleAssignmentEntity> filterRoleAssignments(Collection<?> entities) {
        return entities.stream()
                .filter(RoleAssignmentEntity.class::isInstance)
                .map(RoleAssignmentEntity.class::cast)
                .collect(Collectors.toList());
    }

    private RoleAssignmentEntity roleAssignment(String code, String type) {
        RoleAssignmentEntity entity = new RoleAssignmentEntity();
        entity.setId(UUID.randomUUID());
        entity.setUsername(USERNAME);
        entity.setRoleCode(code);
        entity.setRoleType(type);
        return entity;
    }

    private String assignmentKey(RoleAssignmentEntity assignment) {
        return assignmentKey(assignment.getRoleCode(), assignment.getRoleType());
    }

    private String assignmentKey(String code, String type) {
        return code + ":" + type;
    }

    private static class TestStrategy extends AbstractLdapUserDetailsSynchronizationStrategy<UserDetails> {

        @Override
        protected Class<UserDetails> getUserClass() {
            return UserDetails.class;
        }

        @Override
        protected void mapUserDetailsAttributes(UserDetails userDetails, DirContextOperations ctx) {
        }
    }
}
