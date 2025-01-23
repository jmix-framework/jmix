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
import io.jmix.restds.exception.RestDataStoreAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component("restds_RestClientCredentialsAuthenticator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RestClientCredentialsAuthenticator implements RestAuthenticator {

    private static final Logger log = LoggerFactory.getLogger(RestClientCredentialsAuthenticator.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private RestClient client;

    private String dataStoreName;
    private String clientId;
    private String clientSecret;
    private String tokenPath;
    private String revokePath;

    private String authToken;

    private final ReadWriteLock authLock = new ReentrantReadWriteLock();

    @Autowired
    private Environment environment;

    @Override
    public void setDataStoreName(String name) {
        this.dataStoreName = name;
        initClient();
    }

    private void initClient() {
        String baseUrl = environment.getRequiredProperty(dataStoreName + ".baseUrl");
        clientId = environment.getRequiredProperty(dataStoreName + ".clientId");
        clientSecret = environment.getRequiredProperty(dataStoreName + ".clientSecret");

        tokenPath = environment.getProperty(dataStoreName + ".tokenPath", "/oauth2/token");
        revokePath = environment.getProperty(dataStoreName + ".revokePath", "/oauth2/revoke");

        client = RestClient.builder()
                .baseUrl(baseUrl)
                .requestInterceptor(new LoggingClientHttpRequestInterceptor())
                .build();
    }

    @Override
    public ClientHttpRequestInterceptor getAuthenticationInterceptor() {
        return new RetryingClientHttpRequestInterceptor();
    }

    public String getAuthenticationToken() {
        authLock.readLock().lock();
        try {
            if (authToken != null) {
                return authToken;
            }
        } finally {
            authLock.readLock().unlock();
        }

        authLock.writeLock().lock();
        try {
            if (authToken == null) {
                authToken = obtainAuthToken(clientId, clientSecret);
            }
            return authToken;
        } finally {
            authLock.writeLock().unlock();
        }
    }

    public void resetAuthToken() {
        authLock.writeLock().lock();
        try {
            authToken = null;
        } finally {
            authLock.writeLock().unlock();
        }
    }

    private String obtainAuthToken(String clientId, String clientSecret) {
        ResponseEntity<String> authResponse = null;
        try {
            authResponse = client.post()
                    .uri(tokenPath)
                    .headers(httpHeaders -> {
                        httpHeaders.setBasicAuth(clientId, clientSecret);
                        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    })
                    .body("grant_type=client_credentials")
                    .retrieve()
                    .toEntity(String.class);
        } catch (ResourceAccessException e) {
            throw new RestDataStoreAccessException(dataStoreName, e);
        }
        try {
            JsonNode rootNode = objectMapper.readTree(authResponse.getBody());
            return rootNode.get("access_token").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void revokeAuthenticationToken() {
        authLock.readLock().lock();
        try {
            if (authToken == null) {
                log.warn("No auth token in use");
                return;
            }
            client.post()
                    .uri(revokePath)
                    .headers(httpHeaders -> {
                        httpHeaders.setBasicAuth(clientId, clientSecret);
                        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    })
                    .body("token=" + authToken)
                    .retrieve()
                    .toBodilessEntity();
        } catch (ResourceAccessException e) {
            throw new RestDataStoreAccessException(dataStoreName, e);
        } finally {
            authLock.readLock().unlock();
        }
    }

    private class RetryingClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {
        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            request.getHeaders().setBearerAuth(getAuthenticationToken());
            ClientHttpResponse response = execution.execute(request, body);
            if (response.getStatusCode().is4xxClientError() && response.getStatusCode().value() == 401) {
                resetAuthToken();
                request.getHeaders().setBearerAuth(getAuthenticationToken());
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
