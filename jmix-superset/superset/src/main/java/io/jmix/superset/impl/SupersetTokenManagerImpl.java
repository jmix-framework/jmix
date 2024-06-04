/*
 * Copyright 2024 Haulmont.
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

package io.jmix.superset.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import io.jmix.superset.SupersetProperties;
import io.jmix.superset.SupersetTokenManager;
import io.jmix.superset.client.model.CsrfTokenResponse;
import io.jmix.superset.client.model.LoginResponse;
import io.jmix.superset.client.model.RefreshResponse;
import io.jmix.superset.client.SupersetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Component("sprset_SupersetTokenManagerImpl")
public class SupersetTokenManagerImpl implements SupersetTokenManager {

    private static final Logger log = LoggerFactory.getLogger(SupersetTokenManagerImpl.class);

    private final SupersetClient supersetClient;
    private final SupersetProperties supersetProperties;

    protected final ObjectMapper objectMapper;
    private String accessToken;
    private Long accessTokenExpiresIn; // ms
    private Long csrfTokenExpiresIn; // ms
    private String refreshToken;
    private String csrfToken;

    public SupersetTokenManagerImpl(SupersetClient supersetClient,
                                    SupersetProperties supersetProperties) {
        this.supersetClient = supersetClient;
        this.supersetProperties = supersetProperties;

        objectMapper = buildObjectMapper();
    }

    @Override
    public synchronized void refreshAccessToken() {
        if (accessToken == null) {
            performLogin();
        } else if (isAccessTokenAboutToExpire()) {
            performRefreshingAccessToken();
        }
    }

    @Override
    public synchronized void refreshCsrfToken() {
        if (!supersetProperties.isCsrfProtectionEnabled()) {
            return;
        }
        if (csrfToken == null || isCsrfTokenAboutToExpire()) {
            performCsrfTokenRequest();
        }
    }

    @Nullable
    @Override
    public String getAccessToken() {
        return accessToken;
    }

    @Nullable
    @Override
    public String getRefreshToken() {
        return refreshToken;
    }

    @Nullable
    @Override
    public String getCsrfToken() {
        return csrfToken;
    }

    protected void performLogin() {
        LoginResponse response;
        try {
            response = supersetClient.login();
        } catch (Exception e) {
            log.error("Cannot log in to superset. Dashboard functionality may work incorrectly", e);
            return;
        }

        if (response.getMessage() != null
                || Strings.isNullOrEmpty(response.getAccessToken())) {
            log.error("Cannot log in to superset. Dashboard functionality may work incorrectly. Message from " +
                    "Superset: {}", response.getMessage());
        } else {
            updateAccessToken(response.getAccessToken());
            refreshToken = response.getRefreshToken();
        }
    }

    protected void performRefreshingAccessToken() {
        if (Strings.isNullOrEmpty(refreshToken)) {
            log.error("Failed to refresh access token. Refresh token is null or empty");
            return;
        }

        boolean retry = false;
        RefreshResponse response = null;
        try {
            response = supersetClient.refresh(refreshToken);
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (!Strings.isNullOrEmpty(cause.getMessage()) && cause.getMessage().contains("GOAWAY received")) {
                log.error("Failed to refresh access token. Retrying request");
                retry = true;
            } else {
                log.error("Failed to refresh access token. Dashboard functionality may work incorrectly", e);
                return;
            }
        }

        if (retry) {
            try {
                response = supersetClient.refresh(refreshToken);
            } catch (Exception e) {
                log.error("Failed to refresh access token. Dashboard functionality may work incorrectly", e);
                return;
            }
        }

        if (response.getSystemMessage() != null
                || Strings.isNullOrEmpty(response.getAccessToken())) {
            log.error("Failed to update access token. Dashboard functionality may work incorrectly. Message from " +
                    "Superset: {}", response.getSystemMessage());
        } else {
            updateAccessToken(response.getAccessToken());
        }
    }

    protected void performCsrfTokenRequest() {
        if (Strings.isNullOrEmpty(accessToken)) {
            log.error("Cannot get CSRF token from Superset. Access token is null or empty");
            return;
        }

        CsrfTokenResponse response;
        try {
            response = supersetClient.fetchCsrfToken(accessToken);
        } catch (Exception e) {
            log.error("Cannot get CSRF token from Superset. Dashboard functionality may work incorrectly", e);
            return;
        }

        if (response.getMessage() != null
                || response.getSystemMessage() != null
                || Strings.isNullOrEmpty(response.getResult())) {
            log.error("Failed to update CSRF token. Dashboard functionality may work incorrectly. Message from " +
                    "Superset: {}", response.getMessage() != null ? response.getMessage() : response.getSystemMessage());
        } else {
            csrfToken = response.getResult();
            csrfTokenExpiresIn = new Date().getTime() + supersetProperties.getCsrfTokenExpiration().toMillis();
        }
    }

    protected void updateAccessToken(String newToken) {
        this.accessToken = newToken;
        this.accessTokenExpiresIn = parseExpiresIn(newToken);
    }

    protected ObjectMapper buildObjectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.USE_LONG_FOR_INTS, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * @param accessToken access token to parse
     * @return expiration time in milliseconds or {@code null} if an error occurs while parsing token
     */
    @Nullable
    protected Long parseExpiresIn(String accessToken) {
        Base64.Decoder decoder = Base64.getUrlDecoder();
        String[] chunks = accessToken.split("\\.");
        if (chunks.length >= 2) { // chunks[0] - header, chunks[1] - payload
            String payloadJson = new String(decoder.decode(chunks[1]), StandardCharsets.UTF_8);
            Map<String, Object> payloadMap;
            try {
                payloadMap = objectMapper.readValue(payloadJson, Map.class);
            } catch (JsonProcessingException e) {
                log.error("Cannot parse JWT", e);
                return null;
            }
            Long exp = (Long) payloadMap.get("exp");
            if (exp == null) {
                log.error("There is no 'exp' field in decoded JWT");
                return null;
            }
            // Superset returns expiration time in seconds
            return exp * 1000;
        }
        log.error("JWT does not contain payload part");
        return null;
    }

    protected boolean isAccessTokenAboutToExpire() {
        if (accessTokenExpiresIn == null) {
            accessTokenExpiresIn = getFallbackExpirationTime();
        }
        long currentTimePoint = new Date().getTime();
        return accessTokenExpiresIn - currentTimePoint <= Duration.ofMinutes(1).toMillis();
    }

    protected Long getFallbackExpirationTime() {
        return Duration.ofMinutes(3).toMillis();
    }

    protected boolean isCsrfTokenAboutToExpire() {
        if (supersetProperties.getCsrfTokenExpiration().toMillis() <= 0) {
            return false;
        }
        long currentTimePoint = new Date().getTime();
        return csrfTokenExpiresIn - currentTimePoint <= Duration.ofMinutes(1).toMillis();
    }
}
