/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.rest.api.service;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.gson.*;
import io.jmix.core.*;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.impl.importexport.EntityImportException;
import io.jmix.core.impl.importexport.EntityImportPlanJsonBuilder;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.data.accesscontext.CrudEntityContext;
import io.jmix.rest.RestProperties;
import io.jmix.rest.api.common.RestControllerUtils;
import io.jmix.rest.api.exception.RestAPIException;
import io.jmix.rest.api.service.filter.RestFilterParseException;
import io.jmix.rest.api.service.filter.RestFilterParseResult;
import io.jmix.rest.api.service.filter.RestFilterParser;
import io.jmix.rest.api.service.filter.data.EntitiesSearchResult;
import io.jmix.rest.api.service.filter.data.ResponseInfo;
import io.jmix.rest.api.transform.JsonTransformationDirection;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Class that executes business logic required by the {@link io.jmix.rest.api.controller.EntitiesController}. It
 * performs CRUD operations with entities
 */
@Component("rest_EntitiesControllerManager")
public class EntitiesControllerManager {

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected EntitySerialization entitySerialization;

    @Autowired
    protected EntityImportPlanJsonBuilder entityImportPlanJsonBuilder;

    @Autowired
    protected EntityImportExport entityImportExport;

    @Autowired
    protected AccessManager accessManager;

    @Autowired
    protected RestControllerUtils restControllerUtils;

    @Autowired
    protected RestFilterParser restFilterParser;

    @Autowired
    protected RestProperties restProperties;

    @Autowired
    protected EntityStates entityStates;

    @Autowired
    protected FetchPlanRepository fetchPlanRepository;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected FetchPlans fetchPlans;

    public String loadEntity(String entityName,
                             String entityId,
                             @Nullable String viewName,
                             @Nullable Boolean returnNulls,
                             @Nullable Boolean dynamicAttributes,
                             @Nullable String modelVersion) {

        entityName = restControllerUtils.transformEntityNameIfRequired(entityName, modelVersion, JsonTransformationDirection.FROM_VERSION);
        MetaClass metaClass = restControllerUtils.getMetaClass(entityName);
        checkCanReadEntity(metaClass);

        LoadContext<Object> ctx = new LoadContext<>(metaClass);
        Object id = getIdFromString(entityId, metaClass);
        ctx.setId(id);

        if (!Strings.isNullOrEmpty(viewName)) {
            FetchPlan view = restControllerUtils.getView(metaClass, viewName);
            ctx.setFetchPlan(view);
        }

        ctx.setHint("load_dyn_attr", BooleanUtils.isTrue(dynamicAttributes));

        Object entity = dataManager.load(ctx);
        checkEntityIsNotNull(entityName, entityId, entity);

        List<EntitySerializationOption> serializationOptions = new ArrayList<>();
        serializationOptions.add(EntitySerializationOption.SERIALIZE_INSTANCE_NAME);
        serializationOptions.add(EntitySerializationOption.DO_NOT_SERIALIZE_DENIED_PROPERTY);
        if (BooleanUtils.isTrue(returnNulls)) serializationOptions.add(EntitySerializationOption.SERIALIZE_NULLS);

        restControllerUtils.applyAttributesSecurity(entity);

        String json = entitySerialization.toJson(entity, ctx.getFetchPlan(), serializationOptions.toArray(new EntitySerializationOption[0]));
        json = restControllerUtils.transformJsonIfRequired(entityName, modelVersion, JsonTransformationDirection.TO_VERSION, json);
        return json;
    }

