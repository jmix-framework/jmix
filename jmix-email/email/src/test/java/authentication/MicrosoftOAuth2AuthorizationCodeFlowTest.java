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

import com.microsoft.aad.msal4j.IAuthenticationResult;
import io.jmix.email.EmailerProperties;
import io.jmix.email.authentication.impl.MicrosoftOAuth2AuthorizationCodeFlow;
import io.jmix.email.authentication.impl.RefreshTokenCapturingCacheAspect;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;
import org.jspecify.annotations.Nullable;
import test_support.TestEmailRefreshTokenManager;
import test_support.TestEmailerProperties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class MicrosoftOAuth2AuthorizationCodeFlowTest {

    static final String REDIRECT_URI = "https://app.example.com/email/oauth2/callback";

    TestEmailRefreshTokenManager tokenManager = new TestEmailRefreshTokenManager(null);
    String capturedRefreshToken;
    String receivedCode;
    String receivedRedirectUri;

    MicrosoftOAuth2AuthorizationCodeFlow flow = new MicrosoftOAuth2AuthorizationCodeFlow(
            createProperties(), tokenManager) {
        @Override
        @NullMarked
        protected IAuthenticationResult acquireTokenByAuthorizationCode(RefreshTokenCapturingCacheAspect cacheAspect,
                                                                        String authorizationCode,
                                                                        String redirectUri) {
            receivedCode = authorizationCode;
            receivedRedirectUri = redirectUri;
            return mock(IAuthenticationResult.class);
        }

        @Override
        @NullMarked
        protected String getCapturedRefreshToken(RefreshTokenCapturingCacheAspect cacheAspect) {
            return capturedRefreshToken;
        }
    };

    @Test
    void testAuthorizationUrl() {
        String url = flow.buildAuthorizationUrl(REDIRECT_URI, "state-123");

        assertTrue(url.startsWith("https://login.microsoftonline.com/test-tenant/oauth2/v2.0/authorize?"));
        assertTrue(url.contains("client_id=test-client"));
        assertTrue(url.contains("state=state-123"));
        assertTrue(url.contains("SMTP.Send"));
        assertTrue(url.contains("app.example.com"));
    }

    @Test
    void testCompleteAuthorizationStoresRefreshToken() {
        capturedRefreshToken = "rt-1";

        flow.completeAuthorization("auth-code-1", REDIRECT_URI);

        assertEquals("rt-1", tokenManager.getRefreshTokenValue());
        assertEquals("auth-code-1", receivedCode);
        assertEquals(REDIRECT_URI, receivedRedirectUri);
    }

    @Test
    void testMissingRefreshTokenProducesMeaningfulError() {
        capturedRefreshToken = null;

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> flow.completeAuthorization("auth-code-1", REDIRECT_URI));
        assertTrue(exception.getMessage().contains("no refresh token"));
    }

    private EmailerProperties createProperties() {
        return TestEmailerProperties.create(new EmailerProperties.OAuth2(
                true, "microsoft", "test-client", "test-secret", null, "test-tenant", null));
    }
}
