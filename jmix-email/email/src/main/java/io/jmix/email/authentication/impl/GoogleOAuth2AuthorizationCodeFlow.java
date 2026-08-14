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
import io.jmix.email.EmailerProperties;
import io.jmix.email.authentication.EmailRefreshTokenManager;
import io.jmix.email.authentication.OAuth2AuthorizationCodeFlow;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Authorization code flow for Google accounts. The authorization URL requests
 * {@code access_type=offline} and {@code prompt=consent} so that the token response always
 * contains a refresh token.
 */
@NullMarked
public class GoogleOAuth2AuthorizationCodeFlow extends AbstractOAuth2Flow implements OAuth2AuthorizationCodeFlow {

    private static final Logger log = LoggerFactory.getLogger(GoogleOAuth2AuthorizationCodeFlow.class);

    protected static final String AUTHORIZATION_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth";
    protected static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";

    public GoogleOAuth2AuthorizationCodeFlow(EmailerProperties emailerProperties,
                                             EmailRefreshTokenManager refreshTokenManager) {
        super(emailerProperties, refreshTokenManager);
    }

    @Override
    public String buildAuthorizationUrl(String redirectUri, String state) {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("client_id", getClientId());
        parameters.put("redirect_uri", redirectUri);
        parameters.put("response_type", "code");
        parameters.put("scope", getScope());
        parameters.put("access_type", "offline");
        parameters.put("prompt", "consent");
        parameters.put("state", state);
        return getAuthorizationEndpoint() + "?" + encodeForm(parameters);
    }

    @Override
    public void completeAuthorization(String authorizationCode, String redirectUri) {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("code", authorizationCode);
        parameters.put("client_id", getClientId());
        parameters.put("client_secret", getSecret());
        parameters.put("redirect_uri", redirectUri);
        parameters.put("grant_type", "authorization_code");

        String responseBody;
        try {
            responseBody = postForm(getTokenEndpoint(), parameters);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to exchange the authorization code for tokens", e);
        }

        JsonObject response = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonElement refreshToken = response.get("refresh_token");
        if (refreshToken == null || refreshToken.getAsString().isEmpty()) {
            throw new IllegalStateException("The token response contains no refresh token."
                    + " Check the OAuth client configuration");
        }
        refreshTokenManager.storeRefreshTokenValue(refreshToken.getAsString());
        log.info("Mailbox account has been connected using the authorization code flow");
    }

    protected String postForm(String url, Map<String, String> formParameters) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(encodeForm(formParameters)))
                .timeout(Duration.ofSeconds(30))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Token endpoint returned status %d: %s"
                    .formatted(response.statusCode(), response.body()));
        }
        return response.body();
    }

    protected String encodeForm(Map<String, String> parameters) {
        return parameters.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    protected String getScope() {
        return "https://mail.google.com/";
    }

    protected String getAuthorizationEndpoint() {
        return AUTHORIZATION_ENDPOINT;
    }

    protected String getTokenEndpoint() {
        return TOKEN_ENDPOINT;
    }
}
