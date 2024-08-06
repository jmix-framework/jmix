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
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.Base64;

@SuppressWarnings("UnnecessaryLocalVariable")
@Component("restds_RestInvoker")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RestInvoker {

    private ObjectMapper objectMapper = new ObjectMapper();

    private RestClient restClient;

    private String authToken;

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
        restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();

        // TODO: authenticate on first request
        ResponseEntity<String> authResponse = restClient.post()
                .uri("/oauth2/token")
                .header("Authorization", "Basic " + getBasicAuthCredentials(clientId, clientSecret))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body("grant_type=client_credentials")
                .retrieve()
                .toEntity(String.class);

        try {
            JsonNode rootNode = objectMapper.readTree(authResponse.getBody());
            authToken = rootNode.get("access_token").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String getBasicAuthCredentials(String clientId, String clientSecret) {
        return Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
    }

    @Nullable
    public String load(LoadParams params) {
        try {
            String resultJson = restClient.get()
                    .uri(uriBuilder ->
                            createLoadUri(uriBuilder, params))
                    .header("Authorization", "Bearer " + getAuthenticationToken())
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
                    .header("Authorization", "Bearer " + getAuthenticationToken())
                    .retrieve()
                    .body(String.class);
        } else {
            resultJson = restClient.post()
                    .uri("/rest/entities/{entityName}/search", params.entityName())
                    .body(createSearchPostBody(params, false))
                    .header("Authorization", "Bearer " + getAuthenticationToken())
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
                    .header("Authorization", "Bearer " + getAuthenticationToken())
                    .retrieve()
                    .toBodilessEntity();
        } else {
            response = restClient.post()
                    .uri("/rest/entities/{entityName}/search", entityName)
                    .body(createSearchPostBody(new LoadListParams(entityName, filter), true))
                    .header("Authorization", "Bearer " + getAuthenticationToken())
                    .retrieve()
                    .toBodilessEntity();
        }

        String countStr = response.getHeaders().getFirst("X-Total-Count");
        return countStr == null ? 0 : Long.parseLong(countStr);
    }

    public String create(String entityName, String entityJson) {
        String resultJson = restClient.post()
                .uri("/rest/entities/{entityName}?responseFetchPlan=_base", entityName)
                .header("Authorization", "Bearer " + getAuthenticationToken())
                .body(entityJson)
                .retrieve()
                .body(String.class);

        return resultJson;
    }

    public String update(String entityName, String entityId, String entityJson) {
        String resultJson = restClient.put()
                .uri("/rest/entities/{entityName}/{id}?responseFetchPlan=_base", entityName, entityId)
                .header("Authorization", "Bearer " + getAuthenticationToken())
                .body(entityJson)
                .retrieve()
                .body(String.class);

        return resultJson;
    }

    public void delete(String entityName, Object entity) {
        restClient.delete()
                .uri("/rest/entities/{entityName}/{id}", entityName, EntityValues.getId(entity))
                .header("Authorization", "Bearer " + getAuthenticationToken())
                .retrieve()
                .toBodilessEntity();
    }

    private String getAuthenticationToken() {
        return authToken;
    }
}
