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

import io.jmix.core.SaveContext;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.security.UserRepository;
import io.jmix.data.PersistenceHints;
import io.jmix.saml.SamlProperties;
import io.jmix.saml.mapper.role.SamlAssertionRolesMapper;
import io.jmix.saml.user.JmixSamlUserEntity;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import io.jmix.security.role.assignment.RoleAssignment;
import io.jmix.security.role.assignment.RoleAssignmentRoleType;
import io.jmix.security.role.assignment.RoleAssignmentSource;
import io.jmix.security.role.assignment.RoleAssignmentSynchronizer;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.impl.AssertionBuilder;
import org.opensaml.saml.saml2.core.impl.NameIDBuilder;
import org.opensaml.saml.saml2.core.impl.SubjectBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml2.Saml2Exception;
import org.springframework.security.saml2.core.OpenSamlInitializationService;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml5AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationToken;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

class SynchronizingSamlUserMapperTest {

    @BeforeAll
    static void initOpenSaml() {
        OpenSamlInitializationService.initialize();
    }

    @Test
    void testNewUserGetsUsernameAndIsSaved() {
        // The base class must set the username to a newly created user, otherwise the user is persisted
        // with a null username unless every subclass remembers to do it.
        TestSynchronizingMapper mapper = createMapper();
        when(mapper.userRepository.loadUserByUsername("john")).thenThrow(new UsernameNotFoundException("john"));

        TestAppUser user = mapper.toJmixUser(assertion("john"), responseToken("okta"));

        assertThat(user.getUsername()).isEqualTo("john");
        verify(mapper.dataManager).save(any(io.jmix.core.SaveContext.class));
    }

    @Test
    void testExistingUserIsReturnedFromRepository() {
        TestSynchronizingMapper mapper = createMapper();
        TestAppUser existingUser = new TestAppUser();
        existingUser.setUsername("john");
        when(mapper.userRepository.loadUserByUsername("john")).thenReturn(existingUser);

        TestAppUser user = mapper.toJmixUser(assertion("john"), responseToken("okta"));

        assertThat(user).isSameAs(existingUser);
    }

    @Test
    void testIncompatibleUserTypeFailsWithDiagnosableError() {
        // A composite UserRepository may return a UserDetails of another type; that must not surface as
        // an opaque ClassCastException.
        TestSynchronizingMapper mapper = createMapper();
        when(mapper.userRepository.loadUserByUsername("john"))
                .thenReturn(User.withUsername("john").password("").build());

        assertThatThrownBy(() -> mapper.toJmixUser(assertion("john"), responseToken("okta")))
                .isInstanceOf(Saml2Exception.class)
                .hasMessageContaining("not compatible");
    }

    @Test
    void testReservedUsernamesAreRejected() {
        // An IdP-controlled NameID equal to a built-in username must not map onto the built-in user.
        TestSynchronizingMapper mapper = createMapper();

        assertThatThrownBy(() -> mapper.toJmixUser(assertion("system"), responseToken("okta")))
                .isInstanceOf(Saml2Exception.class)
                .hasMessageContaining("reserved");
        assertThatThrownBy(() -> mapper.toJmixUser(assertion("anonymous"), responseToken("okta")))
                .isInstanceOf(Saml2Exception.class)
                .hasMessageContaining("reserved");
    }

