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

package authentication_event;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import io.jmix.authserver.event.AsUserAuthenticationSuccessEvent;
import io.jmix.sessions.resolver.OAuth2AndCookieSessionIdResolver;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import test_support.AbstractRestControllerFT;
import test_support.TestAsUserAuthenticationSuccessEventListener;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static test_support.RestTestUtils.CLIENT_ID;
import static test_support.RestTestUtils.CLIENT_SECRET;
import static test_support.RestTestUtils.sendGet;
import static test_support.RestTestUtils.statusCode;

class AsUserAuthenticationSuccessEventTest extends AbstractRestControllerFT {

    @Autowired
    TestAsUserAuthenticationSuccessEventListener eventListener;

    @Autowired
    OAuth2AuthorizationService authorizationService;

    @Autowired
    SessionRepository<?> sessionRepository;

    @BeforeEach
    void resetEventListener() {
        eventListener.reset();
    }

    @AfterEach
    void cleanUpEventListener() {
        eventListener.reset();
    }

    @Test
    void passwordGrant_publishesEventForUserAndClient() throws Exception {
        TokenResponse response = requestToken("grant_type", "password", "username", "admin", "password", "admin123");

        assertEquals(200, response.status());
        assertEquals(1, eventListener.getEvents().size());
        AsUserAuthenticationSuccessEvent event = eventListener.getEvents().get(0);
        assertEquals("admin", event.getAuthentication().getName());
        assertEquals("myclient", event.getRegisteredClient().getClientId());
        assertEquals("password", event.getAuthorizationGrantType().getValue());
    }

    @Test
    void passwordGrant_invokesListenerAsUser() throws Exception {
        AtomicReference<String> currentUsername = new AtomicReference<>();
        eventListener.setAction(event ->
                currentUsername.set(SecurityContextHolder.getContext().getAuthentication().getName()));

        requestToken("grant_type", "password", "username", "admin", "password", "admin123");

        assertEquals("admin", currentUsername.get());
    }

    @Test
    void passwordGrant_bindsSessionBeforeEvent() throws Exception {
        AtomicReference<Object> boundSessionId = new AtomicReference<>();
        eventListener.setAction(event -> {
            OAuth2Authorization savedAuthorization = authorizationService.findById(event.getAuthorization().getId());
            boundSessionId.set(savedAuthorization.getAttribute(OAuth2AndCookieSessionIdResolver.SESSION_ID));
        });

        requestToken("grant_type", "password", "username", "admin", "password", "admin123");

        assertNotNull(boundSessionId.get());
    }

    @Test
    void listenerThrowsAuthenticationException_rejectsTokenRequestWithInvalidGrant() throws Exception {
        eventListener.setAction(event -> {
            throw new DisabledException("Login is not allowed");
        });

        TokenResponse response = requestToken("grant_type", "password", "username", "admin", "password", "admin123");

        assertEquals(400, response.status());
        ReadContext body = JsonPath.parse(response.body());
        assertEquals("invalid_grant", body.read("$.error"));
        assertEquals("Login is not allowed", body.read("$.error_description"));
    }

    @Test
    void listenerThrowsAuthenticationException_removesAuthorization() throws Exception {
        AtomicReference<String> authorizationId = new AtomicReference<>();
        eventListener.setAction(event -> {
            authorizationId.set(event.getAuthorization().getId());
            throw new DisabledException("Login is not allowed");
        });

        requestToken("grant_type", "password", "username", "admin", "password", "admin123");

        assertNotNull(authorizationId.get());
        assertNull(authorizationService.findById(authorizationId.get()));
    }

    @Test
    void listenerThrowsAuthenticationException_invalidatesSessionCreatedForRequest() throws Exception {
        AtomicReference<String> sessionId = new AtomicReference<>();
        eventListener.setAction(event -> {
            sessionId.set(event.getAuthorization().getAttribute(OAuth2AndCookieSessionIdResolver.SESSION_ID));
            throw new DisabledException("Login is not allowed");
        });

        requestToken("grant_type", "password", "username", "admin", "password", "admin123");

        assertNotNull(sessionId.get());
        assertNull(sessionRepository.findById(sessionId.get()));
    }