    public EntitiesSearchResult loadEntitiesList(String entityName,
                                                 @Nullable String viewName,
                                                 @Nullable Integer limit,
                                                 @Nullable Integer offset,
                                                 @Nullable String sort,
                                                 @Nullable Boolean returnNulls,
                                                 @Nullable Boolean returnCount,
                                                 @Nullable Boolean dynamicAttributes,
                                                 @Nullable String modelVersion) {
        entityName = restControllerUtils.transformEntityNameIfRequired(entityName, modelVersion, JsonTransformationDirection.FROM_VERSION);
        MetaClass metaClass = restControllerUtils.getMetaClass(entityName);
        checkCanReadEntity(metaClass);

        String queryString = "select e from " + entityName + " e";
        String json = _loadEntitiesList(queryString, viewName, limit, offset, sort, returnNulls, dynamicAttributes, modelVersion,
                metaClass, new HashMap<>());

        json = restControllerUtils.transformJsonIfRequired(entityName, modelVersion, JsonTransformationDirection.TO_VERSION, json);

        Long count = null;
        if (BooleanUtils.isTrue(returnCount)) {
            LoadContext ctx = new LoadContext(metadata.getClass(metaClass.getJavaClass()))
                    .setQuery(new LoadContext.Query(queryString));
            count = dataManager.getCount(ctx);
        }
        return new EntitiesSearchResult(json, count);

    }

    public EntitiesSearchResult searchEntities(String entityName,
                                               String filterJson,
                                               @Nullable String viewName,
                                               @Nullable Integer limit,
                                               @Nullable Integer offset,
                                               @Nullable String sort,
                                               @Nullable Boolean returnNulls,
                                               @Nullable Boolean returnCount,
                                               @Nullable Boolean dynamicAttributes,
                                               @Nullable String modelVersion) {
        if (filterJson == null) {
            throw new RestAPIException("Cannot parse entities filter", "Entities filter cannot be null", HttpStatus.BAD_REQUEST);
        }

        entityName = restControllerUtils.transformEntityNameIfRequired(entityName, modelVersion, JsonTransformationDirection.FROM_VERSION);
        MetaClass metaClass = restControllerUtils.getMetaClass(entityName);
        checkCanReadEntity(metaClass);

        RestFilterParseResult filterParseResult;
        try {
            filterParseResult = restFilterParser.parse(filterJson, metaClass);
        } catch (RestFilterParseException e) {
            throw new RestAPIException("Cannot parse entities filter", e.getMessage(), HttpStatus.BAD_REQUEST, e);
        }

        String jpqlWhere = filterParseResult.getJpqlWhere();
        Map<String, Object> queryParameters = filterParseResult.getQueryParameters();

        String queryString = "select e from " + entityName + " e";

        if (jpqlWhere != null) {
            queryString += " where " + jpqlWhere.replace("{E}", "e");
        }

        String json = _loadEntitiesList(queryString, viewName, limit, offset, sort, returnNulls,
                dynamicAttributes, modelVersion, metaClass, queryParameters);
        Long count = null;
        if (BooleanUtils.isTrue(returnCount)) {
            LoadContext ctx = new LoadContext(metadata.getClass(metaClass.getJavaClass()))
                    .setQuery(new LoadContext.Query(queryString).setParameters(queryParameters));
            count = dataManager.getCount(ctx);
        }

        return new EntitiesSearchResult(json, count);
    }

    public Long countSearchEntities(String entityName,
                                    String filterJson,
                                    @Nullable String modelVersion) {
        if (filterJson == null) {
            throw new RestAPIException("Cannot parse entities filter", "Entities filter cannot be null", HttpStatus.BAD_REQUEST);
        }

        entityName = restControllerUtils.transformEntityNameIfRequired(entityName, modelVersion, JsonTransformationDirection.FROM_VERSION);
        MetaClass metaClass = restControllerUtils.getMetaClass(entityName);
        checkCanReadEntity(metaClass);

        RestFilterParseResult filterParseResult;
        try {
            filterParseResult = restFilterParser.parse(filterJson, metaClass);
        } catch (RestFilterParseException e) {
            throw new RestAPIException("Cannot parse entities filter", e.getMessage(), HttpStatus.BAD_REQUEST, e);
        }

        String jpqlWhere = filterParseResult.getJpqlWhere();
        Map<String, Object> queryParameters = filterParseResult.getQueryParameters();

        String queryString = "select count(e) from " + entityName + " e";

        if (jpqlWhere != null) {
            queryString += " where " + jpqlWhere.replace("{E}", "e");
        }

        return dataManager.loadValue(queryString, Long.class)
                .setParameters(queryParameters)
                .one();
    }

