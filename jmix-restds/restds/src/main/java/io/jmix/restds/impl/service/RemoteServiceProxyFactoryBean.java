package io.jmix.restds.impl.service;

import io.jmix.core.EntitySerialization;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.restds.annotation.RemoteService;
import io.jmix.restds.util.RestDataStoreUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.client.RestClient;

import java.lang.reflect.*;
import java.text.ParseException;
import java.util.Collection;

import static io.jmix.core.entity.EntityValues.isEntity;

public class RemoteServiceProxyFactoryBean implements FactoryBean<Object>, ApplicationContextAware {

    private Class<?> serviceInterface;
    private ApplicationContext applicationContext;

    public void setServiceInterface(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Class<?> getObjectType() {
        return serviceInterface;
    }

    @Override
    public Object getObject() {
        InvocationHandler handler = (proxy, method, args) ->
                switch (method.getName()) {
                    case "equals" -> proxy == args[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "toString" ->
                            proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(proxy));
                    default -> invokeServiceMethod(method, args);
                };

        return Proxy.newProxyInstance(
                serviceInterface.getClassLoader(),
                new Class[]{ serviceInterface },
                handler
        );
    }

    private Object invokeServiceMethod(Method method, Object[] args) {
        RestDataStoreUtils restDataStoreUtils = applicationContext.getBean(RestDataStoreUtils.class);
        EntitySerialization entitySerialization = applicationContext.getBean(EntitySerialization.class);

        RemoteService remoteServiceAnnotation = serviceInterface.getAnnotation(RemoteService.class);
        if (remoteServiceAnnotation == null)
            throw new IllegalStateException("RemoteService annotation is not found for interface " + serviceInterface);
        String storeName = remoteServiceAnnotation.store();

        RestClient restClient = restDataStoreUtils.getRestClient(storeName);

        String resultJson = restClient.post()
                .uri(getServiceUri(storeName, method))
                .body(getParamsJson(entitySerialization, method, args))
                .retrieve()
                .body(String.class);

        return method.getReturnType() == void.class ? null : getResultObject(entitySerialization, method, resultJson);
    }

    private String getServiceUri(String storeName, Method method) {
        String basePath = applicationContext.getEnvironment().getProperty(storeName + ".basePath", "/rest");
        String servicePath = applicationContext.getEnvironment().getProperty(storeName + ".servicesPath", "/services");
        return basePath + servicePath + "/" + serviceInterface.getSimpleName() + "/" + method.getName();
    }

    private String getParamsJson(EntitySerialization entitySerialization, Method method, Object[] args) {
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
            } else if (isEntity(paramValue)) {
                paramJson = entitySerialization.toJson(paramValue);
            } else {
                Datatype<?> datatype = getDatatypeRegistry().find(paramValue.getClass());
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

    private Object getResultObject(EntitySerialization entitySerialization, Method method, String resultJson) {
        Object result;

        Type returnType = method.getGenericReturnType();
        Class<?> rawReturnType = method.getReturnType();

        try {
            if (isEntityType(rawReturnType)) {
                result = entitySerialization.entityFromJson(resultJson, null);
            } else if (isCollectionOfEntities(returnType)) {
                result = entitySerialization.entitiesCollectionFromJson(resultJson, null);
            } else {
                Datatype<?> datatype = getDatatypeRegistry().find(rawReturnType);
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

    private boolean isEntityType(Class<?> aClass) {
        return getMetadata().findClass(aClass) != null;
    }

    private boolean isCollectionOfEntities(Type type) {
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
        return isEntityType((Class<?>) typeArgs[0]);
    }

    private Object deserializePrimitive(String json, Class<?> type) {
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

    private Metadata getMetadata() {
        return applicationContext.getBean(Metadata.class);
    }

    private DatatypeRegistry getDatatypeRegistry() {
        return applicationContext.getBean(DatatypeRegistry.class);
    }
}