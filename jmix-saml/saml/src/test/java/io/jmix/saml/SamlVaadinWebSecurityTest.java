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

import static org.assertj.core.api.Assertions.assertThat;

class SamlVaadinWebSecurityTest {

    @Test
    void testGetSamlLoginUrlUsesAuthenticationEndpointForSingleRegistration() {
        TestSamlVaadinWebSecurity security = new TestSamlVaadinWebSecurity();
        security.relyingPartyRegistrationRepository =
                new InMemoryRelyingPartyRegistrationRepository(relyingPartyRegistration("okta"));

        // Spring Security redirects directly to the single configured relying party.
        // The custom resolver must return the same endpoint so that Vaadin route protection stays consistent.
        assertThat(security.loginUrl()).isEqualTo("/saml2/authenticate?registrationId=okta");
    }

    @Test
    void testGetSamlLoginUrlFallsBackToDefaultLoginPageForMultipleRegistrations() {
        TestSamlVaadinWebSecurity security = new TestSamlVaadinWebSecurity();
        security.relyingPartyRegistrationRepository =
                new InMemoryRelyingPartyRegistrationRepository(
                        relyingPartyRegistration("okta"),
                        relyingPartyRegistration("azure"));

        // With several relying parties Spring Security shows the generated /login page.
        // The resolver must keep Vaadin aligned with that behavior instead of forcing one registration.
        assertThat(security.loginUrl()).isEqualTo("/login");
    }

    private RelyingPartyRegistration relyingPartyRegistration(String registrationId) {
        return RelyingPartyRegistration.withRegistrationId(registrationId)
                .entityId("{baseUrl}/saml2/service-provider-metadata/{registrationId}")
                .assertionConsumerServiceLocation("{baseUrl}/login/saml2/sso/{registrationId}")
                .assertingPartyMetadata(party -> party
                        .entityId("https://idp.example.com/" + registrationId)
                        .singleSignOnServiceLocation("https://idp.example.com/sso/" + registrationId))
                .build();
    }

    private static class TestSamlVaadinWebSecurity extends SamlVaadinWebSecurity {

        String loginUrl() {
            return getSamlLoginUrl();
        }
    }
}
