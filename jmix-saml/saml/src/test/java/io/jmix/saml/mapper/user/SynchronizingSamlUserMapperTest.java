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

import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.security.UserRepository;
import io.jmix.saml.SamlProperties;
import io.jmix.saml.mapper.role.SamlAssertionRolesMapper;
import io.jmix.saml.user.JmixSamlUserEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.impl.AssertionBuilder;
import org.opensaml.saml.saml2.core.impl.NameIDBuilder;
import org.opensaml.saml.saml2.core.impl.SubjectBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml2.Saml2Exception;
import org.springframework.security.saml2.core.OpenSamlInitializationService;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml5AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationToken;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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

    private TestSynchronizingMapper createMapper() {
        TestSynchronizingMapper mapper = new TestSynchronizingMapper();
        mapper.samlProperties = new SamlProperties(true, 128, "/", true, null,
                new SamlProperties.DefaultSamlAssertionRolesMapperConfig("Role", "", ""),
                new SamlProperties.FilterChain(true, null));

        SamlAssertionRolesMapper rolesMapper = mock(SamlAssertionRolesMapper.class);
        when(rolesMapper.toGrantedAuthorities(any())).thenReturn(List.of());
        mapper.rolesMapper = rolesMapper;

        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.getSystemUser()).thenReturn(User.withUsername("system").password("").build());
        when(userRepository.getAnonymousUser()).thenReturn(User.withUsername("anonymous").password("").build());
        mapper.userRepository = userRepository;

        UnconstrainedDataManager dataManager = mock(UnconstrainedDataManager.class);
        when(dataManager.create(TestAppUser.class)).thenReturn(new TestAppUser());
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
