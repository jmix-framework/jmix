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

package io.jmix.superset;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
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

@Component("superset_SupersetAccessTokenManager")
public class SupersetAccessTokenManager {

    private static final Logger log = LoggerFactory.getLogger(SupersetAccessTokenManager.class);

    private final SupersetService supersetService;
    private final SupersetProperties supersetProperties;

    protected final ObjectMapper objectMapper;
    private String accessToken;
    private Long accessTokenExpiresIn;
    private String refreshToken;

    public SupersetAccessTokenManager(SupersetService supersetService,
                                      SupersetProperties supersetProperties) {
        this.supersetService = supersetService;
        this.supersetProperties = supersetProperties;

        objectMapper = buildObjectMapper();
    }

    public synchronized void updateAccessToken() {
        if (accessToken == null) {
            performLogin();
        } else if (isAboutToExpire()) {
            refreshAccessToken();
        }
    }

    public String getAccessToken() {
        if (Strings.isNullOrEmpty(accessToken)) {
            throw new IllegalStateException("Access token is not initialized");
        }
        return accessToken;
    }

    public String getRefreshAccessToken() {
        if (Strings.isNullOrEmpty(refreshToken)) {
            throw new IllegalStateException("Refresh token is not initialized");
        }
        return refreshToken;
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
            updateAccessToken(response.getAccessToken());
            refreshToken = response.getRefreshToken();
        } else {
            log.error("Cannot log in to superset. Dashboard functionality may work incorrectly. Message from Superset:" +
                    " {}", response.getMessage());
        }
    }

    protected void refreshAccessToken() {
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
        if (response.getErrorMessage() == null) {
            updateAccessToken(response.getAccessToken());
        } else {
            log.error("Failed to update access token. Dashboard functionality may work incorrectly. Message from Superset:" +
                    " {}", response.getErrorMessage());
        }
    }

    protected void updateAccessToken(String newToken) {
        this.accessToken = newToken;
        this.accessTokenExpiresIn = parseExpiresIn(newToken);
    }

    protected ObjectMapper buildObjectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.USE_LONG_FOR_INTS, true);
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

    protected boolean isAboutToExpire() {
        long currentTimePoint = new Date().getTime();
        if (accessTokenExpiresIn == null) {
            accessTokenExpiresIn = getFallbackExpirationTime();
        }
        return accessTokenExpiresIn - currentTimePoint < Duration.ofMinutes(1).getSeconds();
    }

    protected Long getFallbackExpirationTime() {
        return supersetProperties.fallbackAccessTokenExpiration.getSeconds();
    }
}
