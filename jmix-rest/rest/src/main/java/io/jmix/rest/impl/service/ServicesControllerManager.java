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

import io.jmix.core.Entity;
import io.jmix.core.EntitySerialization;
import io.jmix.core.EntitySerializationOption;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.rest.exception.RestAPIException;
import io.jmix.rest.impl.RestControllerUtils;
import io.jmix.rest.impl.RestParseUtils;
import io.jmix.rest.impl.config.RestServicesConfiguration;
import io.jmix.rest.impl.controller.ServicesController;
import io.jmix.rest.transform.JsonTransformationDirection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Class that executes business logic required by the {@link ServicesController}. It
 * performs middleware services invocations.
 */
@Component("rest_ServicesControllerManager")
public class ServicesControllerManager {

    @Autowired
    protected RestServicesConfiguration restServicesConfiguration;

    @Autowired
    protected EntitySerialization entitySerializationAPI;

    @Autowired
    protected RestParseUtils restParseUtils;

    @Autowired
    protected RestControllerUtils restControllerUtils;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected BeanFactory beanFactory;

    @Autowired
    protected DatatypeRegistry datatypeRegistry;

    private static final Logger log = LoggerFactory.getLogger(ServicesControllerManager.class);

    @Nullable
    public ServiceCallResult invokeServiceMethodGet(String serviceName,
                                                    String methodName,
                                                    Map<String, String> paramsMap,
                                                    String modelVersion) throws Throwable {
        paramsMap.remove("modelVersion");
        List<String> paramNames = new ArrayList<>(paramsMap.keySet());
        List<String> paramValuesStr = new ArrayList<>(paramsMap.values());
        return _invokeServiceMethod(serviceName, methodName, HttpMethod.GET, paramNames, paramValuesStr, modelVersion);
    }

    @Nullable
    public ServiceCallResult invokeServiceMethodPost(String serviceName,
                                                     String methodName,
                                                     String paramsJson,
                                                     String modelVersion) throws Throwable {
        Map<String, String> paramsMap = restParseUtils.parseParamsJson(paramsJson);
        List<String> paramNames = new ArrayList<>(paramsMap.keySet());
        List<String> paramValuesStr = new ArrayList<>(paramsMap.values());
        return _invokeServiceMethod(serviceName, methodName, HttpMethod.POST, paramNames, paramValuesStr, modelVersion);
    }

    public Collection<RestServicesConfiguration.RestServiceInfo> getServiceInfos() {
        return restServicesConfiguration.getServiceInfos();
    }

    public RestServicesConfiguration.RestServiceInfo getServiceInfo(String serviceName) {
        RestServicesConfiguration.RestServiceInfo serviceInfo = restServicesConfiguration.getServiceInfo(serviceName);
        if (serviceInfo == null) {
            throw new RestAPIException("Service not found",
                    String.format("Service %s not found", serviceName),
                    HttpStatus.NOT_FOUND);
        }
        return serviceInfo;
    }

