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
import jakarta.validation.constraints.Null;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriBuilder;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Base64;
import java.util.List;

@SuppressWarnings("UnnecessaryLocalVariable")
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GenericRestClient {

    private ObjectMapper objectMapper = new ObjectMapper();

    private RestClient restClient;

    private String authToken;

    public GenericRestClient(RestConnectionParams connectionParams) {
        this(connectionParams.baseUrl(), connectionParams.clientId(), connectionParams.clientSecret());
    }

    public GenericRestClient(String baseUrl, String clientId, String clientSecret) {
        restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();

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
    public <E> E load(String entityName, Class<E> entityClass, Object id) {
        try {
            E entity = restClient.get()
                    .uri("/rest/entities/{entityName}/{id}", entityName, id)
                    .header("Authorization", "Bearer " + getAuthenticationToken())
                    .retrieve()
                    .body(entityClass);
            return entity;
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    public <E> List<E> loadList(String entityName, Class<E> entityClass,
                                int limit, int offset,
                                @Nullable String sort,
                                @Nullable String filter) {
        List<E> list;
        if (filter == null) {
            list = restClient.get()
                    .uri(uriBuilder ->
                            createLoadListUri(uriBuilder, entityName, limit, offset, sort, false))
                    .header("Authorization", "Bearer " + getAuthenticationToken())
                    .retrieve()
                    .body(getEntityListTypeRef(entityClass));
        } else {
            list = restClient.post()
                    .uri("/rest/entities/{entityName}/search", entityName)
                    .body(createSearchPostBody(limit, offset, sort, filter, false))
                    .header("Authorization", "Bearer " + getAuthenticationToken())
                    .retrieve()
                    .body(getEntityListTypeRef(entityClass));
        }

        return list;
    }

    private String createSearchPostBody(int limit, int offset, @Nullable String sort, String filter, boolean returnCount) {
        try {
            ObjectNode rootNode = objectMapper.createObjectNode();
            rootNode.set("filter", objectMapper.readTree(filter));
            if (sort != null) {
                rootNode.put("sort", sort);
            }
            if (limit > 0) {
                rootNode.put("limit", limit);
            }
            rootNode.put("offset", offset);
            if (returnCount) {
                rootNode.put("returnCount", true);
            }
            String json = objectMapper.writeValueAsString(rootNode);
            return json;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error creating request body", e);
        }
    }

    private URI createLoadListUri(UriBuilder uriBuilder, String entityName,
                                  int limit, int offset, @Nullable String sort, boolean returnCount) {
        uriBuilder.path("/rest/entities/{entityName}");
        if (sort != null) {
            uriBuilder.queryParam("sort", sort);
        }
        if (limit > 0) {
            uriBuilder.queryParam("limit", limit);
        }
        uriBuilder.queryParam("offset", offset);
        if (returnCount) {
            uriBuilder.queryParam("returnCount", true);
        }
        return uriBuilder.build(entityName);
    }

    public long count(String entityName, @Nullable String filter) {
        ResponseEntity<Void> response;
        if (filter == null) {
            response = restClient.get()
                    .uri(uriBuilder ->
                            createLoadListUri(uriBuilder, entityName, 1, 0, null, true))
                    .header("Authorization", "Bearer " + getAuthenticationToken())
                    .retrieve()
                    .toBodilessEntity();
        } else {
            response = restClient.post()
                    .uri("/rest/entities/{entityName}/search", entityName)
                    .body(createSearchPostBody(1, 0, null, filter, true))
                    .header("Authorization", "Bearer " + getAuthenticationToken())
                    .retrieve()
                    .toBodilessEntity();
        }

        String countStr = response.getHeaders().getFirst("X-Total-Count");
        return countStr == null ? 0 : Long.parseLong(countStr);
    }

    public <E> E create(String entityName, E entity) {
        Object savedEntity = restClient.post()
                .uri("/rest/entities/{entityName}?responseFetchPlan=_base", entityName)
                .header("Authorization", "Bearer " + getAuthenticationToken())
                .body(entity)
                .retrieve()
                .body(entity.getClass());

        return (E) savedEntity;
    }

    public <E> E update(String entityName, E entity) {
        Object savedEntity = restClient.put()
                .uri("/rest/entities/{entityName}/{id}?responseFetchPlan=_base", entityName, EntityValues.getId(entity))
                .header("Authorization", "Bearer " + getAuthenticationToken())
                .body(entity)
                .retrieve()
                .body(entity.getClass());

        return (E) savedEntity;
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

    private static <E> ParameterizedTypeReference<List<E>> getEntityListTypeRef(Class<E> entityClass) {
        return new ParameterizedTypeReference<>() {
            @Override
            public Type getType() {
                return new ParameterizedType() {
                    @Override
                    public Type getRawType() {
                        return List.class;
                    }

                    @Override
                    public Type getOwnerType() {
                        return null;
                    }

                    @Override
                    public Type[] getActualTypeArguments() {
                        return new Type[]{entityClass};
                    }
                };
            }
        };
    }
}
