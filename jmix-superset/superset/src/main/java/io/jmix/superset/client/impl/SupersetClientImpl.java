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

package io.jmix.superset.client.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import io.jmix.core.common.util.Preconditions;
import io.jmix.superset.SupersetProperties;
import io.jmix.superset.client.SupersetClient;
import io.jmix.superset.client.cookie.SupersetCookieManager;
import io.jmix.superset.client.model.*;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service("sprset_SupersetService")
public class SupersetClientImpl implements SupersetClient {
    private static final Logger log = LoggerFactory.getLogger(SupersetClientImpl.class);

    protected final SupersetProperties properties;

    protected HttpClient httpClient;
    protected ObjectMapper objectMapper;
    protected SupersetCookieManager cookieManager;

    public SupersetClientImpl(SupersetProperties properties,
                              SupersetCookieManager cookieManager) {
        this.properties = properties;
        this.cookieManager = cookieManager;

        httpClient = buildHttpClient();
        objectMapper = buildObjectMapper();
    }

    @Override
    public LoginResponse login() throws IOException, InterruptedException {
        return login(new LoginBody()
                .withUsername(properties.getUsername())
                .withPassword(properties.getPassword())
                .withProvider("db")
                .withRefresh(true));
    }

    @Override
    public LoginResponse login(LoginBody body) throws IOException, InterruptedException {
        Preconditions.checkNotNullArgument(body);

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

        String responseBody = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();

        log.debug("Login request is finished");

        try {
            return objectMapper.readValue(responseBody, LoginResponse.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot parse login response", e);
        }
    }

    @Override
    public RefreshResponse refresh(String refreshToken) throws IOException, InterruptedException {
        Preconditions.checkNotEmptyString(refreshToken);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getUrl() + "/api/v1/security/refresh"))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + refreshToken)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        log.debug("Sends a refresh request");

        String responseBody = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();

        log.debug("Refresh request is finished");

        try {
            return objectMapper.readValue(responseBody, RefreshResponse.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot parse refresh response", e);
        }
    }

    @Override
    public GuestTokenResponse fetchGuestToken(GuestTokenBody body, String accessToken, @Nullable String csrfToken)
            throws IOException, InterruptedException {
        checkGuestTokenResources(body.getResources());
        Preconditions.checkNotEmptyString(accessToken);

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

        String responseBody = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();

        log.debug("Guest token request is finished");

        try {
            return objectMapper.readValue(responseBody, GuestTokenResponse.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Cannot parse guest token response", ex);
        }
    }

    @Override
    public CsrfTokenResponse fetchCsrfToken(String accessToken) throws IOException, InterruptedException {
        Preconditions.checkNotEmptyString(accessToken);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getUrl() + "/api/v1/security/csrf_token"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .GET()
                .build();

        log.debug("Sends CSRF token request");

        HttpResponse<String> send = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        log.debug("CSRF token request is finished");

        String responseBody = send.body();

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

    protected void checkGuestTokenResources(List<GuestTokenBody.Resource> resources) {
        if (CollectionUtils.isEmpty(resources)) {
            throw new IllegalArgumentException("Guest token resources cannot be empty");
        }
        for (GuestTokenBody.Resource resource : resources) {
            if (Strings.isNullOrEmpty(resource.getId())) {
                throw new IllegalArgumentException("Resource must contain an id");
            }
        }
    }
}
