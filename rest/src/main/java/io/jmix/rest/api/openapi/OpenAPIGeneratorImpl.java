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

package io.jmix.rest.api.openapi;

import com.google.common.base.Strings;
import io.jmix.core.CoreProperties;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.Resources;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetadataObject;
import io.jmix.rest.api.config.RestQueriesConfiguration;
import io.jmix.rest.api.config.RestQueriesConfiguration.QueryInfo;
import io.jmix.rest.api.config.RestServicesConfiguration;
import io.jmix.rest.api.config.RestServicesConfiguration.RestMethodInfo;
import io.jmix.rest.api.config.RestServicesConfiguration.RestServiceInfo;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletContext;
import java.util.*;
import java.util.stream.Collectors;

import static io.jmix.core.impl.serialization.EntitySerializationImpl.ENTITY_NAME_PROP;
import static io.jmix.core.impl.serialization.EntitySerializationImpl.INSTANCE_NAME_PROP;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component("rest_OpenAPIGenerator")
public class OpenAPIGeneratorImpl implements OpenAPIGenerator {

    private static final Logger log = LoggerFactory.getLogger(OpenAPIGeneratorImpl.class);

    protected static final String ENTITY_PATH = "/entities/%s";
    protected static final String ENTITY_RUD_OPS = "/entities/%s/{entityId}";
    protected static final String ENTITY_SEARCH = "/entities/%s/search";

    protected static final String QUERY_PATH = "/queries/%s/%s";
    protected static final String QUERY_COUNT_PATH = "/queries/%s/%s/count";

    protected static final String SERVICE_PATH = "/services/%s/%s";

    protected static final String SCHEMAS_PREFIX = "#/components/schemas/";
    protected static final String ARRAY_SIGNATURE = "[]";

    @Autowired
    protected CoreProperties coreProperties;
    @Autowired(required = false)
    protected ServletContext servletContext;
    @Autowired
    protected Resources resources;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected RestQueriesConfiguration queriesConfiguration;
    @Autowired
    protected RestServicesConfiguration servicesConfiguration;

    protected OpenAPI openAPI = null;

    private volatile boolean initialized = false;

    @Override
    public OpenAPI generateOpenAPI() {
        checkInitialized();
        return openAPI;
    }

