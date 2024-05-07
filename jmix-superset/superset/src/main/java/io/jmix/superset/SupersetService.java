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
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jmix.superset.model.GuestTokenBody;
import io.jmix.superset.model.GuestTokenResponse;
import io.jmix.superset.model.LoginBody;
import io.jmix.superset.model.LoginResponse;
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
public class SupersetService {
    private static final Logger log = LoggerFactory.getLogger(SupersetService.class);

    protected final SupersetProperties properties;

    protected HttpClient httpClient;

    public SupersetService(SupersetProperties properties) {
        this.properties = properties;

        httpClient = buildHttpClient();
    }

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

        try {
            return new ObjectMapper().readValue(responseBody, LoginResponse.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Cannot parse login response", ex);
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
     * @return response
     */
    public GuestTokenResponse getGuestToken(GuestTokenBody body, String accessToken) {
        String jsonBody;
        try {
            jsonBody = new ObjectMapper().writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize guest token body to JSON string", e);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getUrl() + "/api/v1/security/guest_token"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        String responseBody;
        try {
            responseBody = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException("Failed to get a guest token from Superset", e);
        }

        try {
            return new ObjectMapper().readValue(responseBody, GuestTokenResponse.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Cannot parse guest token response", ex);
        }
    }

    /**
     * Sends a request that blocks current thread.
     *
     * @return
     */
    /*public String getCsrfToken(String accessToken) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getUrl() + "/api/v1/security/csrf_token"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .GET()
                .build();

        try {
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(e -> {
                        try {
                            return new ObjectMapper().readValue(e, CsrfTokenResponse.class);
                        } catch (JsonProcessingException ex) {
                            //todo
                            throw new RuntimeException(ex);
                        }
                    }).get()
                    .getResult();
        } catch (InterruptedException | ExecutionException e) {
            //todo
            throw new RuntimeException(e);
        }
    }*/
    protected HttpClient buildHttpClient() {
        return HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }
}
