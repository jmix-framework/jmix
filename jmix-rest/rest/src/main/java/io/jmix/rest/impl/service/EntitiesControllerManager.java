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

package io.jmix.rest.impl.service;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.gson.*;
import io.jmix.core.*;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.core.common.datastruct.Pair;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.impl.importexport.EntityImportException;
import io.jmix.core.impl.importexport.EntityImportPlanJsonBuilder;
import io.jmix.core.impl.serialization.EntitySerializationException;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.validation.EntityValidationException;
import io.jmix.core.validation.group.RestApiChecks;
import io.jmix.rest.RestProperties;
import io.jmix.rest.exception.RestAPIException;
import io.jmix.rest.impl.RestControllerUtils;
import io.jmix.rest.impl.service.filter.RestFilterParseException;
import io.jmix.rest.impl.service.filter.RestFilterParseResult;
import io.jmix.rest.impl.service.filter.RestFilterParser;
import io.jmix.rest.impl.service.filter.data.EntitiesSearchResult;
import io.jmix.rest.impl.service.filter.data.ResponseInfo;
import io.jmix.rest.transform.JsonTransformationDirection;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static io.jmix.core.EntitySerializationOption.*;

/**
 * Class that executes business logic required by the {@link io.jmix.rest.impl.controller.EntitiesController}. It
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

    @Autowired
    protected Validator validator;

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

        ctx.setHint("jmix.dynattr", BooleanUtils.isTrue(dynamicAttributes));

        Object entity = dataManager.load(ctx);
        checkEntityIsNotNull(entityName, entityId, entity);

        List<EntitySerializationOption> serializationOptions = new ArrayList<>();
        serializationOptions.add(SERIALIZE_INSTANCE_NAME);
        serializationOptions.add(DO_NOT_SERIALIZE_DENIED_PROPERTY);
        if (BooleanUtils.isTrue(returnNulls)) serializationOptions.add(EntitySerializationOption.SERIALIZE_NULLS);

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
                    .setQuery(new LoadContext.Query(queryString));
            if (queryParameters != null) {
                ctx.getQuery().setParameters(queryParameters);
            }
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

        view = !StringUtils.isEmpty(searchEntitiesRequest.getFetchPlan()) ? searchEntitiesRequest.getFetchPlan() : view;

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

        int limitFromProperties = restProperties.getEntityMaxFetchSize(metaClass.getName());
        if (limit != null && limit > limitFromProperties) {
            throw new RestAPIException("The value of limit exceeded", "The value of the limit exceeds the maximum possible value from application.properties", HttpStatus.BAD_REQUEST);
        }
        if (limit != null) {
            query.setMaxResults(limit);
        } else {
            query.setMaxResults(limitFromProperties);
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

        ctx.setHint("jmix.dynattr", BooleanUtils.isTrue(dynamicAttributes));

        List<Object> entities = dataManager.loadList(ctx);

        List<EntitySerializationOption> serializationOptions = new ArrayList<>();
        serializationOptions.add(SERIALIZE_INSTANCE_NAME);
        serializationOptions.add(DO_NOT_SERIALIZE_DENIED_PROPERTY);
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
                if (metadataTools.isJpa(metaProperty)) {
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

        JsonElement jsonElement;
        try {
            jsonElement = new JsonParser().parse(entityJson);
        } catch (JsonSyntaxException e) {
            throw new RestAPIException("Malformed request JSON data structure", "", HttpStatus.BAD_REQUEST, e);
        }

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

        FetchPlan responseFetchPlan = null;
        if (responseView != null) {
            responseFetchPlan = restControllerUtils.getView(metaClass, responseView);
        }

        entityJson = restControllerUtils.transformJsonIfRequired(entityName, modelVersion, JsonTransformationDirection.FROM_VERSION, entityJson);

        Object entity = createEntityFromJson(metaClass, entityJson);

        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(request.getRequestURL().toString())
                .path("/{id}")
                .buildAndExpand(EntityValues.getId(entity).toString());

        if (restProperties.isResponseFetchPlanEnabled() && responseFetchPlan != null && !entityStates.isLoadedWithFetchPlan(entity, responseFetchPlan)) {
            LoadContext loadContext = new LoadContext(metaClass).setFetchPlan(responseFetchPlan);
            loadContext.setId(EntityValues.getId(entity));
            entity = dataManager.load(loadContext);
        }
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

        FetchPlan responseFetchPlan = null;
        if (responseView != null) {
            responseFetchPlan = restControllerUtils.getView(metaClass, responseView);
        }

        entitiesJson = restControllerUtils.transformJsonIfRequired(entityName, modelVersion, JsonTransformationDirection.FROM_VERSION, entitiesJson);

        JsonArray entitiesJsonArray = new JsonParser().parse(entitiesJson).getAsJsonArray();

        List<Object> mainCollectionEntity = new ArrayList<>(createEntitiesFromJson(metaClass, entitiesJsonArray));

        if (restProperties.isResponseFetchPlanEnabled() && responseFetchPlan != null) {
            for (Object entity : mainCollectionEntity) {
                if (!entityStates.isLoadedWithFetchPlan(entity, responseFetchPlan)) {
                    LoadContext loadContext = new LoadContext<>(metaClass).setFetchPlan(responseFetchPlan);
                    loadContext.setId(EntityValues.getId(entity));
                    mainCollectionEntity.set(mainCollectionEntity.indexOf(entity), dataManager.load(loadContext));
                }
            }
        }

        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(request.getRequestURL().toString()).buildAndExpand();
        String bodyJson = createEntitiesJson(mainCollectionEntity, metaClass, responseView, modelVersion);

        return new ResponseInfo(uriComponents.toUri(), bodyJson);
    }

    /**
     * Validates entities using {@link ValidatedList}.
     * If validate the collection of entities using the "n validation for n objects" approach we can not handle
     * the information about which entity failed validation. To achieve that we collect the root entities
     * (what entities should be saved, except composition references, etc) and validate the {@link ValidatedList} with
     * list of root entities.
     * Also if the root object has the reference entity annotated as {@link Valid} we should exclude that entity from the validation.
     *
     * @param rootEntities collection of main entity
     * @param entities     collection of entities to validate
     */
    @SuppressWarnings("unchecked")
    protected void validateEntities(Collection<Object> rootEntities, Collection<Object> entities) {
        Collection<Pair<Object, Object>> referencesToExclude = new ArrayList<>();
        for (Object entity : entities) {
            for (MetaProperty metaProperty : metadata.getClass(entity).getProperties()) {
                if (metaProperty.getRange().isClass()) {
                    Object reference = EntityValues.getValue(entity, metaProperty.getName());
                    if (reference != null && !(reference instanceof Collection)) {
                        reference = Collections.singletonList(reference);
                    }
                    //to handle composition references marked as @Valid
                    if (reference != null && metadataTools.isAnnotationPresent(entity, metaProperty.getName(), Valid.class)) {
                        ((Collection<Object>) reference).stream()
                                //to handle one-to-many composition. when the composition collection objects has a reference to the root entity
                                .filter(x -> !referencesToExclude.contains(new Pair<>(x, entity)))
                                .forEach(x -> referencesToExclude.add(new Pair<>(entity, x)));
                    }
                }
            }
        }
        entities.removeAll(referencesToExclude.stream().map(Pair::getSecond).collect(Collectors.toList()));

        rootEntities = CollectionUtils.retainAll(entities, rootEntities);
        entities.removeAll(rootEntities);
        entities.add(new ValidatedList(rootEntities));

        Set<ConstraintViolation<Object>> violations = new LinkedHashSet<>();
        entities.forEach(entity ->
                violations.addAll(validator.validate(entity, Default.class, RestApiChecks.class)));
        if (!violations.isEmpty()) {
            throw new EntityValidationException("Entity validation failed", violations);
        }
    }

    protected Object createEntityFromJson(MetaClass metaClass, String entityJson) {
        Object entity;
        try {
            entity = entitySerialization.entityFromJson(entityJson, metaClass);
        } catch (EntitySerializationException e) {
            throw new RestAPIException(e.getMessage(), "", HttpStatus.BAD_REQUEST);
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

    protected List<Object> createEntitiesFromJson(MetaClass metaClass, JsonArray entitiesJsonArray) {
        Map<Object, EntityImportPlan> objectEntityImportPlanMap = new LinkedHashMap<>();
        Object entity;
        EntityImportPlan entityImportPlan;
        try {
            for (JsonElement element : entitiesJsonArray) {
                entity = entitySerialization.entityFromJson(element.toString(), metaClass);
                entityImportPlan = entityImportPlanJsonBuilder.buildFromJson(element.toString(), metaClass);
                objectEntityImportPlanMap.put(entity, entityImportPlan);
            }
        } catch (Exception e) {
            throw new RestAPIException("Cannot deserialize an entity from JSON", "", HttpStatus.BAD_REQUEST, e);
        }
        Collection<Object> mainEntities = objectEntityImportPlanMap.keySet();
        SaveContext saveContext = new SaveContext();

        try {
            for (Map.Entry<Object, EntityImportPlan> entry : objectEntityImportPlanMap.entrySet()) {
                entityImportExport.importEntityIntoSaveContext(saveContext, entry.getKey(), entry.getValue(), false);
            }

            validateEntities(mainEntities, new LinkedHashSet<>(saveContext.getEntitiesToSave()));
            mainEntities = CollectionUtils.retainAll(dataManager.save(saveContext), mainEntities);

        } catch (EntityImportException e) {
            throw new RestAPIException("Entity creation failed", e.getMessage(), HttpStatus.BAD_REQUEST, e);
        }

        return new ArrayList<>(mainEntities);
    }

    public ResponseInfo updateEntity(String entityJson,
                                     String entityName,
                                     String entityId,
                                     String responseView,
                                     String modelVersion) {
        String transformedEntityName = restControllerUtils.transformEntityNameIfRequired(entityName, modelVersion, JsonTransformationDirection.FROM_VERSION);
        MetaClass metaClass = restControllerUtils.getMetaClass(transformedEntityName);
        checkCanUpdateEntity(metaClass);

        FetchPlan responseFetchPlan = null;
        if (responseView != null) {
            responseFetchPlan = restControllerUtils.getView(metaClass, responseView);
        }

        //there may be multiple entities in importedEntities (because of @Composition references), so we must find
        // the main entity that will be returned
        Object entity = getUpdatedEntity(entityName, modelVersion, transformedEntityName, metaClass, entityJson, entityId);
        if (restProperties.isResponseFetchPlanEnabled() && responseFetchPlan != null && !entityStates.isLoadedWithFetchPlan(entity, responseFetchPlan)) {
            LoadContext loadContext = new LoadContext<>(metaClass).setFetchPlan(responseFetchPlan);
            loadContext.setId(EntityValues.getId(entity));
            entity = dataManager.load(loadContext);
        }
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

        FetchPlan responseFetchPlan = responseView == null ? null : restControllerUtils.getView(metaClass, responseView);

        JsonElement entitiesJsonElement = new JsonParser().parse(entitiesJson);
        if (!entitiesJsonElement.isJsonArray()) {
            throw new RestAPIException("The body of bulk update request should be an array",
                    "The body of bulk update request should be an array",
                    HttpStatus.BAD_REQUEST);
        }

        JsonArray entitiesJsonArray = entitiesJsonElement.getAsJsonArray();
        Collection<Object> updatedEntities = getUpdatedEntities(entityName, modelVersion, transformedEntityName,
                metaClass, entitiesJsonArray);
        if (restProperties.isResponseFetchPlanEnabled() && responseFetchPlan != null) {
            updatedEntities = updatedEntities.stream().map(entity -> {
                if (!entityStates.isLoadedWithFetchPlan(entity, responseFetchPlan)) {
                    LoadContext<?> loadContext = new LoadContext<>(metaClass).setFetchPlan(responseFetchPlan);
                    loadContext.setId(EntityValues.getId(entity));
                    return dataManager.load(loadContext);
                } else {
                    return entity;
                }
            }).collect(Collectors.toList());
        }
        String bodyJson = createEntitiesJson(updatedEntities, metaClass, responseView, modelVersion);
        return new ResponseInfo(null, bodyJson);
    }

    protected Collection<Object> getUpdatedEntities(String entityName,
                                                    String modelVersion,
                                                    String transformedEntityName,
                                                    MetaClass metaClass,
                                                    JsonArray entitiesJsonArray) {
        Map<Object, EntityImportPlan> objectEntityImportPlanMap = new LinkedHashMap<>();
        Object entity;
        EntityImportPlan entityImportPlan;
        for (JsonElement element : entitiesJsonArray) {
            String entityJson = element.toString();
            String idString = element.getAsJsonObject()
                    .get(Objects.requireNonNull(metadataTools.getPrimaryKeyName(metaClass)))
                    .getAsString();
            Object id = getIdFromString(idString, metaClass);
            LoadContext<Object> loadContext = new LoadContext<>(metaClass).setId(id);
            Object existingEntity = dataManager.load(loadContext);

            checkEntityIsNotNull(transformedEntityName, idString, existingEntity);
            entityJson = restControllerUtils.transformJsonIfRequired(entityName, modelVersion,
                    JsonTransformationDirection.FROM_VERSION, entityJson);
            try {
                entity = entitySerialization.entityFromJson(entityJson, metaClass);
            } catch (Exception e) {
                throw new RestAPIException("Cannot deserialize an entity from JSON", "", HttpStatus.BAD_REQUEST, e);
            }

            EntityValues.setId(entity, id);
            entityImportPlan = entityImportPlanJsonBuilder.buildFromJson(entityJson, metaClass);
            objectEntityImportPlanMap.put(entity, entityImportPlan);
        }

        Collection<Object> mainEntities = objectEntityImportPlanMap.keySet();
        SaveContext saveContext = new SaveContext();
        try {
            for (Map.Entry<Object, EntityImportPlan> entry : objectEntityImportPlanMap.entrySet()) {
                entityImportExport.importEntityIntoSaveContext(saveContext, entry.getKey(), entry.getValue(),
                        false, restProperties.isOptimisticLockingEnabled());
            }

            validateEntities(mainEntities, new LinkedHashSet<>(saveContext.getEntitiesToSave()));
            mainEntities = CollectionUtils.retainAll(dataManager.save(saveContext), mainEntities);
        } catch (EntityImportException e) {
            throw new RestAPIException("Entity update failed", e.getMessage(), HttpStatus.BAD_REQUEST, e);
        }
        return mainEntities;
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

        JsonArray entitiesJsonArray = JsonParser.parseString(entitiesIdJson).getAsJsonArray();

        for (int i = 0; i < entitiesJsonArray.size(); i++) {
            JsonElement element = entitiesJsonArray.get(i);

            if (element.isJsonObject()) {
                element = element.getAsJsonObject().get("id");
                if (element == null) {
                    throw new RestAPIException("Required attribute id is not presented",
                            "Required attribute id is not presented",
                            HttpStatus.BAD_REQUEST);
                }
            }

            String entityId = element.getAsString();
            Object id = getIdFromString(entityId, metaClass);
            Object entity = dataManager.load(new LoadContext<>(metaClass).setId(id));
            checkEntityIsNotNull(entityName, entityId, entity);
            dataManager.remove(entity);
        }
    }

    private Object getIdFromString(String entityId, MetaClass metaClass) {
        try {
            MetaProperty primaryKeyProperty = Objects.requireNonNull(metadataTools.getPrimaryKeyProperty(metaClass));

            Class<?> idClass = primaryKeyProperty.getJavaType();
            if (UUID.class.isAssignableFrom(idClass)) {
                return UUID.fromString(entityId);
            } else if (Integer.class.isAssignableFrom(idClass)) {
                return Integer.valueOf(entityId);
            } else if (Long.class.isAssignableFrom(idClass)) {
                return Long.valueOf(entityId);
            } else {
                if (metadataTools.hasCompositePrimaryKey(metaClass)) {
                    String entityIdJson = new String(Base64.getUrlDecoder().decode(entityId), StandardCharsets.UTF_8);
                    return entitySerialization.entityFromJson(entityIdJson, metadata.getClass(idClass));
                } else {
                    return entityId;
                }
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
    @Nullable
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
        if (restProperties.isResponseFetchPlanEnabled()) {
            FetchPlan view = findOrCreateResponseView(metaClass, responseView);
            json = entitySerialization.toJson(entity, view, SERIALIZE_INSTANCE_NAME);
        } else {
            json = entitySerialization.toJson(entity, null, DO_NOT_SERIALIZE_RO_NON_PERSISTENT_PROPERTIES);
        }
        return restControllerUtils.transformJsonIfRequired(metaClass.getName(), version, JsonTransformationDirection.TO_VERSION, json);
    }

    protected String createEntitiesJson(Collection<Object> entities, MetaClass metaClass, String responseView, String version) {
        String json;
        if (restProperties.isResponseFetchPlanEnabled()) {
            FetchPlan view = findOrCreateResponseView(metaClass, responseView);
            json = entitySerialization.toJson(entities, view, SERIALIZE_INSTANCE_NAME,
                    DO_NOT_SERIALIZE_DENIED_PROPERTY);
        } else {
            json = entitySerialization.toJson(entities, null,
                    DO_NOT_SERIALIZE_RO_NON_PERSISTENT_PROPERTIES, DO_NOT_SERIALIZE_DENIED_PROPERTY);
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
            throw new RestAPIException("Fetch plan not found",
                    String.format("Fetch plan '%s' not found for entity '%s'", responseView, metaClass.getName()),
                    HttpStatus.NOT_FOUND);
        }
        return view;
    }

    protected static class ValidatedList {
        @Valid
        protected Collection<Object> entities;

        public ValidatedList(Collection<Object> entities) {
            this.entities = entities;
        }

        public Collection<Object> getEntities() {
            return entities;
        }

        public void setEntities(Collection<Object> entities) {
            this.entities = entities;
        }
    }

    protected class SearchEntitiesRequestDTO {
        protected JsonObject filter;
        protected String view;
        protected String fetchPlan;
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

        public String getFetchPlan() {
            return fetchPlan;
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

        public void setFetchPlan(String fetchPlan) {
            this.fetchPlan = fetchPlan;
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
