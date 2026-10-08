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

package io.jmix.saml.logout;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.saml2.provider.service.authentication.logout.Saml2LogoutRequest;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.Saml2MessageBinding;
import org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2LogoutRequestRepository;
import org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2LogoutRequestResolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SamlVaadinLogoutSuccessHandlerTest {

    @Test
    void testNullAuthenticationRedirectsToLogoutSuccessUrl() throws Exception {
        // Saml2LogoutResponseFilter invokes the success handler with null authentication after processing
        // the LogoutResponse from the IdP. The handler must finish with a redirect, not 401.
        Saml2LogoutRequestResolver resolver = mock(Saml2LogoutRequestResolver.class);
        SamlVaadinLogoutSuccessHandler handler = new SamlVaadinLogoutSuccessHandler(resolver);

        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.onLogoutSuccess(new MockHttpServletRequest(), response, null);

        assertThat(response.getRedirectedUrl()).isEqualTo("/");
    }

    @Test
    void testUnresolvableLogoutRequestRedirectsToLogoutSuccessUrl() throws Exception {
        Saml2LogoutRequestResolver resolver = mock(Saml2LogoutRequestResolver.class);
        when(resolver.resolve(any(), any())).thenReturn(null);
        SamlVaadinLogoutSuccessHandler handler = new SamlVaadinLogoutSuccessHandler(resolver);
        handler.setLogoutSuccessUrl("/bye");

        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.onLogoutSuccess(new MockHttpServletRequest(), response, authentication());

        assertThat(response.getRedirectedUrl()).isEqualTo("/bye");
    }

    @Test
    void testRedirectBindingSendsRedirectWithQueryAndSavesLogoutRequest() throws Exception {
        Saml2LogoutRequest logoutRequest = logoutRequest(Saml2MessageBinding.REDIRECT);
        Saml2LogoutRequestResolver resolver = mock(Saml2LogoutRequestResolver.class);
        when(resolver.resolve(any(), any())).thenReturn(logoutRequest);
        Saml2LogoutRequestRepository logoutRequestRepository = mock(Saml2LogoutRequestRepository.class);

        SamlVaadinLogoutSuccessHandler handler = new SamlVaadinLogoutSuccessHandler(resolver);
        handler.setLogoutRequestRepository(logoutRequestRepository);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.onLogoutSuccess(request, response, authentication());

        assertThat(response.getRedirectedUrl()).startsWith("https://idp.example.com/slo?SAMLRequest=");
        // The saved logout request is required by Saml2LogoutResponseFilter to validate the LogoutResponse
        verify(logoutRequestRepository).saveLogoutRequest(logoutRequest, request, response);
    }

    @Test
    void testPostBindingRendersAutoSubmittingForm() throws Exception {
        Saml2LogoutRequest logoutRequest = logoutRequest(Saml2MessageBinding.POST);
        Saml2LogoutRequestResolver resolver = mock(Saml2LogoutRequestResolver.class);
        when(resolver.resolve(any(), any())).thenReturn(logoutRequest);
        SamlVaadinLogoutSuccessHandler handler = new SamlVaadinLogoutSuccessHandler(resolver);

        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.onLogoutSuccess(new MockHttpServletRequest(), response, authentication());

        assertThat(response.getContentType()).isEqualTo("text/html");
        assertThat(response.getContentAsString())
                .contains("action=\"https://idp.example.com/slo\"")
                .contains("name=\"SAMLRequest\"");
    }

    private Authentication authentication() {
        return new TestingAuthenticationToken("john", "N/A");
    }

    private Saml2LogoutRequest logoutRequest(Saml2MessageBinding binding) {
        RelyingPartyRegistration registration = RelyingPartyRegistration.withRegistrationId("okta")
                .entityId("{baseUrl}/saml2/service-provider-metadata/{registrationId}")
                .assertionConsumerServiceLocation("{baseUrl}/login/saml2/sso/{registrationId}")
                .assertingPartyMetadata(party -> party
                        .entityId("https://idp.example.com/okta")
                        .singleSignOnServiceLocation("https://idp.example.com/sso/okta")
                        .singleLogoutServiceLocation("https://idp.example.com/slo")
                        .singleLogoutServiceBinding(binding))
                .build();
        return Saml2LogoutRequest.withRelyingPartyRegistration(registration)
                .location("https://idp.example.com/slo")
                .binding(binding)
                .samlRequest("encoded-logout-request")
                .relayState("relay-state")
                .parameters(params -> params.put("SAMLRequest", "encoded-logout-request"))
                .build();
    }
}