    @Test
    void listenerThrowsOAuth2AuthenticationException_returnsItsError() throws Exception {
        eventListener.setAction(event -> {
            throw new OAuth2AuthenticationException(new OAuth2Error("access_denied", "Outside working hours", null));
        });

        TokenResponse response = requestToken("grant_type", "password", "username", "admin", "password", "admin123");

        assertEquals(400, response.status());
        ReadContext body = JsonPath.parse(response.body());
        assertEquals("access_denied", body.read("$.error"));
        assertEquals("Outside working hours", body.read("$.error_description"));
    }

    @Test
    void listenerThrowsOtherException_issuesWorkingToken() throws Exception {
        eventListener.setAction(event -> {
            throw new IllegalStateException("Listener failure");
        });

        TokenResponse response = requestToken("grant_type", "password", "username", "admin", "password", "admin123");

        assertEquals(200, response.status());
        String accessToken = JsonPath.parse(response.body()).read("$.access_token");
        try (CloseableHttpResponse userInfoResponse = sendGet(baseUrl + "/userInfo", accessToken, null)) {
            assertEquals(200, statusCode(userInfoResponse));
        }
    }

    @Test
    void refreshTokenGrant_doesNotPublishEvent() throws Exception {
        TokenResponse passwordResponse =
                requestToken("grant_type", "password", "username", "admin", "password", "admin123");
        String refreshToken = JsonPath.parse(passwordResponse.body()).read("$.refresh_token");
        eventListener.reset();

        TokenResponse refreshResponse = requestToken("grant_type", "refresh_token", "refresh_token", refreshToken);

        assertEquals(200, refreshResponse.status());
        assertTrue(eventListener.getEvents().isEmpty());
    }

    @Test
    void refreshTokenGrant_bindsSessionToNewAccessToken() throws Exception {
        ReadContext passwordBody = JsonPath.parse(
                requestToken("grant_type", "password", "username", "admin", "password", "admin123").body());
        OAuth2Authorization authorization = authorizationService.findByToken(
                passwordBody.read("$.access_token"), OAuth2TokenType.ACCESS_TOKEN);
        String sessionId = authorization.getAttribute(OAuth2AndCookieSessionIdResolver.SESSION_ID);

        ReadContext refreshBody = JsonPath.parse(
                requestToken("grant_type", "refresh_token", "refresh_token", passwordBody.read("$.refresh_token"))
                        .body());

        String newAccessToken = refreshBody.read("$.access_token");
        Session session = sessionRepository.findById(sessionId);
        assertEquals(newAccessToken, session.getAttribute(OAuth2AndCookieSessionIdResolver.ACCESS_TOKEN));
    }

    @Test
    void clientCredentialsGrant_doesNotPublishEvent() throws Exception {
        TokenResponse response = requestToken("grant_type", "client_credentials");

        assertEquals(200, response.status());
        assertTrue(eventListener.getEvents().isEmpty());
    }

    TokenResponse requestToken(String... params) throws IOException {
        List<NameValuePair> formParams = new ArrayList<>();
        for (int i = 0; i < params.length; i += 2) {
            formParams.add(new BasicNameValuePair(params[i], params[i + 1]));
        }
        String credentials = Base64.getEncoder()
                .encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes(StandardCharsets.UTF_8));

        HttpPost httpPost = new HttpPost(oauthUrl + "oauth2/token");
        httpPost.setHeader("Authorization", "Basic " + credentials);
        httpPost.setEntity(new UrlEncodedFormEntity(formParams));

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(httpPost)) {
            return new TokenResponse(response.getStatusLine().getStatusCode(),
                    EntityUtils.toString(response.getEntity()));
        }
    }

    record TokenResponse(int status, String body) {
    }
}
