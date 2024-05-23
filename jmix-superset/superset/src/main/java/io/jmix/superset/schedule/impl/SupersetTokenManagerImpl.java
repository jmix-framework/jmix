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

package io.jmix.superset.schedule.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import io.jmix.superset.SupersetProperties;
import io.jmix.superset.schedule.SupersetTokenManager;
import io.jmix.superset.service.model.CsrfTokenResponse;
import io.jmix.superset.service.model.LoginResponse;
import io.jmix.superset.service.model.RefreshResponse;
import io.jmix.superset.service.SupersetService;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Component("sprset_SupersetTokenManagerImpl")
public class SupersetTokenManagerImpl implements SupersetTokenManager {

    private static final Logger log = LoggerFactory.getLogger(SupersetTokenManagerImpl.class);

    private final SupersetService supersetService;
    private final SupersetProperties supersetProperties;

    protected final ObjectMapper objectMapper;
    private String accessToken;
    private Long accessTokenExpiresIn; // seconds
    private String refreshToken;
    private String csrfToken;

    public SupersetTokenManagerImpl(SupersetService supersetService,
                                    SupersetProperties supersetProperties) {
        this.supersetService = supersetService;
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
        if (supersetProperties.isCsrfProtectionEnabled()) {
            performCsrfTokenRequest();
        }
    }

    @Override
    public String getAccessToken() {
        if (Strings.isNullOrEmpty(accessToken)) {
            throw new IllegalStateException("Access token is not initialized");
        }
        return accessToken;
    }

    @Override
    public String getRefreshToken() {
        if (Strings.isNullOrEmpty(refreshToken)) {
            throw new IllegalStateException("Refresh token is not initialized");
        }
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
            response = supersetService.login();
        } catch (Exception e) {
            log.error("Cannot log in to superset. Dashboard functionality may work incorrectly", e);
            return;
        }

        if (response.getMessage() == null) {
            updateAccessToken(Objects.requireNonNull(response.getAccessToken()));
            refreshToken = response.getRefreshToken();
        } else {
            log.error("Cannot log in to superset. Dashboard functionality may work incorrectly. Message from Superset:" +
                    " {}", response.getMessage());
        }
    }

    protected void performRefreshingAccessToken() {
        boolean retry = false;
        RefreshResponse response = null;
        try {
            response = supersetService.refresh(refreshToken);
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
                response = supersetService.refresh(refreshToken);
            } catch (Exception e) {
                log.error("Failed to refresh access token. Dashboard functionality may work incorrectly", e);
                return;
            }
        }

        // Response cannot be null here
        if (response.getSystemMessage() == null) {
            updateAccessToken(response.getAccessToken());
        } else {
            log.error("Failed to update access token. Dashboard functionality may work incorrectly. Message from Superset:" +
                    " {}", response.getSystemMessage());
        }
    }

    protected void performCsrfTokenRequest() {
        CsrfTokenResponse response;
        try {
            response = supersetService.getCsrfToken(accessToken);
        } catch (Exception e) {
            log.error("Cannot get CSRF token from Superset. Dashboard functionality may work incorrectly", e);
            return;
        }

        if (!Strings.isNullOrEmpty(response.getMessage())
                || !Strings.isNullOrEmpty(response.getSystemMessage())) {
            log.error("Failed to update CSRF token. Dashboard functionality may work incorrectly. Message from Superset:" +
                    " {}", response.getSystemMessage());
        } else {
            csrfToken = response.getResult();
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

    @Nullable
    protected Long parseExpiresIn(String accessToken) {
        Base64.Decoder decoder = Base64.getUrlDecoder();
        String[] chunks = accessToken.split("\\.");
        if (chunks.length >= 2) { // chunks[0] - header, chunks[1] - payload
            String payloadJson = new String(decoder.decode(chunks[1]));
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
            return exp;
        }
        log.error("JWT does not contain payload part");
        return null;
    }

    protected boolean isAccessTokenAboutToExpire() {
        long currentTimePoint = new Date().getTime();
        if (accessTokenExpiresIn == null) {
            accessTokenExpiresIn = getFallbackExpirationTime();
        }
        return (accessTokenExpiresIn * 1000) - currentTimePoint <= Duration.ofMinutes(1).toMillis();
    }

    protected Long getFallbackExpirationTime() {
        // todo rp discuss
        return Duration.ofMinutes(3).getSeconds();
    }
}
