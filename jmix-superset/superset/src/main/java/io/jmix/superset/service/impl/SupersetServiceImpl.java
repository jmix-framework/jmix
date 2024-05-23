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

package io.jmix.superset.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jmix.core.common.util.Preconditions;
import io.jmix.superset.SupersetProperties;
import io.jmix.superset.service.SupersetService;
import io.jmix.superset.service.cookie.SupersetCookieManager;
import io.jmix.superset.service.model.*;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service("superset_SupersetService")
public class SupersetServiceImpl implements SupersetService {
    private static final Logger log = LoggerFactory.getLogger(SupersetServiceImpl.class);

    protected final SupersetProperties properties;

    protected HttpClient httpClient;
    protected ObjectMapper objectMapper;
    protected SupersetCookieManager cookieManager;

    public SupersetServiceImpl(SupersetProperties properties,
                               SupersetCookieManager cookieManager) {
        this.properties = properties;
        this.cookieManager = cookieManager;

        httpClient = buildHttpClient();
        objectMapper = buildObjectMapper();
    }

    @Override
    public LoginResponse login() {
        return login(new LoginBody()
                .withUsername(properties.getUsername())
                .withPassword(properties.getPassword())
                .withProvider("db")
                .withRefresh(true));
    }

    /**
     * Performs login to Superset. It sends a request that blocks current thread.
     * <p>
     * Note, that failed request will return response with {@code message} property.
     *
     * @param body the body to send
     * @return response
     */
    @Override
    public LoginResponse login(LoginBody body) {
        String requestBody;
        try {
            requestBody = new ObjectMapper().writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize properties to JSON string", e);
        }

        log.debug("Sends a request to log in to Superset");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getUrl() + "/api/v1/security/login"))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        String responseBody;
        try {
            responseBody = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException("Failed to log in to Superset", e);
        }

        log.debug("Login request is finished");

        try {
            return objectMapper.readValue(responseBody, LoginResponse.class);
            // todo rp check when no VPN exception is not handled?
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot parse login response", e);
        }
    }

    @Override
    public RefreshResponse refresh(String refreshToken) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getUrl() + "/api/v1/security/refresh"))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + refreshToken)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        log.debug("Sends a refresh request");

        String responseBody;
        try {
            responseBody = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException("Failed to refresh access token", e);
        }

        log.debug("Refresh request is finished");

        try {
            return objectMapper.readValue(responseBody, RefreshResponse.class);
            // todo rp check when no VPN exception is not handled?
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot parse refresh response", e);
        }
    }

    /**
     * Gets a guest token from Superset that can be used to embed dashboard.
     * It sends a request that blocks current thread.
     * <p>
     * Note, that failed request will return response with {@code message} property.
     *
     * @param body        the body to send
     * @param accessToken access token that can be taken from {@link #login(LoginBody)}
     * @param csrfToken   CSRF token should be passed if {@link SupersetProperties#isCsrfProtectionEnabled()} is enabled
     * @return response
     */
    @Override
    public GuestTokenResponse getGuestToken(GuestTokenBody body, String accessToken, @Nullable String csrfToken) {
        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize guest token body to JSON string", e);
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .uri(URI.create(properties.getUrl() + "/api/v1/security/guest_token"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        HttpRequest request;
        if (properties.isCsrfProtectionEnabled()) {
            Preconditions.checkNotEmptyString(csrfToken, "CSRF token cannot be empty");
            request = builder.header("X-Csrf-Token", csrfToken).build();
        } else {
            request = builder.build();
        }

        log.debug("Sends guest token request");

        String responseBody;
        try {
            responseBody = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException("Failed to get a guest token from Superset", e);
        }

        log.debug("Guest token request is finished");

        try {
            return objectMapper.readValue(responseBody, GuestTokenResponse.class);
            // todo rp check when no VPN exception is not handled?
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Cannot parse guest token response", ex);
        }
    }

    @Override
    public CsrfTokenResponse getCsrfToken(String accessToken) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getUrl() + "/api/v1/security/csrf_token"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .GET()
                .build();
        String responseBody;

        log.debug("Sends CSRF token request");

        try {
            HttpResponse<String> send = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            responseBody = send.body();
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException("Failed to get a guest a CSRF token from Superset", e);
        }

        log.debug("CSRF token request is finished");

        try {
            return objectMapper.readValue(responseBody, CsrfTokenResponse.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot parse CSRF token response", e);
        }
    }

    protected HttpClient buildHttpClient() {
        return HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .cookieHandler(cookieManager)
                .build();
    }

    protected ObjectMapper buildObjectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
