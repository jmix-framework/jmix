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

package io.jmix.restds.impl;

import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.core.SaveContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.restds.extension.RestDataStoreExtension;
import io.jmix.restds.extension.RestDataStoreExtension.AfterLoadContext;
import io.jmix.restds.extension.RestDataStoreExtension.AfterSaveContext;
import io.jmix.restds.extension.RestDataStoreExtension.BeforeCountContext;
import io.jmix.restds.extension.RestDataStoreExtension.BeforeLoadContext;
import io.jmix.restds.extension.RestDataStoreExtension.BeforeSaveContext;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.JsonNodeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Calls {@link RestDataStoreExtension} beans for the REST data store. Parses JSON only when at least one extension
 * supports the data store and entity.
 */
@Component("restds_RestDataStoreExtensionSupport")
@NullMarked
public class RestDataStoreExtensionSupport {

    // Decimal numbers must keep their exact digits when JSON is parsed and written back.
    private final ObjectMapper objectMapper = JsonMapper.builder()
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .enable(JsonNodeFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .disable(JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES)
            .enable(StreamWriteFeature.WRITE_BIGDECIMAL_AS_PLAIN)
            .build();

    @Autowired(required = false)
    private List<RestDataStoreExtension> extensions = List.of();

    @Autowired
    private Metadata metadata;

    /**
     * Calls {@link RestDataStoreExtension#beforeLoad} and returns the parameters to add to the request.
     */
    public Map<String, Object> beforeLoad(String dataStoreName, LoadContext<?> loadContext) {
        List<RestDataStoreExtension> supportedExtensions =
                getExtensions(dataStoreName, loadContext.getEntityMetaClass());
        if (supportedExtensions.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> requestParameters = new LinkedHashMap<>();
        BeforeLoadContext context = new BeforeLoadContext(dataStoreName, loadContext, requestParameters);
        for (RestDataStoreExtension extension : supportedExtensions) {
            extension.beforeLoad(context);
        }
        return requestParameters;
    }

    /**
     * Calls {@link RestDataStoreExtension#beforeCount} and returns the parameters to add to the request.
     */
    public Map<String, Object> beforeCount(String dataStoreName, LoadContext<?> loadContext) {
        List<RestDataStoreExtension> supportedExtensions =
                getExtensions(dataStoreName, loadContext.getEntityMetaClass());
        if (supportedExtensions.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> requestParameters = new LinkedHashMap<>();
        BeforeCountContext context = new BeforeCountContext(dataStoreName, loadContext, requestParameters);
        for (RestDataStoreExtension extension : supportedExtensions) {
            extension.beforeCount(context);
        }
        return requestParameters;
    }

    /**
     * Calls {@link RestDataStoreExtension#afterLoad} for each loaded entity.
     *
     * @param entities entities read from {@code json}, in the same order
     * @param json     a JSON array of entities, or a JSON object of one entity
     */
    public void afterLoad(String dataStoreName, LoadContext<?> loadContext, List<?> entities, String json) {
        if (entities.isEmpty()) {
            return;
        }
        List<RestDataStoreExtension> supportedExtensions =
                getExtensions(dataStoreName, loadContext.getEntityMetaClass());
        if (supportedExtensions.isEmpty()) {
            return;
        }
        List<JsonNode> entityJsons = getEntityJsons(objectMapper.readTree(json));
        if (entityJsons.size() != entities.size()) {
            throw new IllegalStateException("Number of JSON objects (" + entityJsons.size()
                    + ") does not match number of loaded entities (" + entities.size() + ")");
        }
        for (int i = 0; i < entities.size(); i++) {
            AfterLoadContext context =
                    new AfterLoadContext(dataStoreName, loadContext, entities.get(i), entityJsons.get(i));
            for (RestDataStoreExtension extension : supportedExtensions) {
                extension.afterLoad(context);
            }
        }
    }

    /**
     * Calls {@link RestDataStoreExtension#beforeSave} and returns the JSON to send.
     *
     * @param json JSON of the entity, as written by the store
     * @return the JSON with the changes made by extensions, or {@code json} itself if no extension was called
     */
    public String beforeSave(String dataStoreName, SaveContext saveContext, Object entity, boolean isNew,
                             String json) {
        List<RestDataStoreExtension> supportedExtensions = getExtensions(dataStoreName, metadata.getClass(entity));
        if (supportedExtensions.isEmpty()) {
            return json;
        }
        ObjectNode entityJson = (ObjectNode) objectMapper.readTree(json);
        BeforeSaveContext context = new BeforeSaveContext(dataStoreName, saveContext, entity, isNew, entityJson);
        for (RestDataStoreExtension extension : supportedExtensions) {
            extension.beforeSave(context);
        }
        return objectMapper.writeValueAsString(entityJson);
    }

    /**
     * Calls {@link RestDataStoreExtension#afterSave}.
     *
     * @param savedEntityJson JSON returned by the remote service
     */
    public void afterSave(String dataStoreName, SaveContext saveContext, Object entity, Object savedEntity,
                          boolean isNew, String savedEntityJson) {
        List<RestDataStoreExtension> supportedExtensions = getExtensions(dataStoreName, metadata.getClass(entity));
        if (supportedExtensions.isEmpty()) {
            return;
        }
        AfterSaveContext context = new AfterSaveContext(dataStoreName, saveContext, entity, savedEntity, isNew,
                objectMapper.readTree(savedEntityJson));
        for (RestDataStoreExtension extension : supportedExtensions) {
            extension.afterSave(context);
        }
    }

    private List<RestDataStoreExtension> getExtensions(String dataStoreName, MetaClass metaClass) {
        if (extensions.isEmpty()) {
            return List.of();
        }
        return extensions.stream()
                .filter(extension -> extension.supports(dataStoreName, metaClass))
                .toList();
    }

    private List<JsonNode> getEntityJsons(JsonNode rootNode) {
        if (!rootNode.isArray()) {
            return List.of(rootNode);
        }
        List<JsonNode> result = new ArrayList<>(rootNode.size());
        for (JsonNode node : rootNode) {
            result.add(node);
        }
        return result;
    }
}