    public EntitiesSearchResult searchEntities(String entityName, String searchRequestBody) {
        SearchEntitiesRequestDTO searchEntitiesRequest = new Gson()
                .fromJson(searchRequestBody, SearchEntitiesRequestDTO.class);

        if (searchEntitiesRequest.getFilter() == null) {
            throw new RestAPIException("Cannot parse entities filter", "Entities filter cannot be null", HttpStatus.BAD_REQUEST);
        }

        //for backward compatibility we should support both 'view' and 'viewName' properties. In future the
        //'viewName' parameter will be removed
        String view = !Strings.isNullOrEmpty(searchEntitiesRequest.getView()) ?
                searchEntitiesRequest.getView() :
                searchEntitiesRequest.getViewName();

        return searchEntities(entityName,
                searchEntitiesRequest.getFilter().toString(),
                view,
                searchEntitiesRequest.getLimit(),
                searchEntitiesRequest.getOffset(),
                searchEntitiesRequest.getSort(),
                searchEntitiesRequest.getReturnNulls(),
                searchEntitiesRequest.getReturnCount(),
                searchEntitiesRequest.getDynamicAttributes(),
                searchEntitiesRequest.getModelVersion()
        );
    }

    public Long countSearchEntities(String entityName, String searchRequestBody) {
        SearchEntitiesRequestDTO searchEntitiesRequest = new Gson()
                .fromJson(searchRequestBody, SearchEntitiesRequestDTO.class);

        if (searchEntitiesRequest.getFilter() == null) {
            throw new RestAPIException("Cannot parse entities filter", "Entities filter cannot be null", HttpStatus.BAD_REQUEST);
        }
        return countSearchEntities(entityName, searchEntitiesRequest.getFilter().toString(), searchEntitiesRequest.getModelVersion());
    }

    protected String _loadEntitiesList(String queryString,
                                       @Nullable String viewName,
                                       @Nullable Integer limit,
                                       @Nullable Integer offset,
                                       @Nullable String sort,
                                       @Nullable Boolean returnNulls,
                                       @Nullable Boolean dynamicAttributes,
                                       @Nullable String modelVersion,
                                       MetaClass metaClass,
                                       Map<String, Object> queryParameters) {
        LoadContext<Object> ctx = new LoadContext<>(metaClass);
        String orderedQueryString = addOrderBy(queryString, sort, metaClass);
        LoadContext.Query query = new LoadContext.Query(orderedQueryString);

        if (limit != null) {
            query.setMaxResults(limit);
        } else {
            query.setMaxResults(restProperties.getEntityMaxFetchSize(metaClass.getName()));
        }
        if (offset != null) {
            query.setFirstResult(offset);
        }
        if (queryParameters != null) {
            query.setParameters(queryParameters);
        }
        ctx.setQuery(query);

        FetchPlan view = null;
        if (!Strings.isNullOrEmpty(viewName)) {
            view = restControllerUtils.getView(metaClass, viewName);
            ctx.setFetchPlan(view);
        }

        ctx.setHint("load_dyn_attr", BooleanUtils.isTrue(dynamicAttributes));

        List<Object> entities = dataManager.loadList(ctx);
        entities.forEach(entity -> restControllerUtils.applyAttributesSecurity(entity));

        List<EntitySerializationOption> serializationOptions = new ArrayList<>();
        serializationOptions.add(EntitySerializationOption.SERIALIZE_INSTANCE_NAME);
        serializationOptions.add(EntitySerializationOption.DO_NOT_SERIALIZE_DENIED_PROPERTY);
        if (BooleanUtils.isTrue(returnNulls)) serializationOptions.add(EntitySerializationOption.SERIALIZE_NULLS);

        String json = entitySerialization.toJson(entities, view, serializationOptions.toArray(new EntitySerializationOption[0]));
        json = restControllerUtils.transformJsonIfRequired(metaClass.getName(), modelVersion, JsonTransformationDirection.TO_VERSION, json);
        return json;
    }