    protected void checkInitialized() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    log.info("Generating OpenAPI documentation");
                    init();
                    initialized = true;
                }
            }
        }
    }

    protected void init() {
        openAPI = new OpenAPI();

        buildServer(openAPI);
        buildInfo(openAPI);
        buildTags(openAPI);
        buildErrorSchema(openAPI);


        buildEntitiesPaths(openAPI);
        buildQueriesPaths(openAPI);
        buildServicesPaths(openAPI);
    }

    protected void buildServer(OpenAPI openAPI) {
        String contextPath = servletContext == null ? null : servletContext.getContextPath();

        StringBuilder url = new StringBuilder();

        if (!Strings.isNullOrEmpty(coreProperties.getWebHostName())) {
            url.append(coreProperties.getWebHostName());
        }

        if (!Strings.isNullOrEmpty(coreProperties.getWebPort())) {
            url.append(":").append(coreProperties.getWebPort());
        }

        if (!Strings.isNullOrEmpty(contextPath)) {
            url.append(contextPath);
        }

        url.append("/rest");

        openAPI.addServersItem(new Server().url(url.toString()));
    }

    protected void buildInfo(OpenAPI openAPI) {
        openAPI.info(new Info()
                .version("0.1")
                .title("Project REST API")
                .description("Generated REST API documentation"));
    }

    protected void buildErrorSchema(OpenAPI openAPI) {
        openAPI.schema("error", new ObjectSchema()
                .name("error")
                .addProperties("error", new StringSchema().description("Error message"))
                .addProperties("details", new StringSchema().description("Detailed error description")));
    }

    protected void buildTags(OpenAPI openAPI) {
        List<Tag> tags = new ArrayList<>();

        List<Tag> entityTags = metadataTools.getAllJpaEntityMetaClasses()
                .stream()
                .filter(mc -> !metadataTools.isSystemLevel(mc))
                .sorted(Comparator.comparing(MetadataObject::getName))
                .map(mc -> new Tag()
                        .name(mc.getName())
                        .description("Entity CRUD operations"))
                .collect(Collectors.toList());

        tags.addAll(entityTags);

        List<Tag> queryTags = queriesConfiguration.getQueries()
                .stream()
                .map(RestQueriesConfiguration.QueryInfo::getEntityName)
                .distinct()
                .sorted(String::compareTo)
                .map(queryEntity -> new Tag()
                        .name(queryEntity + " Queries")
                        .description("Predefined queries execution"))
                .collect(Collectors.toList());
        tags.addAll(queryTags);

        List<Tag> serviceTags = servicesConfiguration.getServiceInfos()
                .stream()
                .sorted(Comparator.comparing(RestServicesConfiguration.RestServiceInfo::getName))
                .map(serviceInfo -> new Tag()
                        .name(serviceInfo.getName())
                        .description("Middleware services execution"))
                .collect(Collectors.toList());
        tags.addAll(serviceTags);

        openAPI.tags(tags);
    }

    /*
     * Entities
     */
    protected void buildEntitiesPaths(OpenAPI openAPI) {
        for (MetaClass metaClass : metadataTools.getAllJpaEntityMetaClasses()) {
            if (metadataTools.isSystemLevel(metaClass)) {
                continue;
            }

            buildEntitySchema(openAPI, metaClass);
            buildEntityPath(openAPI, metaClass);
            buildEntityRUDPaths(openAPI, metaClass);
            buildEntityFilterPaths(openAPI, metaClass);
        }
    }

    protected void buildEntitySchema(OpenAPI openAPI, MetaClass entityClass) {
        Map<String, Schema<?>> properties = new LinkedHashMap<>();

        properties.put(ENTITY_NAME_PROP, new StringSchema()
                ._default(entityClass.getName()));
        properties.put(INSTANCE_NAME_PROP, getNamePatternProperty(entityClass));

        for (MetaProperty metaProperty : entityClass.getProperties()) {
            String fieldName = metaProperty.getName();
            Class<?> propertyType = metaProperty.getJavaType();
            String propertyTypeName = propertyType.getName();

            if (Collection.class.isAssignableFrom(propertyType)) {
                String collectionItemsType = metaProperty.getRange().asClass().getJavaClass().getName();
                Schema<?> itemsProperty = getPropertyFromJavaType(collectionItemsType);

                Schema<?> collectionProperty = new ArraySchema()
                        .items(itemsProperty);
                properties.put(fieldName, collectionProperty);
            } else if (Map.class.isAssignableFrom(propertyType)) {
                properties.put(fieldName, new MapSchema());
            } else {
                properties.put(fieldName, getPropertyFromJavaType(propertyTypeName));
            }
        }

        openAPI.schema(getEntitySchemaName(entityClass.getName()), new ObjectSchema()
                .name(entityClass.getName())
                .properties(properties));
    }

    protected void buildEntityPath(OpenAPI openAPI, MetaClass entityClass) {
        openAPI.path(String.format(ENTITY_PATH, entityClass.getName()),
                new PathItem()
                        .get(createEntityBrowseOperation(entityClass))
                        .post(createEntityCreateOperation(entityClass)));
    }

    protected void buildEntityRUDPaths(OpenAPI openAPI, MetaClass entityClass) {
        openAPI.path(String.format(ENTITY_RUD_OPS, entityClass.getName()),
                new PathItem()
                        .get(createEntityReadOperation(entityClass))
                        .put(createEntityUpdateOperation(entityClass))
                        .delete(createEntityDeleteOperation(entityClass)));
    }

    protected void buildEntityFilterPaths(OpenAPI openAPI, MetaClass entityClass) {
        openAPI.path(String.format(ENTITY_SEARCH, entityClass.getName()),
                new PathItem()
                        .get(createEntitySearchOperation(entityClass, RequestMethod.GET))
                        .post(createEntitySearchOperation(entityClass, RequestMethod.POST)));
    }

    protected Operation createEntityCreateOperation(MetaClass entityClass) {
        String entityName = entityClass.getName();
        return new Operation()
                .addTagsItem(entityName)
                .summary("Creates new entity: " + entityName)
                .description("The method expects a JSON with entity object in the request body. " +
                        "The entity object may contain references to other entities.")
                .responses(
                        new ApiResponses()
                                .addApiResponse("201", createEntityResponse("Entity created. The created entity is returned in the response body.", entityName))
                                .addApiResponse("400", createErrorResponse("Bad request. For example, the entity may have a reference to the non-existing entity."))
                                .addApiResponse("403", createErrorResponse("Forbidden. The user doesn't have permissions to create the entity."))
                                .addApiResponse("404", createErrorResponse("Not found. MetaClass for the entity with the given name not found.")))
                .requestBody(new RequestBody()
                        .description("JSON object with the entity")
                        .required(true)
                        .content(createEntityContent(entityName)));
    }

    protected Operation createEntityBrowseOperation(MetaClass entityClass) {
        String entityName = entityClass.getName();
        return new Operation()
                .addTagsItem(entityName)
                .summary("Gets a list of entities: " + entityName)
                .description("Gets a list of entities")
                .responses(
                        new ApiResponses()
                                .addApiResponse("200", createEntityArrayResponse("Success. The list of entities is returned in the response body.", entityName))
                                .addApiResponse("403", createErrorResponse("Forbidden. The user doesn't have permissions to read the entity."))
                                .addApiResponse("404", createErrorResponse("Not found. MetaClass for the entity with the given name not found.")))
                .parameters(createEntityOptionalParams(false));
    }

    protected Operation createEntityReadOperation(MetaClass entityClass) {
        String entityName = entityClass.getName();
        Operation operation = new Operation()
                .addTagsItem(entityName)
                .summary("Gets a single entity by identifier: " + entityName)
                .description("Gets a single entity by identifier")
                .addParametersItem(new PathParameter()
                        .name("entityId")
                        .description("Entity identifier")
                        .required(true)
                        .schema(new StringSchema()))
                .responses(new ApiResponses()
                        .addApiResponse("200", createEntityResponse("Success. The entity is returned in the response body.", entityName))
                        .addApiResponse("403", createErrorResponse("Forbidden. The user doesn't have permissions to read the entity."))
                        .addApiResponse("404", createErrorResponse("Not found. MetaClass for the entity with the given name not found.")));

        operation.getParameters().addAll(createEntityOptionalParams(true));

        return operation;
    }

    protected Operation createEntityUpdateOperation(MetaClass entityClass) {
        String entityName = entityClass.getName();
        return new Operation()
                .addTagsItem(entityName)
                .summary("Updates the entity: " + entityName)
                .description("Updates the entity. Only fields that are passed in the JSON object " +
                        "(the request body) are updated.")
                .addParametersItem(new PathParameter()
                        .name("entityId")
                        .description("Entity identifier")
                        .required(true)
                        .schema(new StringSchema().description("Entity identifier")))
                .requestBody(new RequestBody()
                        .description("JSON object with the entity")
                        .content(createEntityContent(entityName))
                        .required(true))
                .responses(new ApiResponses()
                        .addApiResponse("200", createEntityResponse("Success. The updated entity is returned in the response body.", entityName))
                        .addApiResponse("403", createErrorResponse("Forbidden. The user doesn't have permissions to update the entity."))
                        .addApiResponse("404", createErrorResponse("Not found. MetaClass for the entity with the given name not found.")));
    }

    protected Operation createEntityDeleteOperation(MetaClass entityClass) {
        String entityName = entityClass.getName();
        return new Operation()
                .addTagsItem(entityName)
                .summary("Deletes the entity: " + entityName)
                .addParametersItem(new PathParameter()
                        .name("entityId")
                        .description("Entity identifier")
                        .required(true)
                        .schema(new StringSchema()))
                .responses(new ApiResponses()
                        .addApiResponse("200", new ApiResponse().description("Success. Entity was deleted."))
                        .addApiResponse("403", createErrorResponse("Forbidden. The user doesn't have permissions to delete the entity"))
                        .addApiResponse("404", createErrorResponse("Not found. MetaClass for the entity with the given name not found.")));
    }

    protected Operation createEntitySearchOperation(MetaClass entityClass, RequestMethod method) {
        String entityName = entityClass.getName();
        Operation operation = new Operation()
                .addTagsItem(entityName)
                .summary("Find entities by filter conditions: " + entityName)
                .description("Finds entities by filter conditions. The filter is defined by JSON object " +
                        "that is passed as in URL parameter.")
                .responses(new ApiResponses()
                        .addApiResponse("200", createEntityArrayResponse("Success. Entities that conforms filter conditions are returned in the response body.", entityName))
                        .addApiResponse("400", createErrorResponse("Bad request. For example, the condition value cannot be parsed."))
                        .addApiResponse("403", createErrorResponse("Forbidden. The user doesn't have permissions to read the entity."))
                        .addApiResponse("404", createErrorResponse("Not found. MetaClass for the entity with the given name not found.")));

        if (RequestMethod.GET == method) {
            operation.addParametersItem(new QueryParameter()
                    .name("filter")
                    .required(true)
                    .schema(new StringSchema().description("JSON with filter definition")));
            operation.getParameters().addAll(createEntityOptionalParams(false));
        } else {
            operation.requestBody(new RequestBody()
                    .description("JSON with filter definition")
                    .content(new Content()
                            .addMediaType(APPLICATION_JSON_VALUE, new MediaType().schema(new StringSchema())))
                    .required(true));
            operation.parameters(createEntityOptionalParams(false));
        }

        return operation;
    }

    protected List<Parameter> createEntityOptionalParams(boolean singleEntityOperation) {
        List<Parameter> singleEntityParams = Arrays.asList(
                new QueryParameter()
                        .name("dynamicAttributes")
                        .description("Specifies whether entity dynamic attributes should be returned.")
                        .schema(new BooleanSchema()),
                new QueryParameter()
                        .name("returnNulls")
                        .description("Specifies whether null fields will be written to the result JSON.")
                        .schema(new BooleanSchema()),
                new QueryParameter()
                        .name("fetchPlan")
                        .description("Name of the fetchPlan which is used for loading the entity.")
                        .schema(new BooleanSchema())
        );

        if (singleEntityOperation) {
            return singleEntityParams;
        }

        List<Parameter> multipleEntityParams = new ArrayList<>(Arrays.asList(
                new QueryParameter()
                        .name("returnCount")
                        .description("Specifies whether the total count of entities should be returned in the " +
                                "'X-Total-Count' header.")
                        .schema(new BooleanSchema()),
                new QueryParameter()
                        .name("offset")
                        .description("Position of the first result to retrieve.")
                        .schema(new StringSchema()),
                new QueryParameter()
                        .name("limit")
                        .description("Number of extracted entities.")
                        .schema(new StringSchema()),
                new QueryParameter()
                        .name("sort")
                        .description("Name of the field to be sorted by. If the name is preceding by the '+' " +
                                "character, then the sort order is ascending, if by the '-' character then " +
                                "descending. If there is no special character before the property name, then " +
                                "ascending sort will be used.")
                        .schema(new StringSchema())
        ));
        multipleEntityParams.addAll(singleEntityParams);

        return multipleEntityParams;
    }

    protected Schema<?> getNamePatternProperty(MetaClass entityClass) {
        StringSchema namePatternProperty = new StringSchema();
        namePatternProperty.setDefault(
                metadataTools.getInstanceNameRelatedProperties(entityClass).stream()
                        .map(MetadataObject::getName)
                        .collect(Collectors.joining(",")));
        return namePatternProperty;
    }

    /*
     * Services
     */
    protected void buildServicesPaths(OpenAPI openAPI) {
        for (RestServiceInfo serviceInfo : servicesConfiguration.getServiceInfos()) {
            String serviceName = serviceInfo.getName();

            for (RestMethodInfo methodInfo : serviceInfo.getMethods()) {
                openAPI.path(String.format(SERVICE_PATH, serviceName, methodInfo.getName()),
                        new PathItem()
                                .get(createServiceMethodOp(serviceName, methodInfo, RequestMethod.GET))
                                .post(createServiceMethodOp(serviceName, methodInfo, RequestMethod.POST)));
            }
        }
    }

    protected Operation createServiceMethodOp(String service, RestMethodInfo methodInfo, RequestMethod requestMethod) {
        return new Operation()
                .addTagsItem(service)
                .summary(service + "#" + methodInfo.getName())
                .description("Executes the service method. This request expects query parameters with the names defined " +
                        "in services configuration on the middleware.")
                .responses(new ApiResponses()
                        .addApiResponse("200", new ApiResponse()
                                        .description("Returns the result of the method execution. It can be of simple datatype " +
                                                "as well as JSON that represents an entity or entities collection.")
                                //.schema(new StringSchema())
                        )
                        .addApiResponse("204", new ApiResponse().description("No content. This status is returned when the service " +
                                "method was executed successfully but returns null or is of void type."))
                        .addApiResponse("403", createErrorResponse("Forbidden. The user doesn't have permissions to invoke the service method.")))
                .parameters(createServiceMethodParams(methodInfo, requestMethod))
                .requestBody(createServiceMethodRequestBody(methodInfo, requestMethod));
    }

    protected List<Parameter> createServiceMethodParams(RestMethodInfo methodInfo, RequestMethod requestMethod) {
        if (RequestMethod.GET == requestMethod) {
            return methodInfo.getParams()
                    .stream()
                    .map(p -> createGetOperationParam(p.getName(), p.getType()))
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    protected RequestBody createServiceMethodRequestBody(RestMethodInfo methodInfo, RequestMethod method) {
        if (method == RequestMethod.POST) {
            ObjectSchema schema = new ObjectSchema();
            for (RestServicesConfiguration.RestMethodParamInfo param : methodInfo.getParams()) {
                schema.addProperties(param.getName(), getPropertyFromJavaType(param.getType()));
            }
            return new RequestBody()
                    .content(new Content()
                            .addMediaType(APPLICATION_JSON_VALUE, new MediaType().schema(schema)))
                    .required(true);

        }
        return null;
    }

    /*
     * Queries
     */
    protected void buildQueriesPaths(OpenAPI openAPI) {
        for (QueryInfo queryInfo : queriesConfiguration.getQueries()) {
            String entity = queryInfo.getEntityName();
            String queryName = queryInfo.getName();

            openAPI.path(String.format(QUERY_PATH, entity, queryName),
                    new PathItem()
                            .get(createQueryOperation(queryInfo, RequestMethod.GET))
                            .post(createQueryOperation(queryInfo, RequestMethod.POST)));

            openAPI.path(String.format(QUERY_COUNT_PATH, entity, queryName),
                    new PathItem()
                            .get(createQueryCountOperation(queryInfo, RequestMethod.GET))
                            .post(createQueryCountOperation(queryInfo, RequestMethod.POST)));
        }
    }

    protected Operation createQueryOperation(QueryInfo query, RequestMethod method) {
        String entityName = query.getEntityName();
        return new Operation()
                .addTagsItem(query.getEntityName() + " Queries")
                .summary(query.getName())
                .description("Executes a predefined query. Query parameters must be passed in the request body as JSON map.")
                .responses(new ApiResponses()
                        .addApiResponse("200", createEntityArrayResponse("Success", entityName))
                        .addApiResponse("403", createErrorResponse("Forbidden. A user doesn't have permissions to read the entity."))
                        .addApiResponse("404", createErrorResponse("Not found. MetaClass for the entity with the given name not found.")))
                .parameters(createQueryOpParams(query, method, true))
                .requestBody(createQueryRequestBody(query, method));
    }

    protected Operation createQueryCountOperation(QueryInfo query, RequestMethod method) {
        return new Operation()
                .addTagsItem(query.getEntityName() + " Queries")
                .summary("Return a number of entities in query result")
                .description("Returns a number of entities that matches the query. You can use the all keyword for " +
                        "the queryNameParam to get the number of all available entities.")
                .responses(new ApiResponses()
                        .addApiResponse("200", new ApiResponse()
                                .description("Success. Entities count is returned")
                                .content(new Content()
                                        .addMediaType(APPLICATION_JSON_VALUE, new MediaType()
                                                .schema(new IntegerSchema().description("Entities count")))))
                        .addApiResponse("403", createErrorResponse("Forbidden. The user doesn't have permissions to read the entity."))
                        .addApiResponse("404", createErrorResponse("MetaClass not found or query with the given name not found")))
                .parameters(createQueryOpParams(query, method, false))
                .requestBody(createQueryRequestBody(query, method));
    }

    protected List<Parameter> createQueryOpParams(QueryInfo query, RequestMethod method,
                                                  boolean generateOptionalParams) {
        List<Parameter> optionalParams = generateOptionalParams ?
                createOptionalQueryParams() : Collections.emptyList();

        if (RequestMethod.GET == method) {
            List<Parameter> queryParams = query.getParams()
                    .stream()
                    .map(p -> createGetOperationParam(p.getName(), p.getType()))
                    .collect(Collectors.toList());

            queryParams.addAll(optionalParams);

            return queryParams;
        } else {
            return optionalParams;
        }
    }

    protected List<Parameter> createOptionalQueryParams() {
        return Arrays.asList(
                new QueryParameter()
                        .name("dynamicAttributes")
                        .description("Specifies whether entity dynamic attributes should be returned.")
                        .schema(new BooleanSchema()),
                new QueryParameter()
                        .name("returnCount")
                        .description("Specifies whether the total count of entities should be returned in the " +
                                "'X-Total-Count' header.")
                        .schema(new BooleanSchema()),
                new QueryParameter()
                        .name("returnNulls")
                        .description("Specifies whether null fields will be written to the result JSON.")
                        .schema(new BooleanSchema()),
                new QueryParameter()
                        .name("fetchPlan")
                        .description("Name of the fetch plan which is used for loading the entity. Specify this parameter " +
                                "if you want to extract entities with the view other than it is defined in the REST " +
                                "queries configuration file.")
                        .schema(new StringSchema()),
                new QueryParameter()
                        .name("offset")
                        .description("Position of the first result to retrieve.")
                        .schema(new StringSchema()),
                new QueryParameter()
                        .name("limit")
                        .description("Number of extracted entities.")
                        .schema(new StringSchema())
        );
    }

    protected RequestBody createQueryRequestBody(QueryInfo query, RequestMethod method) {
        if (method == RequestMethod.POST) {
            ObjectSchema schema = new ObjectSchema();
            for (RestQueriesConfiguration.QueryParamInfo param : query.getParams()) {
                schema.addProperties(param.getName(), getPropertyFromJavaType(param.getType()));
            }
            return new RequestBody()
                    .content(new Content()
                            .addMediaType(APPLICATION_JSON_VALUE, new MediaType().schema(schema)))
                    .required(true);

        }
        return null;
    }

    /*
     * Common
     */
    protected Parameter createGetOperationParam(String parameterName, String parameterType) {
        boolean paramIsArray = parameterType != null && parameterType.contains(ARRAY_SIGNATURE);

        Parameter parameter = new QueryParameter()
                .name(parameterName)
                .required(true);

        if (paramIsArray) {
            parameter.schema(new ArraySchema()
                    .items(new StringSchema()));
        } else {
            parameter.schema(new StringSchema());
        }

        return parameter;
    }

    protected Schema<?> getPropertyFromJavaType(String type) {
        if (type == null) {
            return new StringSchema();
        }

        if (type.contains(ARRAY_SIGNATURE)) {
            String itemsType = type.replace(ARRAY_SIGNATURE, "");
            return new ArraySchema()
                    .items(getPropertyFromJavaType(itemsType));
        }

        Schema<?> primitiveProperty = getPrimitiveProperty(type);
        if (primitiveProperty != null) {
            return primitiveProperty;
        }

        Schema<?> entityProperty = getObjectProperty(type);
        if (entityProperty != null) {
            return entityProperty;
        }

        return new StringSchema().description(type);
    }

    protected Schema<?> getObjectProperty(String classFqn) {
        Class<?> clazz;
        try {
            clazz = ReflectionHelper.loadClass(classFqn);
        } catch (ClassNotFoundException e) {
            return null;
        }

        MetaClass metaClass = metadata.findClass(clazz);
        if (metaClass != null) {
            return new ObjectSchema()
                    .$ref(getEntitySchemaRef(metaClass.getName()))
                    .description(metaClass.getName());
        }

        if (Enum.class.isAssignableFrom(clazz)) {
            return new StringSchema().description(classFqn);
        }

        return null;
    }

    protected Schema<?> getPrimitiveProperty(String type) {
        String primitiveType = type;
        if (type.contains(".")) {
            primitiveType = primitiveType.substring(primitiveType.lastIndexOf(".") + 1).toLowerCase();
        }

        switch (primitiveType) {
            case "boolean":
                return new BooleanSchema().example(true);
            case "float":
            case "double":
                return new NumberSchema().example("3.14");
            case "byte":
            case "short":
            case "int":
            case "integer":
                return new IntegerSchema().example(42);
            case "long":
                return new IntegerSchema().format("int64").example(Long.MAX_VALUE >> 4);
            case "date":
                return new DateTimeSchema().example("2005-14-10T13:17:42.16Z");
            case "uuid":
                UUIDSchema uuidProp = new UUIDSchema();
                uuidProp.setExample("19474a3b-99b5-482e-9e77-852be9adf817");
                return uuidProp;
            case "string":
                return new StringSchema().example("String");
            default:
                return null;
        }
    }

    protected ApiResponse createErrorResponse(String msg) {
        return new ApiResponse()
                .description(msg)
                .content(new Content()
                        .addMediaType(APPLICATION_JSON_VALUE, new MediaType()
                                .schema(new ObjectSchema().$ref(getErrorSchemaRef()))));
    }

    protected ApiResponse createEntityResponse(String msg, String entityName) {
        return new ApiResponse()
                .description(msg)
                .content(createEntityContent(entityName));
    }

    protected ApiResponse createEntityArrayResponse(String msg, String entityName) {
        return new ApiResponse()
                .description(msg)
                .content(new Content()
                        .addMediaType(APPLICATION_JSON_VALUE, new MediaType()
                                .schema(new ArraySchema()
                                        .items(new ObjectSchema().$ref(getEntitySchemaRef(entityName))))));
    }

    protected Content createEntityContent(String entityName) {
        return new Content()
                .addMediaType(APPLICATION_JSON_VALUE, new MediaType()
                        .schema(new ObjectSchema().$ref(getEntitySchemaRef(entityName))));
    }

    protected String getEntitySchemaName(String entityName) {
        return "entity_" + entityName;
    }

    protected String getEntitySchemaRef(String entityName) {
        return SCHEMAS_PREFIX + getEntitySchemaName(entityName);
    }

    protected String getErrorSchemaRef() {
        return SCHEMAS_PREFIX + "error";
    }
}