    @Test
    void testSynchronizationOnSynchronizesAssertedRolesAsSaml() {
        TestSynchronizingMapper mapper = createMapperForNewUser();
        mapper.setSynchronizeRoleAssignments(true);

        mapper.toJmixUser(assertion("john"), responseToken("okta"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<RoleAssignment>> assignments = ArgumentCaptor.forClass(Collection.class);
        verify(mapper.roleAssignmentSynchronizer).synchronize(eq("john"), assignments.capture(),
                eq(RoleAssignmentSource.SAML), any(SaveContext.class));
        assertThat(assignments.getValue())
                .extracting(RoleAssignment::getUsername, RoleAssignment::getRoleCode, RoleAssignment::getRoleType)
                .containsExactly(tuple("john", "manager", RoleAssignmentRoleType.RESOURCE));
    }

    @Test
    void testSynchronizationOnSavesWithoutSoftDeletion() {
        TestSynchronizingMapper mapper = createMapperForNewUser();
        mapper.setSynchronizeRoleAssignments(true);

        mapper.toJmixUser(assertion("john"), responseToken("okta"));

        ArgumentCaptor<SaveContext> saveContext = ArgumentCaptor.forClass(SaveContext.class);
        verify(mapper.dataManager).save(saveContext.capture());
        assertThat(saveContext.getValue().getHints()).containsEntry(PersistenceHints.SOFT_DELETION, false);
    }

    @Test
    void testManualAuthoritiesAreAddedOnce() {
        TestSynchronizingMapper mapper = createMapperForNewUser();
        mapper.setSynchronizeRoleAssignments(true);
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_auditor"), new SimpleGrantedAuthority("ROLE_manager")))
                .when(mapper.roleAssignmentSynchronizer)
                .synchronize(anyString(), anyCollection(), anyString(), any(SaveContext.class));

        TestAppUser user = mapper.toJmixUser(assertion("john"), responseToken("okta"));

        assertThat(user.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_manager", "ROLE_auditor");
    }

    @Test
    void testSynchronizationOffAddsManualAuthoritiesOnly() {
        TestSynchronizingMapper mapper = createMapperForNewUser();
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_auditor")))
                .when(mapper.roleAssignmentSynchronizer).getManualAuthorities("john");

        TestAppUser user = mapper.toJmixUser(assertion("john"), responseToken("okta"));

        assertThat(user.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_manager", "ROLE_auditor");
        verify(mapper.roleAssignmentSynchronizer, never())
                .synchronize(anyString(), anyCollection(), anyString(), any(SaveContext.class));
    }

    @Test
    void testWithoutSynchronizerAssertedAuthoritiesAreKept() {
        TestSynchronizingMapper mapper = createMapperForNewUser();
        mapper.roleAssignmentSynchronizer = null;

        TestAppUser user = mapper.toJmixUser(assertion("john"), responseToken("okta"));

        assertThat(user.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_manager");
    }

    @Test
    void testWithoutSynchronizerSynchronizationFails() {
        TestSynchronizingMapper mapper = createMapperForNewUser();
        mapper.roleAssignmentSynchronizer = null;
        mapper.setSynchronizeRoleAssignments(true);

        assertThatThrownBy(() -> mapper.toJmixUser(assertion("john"), responseToken("okta")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("jmix-security-data");
    }

    @Test
    void testBuiltRoleAssignmentsHaveOwnSource() {
        // The method is an extension point: an override that saves its entities must not store them as manual.
        TestSynchronizingMapper mapper = createMapper();

        List<RoleAssignmentEntity> entities = mapper.buildRoleAssignmentEntities("john",
                List.of(new SimpleGrantedAuthority("ROLE_manager")));

        assertThat(entities)
                .extracting(RoleAssignmentEntity::getSource)
                .containsExactly(RoleAssignmentSource.SAML);
    }

    @Test
    void testDeprecatedDiffMethodCannotBeOverridden() throws NoSuchMethodException {
        // The method is no longer called: an override must fail to compile rather than be ignored silently.
        Method method = SynchronizingSamlUserMapper.class.getDeclaredMethod("updateRoleAssignmentsSaveContext",
                List.class, List.class, SaveContext.class);

        assertThat(Modifier.isFinal(method.getModifiers())).isTrue();
    }

    /**
     * Spring introspects the fields and methods of a mapper bean, which loads their types. The SAML add-on depends
     * on jmix-security-data at compile time only, so no field or method of the mapper may be typed with its classes.
     */
    @Test
    void testMapperMembersDoNotReferenceSecurityData() {
        List<String> typeNames = new ArrayList<>();
        for (Field field : SynchronizingSamlUserMapper.class.getDeclaredFields()) {
            typeNames.add(field.getGenericType().getTypeName());
        }
        for (Method method : SynchronizingSamlUserMapper.class.getDeclaredMethods()) {
            typeNames.add(method.getReturnType().getName());
            for (Class<?> parameterType : method.getParameterTypes()) {
                typeNames.add(parameterType.getName());
            }
        }

        assertThat(typeNames).noneMatch(name -> name.contains("io.jmix.securitydata"));
    }

    private TestSynchronizingMapper createMapperForNewUser() {
        TestSynchronizingMapper mapper = createMapper();
        when(mapper.userRepository.loadUserByUsername("john")).thenThrow(new UsernameNotFoundException("john"));
        return mapper;
    }

    private TestSynchronizingMapper createMapper() {
        TestSynchronizingMapper mapper = new TestSynchronizingMapper();
        mapper.samlProperties = new SamlProperties(true, 128, "/", true, null,
                new SamlProperties.DefaultSamlAssertionRolesMapperConfig("Role", "", ""),
                new SamlProperties.FilterChain(true, null));

        SamlAssertionRolesMapper rolesMapper = mock(SamlAssertionRolesMapper.class);
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_manager"))).when(rolesMapper).toGrantedAuthorities(any());
        mapper.rolesMapper = rolesMapper;
        RoleGrantedAuthorityUtils roleGrantedAuthorityUtils = mock(RoleGrantedAuthorityUtils.class);
        when(roleGrantedAuthorityUtils.getDefaultRolePrefix()).thenReturn("ROLE_");
        when(roleGrantedAuthorityUtils.getDefaultRowLevelRolePrefix()).thenReturn("ROW_LEVEL_ROLE_");
        mapper.roleGrantedAuthorityUtils = roleGrantedAuthorityUtils;
        mapper.roleAssignmentSynchronizer = mock(RoleAssignmentSynchronizer.class);

        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.getSystemUser()).thenReturn(User.withUsername("system").password("").build());
        when(userRepository.getAnonymousUser()).thenReturn(User.withUsername("anonymous").password("").build());
        mapper.userRepository = userRepository;

        UnconstrainedDataManager dataManager = mock(UnconstrainedDataManager.class);
        when(dataManager.create(TestAppUser.class)).thenReturn(new TestAppUser());
        when(dataManager.create(RoleAssignmentEntity.class)).thenAnswer(invocation -> new RoleAssignmentEntity());
        mapper.dataManager = dataManager;

        mapper.initLocks();
        return mapper;
    }

    private Assertion assertion(String username) {
        Assertion assertion = new AssertionBuilder().buildObject();
        NameID nameId = new NameIDBuilder().buildObject();
        nameId.setValue(username);
        Subject subject = new SubjectBuilder().buildObject();
        subject.setNameID(nameId);
        assertion.setSubject(subject);
        return assertion;
    }

    private OpenSaml5AuthenticationProvider.ResponseToken responseToken(String registrationId) {
        RelyingPartyRegistration registration = RelyingPartyRegistration.withRegistrationId(registrationId)
                .entityId("{baseUrl}/saml2/service-provider-metadata/{registrationId}")
                .assertionConsumerServiceLocation("{baseUrl}/login/saml2/sso/{registrationId}")
                .assertingPartyMetadata(party -> party
                        .entityId("https://idp.example.com/" + registrationId)
                        .singleSignOnServiceLocation("https://idp.example.com/sso/" + registrationId))
                .build();
        Saml2AuthenticationToken token = new Saml2AuthenticationToken(registration, "<Response/>");

        OpenSaml5AuthenticationProvider.ResponseToken responseToken =
                mock(OpenSaml5AuthenticationProvider.ResponseToken.class);
        when(responseToken.getToken()).thenReturn(token);
        return responseToken;
    }

    private static class TestSynchronizingMapper extends SynchronizingSamlUserMapper<TestAppUser> {

        @Override
        protected Class<TestAppUser> getApplicationUserClass() {
            return TestAppUser.class;
        }

        @Override
        protected void populateUserAttributes(Assertion assertion,
                                              OpenSaml5AuthenticationProvider.ResponseToken responseToken,
                                              TestAppUser jmixUser) {
        }
    }

    public static class TestAppUser extends JmixSamlUserEntity {

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