    protected String addOrderBy(String queryString, @Nullable String sort, MetaClass metaClass) {
        if (Strings.isNullOrEmpty(sort)) {
            return queryString;
        }
        StringBuilder orderBy = new StringBuilder(queryString).append(" order by ");
        Iterable<String> iterableColumns = Splitter.on(",").trimResults().omitEmptyStrings().split(sort);
        for (String column : iterableColumns) {
            String order = "";
            if (column.startsWith("-") || column.startsWith("+")) {
                order = column.substring(0, 1);
                column = column.substring(1);
            }
            MetaPropertyPath propertyPath = metaClass.getPropertyPath(column);
            if (propertyPath != null) {
                switch (order) {
                    case "-":
                        order = " desc, ";
                        break;
                    case "+":
                    default:
                        order = " asc, ";
                        break;
                }
                MetaProperty metaProperty = propertyPath.getMetaProperty();
                if (metaProperty.getRange().isClass()) {
                    if (!metaProperty.getRange().getCardinality().isMany()) {
                        for (String exp : getEntityPropertySortExpression(propertyPath)) {
                            orderBy.append(exp).append(order);
                        }
                    }
                } else {
                    orderBy.append("e.").append(column).append(order);
                }
            }
        }
        return orderBy.substring(0, orderBy.length() - 2);
    }

    protected List<String> getEntityPropertySortExpression(MetaPropertyPath metaPropertyPath) {
        Collection<MetaProperty> properties = metadataTools.getInstanceNameRelatedProperties(
                metaPropertyPath.getMetaProperty().getRange().asClass());
        if (!properties.isEmpty()) {
            List<String> sortExpressions = new ArrayList<>(properties.size());
            for (MetaProperty metaProperty : properties) {
                if (metadataTools.isPersistent(metaProperty)) {
                    MetaPropertyPath childPropertyPath = new MetaPropertyPath(metaPropertyPath, metaProperty);
                    if (metaProperty.getRange().isClass()) {
                        if (!metaProperty.getRange().getCardinality().isMany()) {
                            sortExpressions.addAll(getEntityPropertySortExpression(childPropertyPath));
                        }
                    } else {
                        sortExpressions.add(String.format("e.%s", childPropertyPath.toString()));
                    }
                }
            }
            return sortExpressions;
        } else {
            return Collections.singletonList(String.format("e.%s", metaPropertyPath.toString()));
        }
    }

    public ResponseInfo createEntity(String entityJson,
                                     String entityName,
                                     String responseView,
                                     String modelVersion,
                                     HttpServletRequest request) {
        JsonElement jsonElement = new JsonParser().parse(entityJson);

        ResponseInfo responseInfo;
        if (jsonElement.isJsonArray()) {
            responseInfo = createResponseInfoEntities(request, entityJson, entityName, responseView, modelVersion);
        } else {
            responseInfo = createResponseInfoEntity(request, entityJson, entityName, responseView, modelVersion);
        }
        return responseInfo;
    }

    protected ResponseInfo createResponseInfoEntity(HttpServletRequest request,
                                                    String entityJson,
                                                    String entityName,
                                                    String responseView,
                                                    String modelVersion) {
        String transformedEntityName = restControllerUtils.transformEntityNameIfRequired(entityName, modelVersion, JsonTransformationDirection.FROM_VERSION);
        MetaClass metaClass = restControllerUtils.getMetaClass(transformedEntityName);
        checkCanCreateEntity(metaClass);

        entityJson = restControllerUtils.transformJsonIfRequired(entityName, modelVersion, JsonTransformationDirection.FROM_VERSION, entityJson);

        Object entity = createEntityFromJson(metaClass, entityJson);

        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(request.getRequestURL().toString())
                .path("/{id}")
                .buildAndExpand(EntityValues.getId(entity).toString());

        if (restProperties.isResponseViewEnabled() && responseView != null && !entityStates.isLoadedWithFetchPlan(entity, responseView)) {
            LoadContext loadContext = new LoadContext(metaClass).setFetchPlan(restControllerUtils.getView(metaClass, responseView));
            loadContext.setId(EntityValues.getId(entity));
            entity = dataManager.load(loadContext);
        }
        restControllerUtils.applyAttributesSecurity(entity);
        String bodyJson = createEntityJson(entity, metaClass, responseView, modelVersion);
        return new ResponseInfo(uriComponents.toUri(), bodyJson);
    }