    @Nullable
    protected ServiceCallResult _invokeServiceMethod(String serviceName,
                                                     String methodName,
                                                     HttpMethod httpMethod,
                                                     List<String> paramNames,
                                                     List<String> paramValuesStr,
                                                     String modelVersion) throws Throwable {
        Object service = beanFactory.getBean(serviceName);
        RestServicesConfiguration.RestMethodInfo restMethodInfo =
                restServicesConfiguration.getRestMethodInfo(serviceName, methodName, httpMethod.name(), paramNames);
        if (restMethodInfo == null) {
            throw new RestAPIException("Service method not found",
                    serviceName + "." + methodName + "(" + paramNames.stream().collect(Collectors.joining(",")) + ")",
                    HttpStatus.NOT_FOUND);
        }
        Method serviceMethod = restMethodInfo.getMethod();
        List<Object> paramValues = new ArrayList<>();
        Type[] types = restMethodInfo.getMethod().getGenericParameterTypes();
        for (int i = 0; i < types.length; i++) {
            int idx = i;
            try {
                idx = paramNames.indexOf(restMethodInfo.getParams().get(i).getName());
                String valueStr = idx == -1 ? null : paramValuesStr.get(idx);
                paramValues.add(restParseUtils.toObject(types[i], valueStr, modelVersion));
            } catch (Exception e) {
                log.error("Error on parsing service param value", e);
                throw new RestAPIException("Invalid parameter value",
                        "Invalid parameter value for " + paramNames.get(idx),
                        HttpStatus.BAD_REQUEST,
                        e);
            }
        }

        Object methodResult;
        try {
            methodResult = serviceMethod.invoke(service, paramValues.toArray());
        } catch (InvocationTargetException | IllegalAccessException ex) {
            throw ex.getCause();
        }

        if (methodResult == null) {
            return null;
        }

        Class<?> methodReturnType = serviceMethod.getReturnType();
        if (Entity.class.isAssignableFrom(methodReturnType)) {
            String entityJson = entitySerializationAPI.toJson(methodResult,
                    null,
                    EntitySerializationOption.SERIALIZE_INSTANCE_NAME,
                    EntitySerializationOption.DO_NOT_SERIALIZE_DENIED_PROPERTY);
            entityJson = restControllerUtils.transformJsonIfRequired(metadata.getClass(methodResult).getName(),
                    modelVersion, JsonTransformationDirection.TO_VERSION, entityJson);
            return new ServiceCallResult(entityJson, true);
        } else if (Collection.class.isAssignableFrom(methodReturnType)) {
            Type returnTypeArgument = getMethodReturnTypeArgument(serviceMethod);
            if ((returnTypeArgument instanceof Class && Entity.class.isAssignableFrom((Class) returnTypeArgument))
                    || isEntitiesCollection((Collection) methodResult)) {
                Collection<?> entities = (Collection<?>) methodResult;
                String entitiesJson = entitySerializationAPI.toJson(entities,
                        null,
                        EntitySerializationOption.SERIALIZE_INSTANCE_NAME,
                        EntitySerializationOption.DO_NOT_SERIALIZE_DENIED_PROPERTY);
                if (returnTypeArgument != null) {
                    MetaClass metaClass = metadata.getClass((Class) returnTypeArgument);
                    if (metaClass != null) {
                        entitiesJson = restControllerUtils.transformJsonIfRequired(metaClass.getName(), modelVersion,
                                JsonTransformationDirection.TO_VERSION, entitiesJson);
                    } else {
                        log.error("MetaClass for service collection parameter type {} not found", returnTypeArgument);
                    }
                }
                return new ServiceCallResult(entitiesJson, true);
            } else {
                return new ServiceCallResult(restParseUtils.serialize(methodResult), true);
            }
        } else {
            Datatype<?> datatype = datatypeRegistry.find(methodReturnType);
            if (datatype != null) {
                return new ServiceCallResult(datatype.format(methodResult), false);
            } else {
                return new ServiceCallResult(restParseUtils.serialize(methodResult), true);
            }
        }
    }

    @Nullable
    protected Type getMethodReturnTypeArgument(Method serviceMethod) {
        Type returnTypeArgument = null;
        Type genericReturnType = serviceMethod.getGenericReturnType();
        if (genericReturnType instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) genericReturnType).getActualTypeArguments();
            if (actualTypeArguments.length > 0) {
                returnTypeArgument = actualTypeArguments[0];
            }
        }
        return returnTypeArgument;
    }

    protected boolean isEntitiesCollection(Collection collection) {
        if (collection.isEmpty()) return false;
        for (Object item : collection) {
            if (!(item instanceof Entity)) {
                return false;
            }
        }
        return true;
    }

    public static class ServiceCallResult {
        protected String stringValue;
        protected boolean validJson;

        public ServiceCallResult(String stringValue, boolean validJson) {
            this.stringValue = stringValue;
            this.validJson = validJson;
        }

        public boolean isValidJson() {
            return validJson;
        }

        public String getStringValue() {
            return stringValue;
        }
    }
}
