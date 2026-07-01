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

package io.jmix.oidc;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import static org.assertj.core.api.Assertions.assertThat;

class OidcVaadinWebSecurityTest {

    @Test
    void testGetOidcLoginUrlUsesAuthorizationEndpointForSingleClient() {
        TestOidcVaadinWebSecurity security = new TestOidcVaadinWebSecurity();
        security.setClientRegistrationRepository(
                new InMemoryClientRegistrationRepository(clientRegistration("keycloak")));

        // Spring Security redirects straight to the provider when exactly one OIDC client is configured.
        // The custom resolver must return the same URL so that Vaadin route protection behaves identically.
        assertThat(security.loginUrl()).isEqualTo("/oauth2/authorization/keycloak");
    }

    @Test
    void testGetOidcLoginUrlFallsBackToDefaultLoginPageForMultipleClients() {
        TestOidcVaadinWebSecurity security = new TestOidcVaadinWebSecurity();
        security.setClientRegistrationRepository(
                new InMemoryClientRegistrationRepository(
                        clientRegistration("keycloak"),
                        clientRegistration("okta")));

        // With several clients Spring Security exposes its generated /login page.
        // The resolver must keep Vaadin aligned with that decision instead of picking an arbitrary provider.
        assertThat(security.loginUrl()).isEqualTo("/login");
    }

    private ClientRegistration clientRegistration(String registrationId) {
        return ClientRegistration.withRegistrationId(registrationId)
                .clientId("client")
                .clientSecret("secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .authorizationUri("https://idp.example.com/oauth2/authorize")
                .tokenUri("https://idp.example.com/oauth2/token")
                .jwkSetUri("https://idp.example.com/oauth2/jwks")
                .issuerUri("https://idp.example.com")
                .userInfoUri("https://idp.example.com/userinfo")
                .userNameAttributeName("sub")
                .clientName(registrationId)
                .build();
    }

    private static class TestOidcVaadinWebSecurity extends OidcVaadinWebSecurity {

        String loginUrl() {
            return getOidcLoginUrl();
        }
    }
}
