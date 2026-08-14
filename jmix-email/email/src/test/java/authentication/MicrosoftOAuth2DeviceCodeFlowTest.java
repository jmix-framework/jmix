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

import com.microsoft.aad.msal4j.DeviceCode;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.email.EmailerProperties;
import io.jmix.email.authentication.EmailRefreshTokenManager;
import io.jmix.email.authentication.OAuth2DeviceCodeSession;
import io.jmix.email.authentication.impl.MicrosoftOAuth2DeviceCodeFlow;
import io.jmix.email.authentication.impl.RefreshTokenCapturingCacheAspect;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jspecify.annotations.Nullable;
import test_support.TestEmailRefreshTokenManager;
import test_support.TestEmailerProperties;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MicrosoftOAuth2DeviceCodeFlowTest {

    TestEmailRefreshTokenManager tokenManager = new TestEmailRefreshTokenManager(null);
    SystemAuthenticator systemAuthenticator = mock(SystemAuthenticator.class);
    CompletableFuture<IAuthenticationResult> tokenFuture = new CompletableFuture<>();
    String capturedRefreshToken;

    TestDeviceCodeFlow flow;

    @BeforeEach
    void setUp() {
        doAnswer(invocation -> {
            invocation.getArgument(0, Runnable.class).run();
            return null;
        }).when(systemAuthenticator).runWithSystem(any(Runnable.class));

        flow = new TestDeviceCodeFlow(
                TestEmailerProperties.create(new EmailerProperties.OAuth2(
                        true, "microsoft", "test-client", "test-secret", null, "test-tenant", null)),
                tokenManager,
                systemAuthenticator);
    }

    @Test
    void testSessionCarriesUserInstructions() {
        OAuth2DeviceCodeSession session = flow.start();

        assertEquals(OAuth2DeviceCodeSession.Status.PENDING, session.getStatus());
        assertEquals("ABC-123", session.getUserCode());
        assertEquals("https://microsoft.com/devicelogin", session.getVerificationUri());
    }

    @Test
    void testCompletionStoresRefreshToken() {
        OAuth2DeviceCodeSession session = flow.start();

        capturedRefreshToken = "device-rt";
        tokenFuture.complete(mock(IAuthenticationResult.class));

        assertEquals(OAuth2DeviceCodeSession.Status.COMPLETED, session.getStatus());
        assertEquals("device-rt", tokenManager.getRefreshTokenValue());
        verify(systemAuthenticator).runWithSystem(any(Runnable.class));
    }

    @Test
    void testCompletionWithoutRefreshTokenFails() {
        OAuth2DeviceCodeSession session = flow.start();

        capturedRefreshToken = null;
        tokenFuture.complete(mock(IAuthenticationResult.class));

        assertEquals(OAuth2DeviceCodeSession.Status.FAILED, session.getStatus());
        assertNotNull(session.getErrorMessage());
    }

    @Test
    void testProviderFailureIsReported() {
        OAuth2DeviceCodeSession session = flow.start();

        tokenFuture.completeExceptionally(new RuntimeException("authorization_declined"));

        assertEquals(OAuth2DeviceCodeSession.Status.FAILED, session.getStatus());
        assertTrue(session.getErrorMessage().contains("authorization_declined"));
    }

    @NullMarked
    class TestDeviceCodeFlow extends MicrosoftOAuth2DeviceCodeFlow {

        TestDeviceCodeFlow(EmailerProperties emailerProperties,
                           EmailRefreshTokenManager refreshTokenManager,
                           SystemAuthenticator systemAuthenticator) {
            super(emailerProperties, refreshTokenManager, systemAuthenticator);
        }

        @Override
        protected CompletableFuture<IAuthenticationResult> acquireDeviceCodeToken(
                RefreshTokenCapturingCacheAspect cacheAspect,
                Consumer<DeviceCode> deviceCodeConsumer) {
            DeviceCode deviceCode = mock(DeviceCode.class);
            when(deviceCode.userCode()).thenReturn("ABC-123");
            when(deviceCode.verificationUri()).thenReturn("https://microsoft.com/devicelogin");
            when(deviceCode.message()).thenReturn("Enter the code on the verification page");
            deviceCodeConsumer.accept(deviceCode);
            return tokenFuture;
        }

        @Override
        @Nullable
        protected String getCapturedRefreshToken(RefreshTokenCapturingCacheAspect cacheAspect) {
            return capturedRefreshToken;
        }
    }
}
