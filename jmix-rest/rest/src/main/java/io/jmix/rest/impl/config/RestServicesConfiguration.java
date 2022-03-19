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

package io.jmix.rest.impl.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.jmix.core.Resources;
import io.jmix.core.common.util.Dom4j;
import io.jmix.rest.exception.RestAPIException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringTokenizer;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Class is used for loading and storing an information about service methods that are available for REST API.
 * information loaded from configuration files defined by the {@code jmix.rest.servicesConfig} application property.
 * <p>
 * Configuration file must define method name and method argument names that will be user for method invocation by the
 * REST API.
 * <p>
 * Method parameter types can be omitted if the service doesn't contain an overloaded method with the same parameters
 * number. Otherwise, types must be defined.
 */
@Component("rest_RestServicesConfiguration")
public class RestServicesConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RestServicesConfiguration.class);

    public static final String JMIX_REST_SERVICES_CONFIG_PROP_NAME = "jmix.rest.services-config";

    protected Map<String, RestServiceInfo> serviceInfosMap = new ConcurrentHashMap<>();

    protected volatile boolean initialized;

    protected ReadWriteLock lock = new ReentrantReadWriteLock();

    @Autowired
    protected Resources resources;

    @Autowired
    protected Environment environment;

    @Autowired
    protected BeanFactory beanFactory;

    @Nullable
    public RestMethodInfo getRestMethodInfo(String serviceName, String methodName, String httpMethod, List<String> methodParamNames) {
        lock.readLock().lock();
        try {
            checkInitialized();
            RestServiceInfo restServiceInfo = serviceInfosMap.get(serviceName);
            if (restServiceInfo == null) return null;
            List<RestMethodInfo> restMethodInfos = restServiceInfo.getMethods().stream()
                    .filter(restMethodInfo -> methodName.equals(restMethodInfo.getName())
                            && httpMethodMatches(restMethodInfo.getHttpMethod(), httpMethod)
                            && paramsMatches(restMethodInfo.getParams(), methodParamNames))
                    .collect(Collectors.toList());
            if (restMethodInfos.size() > 1) {
                String errorMsg = String.format("Cannot determine the service method to call. %d suitable methods have been found",
                        restMethodInfos.size());
                throw new RestAPIException(errorMsg, errorMsg, HttpStatus.BAD_REQUEST);
            }
            return restMethodInfos.size() == 1 ? restMethodInfos.get(0) : null;
        } finally {
            lock.readLock().unlock();
        }
    }

    protected boolean httpMethodMatches(@Nullable String httpMethod1, @Nullable String httpMethod2) {
        if (httpMethod1 == null || httpMethod2 == null) {
            return true;
        }
        return httpMethod1.equalsIgnoreCase(httpMethod2);
    }

    protected boolean paramsMatches(List<RestMethodParamInfo> paramInfos, List<String> paramNames) {
        if (paramNames.size() > paramInfos.size()) {
            return false;
        }

        return paramInfos.stream()
                .noneMatch(paramInfo -> paramInfo.required && !paramNames.contains(paramInfo.name));
    }

    protected void checkInitialized() {
        if (!initialized) {
            lock.readLock().unlock();
            lock.writeLock().lock();
            try {
                if (!initialized) {
                    init();
                    initialized = true;
                }
            } finally {
                lock.readLock().lock();
                lock.writeLock().unlock();
            }
        }
    }

    protected void init() {
        String configName = environment.getProperty(JMIX_REST_SERVICES_CONFIG_PROP_NAME);
        StringTokenizer tokenizer = new StringTokenizer(configName);
        for (String location : tokenizer.getTokenArray()) {
            Resource resource = resources.getResource(location);
            if (resource.exists()) {
                try (InputStream stream = resource.getInputStream()) {
                    loadConfig(Dom4j.readDocument(stream).getRootElement());
                } catch (IOException e) {
                    throw new RuntimeException("Error on parsing rest services config", e);
                }
            } else {
                log.warn("Resource {} not found, ignore it", location);
            }
        }
    }

    protected void loadConfig(Element rootElem) {
        for (Element serviceElem : rootElem.elements("service")) {
            String serviceName = serviceElem.attributeValue("name");
            if (!beanFactory.containsBean(serviceName)) {
                log.error("Service not found: {}", serviceName);
                continue;
            }
            List<RestMethodInfo> methodInfos = new ArrayList<>();

            for (Element methodElem : serviceElem.elements("method")) {
                String methodName = methodElem.attributeValue("name");
                String httpMethod = methodElem.attributeValue("httpMethod");
                boolean anonymousAllowed = Boolean.parseBoolean(methodElem.attributeValue("anonymousAllowed"));
                List<RestMethodParamInfo> params = new ArrayList<>();
                for (Element paramEl : methodElem.elements("param")) {
                    params.add(new RestMethodParamInfo(paramEl.attributeValue("name"),
                            paramEl.attributeValue("type"),
                            Boolean.parseBoolean(paramEl.attributeValue("required", "true"))));
                }
                Method method = _findMethod(serviceName, methodName, params);
                if (method != null) {
                    methodInfos.add(new RestMethodInfo(methodName, httpMethod, params, method));
                }
            }

            serviceInfosMap.put(serviceName, new RestServiceInfo(serviceName, methodInfos));
        }
    }

    @Nullable
    protected Method _findMethod(String serviceName, String methodName, List<RestMethodParamInfo> paramInfos) {
        List<Class<?>> paramTypes = new ArrayList<>();
        for (RestMethodParamInfo paramInfo : paramInfos) {
            if (StringUtils.isNotEmpty(paramInfo.getType())) {
                try {
                    paramTypes.add(ClassUtils.forName(paramInfo.getType(), null));
                } catch (ClassNotFoundException e) {
                    log.error("Class {} for method parameter not found. Service: {}, method: {}, param: {}",
                            paramInfo.getType(),
                            serviceName,
                            methodName,
                            paramInfo.getName());
                    return null;
                }
            }
        }

        if (!paramTypes.isEmpty() && paramInfos.size() != paramTypes.size()) {
            log.error("Service method parameters types must be defined for all parameters or for none of them. Service: {}, method: {}",
                    serviceName, methodName);
            return null;
        }

        Object service = beanFactory.getBean(serviceName);
        Class<?> serviceClass = AopUtils.isAopProxy(service) ? AopUtils.getTargetClass(service) : service.getClass();
        Method serviceMethod = null;
        //the service object we get here is proxy. To get methods with type information
        //we need to know actual interfaces implemented by the service (this is required when parameterized
        //collection of entities is passed as an argument)
        Method[] methods = serviceClass.getMethods();

        if (!serviceClass.isAnnotationPresent(Service.class)) {
            log.error("Bean {} does not have @Service annotation", serviceName);
            return null;
        }

        if (paramTypes.isEmpty()) {
            List<Method> appropriateMethods = new ArrayList<>();
            for (Method method : methods) {
                if (methodName.equals(method.getName()) && method.getParameterTypes().length == paramInfos.size()) {
                    appropriateMethods.add(method);
                }
            }
            if (appropriateMethods.size() == 1) {
                serviceMethod = appropriateMethods.get(0);
            } else if (appropriateMethods.size() > 1) {
                log.error("There are multiple methods with given argument numbers. Parameters type must be defined. Service: {}, method: {}",
                        serviceName, methodName);
                return null;
            } else {
                log.error("Method not found. Service: {}, method: {}, number of arguments: {}", serviceName, methodName, paramInfos.size());
                return null;
            }
        } else {
            try {
                serviceMethod = serviceClass.getMethod(methodName, paramTypes.toArray(new Class[0]));
            } catch (NoSuchMethodException ignored) {
            }
            if (serviceMethod == null) {
                log.error("Method not found. Service: {}, method: {}, argument types: {}", serviceName, methodName, paramTypes);
                return null;
            }
        }
        return serviceMethod;
    }

    public Collection<RestServiceInfo> getServiceInfos() {
        lock.readLock().lock();
        try {
            checkInitialized();
            return serviceInfosMap.values();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Nullable
    public RestServiceInfo getServiceInfo(String serviceName) {
        lock.readLock().lock();
        try {
            checkInitialized();
            return serviceInfosMap.get(serviceName);
        } finally {
            lock.readLock().unlock();
        }
    }

    public static class RestServiceInfo {
        protected String name;
        protected List<RestMethodInfo> methods;

        public RestServiceInfo(String name, List<RestMethodInfo> methods) {
            this.name = name;
            this.methods = methods;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<RestMethodInfo> getMethods() {
            return methods;
        }

        public void setMethods(List<RestMethodInfo> methods) {
            this.methods = methods;
        }
    }

    public static class RestMethodInfo {
        protected String name;
        protected String httpMethod;
        protected List<RestMethodParamInfo> params;
        @JsonIgnore
        protected Method method;

        public RestMethodInfo(String name, String httpMethod, List<RestMethodParamInfo> params, Method method) {
            this.name = name;
            this.httpMethod = httpMethod;
            this.params = params;
            this.method = method;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getHttpMethod() {
            return httpMethod;
        }

        public void setHttpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
        }

        public List<RestMethodParamInfo> getParams() {
            return params;
        }

        public void setParams(List<RestMethodParamInfo> params) {
            this.params = params;
        }

        public Method getMethod() {
            return method;
        }

        public void setMethod(Method method) {
            this.method = method;
        }
    }

    public static class RestMethodParamInfo {
        protected String name;
        protected String type;
        protected boolean required;

        public RestMethodParamInfo(String name, String type, boolean required) {
            this.name = name;
            this.type = type;
            this.required = required;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }
    }
}
