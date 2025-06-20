/*
 * Copyright 2025 Haulmont.
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

package io.jmix.restds.impl.service;

import io.jmix.core.EntitySerialization;
import io.jmix.core.Metadata;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.restds.annotation.RemoteService;
import io.jmix.restds.util.RestDataStoreUtils;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Collection;

@Component("restds_RemoteServiceInvoker")
public class RemoteServiceInvoker {

    protected final Environment environment;
    protected final RestDataStoreUtils restDataStoreUtils;
    protected final EntitySerialization entitySerialization;
    protected final Metadata metadata;
    protected final DatatypeRegistry datatypeRegistry;

    public RemoteServiceInvoker(Environment environment, RestDataStoreUtils restDataStoreUtils, EntitySerialization entitySerialization, Metadata metadata, DatatypeRegistry datatypeRegistry) {
        this.environment = environment;
        this.restDataStoreUtils = restDataStoreUtils;
        this.entitySerialization = entitySerialization;
        this.metadata = metadata;
        this.datatypeRegistry = datatypeRegistry;
    }

    @Nullable
    public Object invokeServiceMethod(Class<?> serviceInterface, Method method, @Nullable Object[] args) {
        RemoteService remoteServiceAnnotation = serviceInterface.getAnnotation(RemoteService.class);
        if (remoteServiceAnnotation == null)
            throw new IllegalStateException("RemoteService annotation is not found for interface " + serviceInterface);
        String storeName = remoteServiceAnnotation.store();
        String serviceName = remoteServiceAnnotation.remoteName().isEmpty() ?
                serviceInterface.getSimpleName() : remoteServiceAnnotation.remoteName();

        RestClient restClient = restDataStoreUtils.getRestClient(storeName);

        String resultJson = restClient.post()
                .uri(getServiceUri(serviceName, storeName, method))
                .body(getParamsJson(entitySerialization, method, args))
                .retrieve()
                .body(String.class);

        return method.getReturnType() == void.class ? null : getResultObject(entitySerialization, method, resultJson);
    }

    protected String getServiceUri(String serviceName, String storeName, Method method) {
        String basePath = environment.getProperty(storeName + ".basePath", "/rest");
        String servicePath = environment.getProperty(storeName + ".servicesPath", "/services");
        return basePath + servicePath + "/" + serviceName + "/" + method.getName();
    }

    protected String getParamsJson(EntitySerialization entitySerialization, Method method, @Nullable Object[] args) {
        if (args == null) {
            return "{}";
        }

        Parameter[] parameters = method.getParameters();
        if (parameters.length != args.length)
            throw new IllegalArgumentException("Number of parameters does not match number of arguments");

        StringBuilder paramsJson = new StringBuilder("{");
        for (int i = 0; i < parameters.length; i++) {
            String paramName = parameters[i].getName();
            Object paramValue = args[i];
            String paramJson;

            if (paramValue == null) {
                paramJson = "null";
            } else if (EntityValues.isEntity(paramValue)) {
                paramJson = entitySerialization.toJson(paramValue);
            } else {
                Datatype<?> datatype = datatypeRegistry.find(paramValue.getClass());
                if (datatype != null) {
                    String formatted = datatype.format(paramValue);
                    if (paramValue instanceof Boolean || paramValue instanceof Number) {
                        paramJson = formatted;
                    } else {
                        paramJson = "\"" + formatted + "\"";
                    }
                } else {
                    paramJson = entitySerialization.objectToJson(paramValue);
                }
            }
            paramsJson.append("\"").append(paramName).append("\":").append(paramJson);
            if (i < parameters.length - 1) {
                paramsJson.append(",");
            }
        }
        paramsJson.append("}");

        return paramsJson.toString();
    }

    @Nullable
    protected Object getResultObject(EntitySerialization entitySerialization, Method method, @Nullable String resultJson) {
        if (resultJson == null)
            return null;

        Object result;

        Type returnType = method.getGenericReturnType();
        Class<?> rawReturnType = method.getReturnType();

        try {
            if (isEntity(rawReturnType)) {
                result = entitySerialization.entityFromJson(resultJson, null);
            } else if (isCollectionOfEntities(returnType)) {
                result = entitySerialization.entitiesCollectionFromJson(resultJson, null);
            } else {
                Datatype<?> datatype = datatypeRegistry.find(rawReturnType);
                if (datatype != null) {
                    result = datatype.parse(resultJson);
                } else {
                    if (rawReturnType.isPrimitive())
                        result = deserializePrimitive(resultJson, rawReturnType);
                    else
                        result = entitySerialization.objectFromJson(resultJson, returnType);
                }
            }
        } catch (ParseException e) {
            throw new RuntimeException("Error deserializing response", e);
        }

        return result;
    }

    protected boolean isEntity(Class<?> aClass) {
        return metadata.findClass(aClass) != null;
    }

    protected boolean isCollectionOfEntities(Type type) {
        if (!(type instanceof ParameterizedType parameterizedType)) {
            return false;
        }
        // Check if raw type is a collection
        Class<?> rawType = (Class<?>) parameterizedType.getRawType();
        if (!Collection.class.isAssignableFrom(rawType)) {
            return false;
        }
        // Check if type argument is an entity
        Type[] typeArgs = parameterizedType.getActualTypeArguments();
        if (typeArgs.length != 1 || !(typeArgs[0] instanceof Class<?>)) {
            return false;
        }
        return isEntity((Class<?>) typeArgs[0]);
    }

    @Nullable
    protected Object deserializePrimitive(@Nullable String json, Class<?> type) {
        if (json == null)
            return null;

        if (type == boolean.class) {
            return Boolean.parseBoolean(json);
        } else if (type == int.class) {
            return Integer.parseInt(json);
        } else if (type == long.class) {
            return Long.parseLong(json);
        } else if (type == double.class) {
            return Double.parseDouble(json);
        } else if (type == float.class) {
            return Float.parseFloat(json);
        } else if (type == short.class) {
            return Short.parseShort(json);
        } else if (type == byte.class) {
            return Byte.parseByte(json);
        } else if (type == char.class) {
            // Assuming char is represented as a single-character string
            return json.isEmpty() ? '\0' : json.charAt(0);
        }
        // Should not reach here
        throw new IllegalArgumentException("Unsupported primitive type: " + type.getName());
    }
}
