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
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.core.entity.EntityValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@SuppressWarnings("UnnecessaryLocalVariable")
@Component("restds_RestInvoker")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RestInvoker {

    private static final Logger log = LoggerFactory.getLogger(RestInvoker.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RestClient restClient;
    private final RestClient authClient;

    private final String clientId;
    private final String clientSecret;

    private String authToken;

    private final ReadWriteLock authLock = new ReentrantReadWriteLock();

    public record LoadParams(String entityName,
                                 Object id,
                                 @Nullable String fetchPlanName) {

        public LoadParams(String entityName, Object id) {
            this(entityName, id, null);
        }

    }
    public record LoadListParams(String entityName,
                                 int limit,
                                 int offset,
                                 @Nullable String sort,
                                 @Nullable String filter,
                                 @Nullable String fetchPlanName) {

        public LoadListParams(String entityName, @Nullable String filter) {
            this(entityName, 1, 0, null, filter, null);
        }
    }

    public RestInvoker(RestConnectionParams connectionParams) {
        this(connectionParams.baseUrl(), connectionParams.clientId(), connectionParams.clientSecret());
    }

    public RestInvoker(String baseUrl, String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;

        restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestInterceptor(new RetryingClientHttpRequestInterceptor())
                .requestInterceptor(new LoggingClientHttpRequestInterceptor(true))
                .build();

        authClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestInterceptor(new LoggingClientHttpRequestInterceptor(false))
                .build();
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
        ResponseEntity<String> authResponse = authClient.post()
                .uri("/oauth2/token")
                .headers(httpHeaders -> {
                    httpHeaders.setBasicAuth(clientId, clientSecret);
                    httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                })
                .body("grant_type=client_credentials")
                .retrieve()
                .toEntity(String.class);
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
            authClient.post()
                    .uri("/oauth2/revoke")
                    .headers(httpHeaders -> {
                        httpHeaders.setBasicAuth(clientId, clientSecret);
                        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    })
                    .body("token=" + authToken)
                    .retrieve()
                    .toBodilessEntity();
        } finally {
            authLock.readLock().unlock();
        }
    }

    @Nullable
    public String load(LoadParams params) {
        try {
            String resultJson = restClient.get()
                    .uri(uriBuilder ->
                            createLoadUri(uriBuilder, params))
                    .retrieve()
                    .body(String.class);
            return resultJson;
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    private URI createLoadUri(UriBuilder uriBuilder, LoadParams params) {
        uriBuilder.path("/rest/entities/{entityName}/{id}");
        if (params.fetchPlanName() != null) {
            uriBuilder.queryParam("fetchPlan", params.fetchPlanName());
        }
        return uriBuilder.build(params.entityName(), params.id());
    }

    public String loadList(LoadListParams params) {
        String resultJson;
        if (params.filter() == null) {
            resultJson = restClient.get()
                    .uri(uriBuilder ->
                            createLoadListUri(uriBuilder, params, false))
                    .retrieve()
                    .body(String.class);
        } else {
            resultJson = restClient.post()
                    .uri("/rest/entities/{entityName}/search", params.entityName())
                    .body(createSearchPostBody(params, false))
                    .retrieve()
                    .body(String.class);
        }
        if (resultJson == null) {
            throw new IllegalStateException("Result JSON is null");
        }
        return resultJson;
    }

    private String createSearchPostBody(LoadListParams params, boolean returnCount) {
        try {
            ObjectNode rootNode = objectMapper.createObjectNode();
            rootNode.set("filter", objectMapper.readTree(params.filter()));
            if (params.sort() != null) {
                rootNode.put("sort", params.sort());
            }
            if (params.limit() > 0) {
                rootNode.put("limit", params.limit());
            }
            rootNode.put("offset", params.offset());
            if (params.fetchPlanName() != null) {
                rootNode.put("fetchPlan", params.fetchPlanName());
            }
            if (returnCount) {
                rootNode.put("returnCount", true);
            }
            String json = objectMapper.writeValueAsString(rootNode);
            return json;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error creating request body", e);
        }
    }

    private URI createLoadListUri(UriBuilder uriBuilder, LoadListParams params, boolean returnCount) {
        uriBuilder.path("/rest/entities/{entityName}");
        if (params.sort() != null) {
            uriBuilder.queryParam("sort", params.sort());
        }
        if (params.limit() > 0) {
            uriBuilder.queryParam("limit", params.limit());
        }
        uriBuilder.queryParam("offset", params.offset());
        if (params.fetchPlanName() != null) {
            uriBuilder.queryParam("fetchPlan", params.fetchPlanName());
        }
        if (returnCount) {
            uriBuilder.queryParam("returnCount", true);
        }
        return uriBuilder.build(params.entityName());
    }

    public long count(String entityName, @Nullable String filter) {
        ResponseEntity<Void> response;
        if (filter == null) {
            response = restClient.get()
                    .uri(uriBuilder ->
                            createLoadListUri(uriBuilder, new LoadListParams(entityName, null), true))
                    .retrieve()
                    .toBodilessEntity();
        } else {
            response = restClient.post()
                    .uri("/rest/entities/{entityName}/search", entityName)
                    .body(createSearchPostBody(new LoadListParams(entityName, filter), true))
                    .retrieve()
                    .toBodilessEntity();
        }

        String countStr = response.getHeaders().getFirst("X-Total-Count");
        return countStr == null ? 0 : Long.parseLong(countStr);
    }

    public String create(String entityName, String entityJson) {
        String resultJson = restClient.post()
                .uri("/rest/entities/{entityName}?responseFetchPlan=_base", entityName)
                .body(entityJson)
                .retrieve()
                .body(String.class);

        return resultJson;
    }

    public String update(String entityName, String entityId, String entityJson) {
        String resultJson = restClient.put()
                .uri("/rest/entities/{entityName}/{id}?responseFetchPlan=_base", entityName, entityId)
                .body(entityJson)
                .retrieve()
                .body(String.class);

        return resultJson;
    }

    public void delete(String entityName, String entityId) {
        restClient.delete()
                .uri("/rest/entities/{entityName}/{id}", entityName, entityId)
                .retrieve()
                .toBodilessEntity();
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

        private final boolean traceBody;

        public LoggingClientHttpRequestInterceptor(boolean traceBody) {
            this.traceBody = traceBody;
        }

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            log.debug("Request: {} {}", request.getMethod(), request.getURI());
            if (traceBody)
                log.trace("Request body: {}", new String(body));

            ClientHttpResponse response = execution.execute(request, body);

            log.debug("Response: {}", response.getStatusCode());
            if (traceBody)
                log.trace("Response body: {}", response.getBody());
            return response;
        }
    }
}
