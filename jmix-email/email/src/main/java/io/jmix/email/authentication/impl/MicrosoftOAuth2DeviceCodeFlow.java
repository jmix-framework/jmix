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

package io.jmix.email.authentication.impl;

import com.microsoft.aad.msal4j.DeviceCode;
import com.microsoft.aad.msal4j.DeviceCodeFlowParameters;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.PublicClientApplication;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.email.EmailerProperties;
import io.jmix.email.authentication.EmailRefreshTokenManager;
import io.jmix.email.authentication.OAuth2DeviceCodeFlow;
import io.jmix.email.authentication.OAuth2DeviceCodeSession;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Device code flow for Microsoft accounts. Runs on a public client application with the same
 * client id, so the app registration must have "Allow public client flows" enabled.
 */
@NullMarked
public class MicrosoftOAuth2DeviceCodeFlow extends AbstractOAuth2Flow implements OAuth2DeviceCodeFlow {

    private static final Logger log = LoggerFactory.getLogger(MicrosoftOAuth2DeviceCodeFlow.class);

    protected static final long DEVICE_CODE_INIT_TIMEOUT_SEC = 30;

    protected final SystemAuthenticator systemAuthenticator;

    public MicrosoftOAuth2DeviceCodeFlow(EmailerProperties emailerProperties,
                                         EmailRefreshTokenManager refreshTokenManager,
                                         SystemAuthenticator systemAuthenticator) {
        super(emailerProperties, refreshTokenManager);
        this.systemAuthenticator = systemAuthenticator;
    }

    @Override
    public OAuth2DeviceCodeSession start() {
        OAuth2DeviceCodeSession session = new OAuth2DeviceCodeSession();
        RefreshTokenCapturingCacheAspect cacheAspect = new RefreshTokenCapturingCacheAspect();
        CountDownLatch initLatch = new CountDownLatch(1);

        CompletableFuture<IAuthenticationResult> future;
        try {
            future = acquireDeviceCodeToken(cacheAspect, deviceCode -> {
                log.debug("Device code obtained, waiting for user verification");
                session.init(deviceCode.userCode(), deviceCode.verificationUri(), deviceCode.message());
                initLatch.countDown();
            });
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start Microsoft device code flow", e);
        }

        future.whenComplete((result, error) -> {
            if (error != null) {
                log.warn("Microsoft device code flow failed", error);
                session.fail(rootMessage(error));
            } else {
                completeSession(session, cacheAspect);
            }
            initLatch.countDown();
        });

        awaitInitialization(initLatch, session);
        return session;
    }

    /**
     * Stores the obtained refresh token. Runs on an MSAL callback thread without a security
     * context, so the token is stored under system authentication.
     */
    protected void completeSession(OAuth2DeviceCodeSession session, RefreshTokenCapturingCacheAspect cacheAspect) {
        String refreshToken = getCapturedRefreshToken(cacheAspect);
        if (refreshToken == null) {
            session.fail("Authentication succeeded but no refresh token was returned."
                    + " Check that the app registration allows public client flows");
            return;
        }
        try {
            systemAuthenticator.runWithSystem(() -> refreshTokenManager.storeRefreshTokenValue(refreshToken));
            log.info("Mailbox account has been connected using the device code flow");
            session.complete();
        } catch (Exception e) {
            log.error("Failed to store the refresh token obtained by the device code flow", e);
            session.fail(rootMessage(e));
        }
    }

    protected CompletableFuture<IAuthenticationResult> acquireDeviceCodeToken(
            RefreshTokenCapturingCacheAspect cacheAspect,
            Consumer<DeviceCode> deviceCodeConsumer) throws Exception {
        PublicClientApplication application = PublicClientApplication
                .builder(getClientId())
                .authority(buildAuthorityUrl())
                .setTokenCacheAccessAspect(cacheAspect)
                .build();
        DeviceCodeFlowParameters parameters = DeviceCodeFlowParameters
                .builder(getScopes(), deviceCodeConsumer)
                .build();
        return application.acquireToken(parameters);
    }

    protected void awaitInitialization(CountDownLatch initLatch, OAuth2DeviceCodeSession session) {
        try {
            if (!initLatch.await(DEVICE_CODE_INIT_TIMEOUT_SEC, TimeUnit.SECONDS)) {
                session.fail("Timed out waiting for the device code from the provider");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            session.fail("Interrupted while waiting for the device code");
        }
    }

    protected String rootMessage(Throwable throwable) {
        Throwable root = throwable;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root.getMessage() != null ? root.getMessage() : root.getClass().getSimpleName();
    }

    @Nullable
    protected String getCapturedRefreshToken(RefreshTokenCapturingCacheAspect cacheAspect) {
        return cacheAspect.getLatestRefreshToken();
    }

    protected Set<String> getScopes() {
        return Collections.singleton("https://outlook.office.com/SMTP.Send");
    }

    protected String buildAuthorityUrl() {
        return getBaseAuthorityUrl() + "/" + getTenant();
    }

    protected String getBaseAuthorityUrl() {
        return "https://login.microsoftonline.com";
    }

    protected String getTenant() {
        return emailerProperties.getOAuth2().getTenantId();
    }
}
