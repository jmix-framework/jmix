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

import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.auth.oauth2.UserCredentials;
import io.jmix.email.EmailerProperties;
import io.jmix.email.authentication.impl.GoogleOAuth2TokenProvider;
import org.junit.jupiter.api.Test;
import test_support.TestEmailRefreshTokenManager;
import test_support.TestEmailerProperties;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoogleOAuth2TokenProviderTest {

    TestTokenServerTransport transport = new TestTokenServerTransport();
    TestEmailRefreshTokenManager tokenManager = new TestEmailRefreshTokenManager("initial-rt");
    GoogleOAuth2TokenProvider provider = new GoogleOAuth2TokenProvider(createProperties(), tokenManager) {
        @Override
        protected UserCredentials createUserCredentials() {
            return UserCredentials.newBuilder()
                    .setClientId(getClientId())
                    .setClientSecret(getSecret())
                    .setRefreshToken(getRefreshToken())
                    .setHttpTransportFactory(() -> transport)
                    .build();
        }
    };

    @Test
    void testAccessTokenIsCached() {
        transport.respondWith("token-1", 3600);

        assertEquals("token-1", provider.getAccessToken());
        assertEquals("token-1", provider.getAccessToken());
        assertEquals(1, transport.requestCount);
    }

    @Test
    void testExpiredAccessTokenIsRefreshed() {
        transport.respondWith("token-1", 1);
        assertEquals("token-1", provider.getAccessToken());

        transport.respondWith("token-2", 3600);
        assertEquals("token-2", provider.getAccessToken());
        assertEquals(2, transport.requestCount);
    }

    @Test
    void testCredentialsRebuiltOnRefreshTokenChange() {
        transport.respondWith("token-1", 3600);
        assertEquals("token-1", provider.getAccessToken());
        assertTrue(transport.lastRequestContent.contains("refresh_token=initial-rt"));

        tokenManager.storeRefreshTokenValue("updated-rt");

        transport.respondWith("token-2", 3600);
        assertEquals("token-2", provider.getAccessToken());
        assertEquals(2, transport.requestCount);
        assertTrue(transport.lastRequestContent.contains("refresh_token=updated-rt"));
    }

    private EmailerProperties createProperties() {
        return TestEmailerProperties.create(new EmailerProperties.OAuth2(
                true, "google", "test-client", "test-secret", null, "common"));
    }

    static class TestTokenServerTransport extends MockHttpTransport {

        int requestCount;
        String lastRequestContent;
        String responseJson;

        void respondWith(String accessToken, int expiresInSec) {
            responseJson = "{\"access_token\":\"%s\",\"expires_in\":%d,\"token_type\":\"Bearer\"}"
                    .formatted(accessToken, expiresInSec);
        }

        @Override
        public LowLevelHttpRequest buildRequest(String method, String url) {
            return new MockLowLevelHttpRequest() {
                @Override
                public LowLevelHttpResponse execute() throws IOException {
                    requestCount++;
                    lastRequestContent = getContentAsString();
                    return new MockLowLevelHttpResponse()
                            .setStatusCode(200)
                            .setContentType("application/json")
                            .setContent(responseJson);
                }
            };
        }
    }
}
