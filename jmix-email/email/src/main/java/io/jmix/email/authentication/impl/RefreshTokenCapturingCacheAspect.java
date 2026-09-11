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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.aad.msal4j.ITokenCacheAccessAspect;
import com.microsoft.aad.msal4j.ITokenCacheAccessContext;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Captures the latest refresh token from the MSAL token cache. The cache itself is kept in memory
 * within the client application instance, so nothing is loaded on {@code beforeCacheAccess}.
 */
public class RefreshTokenCapturingCacheAspect implements ITokenCacheAccessAspect {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenCapturingCacheAspect.class);

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