    protected ResponseInfo createResponseInfoEntities(HttpServletRequest request,
                                                      String entitiesJson,
                                                      String entityName,
                                                      String responseView,
                                                      String modelVersion) {
        String transformedEntityName = restControllerUtils.transformEntityNameIfRequired(entityName, modelVersion, JsonTransformationDirection.FROM_VERSION);
        MetaClass metaClass = restControllerUtils.getMetaClass(transformedEntityName);
        checkCanCreateEntity(metaClass);

        entitiesJson = restControllerUtils.transformJsonIfRequired(entityName, modelVersion, JsonTransformationDirection.FROM_VERSION, entitiesJson);

        Collection<Object> mainCollectionEntity = new ArrayList<>();
        JsonArray entitiesJsonArray = new JsonParser().parse(entitiesJson).getAsJsonArray();

        if (restProperties.isResponseViewEnabled() && responseView != null) {
            for (JsonElement jsonElement : entitiesJsonArray) {
                Object mainEntity = createEntityFromJson(metaClass, jsonElement.toString());
                if (!entityStates.isLoadedWithFetchPlan(mainEntity, responseView)) {
                    LoadContext loadContext = new LoadContext<>(metaClass).setFetchPlan(restControllerUtils.getView(metaClass, responseView));
                    loadContext.setId(EntityValues.getId(mainEntity));
                    mainEntity = dataManager.load(loadContext);
                }
                restControllerUtils.applyAttributesSecurity(mainEntity);
                mainCollectionEntity.add(mainEntity);
            }
        } else {
            for (JsonElement jsonElement : entitiesJsonArray) {
                Object mainEntity = createEntityFromJson(metaClass, jsonElement.toString());
                restControllerUtils.applyAttributesSecurity(mainEntity);
                mainCollectionEntity.add(mainEntity);
            }
        }
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(request.getRequestURL().toString()).buildAndExpand();
        String bodyJson = createEntitiesJson(mainCollectionEntity, metaClass, responseView, modelVersion);

        return new ResponseInfo(uriComponents.toUri(), bodyJson);
    }

    protected Object createEntityFromJson(MetaClass metaClass, String entityJson) {
        Object entity;
        try {
            entity = entitySerialization.entityFromJson(entityJson, metaClass);
        } catch (Exception e) {
            throw new RestAPIException("Cannot deserialize an entity from JSON", "", HttpStatus.BAD_REQUEST, e);
        }

        EntityImportPlan entityImportPlan = entityImportPlanJsonBuilder.buildFromJson(entityJson, metaClass);

        Collection<Object> importedEntities;
        try {
            importedEntities = entityImportExport.importEntities(Collections.singletonList(entity), entityImportPlan, true);
        } catch (EntityImportException e) {
            throw new RestAPIException("Entity creation failed", e.getMessage(), HttpStatus.BAD_REQUEST, e);
        }

        //if many entities were created (because of @Composition references) we must find the main entity
        return getMainEntity(importedEntities, metaClass);
    }

    public ResponseInfo updateEntity(String entityJson,
                                     String entityName,
                                     String entityId,
                                     String responseView,
                                     String modelVersion) {
        String transformedEntityName = restControllerUtils.transformEntityNameIfRequired(entityName, modelVersion, JsonTransformationDirection.FROM_VERSION);
        MetaClass metaClass = restControllerUtils.getMetaClass(transformedEntityName);
        checkCanUpdateEntity(metaClass);

        //there may be multiple entities in importedEntities (because of @Composition references), so we must find
        // the main entity that will be returned
        Object entity = getUpdatedEntity(entityName, modelVersion, transformedEntityName, metaClass, entityJson, entityId);
        if (restProperties.isResponseViewEnabled() && responseView != null && !entityStates.isLoadedWithFetchPlan(entity, responseView)) {
            LoadContext loadContext = new LoadContext<>(metaClass).setFetchPlan(restControllerUtils.getView(metaClass, responseView));
            loadContext.setId(EntityValues.getId(entity));
            entity = dataManager.load(loadContext);
        }
        restControllerUtils.applyAttributesSecurity(entity);
        String bodyJson = createEntityJson(entity, metaClass, responseView, modelVersion);
        return new ResponseInfo(null, bodyJson);
    }

