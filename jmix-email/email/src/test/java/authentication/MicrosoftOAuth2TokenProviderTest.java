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

import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IClientCredential;
import io.jmix.email.EmailerProperties;
import io.jmix.email.authentication.EmailRefreshTokenManager;
import io.jmix.email.authentication.impl.MicrosoftOAuth2TokenProvider;
import org.junit.jupiter.api.Test;
import org.jspecify.annotations.Nullable;
import test_support.TestEmailRefreshTokenManager;
import test_support.TestEmailerProperties;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MicrosoftOAuth2TokenProviderTest {

    TestEmailRefreshTokenManager tokenManager = new TestEmailRefreshTokenManager("initial-rt");
    TestMicrosoftOAuth2TokenProvider provider = new TestMicrosoftOAuth2TokenProvider(
            TestEmailerProperties.create(new EmailerProperties.OAuth2(
                    true, "microsoft", "test-client", "test-secret", null, "test-tenant")),
            tokenManager);

    @Test
    void testAccessTokenIsCached() {
        provider.refreshTokenResult = authResult("token-1", hoursFromNow(1));

        assertEquals("token-1", provider.getAccessToken());
        assertEquals("token-1", provider.getAccessToken());
        assertEquals(1, provider.refreshTokenCalls.size());
        assertEquals("initial-rt", provider.refreshTokenCalls.get(0));
    }

    @Test
    void testExpiringTokenIsRefreshedSilently() {
        provider.refreshTokenResult = authResult("token-1", secondsFromNow(30));
        assertEquals("token-1", provider.getAccessToken());

        provider.silentResult = authResult("token-2", hoursFromNow(1));
        assertEquals("token-2", provider.getAccessToken());
        assertEquals(1, provider.refreshTokenCalls.size());
    }

    @Test
    void testRotatedRefreshTokenIsPersisted() {
        provider.refreshTokenResult = authResult("token-1", hoursFromNow(1));
        provider.capturedRefreshToken = "rotated-rt";

        provider.getAccessToken();
        assertEquals("rotated-rt", tokenManager.getRefreshTokenValue());

        // The stored value matches the one known to the provider, no re-initialization happens
        provider.getAccessToken();
        assertEquals(1, provider.initializationCount);
    }

    @Test
    void testExternallyUpdatedTokenTriggersReinitialization() {
        provider.refreshTokenResult = authResult("token-1", hoursFromNow(1));
        assertEquals("token-1", provider.getAccessToken());

        tokenManager.storeRefreshTokenValue("external-rt");

        provider.refreshTokenResult = authResult("token-2", hoursFromNow(1));
        assertEquals("token-2", provider.getAccessToken());
        assertEquals(2, provider.initializationCount);
        assertEquals("external-rt", provider.refreshTokenCalls.get(1));
    }

    IAuthenticationResult authResult(String accessToken, Date expiresOn) {
        IAuthenticationResult result = mock(IAuthenticationResult.class);
        when(result.accessToken()).thenReturn(accessToken);
        when(result.expiresOnDate()).thenReturn(expiresOn);
        return result;
    }

    Date hoursFromNow(int hours) {
        return new Date(System.currentTimeMillis() + hours * 3600_000L);
    }

    Date secondsFromNow(int seconds) {
        return new Date(System.currentTimeMillis() + seconds * 1000L);
    }

    static class TestMicrosoftOAuth2TokenProvider extends MicrosoftOAuth2TokenProvider {

        IAuthenticationResult refreshTokenResult;
        IAuthenticationResult silentResult;
        String capturedRefreshToken;
        List<String> refreshTokenCalls = new ArrayList<>();
        int initializationCount;

        TestMicrosoftOAuth2TokenProvider(EmailerProperties emailerProperties,
                                         EmailRefreshTokenManager refreshTokenManager) {
            super(emailerProperties, refreshTokenManager);
        }

        @Override
        protected ConfidentialClientApplication buildClientApplication(IClientCredential credential) {
            initializationCount++;
            return super.buildClientApplication(credential);
        }

        @Override
        @Nullable
        protected IAuthenticationResult acquireTokenSilently() {
            return silentResult;
        }

        @Override
        protected IAuthenticationResult acquireTokenByRefreshToken(String refreshToken) {
            refreshTokenCalls.add(refreshToken);
            return refreshTokenResult;
        }

        @Override
        @Nullable
        protected String getCapturedRefreshToken() {
            return capturedRefreshToken;
        }
    }
}
