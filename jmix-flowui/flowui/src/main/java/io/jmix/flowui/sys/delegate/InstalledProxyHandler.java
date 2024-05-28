/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.sys.delegate;

import com.vaadin.flow.component.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class InstalledProxyHandler implements InvocationHandler {

    private final Component component;
    private final Method method;

    public InstalledProxyHandler(Component component, Method method) {
        this.component = component;
        this.method = method;
    }

    @Override
    public Object invoke(Object proxy, Method invokedMethod, Object[] args) throws Throwable {
        switch (invokedMethod.getName()) {
            case "toString" -> {
                return this.toString();
            }
            case "equals" -> {
                return args.length == 1 && args[0] == proxy;
            }
            case "hashCode" -> {
                return this.hashCode();
            }
        }

        if (invokedMethod.getParameterCount() == method.getParameterCount()) {
            try {
                return this.method.invoke(component, args);
            } catch (InvocationTargetException e) {
                if (e.getTargetException() instanceof RuntimeException) {
                    throw e.getTargetException();
                }

                throw e.getTargetException();
            }
        }

        throw new UnsupportedOperationException(
                String.format("%s does not support method %s. Check types and number of parameters",
                        InstalledProxyHandler.class.getSimpleName(), invokedMethod));
    }

    @Override
    public String toString() {
        return "InstalledProxyHandler{" +
                "origin=" + component.getClass() +
                ", method=" + method +
                '}';
    }
}
