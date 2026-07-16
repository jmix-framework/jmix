/*
 * Copyright 2025 Haulmont.
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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.aad.msal4j.*;
import io.jmix.email.EmailerProperties;
import io.jmix.email.authentication.EmailRefreshTokenManager;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.slf4j.LoggerFactory.getLogger;

public class MicrosoftOAuth2TokenProvider extends AbstractOAuth2TokenProvider {

    private static final Logger log = getLogger(MicrosoftOAuth2TokenProvider.class);

    protected static final long EXPIRATION_SKEW_MILLIS = 2 * 60 * 1000;

    protected final RefreshTokenCapturingCacheAspect cacheAspect = new RefreshTokenCapturingCacheAspect();

    protected ConfidentialClientApplication clientApplication;
    protected IAuthenticationResult cachedResult;

    /**
     * Refresh token value the provider considers to be currently persisted. It is used to detect external
     * token updates (via the email token view or another application node) that require re-initialization
     * of the client application.
     */
    protected String currentRefreshToken;

    public MicrosoftOAuth2TokenProvider(EmailerProperties emailerProperties, EmailRefreshTokenManager refreshTokenManager) {
        super(emailerProperties, refreshTokenManager);
    }

    @Override
    public synchronized String getAccessToken() {
        try {
            String storedRefreshToken = getRefreshToken();
            if (clientApplication == null || !storedRefreshToken.equals(currentRefreshToken)) {
                log.debug("Initializing Microsoft client application");
                clientApplication = buildClientApplication(createCredential());
                currentRefreshToken = storedRefreshToken;
                cachedResult = null;
            }

            if (cachedResult != null && !isExpiringSoon(cachedResult)) {
                return cachedResult.accessToken();
            }

            IAuthenticationResult result = acquireTokenSilently();
            if (result == null) {
                result = acquireTokenByRefreshToken(currentRefreshToken);
            }
            cachedResult = result;
            persistRotatedRefreshToken();

            log.debug("Access token has been acquired with scopes: {} (expiration date = {})",
                    result.scopes(), result.expiresOnDate());
            return result.accessToken();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to acquire Microsoft SMTP access token", e);
        }
    }

    /**
     * Tries to get a token from the MSAL cache, refreshing it silently if needed. MSAL uses the latest
     * rotated refresh token stored in its internal cache for silent refreshes.
     *
     * @return authentication result or null if silent acquisition is not possible
     */
    @Nullable
    protected IAuthenticationResult acquireTokenSilently() {
        try {
            Set<IAccount> accounts = clientApplication.getAccounts().get();
            if (accounts == null || accounts.isEmpty()) {
                return null;
            }
            SilentParameters parameters = SilentParameters
                    .builder(getScopes(), accounts.iterator().next())
                    .build();
            return clientApplication.acquireTokenSilently(parameters).get();
        } catch (Exception e) {
            log.debug("Silent token acquisition failed, the token will be acquired by refresh token", e);
            return null;
        }
    }

    protected IAuthenticationResult acquireTokenByRefreshToken(String refreshToken) throws Exception {
        RefreshTokenParameters parameters = RefreshTokenParameters.builder(getScopes(), refreshToken).build();
        return clientApplication.acquireToken(parameters).get();
    }

    protected boolean isExpiringSoon(IAuthenticationResult result) {
        Date expiresOn = result.expiresOnDate();
        return expiresOn == null || expiresOn.getTime() - System.currentTimeMillis() < EXPIRATION_SKEW_MILLIS;
    }

    /**
     * Microsoft rotates the refresh token on every redemption. Stores the latest rotated value so that
     * authentication survives application restarts after the originally configured token becomes invalid.
     */
    protected void persistRotatedRefreshToken() {
        String rotatedToken = getCapturedRefreshToken();
        if (rotatedToken == null || rotatedToken.equals(currentRefreshToken)) {
            return;
        }
        try {
            refreshTokenManager.storeRefreshTokenValue(rotatedToken);
            currentRefreshToken = rotatedToken;
            log.debug("Rotated refresh token has been stored");
        } catch (Exception e) {
            log.error("Failed to store rotated refresh token." +
                    " The previously stored value may become invalid over time", e);
        }
    }

    @Nullable
    protected String getCapturedRefreshToken() {
        return cacheAspect.getLatestRefreshToken();
    }

    protected IClientCredential createCredential() {
        return ClientCredentialFactory.createFromSecret(getSecret());
    }

    protected ConfidentialClientApplication buildClientApplication(IClientCredential credential) {
        try {
            return ConfidentialClientApplication
                    .builder(getClientId(), credential)
                    .authority(buildAuthorityUrl())
                    .setTokenCacheAccessAspect(cacheAspect)
                    .build();
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Unable to build client application", e);
        }
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

    /**
     * Captures the latest refresh token from the MSAL token cache. The cache itself is kept in memory
     * within the client application instance, so nothing is loaded on {@code beforeCacheAccess}.
     */
    protected static class RefreshTokenCapturingCacheAspect implements ITokenCacheAccessAspect {

        protected final AtomicReference<String> latestRefreshToken = new AtomicReference<>();

        @Override
        public void beforeCacheAccess(ITokenCacheAccessContext context) {
        }

        @Override
        public void afterCacheAccess(ITokenCacheAccessContext context) {
            if (!context.hasCacheChanged()) {
                return;
            }
            try {
                JsonObject root = JsonParser.parseString(context.tokenCache().serialize()).getAsJsonObject();
                JsonElement refreshTokens = root.get("RefreshToken");
                if (refreshTokens == null || !refreshTokens.isJsonObject()) {
                    return;
                }
                for (Map.Entry<String, JsonElement> entry : refreshTokens.getAsJsonObject().entrySet()) {
                    JsonElement tokenValue = entry.getValue().getAsJsonObject().get("secret");
                    if (tokenValue != null && !tokenValue.getAsString().isEmpty()) {
                        latestRefreshToken.set(tokenValue.getAsString());
                        return;
                    }
                }
            } catch (Exception e) {
                log.warn("Unable to extract refresh token from MSAL token cache", e);
            }
        }

        @Nullable
        public String getLatestRefreshToken() {
            return latestRefreshToken.get();
        }
    }
}
