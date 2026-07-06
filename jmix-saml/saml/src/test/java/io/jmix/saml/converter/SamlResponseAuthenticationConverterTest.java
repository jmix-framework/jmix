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

package io.jmix.saml.converter;

import io.jmix.saml.mapper.user.SamlUserMapper;
import io.jmix.saml.user.JmixSamlUserDetails;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.impl.AssertionBuilder;
import org.opensaml.saml.saml2.core.impl.NameIDBuilder;
import org.opensaml.saml.saml2.core.impl.ResponseBuilder;
import org.opensaml.saml.saml2.core.impl.SubjectBuilder;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.saml2.core.OpenSamlInitializationService;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationToken;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SamlResponseAuthenticationConverterTest {

    @BeforeAll
    static void initOpenSaml() {
        OpenSamlInitializationService.initialize();
    }

    @Test
    void testDisabledUserCannotAuthenticate() {
        // A user deactivated in the Jmix application must not log in even though the identity provider
        // authenticated them successfully. The account status failure must keep its type and message.
        TestUserDetails user = new TestUserDetails("john");
        user.setEnabled(false);
        SamlResponseAuthenticationConverter converter = new SamlResponseAuthenticationConverter(userMapper(user));

        assertThatThrownBy(() -> converter.convert(responseToken()))
                .isInstanceOf(DisabledException.class);
    }

    @Test
    void testEnabledUserIsConverted() {
        TestUserDetails user = new TestUserDetails("john");
        SamlResponseAuthenticationConverter converter = new SamlResponseAuthenticationConverter(userMapper(user));

        Saml2Authentication authentication = converter.convert(responseToken());

        assertThat(authentication.getPrincipal()).isSameAs(user);
    }

    private SamlUserMapper<JmixSamlUserDetails> userMapper(JmixSamlUserDetails user) {
        @SuppressWarnings("unchecked")
        SamlUserMapper<JmixSamlUserDetails> userMapper = mock(SamlUserMapper.class);
        when(userMapper.toJmixUser(any(), any())).thenReturn(user);
        return userMapper;
    }

    private OpenSaml4AuthenticationProvider.ResponseToken responseToken() {
        Assertion assertion = new AssertionBuilder().buildObject();
        NameID nameId = new NameIDBuilder().buildObject();
        nameId.setValue("john");
        Subject subject = new SubjectBuilder().buildObject();
        subject.setNameID(nameId);
        assertion.setSubject(subject);

        Response response = new ResponseBuilder().buildObject();
        response.getAssertions().add(assertion);

        RelyingPartyRegistration registration = RelyingPartyRegistration.withRegistrationId("okta")
                .entityId("{baseUrl}/saml2/service-provider-metadata/{registrationId}")
                .assertionConsumerServiceLocation("{baseUrl}/login/saml2/sso/{registrationId}")
                .assertingPartyMetadata(party -> party
                        .entityId("https://idp.example.com/okta")
                        .singleSignOnServiceLocation("https://idp.example.com/sso/okta"))
                .build();
        Saml2AuthenticationToken token = new Saml2AuthenticationToken(registration, "<Response/>");

        OpenSaml4AuthenticationProvider.ResponseToken responseToken =
                mock(OpenSaml4AuthenticationProvider.ResponseToken.class);
        when(responseToken.getToken()).thenReturn(token);
        when(responseToken.getResponse()).thenReturn(response);
        return responseToken;
    }

    private static class TestUserDetails implements JmixSamlUserDetails {

        private final String username;
        private Collection<? extends GrantedAuthority> authorities = List.of();
        private boolean enabled = true;

        TestUserDetails(String username) {
            this.username = username;
        }

        @Override
        public String getName() {
            return username;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public String getPassword() {
            return "";
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
            this.authorities = authorities;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
