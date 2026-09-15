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

package authentication;

import io.jmix.email.EmailerProperties;
import io.jmix.email.authentication.impl.GoogleOAuth2AuthorizationCodeFlow;
import org.junit.jupiter.api.Test;
import test_support.TestEmailRefreshTokenManager;
import test_support.TestEmailerProperties;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GoogleOAuth2AuthorizationCodeFlowTest {

    static final String REDIRECT_URI = "https://app.example.com/email/oauth2/callback";

    TestEmailRefreshTokenManager tokenManager = new TestEmailRefreshTokenManager(null);
    Map<String, String> sentForm = new HashMap<>();
    String tokenResponse = "{\"access_token\":\"at-1\",\"refresh_token\":\"rt-1\",\"expires_in\":3599}";

    GoogleOAuth2AuthorizationCodeFlow flow = new GoogleOAuth2AuthorizationCodeFlow(
            createProperties(), tokenManager) {
        @Override
        protected String postForm(String url, Map<String, String> formParameters) {
            sentForm.putAll(formParameters);
            return tokenResponse;
        }
    };

    @Test
    void testAuthorizationUrl() {
        String url = flow.buildAuthorizationUrl(REDIRECT_URI, "state-123");

        assertTrue(url.startsWith("https://accounts.google.com/o/oauth2/v2/auth?"));
        assertTrue(url.contains("client_id=test-client"));
        assertTrue(url.contains("redirect_uri=https%3A%2F%2Fapp.example.com%2Femail%2Foauth2%2Fcallback"));
        assertTrue(url.contains("response_type=code"));
        assertTrue(url.contains("scope=https%3A%2F%2Fmail.google.com%2F"));
        assertTrue(url.contains("access_type=offline"));
        assertTrue(url.contains("prompt=consent"));
        assertTrue(url.contains("state=state-123"));
    }

    @Test
    void testCompleteAuthorizationStoresRefreshToken() {
        flow.completeAuthorization("auth-code-1", REDIRECT_URI);

        assertEquals("rt-1", tokenManager.getRefreshTokenValue());
        assertEquals("auth-code-1", sentForm.get("code"));
        assertEquals("test-client", sentForm.get("client_id"));
        assertEquals("test-secret", sentForm.get("client_secret"));
        assertEquals(REDIRECT_URI, sentForm.get("redirect_uri"));
        assertEquals("authorization_code", sentForm.get("grant_type"));
    }

    @Test
    void testMissingRefreshTokenProducesMeaningfulError() {
        tokenResponse = "{\"access_token\":\"at-1\",\"expires_in\":3599}";

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> flow.completeAuthorization("auth-code-1", REDIRECT_URI));
        assertTrue(exception.getMessage().contains("no refresh token"));
    }

    private EmailerProperties createProperties() {
        return TestEmailerProperties.create(new EmailerProperties.OAuth2(
                true, "google", "test-client", "test-secret", null, "common", null));
    }
}
