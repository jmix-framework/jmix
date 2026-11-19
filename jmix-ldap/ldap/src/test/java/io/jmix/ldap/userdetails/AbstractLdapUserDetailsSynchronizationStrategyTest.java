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

import io.jmix.core.SaveContext;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.security.UserRepository;
import io.jmix.data.PersistenceHints;
import io.jmix.ldap.LdapProperties;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import io.jmix.security.role.assignment.RoleAssignment;
import io.jmix.security.role.assignment.RoleAssignmentRoleType;
import io.jmix.security.role.assignment.RoleAssignmentSource;
import io.jmix.security.role.assignment.RoleAssignmentSynchronizer;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import org.jspecify.annotations.NullMarked;
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
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AbstractLdapUserDetailsSynchronizationStrategyTest {

    private static final String USERNAME = "alice";
    private static final String SHARED_CODE = "qa-shared-code";
    private static final String ROLE_PREFIX = "ROLE_";
    private static final String ROW_LEVEL_ROLE_PREFIX = "ROW_LEVEL_ROLE_";

    private UnconstrainedDataManager dataManager;
    private LdapProperties ldapProperties;
    private RoleAssignmentSynchronizer synchronizer;
    private TestStrategy strategy;

    @BeforeEach
    void setUp() {
        dataManager = mock(UnconstrainedDataManager.class);
        UserRepository userRepository = mock(UserRepository.class);
        JmixLdapGrantedAuthoritiesMapper authoritiesMapper = mock(JmixLdapGrantedAuthoritiesMapper.class);
        ldapProperties = mock(LdapProperties.class);
        RoleGrantedAuthorityUtils roleGrantedAuthorityUtils = mock(RoleGrantedAuthorityUtils.class);
        synchronizer = mock(RoleAssignmentSynchronizer.class);

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
        when(dataManager.create(RoleAssignmentEntity.class)).thenAnswer(invocation -> new RoleAssignmentEntity());

        strategy = new TestStrategy();
        strategy.dataManager = dataManager;
        strategy.userRepository = userRepository;
        strategy.authoritiesMapper = authoritiesMapper;
        strategy.ldapProperties = ldapProperties;
        strategy.roleGrantedAuthorityUtils = roleGrantedAuthorityUtils;
        strategy.roleAssignmentSynchronizer = synchronizer;
    }

    @Test
    void testSynchronizesGrantedRolesAsLdap() {
        // Both a resource and a row-level role exist for the shared code: the role type is part of the key.
        Set<GrantedAuthority> granted = Set.of(
                new SimpleGrantedAuthority(ROLE_PREFIX + SHARED_CODE),
                new SimpleGrantedAuthority(ROW_LEVEL_ROLE_PREFIX + SHARED_CODE));

        strategy.synchronizeUserDetails(mock(DirContextOperations.class), USERNAME, granted);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<RoleAssignment>> assignments = ArgumentCaptor.forClass(Collection.class);
        verify(synchronizer).synchronize(eq(USERNAME), assignments.capture(), eq(RoleAssignmentSource.LDAP),
                any(SaveContext.class));
        assertEquals(2, assignments.getValue().size());
        assertEquals(Set.of(
                        assignmentKey(USERNAME, SHARED_CODE, RoleAssignmentRoleType.RESOURCE),
                        assignmentKey(USERNAME, SHARED_CODE, RoleAssignmentRoleType.ROW_LEVEL)),
                assignments.getValue().stream()
                        .map(assignment -> assignmentKey(assignment.getUsername(), assignment.getRoleCode(),
                                assignment.getRoleType()))
                        .collect(Collectors.toSet()));
    }

    @Test
    void testKeepsSoftDeletion() {
        strategy.synchronizeUserDetails(mock(DirContextOperations.class), USERNAME,
                Set.of(new SimpleGrantedAuthority(ROLE_PREFIX + "manager")));

        ArgumentCaptor<SaveContext> saveContext = ArgumentCaptor.forClass(SaveContext.class);
        verify(dataManager).save(saveContext.capture());
        assertFalse(saveContext.getValue().getHints().containsKey(PersistenceHints.SOFT_DELETION));
    }

    @Test
    void testSynchronizationOffDoesNotSynchronize() {
        when(ldapProperties.getSynchronizeRoleAssignments()).thenReturn(false);

        strategy.synchronizeUserDetails(mock(DirContextOperations.class), USERNAME,
                Set.of(new SimpleGrantedAuthority(ROLE_PREFIX + "manager")));

        verifyNoInteractions(synchronizer);
    }

    @Test
    void testBuiltRoleAssignmentsHaveOwnSource() {
        // The method is an extension point: an override that saves its entities must not store them as manual.
        Collection<RoleAssignmentEntity> entities =
                strategy.buildRoleAssignments(Set.of(new SimpleGrantedAuthority(ROLE_PREFIX + "manager")), USERNAME);

        assertEquals(1, entities.size());
        assertEquals(RoleAssignmentSource.LDAP, entities.iterator().next().getSource());
    }

    private String assignmentKey(String username, String roleCode, String roleType) {
        return username + "/" + roleCode + "/" + roleType;
    }

    @NullMarked
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
