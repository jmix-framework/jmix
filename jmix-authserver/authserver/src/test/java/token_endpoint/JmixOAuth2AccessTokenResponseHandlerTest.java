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

package token_endpoint;

import io.jmix.authserver.authentication.IssuedAuthorizationProcessor;
import io.jmix.authserver.authentication.JmixOAuth2AccessTokenResponseHandler;
import io.jmix.authserver.event.AsUserAuthenticationSuccessEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.security.Principal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Covers the handler outside of a full application: the {@code authorization_code} grant and the token endpoint
 * without the Sessions add-on, that is, without {@link IssuedAuthorizationProcessor}s.
 */
class JmixOAuth2AccessTokenResponseHandlerTest {

    static final String AUTHORIZATION_ID = "authorization-1";

    static final RegisteredClient CLIENT = RegisteredClient.withId("client-1")
            .clientId("spa")
            .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("http://localhost/callback")
            .build();

    InMemoryOAuth2AuthorizationService authorizationService = new InMemoryOAuth2AuthorizationService();
    List<AsUserAuthenticationSuccessEvent> events = new ArrayList<>();
    Consumer<AsUserAuthenticationSuccessEvent> listener = events::add;

    JmixOAuth2AccessTokenResponseHandler handler;
    OAuth2AccessTokenAuthenticationToken tokenAuthentication;

    @BeforeEach
    void setUp() {
        handler = new JmixOAuth2AccessTokenResponseHandler(
                authorizationService,
                event -> listener.accept((AsUserAuthenticationSuccessEvent) event),
                new StaticListableBeanFactory().getBeanProvider(IssuedAuthorizationProcessor.class));

        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                "access-token-1", Instant.now(), Instant.now().plusSeconds(300));
        authorizationService.save(OAuth2Authorization.withRegisteredClient(CLIENT)
                .id(AUTHORIZATION_ID)
                .principalName("alice")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .attribute(Principal.class.getName(),
                        UsernamePasswordAuthenticationToken.authenticated("alice", null, List.of()))
                .accessToken(accessToken)
                .build());

        tokenAuthentication = new OAuth2AccessTokenAuthenticationToken(CLIENT,
                new OAuth2ClientAuthenticationToken(CLIENT, ClientAuthenticationMethod.NONE, null), accessToken);
    }

    @Test
    void authorizationCodeGrant_publishesEventForUser() throws Exception {
        handler.onAuthenticationSuccess(authorizationCodeTokenRequest(), new MockHttpServletResponse(),
                tokenAuthentication);

        assertEquals(1, events.size());
        assertEquals("alice", events.get(0).getAuthentication().getName());
        assertEquals(AuthorizationGrantType.AUTHORIZATION_CODE, events.get(0).getAuthorizationGrantType());
    }

    @Test
    void listenerRejects_withoutSession_removesAuthorization() {
        listener = event -> {
            throw new DisabledException("Login is not allowed");
        };

        assertThrows(OAuth2AuthenticationException.class, () ->
                handler.onAuthenticationSuccess(authorizationCodeTokenRequest(), new MockHttpServletResponse(),
                        tokenAuthentication));

        assertNull(authorizationService.findById(AUTHORIZATION_ID));
    }

    @Test
    void listenerRejects_keepsSessionStartedBeforeRequest() {
        listener = event -> {
            throw new DisabledException("Login is not allowed");
        };
        MockHttpServletRequest request = authorizationCodeTokenRequest();
        MockHttpSession session = new MockHttpSession();
        session.setNew(false);
        request.setSession(session);

        assertThrows(OAuth2AuthenticationException.class, () ->
                handler.onAuthenticationSuccess(request, new MockHttpServletResponse(), tokenAuthentication));

        assertFalse(session.isInvalid());
    }

    MockHttpServletRequest authorizationCodeTokenRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/oauth2/token");
        request.addParameter("grant_type", "authorization_code");
        return request;
    }
}