    public ResponseInfo updateEntities(String entitiesJson,
                                       String entityName,
                                       String responseView,
                                       String modelVersion) {
        String transformedEntityName = restControllerUtils.transformEntityNameIfRequired(entityName, modelVersion, JsonTransformationDirection.FROM_VERSION);
        MetaClass metaClass = restControllerUtils.getMetaClass(transformedEntityName);
        checkCanUpdateEntity(metaClass);

        JsonArray entitiesJsonArray = new JsonParser().parse(entitiesJson).getAsJsonArray();
        Collection<Object> entities = new ArrayList<>();
        if (restProperties.isResponseViewEnabled() && responseView != null) {
            for (JsonElement jsonElement : entitiesJsonArray) {
                String entityId = jsonElement.getAsJsonObject().get("id").getAsString();
                Object entity = getUpdatedEntity(entityName, modelVersion, transformedEntityName, metaClass, jsonElement.toString(), entityId);
                if (!entityStates.isLoadedWithFetchPlan(entity, responseView)) {
                    LoadContext loadContext = new LoadContext<>(metaClass).setFetchPlan(restControllerUtils.getView(metaClass, responseView));
                    loadContext.setId(EntityValues.getId(entity));
                    entity = dataManager.load(loadContext);
                }
                restControllerUtils.applyAttributesSecurity(entity);
                entities.add(entity);
            }
        } else {
            for (JsonElement jsonElement : entitiesJsonArray) {
                String entityId = jsonElement.getAsJsonObject().get("id").getAsString();
                Object entity = getUpdatedEntity(entityName, modelVersion, transformedEntityName, metaClass, jsonElement.toString(), entityId);
                restControllerUtils.applyAttributesSecurity(entity);
                entities.add(entity);
            }
        }
        String bodyJson = createEntitiesJson(entities, metaClass, responseView, modelVersion);
        return new ResponseInfo(null, bodyJson);
    }

    protected Object getUpdatedEntity(String entityName,
                                      String modelVersion,
                                      String transformedEntityName,
                                      MetaClass metaClass,
                                      String entityJson,
                                      String entityId) {
        Object id = getIdFromString(entityId, metaClass);

        LoadContext loadContext = new LoadContext(metaClass).setId(id);
        @SuppressWarnings("unchecked")
        Object existingEntity = dataManager.load(loadContext);

        checkEntityIsNotNull(transformedEntityName, entityId, existingEntity);
        entityJson = restControllerUtils.transformJsonIfRequired(entityName, modelVersion, JsonTransformationDirection.FROM_VERSION, entityJson);

        Object entity;
        try {
            entity = entitySerialization.entityFromJson(entityJson, metaClass);
        } catch (Exception e) {
            throw new RestAPIException("Cannot deserialize an entity from JSON", "", HttpStatus.BAD_REQUEST, e);
        }

        //noinspection unchecked
        EntityValues.setId(entity, id);

        EntityImportPlan entityImportPlan = entityImportPlanJsonBuilder.buildFromJson(entityJson, metaClass);
        Collection<Object> importedEntities;
        try {
            importedEntities = entityImportExport.importEntities(Collections.singletonList(entity),
                    entityImportPlan, true, restProperties.isOptimisticLockingEnabled());
        } catch (EntityImportException e) {
            throw new RestAPIException("Entity update failed", e.getMessage(), HttpStatus.BAD_REQUEST, e);
        }

        //there may be multiple entities in importedEntities (because of @Composition references), so we must find
        // the main entity that will be returned
        return getMainEntity(importedEntities, metaClass);
    }

    public void deleteEntity(String entityName,
                             String entityId,
                             String modelVersion) {
        entityName = restControllerUtils.transformEntityNameIfRequired(entityName, modelVersion, JsonTransformationDirection.FROM_VERSION);
        MetaClass metaClass = restControllerUtils.getMetaClass(entityName);
        checkCanDeleteEntity(metaClass);
        Object id = getIdFromString(entityId, metaClass);
        Object entity = dataManager.load(new LoadContext<>(metaClass).setId(id));
        checkEntityIsNotNull(entityName, entityId, entity);
        dataManager.remove(entity);
    }

