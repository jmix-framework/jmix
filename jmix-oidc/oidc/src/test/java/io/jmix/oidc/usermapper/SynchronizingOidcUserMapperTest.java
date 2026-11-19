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

import io.jmix.core.SaveContext;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.security.UserRepository;
import io.jmix.data.PersistenceHints;
import io.jmix.oidc.OidcProperties;
import io.jmix.oidc.claimsmapper.ClaimsRolesMapper;
import io.jmix.oidc.jwt.JmixJwtAuthenticationConverter;
import io.jmix.oidc.user.JmixOidcUserEntity;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import io.jmix.security.role.assignment.RoleAssignment;
import io.jmix.security.role.assignment.RoleAssignmentRoleType;
import io.jmix.security.role.assignment.RoleAssignmentSource;
import io.jmix.security.role.assignment.RoleAssignmentSynchronizer;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SynchronizingOidcUserMapperTest {

    static final String USERNAME = "john";

    UnconstrainedDataManager dataManager;
    RoleAssignmentSynchronizer synchronizer;
    TestMapper mapper;

    @BeforeEach
    void setUp() {
        dataManager = mock(UnconstrainedDataManager.class);
        when(dataManager.create(TestUser.class)).thenAnswer(invocation -> new TestUser());
        when(dataManager.create(RoleAssignmentEntity.class)).thenAnswer(invocation -> new RoleAssignmentEntity());

        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.loadUserByUsername(USERNAME)).thenThrow(new UsernameNotFoundException(USERNAME));

        ClaimsRolesMapper claimsRolesMapper = mock(ClaimsRolesMapper.class);
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_manager")))
                .when(claimsRolesMapper).toGrantedAuthorities(any());

        RoleGrantedAuthorityUtils roleGrantedAuthorityUtils = mock(RoleGrantedAuthorityUtils.class);
        when(roleGrantedAuthorityUtils.getDefaultRolePrefix()).thenReturn("ROLE_");
        when(roleGrantedAuthorityUtils.getDefaultRowLevelRolePrefix()).thenReturn("ROW_LEVEL_ROLE_");

        synchronizer = mock(RoleAssignmentSynchronizer.class);

        mapper = new TestMapper(dataManager, userRepository, claimsRolesMapper, roleGrantedAuthorityUtils);
        mapper.roleAssignmentSynchronizer = synchronizer;
    }

    @Test
    void toJmixUser_synchronizationOn_synchronizesAssertedRolesAsOidc() {
        mapper.setSynchronizeRoleAssignments(true);

        mapper.toJmixUser(oidcUser());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<RoleAssignment>> assignments = ArgumentCaptor.forClass(Collection.class);
        verify(synchronizer).synchronize(eq(USERNAME), assignments.capture(), eq(RoleAssignmentSource.OIDC),
                any(SaveContext.class));
        assertThat(assignments.getValue())
                .extracting(RoleAssignment::getUsername, RoleAssignment::getRoleCode, RoleAssignment::getRoleType)
                .containsExactly(tuple(USERNAME, "manager", RoleAssignmentRoleType.RESOURCE));
    }

    @Test
    void toJmixUser_synchronizationOn_savesWithoutSoftDeletion() {
        mapper.setSynchronizeRoleAssignments(true);

        mapper.toJmixUser(oidcUser());

        ArgumentCaptor<SaveContext> saveContext = ArgumentCaptor.forClass(SaveContext.class);
        verify(dataManager).save(saveContext.capture());
        assertThat(saveContext.getValue().getHints()).containsEntry(PersistenceHints.SOFT_DELETION, false);
    }

    @Test
    void toJmixUser_manualAuthorities_areAddedOnce() {
        mapper.setSynchronizeRoleAssignments(true);
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_auditor"), new SimpleGrantedAuthority("ROLE_manager")))
                .when(synchronizer).synchronize(anyString(), anyCollection(), anyString(), any(SaveContext.class));

        TestUser user = mapper.toJmixUser(oidcUser());

        assertThat(user.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_manager", "ROLE_auditor");
    }

    @Test
    void toJmixUser_synchronizationOff_addsManualAuthoritiesOnly() {
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_auditor")))
                .when(synchronizer).getManualAuthorities(USERNAME);

        TestUser user = mapper.toJmixUser(oidcUser());

        assertThat(user.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_manager", "ROLE_auditor");
        verify(synchronizer, never()).synchronize(anyString(), anyCollection(), anyString(), any(SaveContext.class));
    }

    @Test
    void toJmixUser_withoutSynchronizer_keepsAssertedAuthorities() {
        mapper.roleAssignmentSynchronizer = null;

        TestUser user = mapper.toJmixUser(oidcUser());

        assertThat(user.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_manager");
    }

    @Test
    void toJmixUser_withoutSynchronizer_synchronizationOn_fails() {
        mapper.roleAssignmentSynchronizer = null;
        mapper.setSynchronizeRoleAssignments(true);

        assertThatThrownBy(() -> mapper.toJmixUser(oidcUser()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("jmix-security-data");
    }

    @Test
    void jwtConverter_manualAuthorities_reachToken() {
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_auditor")))
                .when(synchronizer).getManualAuthorities(USERNAME);
        JmixJwtAuthenticationConverter converter =
                new JmixJwtAuthenticationConverter(mapper, mock(OidcProperties.class));
        converter.setUsernameClaimName("sub");
        Instant now = Instant.now();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(USERNAME)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(60))
                .build();

        assertThat(Objects.requireNonNull(converter.convert(jwt)).getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_auditor");
    }

    /**
     * Spring introspects the fields and methods of a mapper bean, which loads their types. The OIDC add-on depends
     * on jmix-security-data at compile time only, so no field or method of the mapper may be typed with its classes.
     */
    @Test
    void mapperMembers_doNotReferenceSecurityData() {
        List<String> typeNames = new ArrayList<>();
        for (Field field : SynchronizingOidcUserMapper.class.getDeclaredFields()) {
            typeNames.add(field.getGenericType().getTypeName());
        }
        for (Method method : SynchronizingOidcUserMapper.class.getDeclaredMethods()) {
            typeNames.add(method.getReturnType().getName());
            for (Class<?> parameterType : method.getParameterTypes()) {
                typeNames.add(parameterType.getName());
            }
        }

        assertThat(typeNames).noneMatch(name -> name.contains("io.jmix.securitydata"));
    }

    OidcUser oidcUser() {
        Instant now = Instant.now();
        OidcIdToken idToken = OidcIdToken.withTokenValue("token")
                .subject(USERNAME)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(60))
                .build();
        return new DefaultOidcUser(List.of(), idToken);
    }

    static class TestMapper extends SynchronizingOidcUserMapper<TestUser> {

        TestMapper(UnconstrainedDataManager dataManager, UserRepository userRepository,
                   ClaimsRolesMapper claimsRolesMapper, RoleGrantedAuthorityUtils roleGrantedAuthorityUtils) {
            super(dataManager, userRepository, claimsRolesMapper, roleGrantedAuthorityUtils);
        }

        @Override
        protected Class<TestUser> getApplicationUserClass() {
            return TestUser.class;
        }

        @Override
        protected void populateUserAttributes(OidcUser oidcUser, TestUser jmixUser) {
            jmixUser.setUsername(oidcUser.getName());
        }
    }

    public static class TestUser extends JmixOidcUserEntity {

        private String username;
        private Collection<? extends GrantedAuthority> authorities = List.of();

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
            this.authorities = authorities;
        }

        @Override
        public String getPassword() {
            return "";
        }

        @Override
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }
}
