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

import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class RemoteServiceProxyFactoryBean implements FactoryBean<Object>, ApplicationContextAware {

    protected Class<?> serviceInterface;
    protected ApplicationContext applicationContext;

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

    @Nullable
    protected Object invokeServiceMethod(Method method, Object[] args) {
        RemoteServiceInvoker serviceInvoker = applicationContext.getBean(RemoteServiceInvoker.class);
        return serviceInvoker.invokeServiceMethod(serviceInterface, method, args);
    }
}