    public void deleteEntities(String entityName,
                               String entitiesIdJson,
                               String modelVersion) {
        entityName = restControllerUtils.transformEntityNameIfRequired(entityName, modelVersion, JsonTransformationDirection.FROM_VERSION);
        MetaClass metaClass = restControllerUtils.getMetaClass(entityName);
        checkCanDeleteEntity(metaClass);

        JsonArray entitiesJsonArray = new JsonParser().parse(entitiesIdJson).getAsJsonArray();

        for (int i = 0; i < entitiesJsonArray.size(); i++) {
            String entityId = entitiesJsonArray.get(i).getAsString();

            Object id = getIdFromString(entityId, metaClass);
            Object entity = dataManager.load(new LoadContext<>(metaClass).setId(id));
            checkEntityIsNotNull(entityName, entityId, entity);
            dataManager.remove(entity);
        }
    }

    private Object getIdFromString(String entityId, MetaClass metaClass) {
        try {
            Method getIdMethod = metaClass.getJavaClass().getMethod("getId");
            Class<?> idClass = getIdMethod.getReturnType();
            if (UUID.class.isAssignableFrom(idClass)) {
                return UUID.fromString(entityId);
            } else if (Integer.class.isAssignableFrom(idClass)) {
                return Integer.valueOf(entityId);
            } else if (Long.class.isAssignableFrom(idClass)) {
                return Long.valueOf(entityId);
            } else {
                return entityId;
            }
        } catch (Exception e) {
            throw new RestAPIException("Invalid entity ID",
                    String.format("Cannot convert %s into valid entity ID", entityId),
                    HttpStatus.BAD_REQUEST,
                    e);
        }
    }

    protected void checkEntityIsNotNull(String entityName, String entityId, Object entity) {
        if (entity == null) {
            throw new RestAPIException("Entity not found",
                    String.format("Entity %s with id %s not found", entityName, entityId),
                    HttpStatus.NOT_FOUND);
        }
    }

    protected void checkCanReadEntity(MetaClass metaClass) {
        CrudEntityContext entityContext = applyEntityConstraints(metaClass);
        if (!entityContext.isReadPermitted()) {
            throw new RestAPIException("Reading forbidden",
                    String.format("Reading of the %s is forbidden", metaClass.getName()),
                    HttpStatus.FORBIDDEN);
        }
    }

    protected void checkCanCreateEntity(MetaClass metaClass) {
        CrudEntityContext entityContext = applyEntityConstraints(metaClass);
        if (!entityContext.isCreatePermitted()) {
            throw new RestAPIException("Creation forbidden",
                    String.format("Creation of the %s is forbidden", metaClass.getName()),
                    HttpStatus.FORBIDDEN);
        }
    }

    protected void checkCanDeleteEntity(MetaClass metaClass) {
        CrudEntityContext entityContext = applyEntityConstraints(metaClass);
        if (!entityContext.isDeletePermitted()) {
            throw new RestAPIException("Deletion forbidden",
                    String.format("Deletion of the %s is forbidden", metaClass.getName()),
                    HttpStatus.FORBIDDEN);
        }
    }

    protected void checkCanUpdateEntity(MetaClass metaClass) {
        CrudEntityContext entityContext = applyEntityConstraints(metaClass);
        if (!entityContext.isUpdatePermitted()) {
            throw new RestAPIException("Updating forbidden",
                    String.format("Updating of the %s is forbidden", metaClass.getName()),
                    HttpStatus.FORBIDDEN);
        }
    }

    protected CrudEntityContext applyEntityConstraints(MetaClass metaClass) {
        CrudEntityContext entityContext = new CrudEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(entityContext);
        return entityContext;
    }

    /**
     * Finds entity with given metaClass.
     */
    protected Object getMainEntity(Collection<Object> importedEntities, MetaClass metaClass) {
        Object mainEntity = null;
        if (importedEntities.size() > 1) {
            Optional<Object> first = importedEntities.stream().filter(e -> metadata.getClass(e).equals(metaClass)).findFirst();
            if (first.isPresent()) mainEntity = first.get();
        } else {
            mainEntity = importedEntities.iterator().next();
        }
        return mainEntity;
    }

