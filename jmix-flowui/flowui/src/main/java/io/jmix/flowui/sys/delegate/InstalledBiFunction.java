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

import io.jmix.flowui.view.Install;
import io.jmix.flowui.view.View;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiFunction;

public class InstalledBiFunction implements BiFunction<Object, Object, Object> {

    private final View<?> controller;
    private final Method method;

    public InstalledBiFunction(View<?> controller, Method method) {
        this.controller = controller;
        this.method = method;
    }

    @Override
    public Object apply(Object o1, Object o2) {
        try {
            return method.invoke(controller, o1, o2);
        } catch (IllegalAccessException | InvocationTargetException e) {
            if (e instanceof InvocationTargetException
                    && ((InvocationTargetException) e).getTargetException() instanceof RuntimeException) {
                throw (RuntimeException) ((InvocationTargetException) e).getTargetException();
            }

            throw new RuntimeException(String.format("Exception on @%s invocation", Install.class.getSimpleName()), e);
        }
    }

    @Override
    public String toString() {
        return "InstalledBiFunction{" +
                "frameOwner=" + controller.getClass() +
                ", method=" + method +
                '}';
    }
}
