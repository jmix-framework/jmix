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

package io.jmix.saml;

import org.junit.jupiter.api.Test;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.Saml2MessageBinding;

import static org.assertj.core.api.Assertions.assertThat;

class SamlVaadinWebSecurityTest {

    @Test
    void testRedirectBindingWrapperOverridesBindingForBothLookupMethods() {
        // The wrapped repository backs the logout request resolver shared by both logout paths, so both
        // lookup methods must consistently force the REDIRECT binding while keeping the endpoint location.
        TestSamlVaadinWebSecurity security = new TestSamlVaadinWebSecurity();
        RelyingPartyRegistrationRepository wrapper =
                security.wrapWithRedirectBinding(new InMemoryRelyingPartyRegistrationRepository(
                        relyingPartyRegistration("okta")));

        RelyingPartyRegistration byId = wrapper.findByRegistrationId("okta");
        assertThat(byId.getAssertingPartyMetadata().getSingleLogoutServiceBinding())
                .isEqualTo(Saml2MessageBinding.REDIRECT);
        assertThat(byId.getAssertingPartyMetadata().getSingleLogoutServiceLocation())
                .isEqualTo("https://idp.example.com/slo/okta");

        RelyingPartyRegistration byEntityId = wrapper.findUniqueByAssertingPartyEntityId("https://idp.example.com/okta");
        assertThat(byEntityId.getAssertingPartyMetadata().getSingleLogoutServiceBinding())
                .isEqualTo(Saml2MessageBinding.REDIRECT);
    }

    private RelyingPartyRegistration relyingPartyRegistration(String registrationId) {
        return RelyingPartyRegistration.withRegistrationId(registrationId)
                .entityId("{baseUrl}/saml2/service-provider-metadata/{registrationId}")
                .assertionConsumerServiceLocation("{baseUrl}/login/saml2/sso/{registrationId}")
                .assertingPartyMetadata(party -> party
                        .entityId("https://idp.example.com/" + registrationId)
                        .singleSignOnServiceLocation("https://idp.example.com/sso/" + registrationId)
                        .singleLogoutServiceLocation("https://idp.example.com/slo/" + registrationId)
                        .singleLogoutServiceBinding(Saml2MessageBinding.POST))
                .build();
    }

    private static class TestSamlVaadinWebSecurity extends SamlVaadinWebSecurity {

        RelyingPartyRegistrationRepository wrapWithRedirectBinding(RelyingPartyRegistrationRepository repository) {
            return createRelyingPartyRegistrationRepositoryWrapperWithRedirectBinding(repository);
        }
    }
}