    /**
     * We pass the EntitySerializationOption.DO_NOT_SERIALIZE_RO_NON_PERSISTENT_PROPERTIES because for create and update
     * operations in the result JSON we don't want to return results for entity methods annotated with @MetaProperty
     * annotation. We do this because such methods may use other entities properties (references to other entities) and
     * as a result we get an UnfetchedAttributeException while producing the JSON for response
     */
    protected String createEntityJson(Object entity, MetaClass metaClass, String responseView, String version) {
        Preconditions.checkNotNullArgument(entity);

        String json;
        if (restProperties.isResponseViewEnabled()) {
            FetchPlan view = findOrCreateResponseView(metaClass, responseView);
            json = entitySerialization.toJson(entity, view, EntitySerializationOption.SERIALIZE_INSTANCE_NAME);
        } else {
            json = entitySerialization.toJson(entity, null, EntitySerializationOption.DO_NOT_SERIALIZE_RO_NON_PERSISTENT_PROPERTIES);
        }
        return restControllerUtils.transformJsonIfRequired(metaClass.getName(), version, JsonTransformationDirection.TO_VERSION, json);
    }

    protected String createEntitiesJson(Collection<Object> entities, MetaClass metaClass, String responseView, String version) {
        String json;
        if (restProperties.isResponseViewEnabled()) {
            FetchPlan view = findOrCreateResponseView(metaClass, responseView);
            json = entitySerialization.toJson(entities, view, EntitySerializationOption.SERIALIZE_INSTANCE_NAME);
        } else {
            json = entitySerialization.toJson(entities, null, EntitySerializationOption.DO_NOT_SERIALIZE_RO_NON_PERSISTENT_PROPERTIES);
        }
        json = restControllerUtils.transformJsonIfRequired(metaClass.getName(), version, JsonTransformationDirection.TO_VERSION, json);
        return json;
    }

    protected FetchPlan findOrCreateResponseView(MetaClass metaClass, String responseView) {
        if (StringUtils.isEmpty(responseView)) {
            //noinspection ConstantConditions
            return fetchPlans.builder(metaClass.getJavaClass())
                    .add(metadataTools.getPrimaryKeyName(metaClass))
                    .build();
        }

        FetchPlan view = fetchPlanRepository.findFetchPlan(metaClass, responseView);

        if (view == null) {
            throw new RestAPIException("View not found",
                    String.format("View '%s' not found for entity '%s'", responseView, metaClass.getName()),
                    HttpStatus.NOT_FOUND);
        }
        return view;
    }


    protected class SearchEntitiesRequestDTO {
        protected JsonObject filter;
        protected String view;
        @Deprecated
        //the viewName property has been left for a backward compatibility. It will removed in future releases
        protected String viewName;
        protected Integer limit;
        protected Integer offset;
        protected String sort;
        protected Boolean returnNulls;
        protected Boolean returnCount;
        protected Boolean dynamicAttributes;
        protected String modelVersion;

        public SearchEntitiesRequestDTO() {
        }

        public JsonObject getFilter() {
            return filter;
        }

        public String getView() {
            return view;
        }

        @Deprecated
        public String getViewName() {
            return viewName;
        }

        public Integer getLimit() {
            return limit;
        }

        public Integer getOffset() {
            return offset;
        }

        public String getSort() {
            return sort;
        }

        public Boolean getReturnNulls() {
            return returnNulls;
        }

        public Boolean getReturnCount() {
            return returnCount;
        }

        public Boolean getDynamicAttributes() {
            return dynamicAttributes;
        }

        public String getModelVersion() {
            return modelVersion;
        }

        public void setFilter(JsonObject filter) {
            this.filter = filter;
        }

        public void setView(String view) {
            this.view = view;
        }

        @Deprecated
        public void setViewName(String viewName) {
            this.viewName = viewName;
        }

        public void setLimit(Integer limit) {
            this.limit = limit;
        }

        public void setOffset(Integer offset) {
            this.offset = offset;
        }

        public void setSort(String sort) {
            this.sort = sort;
        }

        public void setReturnNulls(Boolean returnNulls) {
            this.returnNulls = returnNulls;
        }

        public void setReturnCount(Boolean returnCount) {
            this.returnCount = returnCount;
        }

        public void setDynamicAttributes(Boolean dynamicAttributes) {
            this.dynamicAttributes = dynamicAttributes;
        }

        public void setModelVersion(String modelVersion) {
            this.modelVersion = modelVersion;
        }
    }
}
