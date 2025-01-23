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

package io.jmix.restds.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jmix.restds.exception.InvalidRefreshTokenException;
import io.jmix.restds.exception.RestDataStoreAccessException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component("restds_RestPasswordAuthenticator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RestPasswordAuthenticator implements RestAuthenticator {

    private static final Logger log = LoggerFactory.getLogger(RestPasswordAuthenticator.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private RestClient client;

    private String dataStoreName;
    private String clientId;
    private String clientSecret;
    private String tokenPath;

    @Autowired
    private RestTokenHolder tokenHolder;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void setDataStoreName(String name) {
        this.dataStoreName = name;
        initClient();
    }

    private void initClient() {
        Environment environment = applicationContext.getEnvironment();
        String baseUrl = environment.getRequiredProperty(dataStoreName + ".baseUrl");
        clientId = environment.getRequiredProperty(dataStoreName + ".clientId");
        clientSecret = environment.getRequiredProperty(dataStoreName + ".clientSecret");

        tokenPath = environment.getProperty(dataStoreName + ".tokenPath", "/oauth2/token");

        client = RestClient.builder()
                .baseUrl(baseUrl)
                .requestInterceptor(new LoggingClientHttpRequestInterceptor())
                .build();
    }

    @Override
    public ClientHttpRequestInterceptor getAuthenticationInterceptor() {
        return new RetryingClientHttpRequestInterceptor();
    }

    public void authenticate(String username, String password) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "password");
        params.add("username", username);
        params.add("password", password);

        ResponseEntity<String> authResponse;
        try {
            authResponse = client.post()
                    .uri(tokenPath)
                    .headers(httpHeaders -> {
                        httpHeaders.setBasicAuth(clientId, clientSecret);
                        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    })
                    .body(params)
                    .retrieve()
                    .onStatus(statusCode -> statusCode == HttpStatus.BAD_REQUEST, (request, response) -> {
                        throw new BadCredentialsException(IOUtils.toString(response.getBody(), StandardCharsets.UTF_8));
                    })
                    .toEntity(String.class);
        } catch (ResourceAccessException e) {
            throw new RestDataStoreAccessException(dataStoreName, e);
        }
        try {
            JsonNode rootNode = objectMapper.readTree(authResponse.getBody());
            String accessToken = rootNode.get("access_token").asText();
            if (!rootNode.has("refresh_token")) {
                throw new IllegalStateException("Refresh token is not provided. Add 'refresh_token' to authorization server grant types.");
            }
            String refreshToken = rootNode.get("refresh_token").asText();
            tokenHolder.setTokens(accessToken, refreshToken);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String authenticate(String refreshToken) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "refresh_token");
        params.add("refresh_token", refreshToken);

        ResponseEntity<String> authResponse;
        try {
            authResponse = client.post()
                    .uri(tokenPath)
                    .headers(httpHeaders -> {
                        httpHeaders.setBasicAuth(clientId, clientSecret);
                        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    })
                    .body(params)
                    .retrieve()
                    .onStatus(statusCode -> statusCode == HttpStatus.BAD_REQUEST, (request, response) -> {
                        throw new InvalidRefreshTokenException(dataStoreName);
                    })
                    .toEntity(String.class);
        } catch (ResourceAccessException e) {
            throw new RestDataStoreAccessException(dataStoreName, e);
        }
        try {
            JsonNode rootNode = objectMapper.readTree(authResponse.getBody());
            String accessToken = rootNode.get("access_token").asText();
            tokenHolder.setTokens(accessToken, refreshToken);
            return accessToken;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String getAccessToken() {
        String accessToken = tokenHolder.getAccessToken();
        if (accessToken == null) {
            throw new IllegalStateException("Access token is not stored. Authenticate with username and password first.");
        }
        return accessToken;
    }

    private String getAccessTokenByRefreshToken() {
        String refreshToken = tokenHolder.getRefreshToken();
        if (refreshToken == null) {
            throw new IllegalStateException("Refresh token is not stored. Authenticate with username and password first.");
        }
        String accessToken = authenticate(refreshToken);
        return accessToken;
    }

    private class RetryingClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {
        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            request.getHeaders().setBearerAuth(getAccessToken());
            ClientHttpResponse response = execution.execute(request, body);
            if (response.getStatusCode().is4xxClientError() && response.getStatusCode().value() == 401) {
                request.getHeaders().setBearerAuth(getAccessTokenByRefreshToken());
                response = execution.execute(request, body);
            }
            return response;
        }
    }

    private static class LoggingClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            log.debug("Request: {} {}", request.getMethod(), request.getURI());

            ClientHttpResponse response = execution.execute(request, body);

            log.debug("Response: {}", response.getStatusCode());
            return response;
        }
    }
}
