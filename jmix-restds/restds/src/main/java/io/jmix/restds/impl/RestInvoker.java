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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.restds.exception.RestDataStoreAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("UnnecessaryLocalVariable")
@Component("restds_RestInvoker")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RestInvoker implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(RestInvoker.class);

    public static final String DEFAULT_AUTHENTICATOR = "restds_RestClientCredentialsAuthenticator";

    private static final Set<String> STORE_PARAMETER_NAMES =
            Set.of("fetchPlan", "limit", "offset", "sort", "filter", "returnCount");

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String dataStoreName;

    private RestAuthenticator authenticator;

    private RestClient restClient;

    private String basePath;
    private String entitiesPath;
    private String userInfoPath;
    private String permissionsPath;
    private String capabilitiesPath;

    @Autowired
    private ApplicationContext applicationContext;


    /**
     * Parameters of loading one entity by id.
     *
     * @param additionalParams parameters added to the query string of the request. Names that the store sets itself
     *                         ({@code fetchPlan}, {@code limit}, {@code offset}, {@code sort}, {@code filter},
     *                         {@code returnCount}) and {@code null} values are not allowed.
     */
    public record LoadParams(String entityName,
                             Object id,
                             @Nullable String fetchPlan,
                             Map<String, Object> additionalParams) {

        public LoadParams {
            additionalParams = checkAdditionalParams(additionalParams);
        }

        public LoadParams(String entityName, Object id, @Nullable String fetchPlan) {
            this(entityName, id, fetchPlan, Map.of());
        }

        public LoadParams(String entityName, Object id) {
            this(entityName, id, null);
        }
    }

    /**
     * Parameters of loading a list of entities.
     *
     * @param additionalParams parameters added to the query string of a GET request, or as fields of the JSON body
     *                         when {@code filter} is set and the search endpoint is used. Names that the store sets
     *                         itself ({@code fetchPlan}, {@code limit}, {@code offset}, {@code sort}, {@code filter},
     *                         {@code returnCount}) and {@code null} values are not allowed.
     */
    public record LoadListParams(String entityName,
                                 int limit,
                                 int offset,
                                 @Nullable String sort,
                                 @Nullable String filter,
                                 @Nullable String fetchPlan,
                                 Map<String, Object> additionalParams) {

        public LoadListParams {
            additionalParams = checkAdditionalParams(additionalParams);
        }

        public LoadListParams(String entityName,
                              int limit,
                              int offset,
                              @Nullable String sort,
                              @Nullable String filter,
                              @Nullable String fetchPlan) {
            this(entityName, limit, offset, sort, filter, fetchPlan, Map.of());
        }

        public LoadListParams(String entityName, @Nullable String filter) {
            this(entityName, 1, 0, null, filter, null);
        }
    }

    public RestInvoker(String dataStoreName) {
        this.dataStoreName = dataStoreName;
    }

    @Override
    public void afterPropertiesSet() {
        Environment environment = applicationContext.getEnvironment();

        String authenticatorBeanName = environment.getProperty(
                dataStoreName + ".authenticator", DEFAULT_AUTHENTICATOR);

        authenticator = (RestAuthenticator) applicationContext.getBean(authenticatorBeanName);
        authenticator.setDataStoreName(dataStoreName);

        String baseUrl = environment.getRequiredProperty(dataStoreName + ".baseUrl");

        basePath = environment.getProperty(dataStoreName + ".basePath", "/rest");
        entitiesPath = environment.getProperty(dataStoreName + ".entitiesPath", "/entities");
        userInfoPath = environment.getProperty(dataStoreName + ".userInfoPath", "/userInfo");
        permissionsPath = environment.getProperty(dataStoreName + ".permissionsPath", "/permissions");
        capabilitiesPath = environment.getProperty(dataStoreName + ".capabilitiesPath", "/capabilities");

        RestClient.Builder builder = applicationContext.getBean(RestClient.Builder.class);

        restClient = builder
                .baseUrl(baseUrl)
                .messageConverters(converters ->
                        converters.add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8)))
                .requestInterceptor(authenticator.getAuthenticationInterceptor())
                .requestInterceptor(new LoggingClientHttpRequestInterceptor())
                .build();
    }

    public RestClient getRestClient() {
        return restClient;
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
        } catch (ResourceAccessException e) {
            throw new RestDataStoreAccessException(dataStoreName, e);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    private URI createLoadUri(UriBuilder uriBuilder, LoadParams params) {
        Map<String, Object> uriVariables = new HashMap<>();
        uriBuilder.path(basePath + entitiesPath + "/{entityName}/{id}");
        uriVariables.put("entityName", params.entityName());
        uriVariables.put("id", params.id());
        if (params.fetchPlan() != null) {
            uriBuilder.queryParam("fetchPlan", "{fetchPlan}");
            uriVariables.put("fetchPlan", params.fetchPlan());
        }
        addAdditionalQueryParams(uriBuilder, params.additionalParams(), uriVariables);
        return uriBuilder.build(uriVariables);
    }

    public String loadList(LoadListParams params) {
        String resultJson;
        try {
            if (params.filter() == null) {
                resultJson = restClient.get()
                        .uri(uriBuilder ->
                                createLoadListUri(uriBuilder, params, false))
                        .retrieve()
                        .body(String.class);
            } else {
                resultJson = restClient.post()
                        .uri(basePath + entitiesPath + "/{entityName}/search", params.entityName())
                        .body(createSearchPostBody(params, false))
                        .retrieve()
                        .body(String.class);
            }
        } catch (ResourceAccessException e) {
            throw new RestDataStoreAccessException(dataStoreName, e);
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
            if (params.fetchPlan() != null) {
                if (isJsonObject(params.fetchPlan())) {
                    rootNode.set("fetchPlan", objectMapper.readTree(params.fetchPlan()));
                } else {
                    rootNode.put("fetchPlan", params.fetchPlan());
                }
            }
            for (Map.Entry<String, Object> entry : params.additionalParams().entrySet()) {
                rootNode.set(entry.getKey(), objectMapper.valueToTree(entry.getValue()));
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
        Map<String, Object> uriVariables = new HashMap<>();
        uriBuilder.path(basePath + entitiesPath + "/{entityName}");
        uriVariables.put("entityName", params.entityName());
        if (params.sort() != null) {
            uriBuilder.queryParam("sort", params.sort());
        }
        if (params.limit() > 0) {
            uriBuilder.queryParam("limit", params.limit());
        }
        uriBuilder.queryParam("offset", params.offset());
        if (params.fetchPlan() != null) {
            uriBuilder.queryParam("fetchPlan", "{fetchPlan}");
            uriVariables.put("fetchPlan", params.fetchPlan());
        }
        if (returnCount) {
            uriBuilder.queryParam("returnCount", true);
        }
        addAdditionalQueryParams(uriBuilder, params.additionalParams(), uriVariables);
        return uriBuilder.build(uriVariables);
    }

    /*
     * Values are passed as URI variables, so they are encoded and not read as URI templates.
     */
    private void addAdditionalQueryParams(UriBuilder uriBuilder, Map<String, Object> additionalParams,
                                          Map<String, Object> uriVariables) {
        int index = 0;
        for (Map.Entry<String, Object> entry : additionalParams.entrySet()) {
            String variableName = "additionalParam" + index++;
            uriBuilder.queryParam(entry.getKey(), "{" + variableName + "}");
            uriVariables.put(variableName, entry.getValue());
        }
    }

    private static Map<String, Object> checkAdditionalParams(Map<String, Object> additionalParams) {
        for (Map.Entry<String, Object> entry : additionalParams.entrySet()) {
            if (STORE_PARAMETER_NAMES.contains(entry.getKey())) {
                throw new IllegalArgumentException("Request parameter '" + entry.getKey()
                        + "' is set by the REST data store and cannot be added");
            }
            if (entry.getValue() == null) {
                throw new IllegalArgumentException("Value of request parameter '" + entry.getKey() + "' is null");
            }
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(additionalParams));
    }

    public long count(String entityName, @Nullable String filter) {
        return count(entityName, filter, Map.of());
    }

    /**
     * Counts entities.
     *
     * @param additionalParams parameters added to the request, with the same rules as in
     *                         {@link LoadListParams#additionalParams()}
     */
    public long count(String entityName, @Nullable String filter, Map<String, Object> additionalParams) {
        LoadListParams params = new LoadListParams(entityName, 1, 0, null, filter, null, additionalParams);
        ResponseEntity<Void> response;
        try {
            if (filter == null) {
                response = restClient.get()
                        .uri(uriBuilder ->
                                createLoadListUri(uriBuilder, params, true))
                        .retrieve()
                        .toBodilessEntity();
            } else {
                response = restClient.post()
                        .uri(basePath + entitiesPath + "/{entityName}/search", entityName)
                        .body(createSearchPostBody(params, true))
                        .retrieve()
                        .toBodilessEntity();
            }
        } catch (ResourceAccessException e) {
            throw new RestDataStoreAccessException(dataStoreName, e);
        }
        String countStr = response.getHeaders().getFirst("X-Total-Count");
        return countStr == null ? 0 : Long.parseLong(countStr);
    }

    public String create(String entityName, String entityJson) {
        try {
            String resultJson = restClient.post()
                    .uri(basePath + entitiesPath + "/{entityName}?responseFetchPlan=_base", entityName)
                    .body(entityJson)
                    .retrieve()
                    .body(String.class);

            return resultJson;
        } catch (ResourceAccessException e) {
            throw new RestDataStoreAccessException(dataStoreName, e);
        }
    }

    public String update(String entityName, String entityId, String entityJson) {
        try {
            String resultJson = restClient.put()
                    .uri(basePath + entitiesPath + "/{entityName}/{id}?responseFetchPlan=_base", entityName, entityId)
                    .body(entityJson)
                    .retrieve()
                    .body(String.class);

            return resultJson;
        } catch (ResourceAccessException e) {
            throw new RestDataStoreAccessException(dataStoreName, e);
        }
    }

    public void delete(String entityName, String entityId) {
        try {
            restClient.delete()
                    .uri(basePath + entitiesPath + "/{entityName}/{id}", entityName, entityId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (ResourceAccessException e) {
            throw new RestDataStoreAccessException(dataStoreName, e);
        }
    }

    public String userInfo() {
        try {
            String resultJson = restClient.get()
                    .uri(basePath + userInfoPath)
                    .retrieve()
                    .body(String.class);

            return resultJson;
        } catch (ResourceAccessException e) {
            throw new RestDataStoreAccessException(dataStoreName, e);
        }
    }

    public String permissions() {
        try {
            String resultJson = restClient.get()
                    .uri(basePath + permissionsPath)
                    .retrieve()
                    .body(String.class);

            return resultJson;
        } catch (ResourceAccessException e) {
            throw new RestDataStoreAccessException(dataStoreName, e);
        }
    }

    public String capabilities() {
        try {
            String resultJson = restClient.get()
                    .uri(basePath + capabilitiesPath)
                    .retrieve()
                    .body(String.class);

            return resultJson;
        } catch (ResourceAccessException e) {
            throw new RestDataStoreAccessException(dataStoreName, e);
        }
    }

    public RestAuthenticator getAuthenticator() {
        return authenticator;
    }

    private boolean isJsonObject(String s) {
        String trimmed = s.trim();
        return trimmed.startsWith("{") && trimmed.endsWith("}");
    }

    private static class LoggingClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            log.debug("Request: {} {}", request.getMethod(), request.getURI());
            if (request.getMethod().equals(HttpMethod.POST) || request.getMethod().equals(HttpMethod.PUT))
                log.trace("Request body: {}", new String(body, StandardCharsets.UTF_8));

            ClientHttpResponse response = execution.execute(request, body);

            log.debug("Response: {}", response.getStatusCode());
            return response;
        }
    }
}
