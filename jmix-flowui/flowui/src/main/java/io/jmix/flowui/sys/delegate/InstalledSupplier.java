/*
 * Copyright 2019 Haulmont.
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


import io.jmix.flowui.SameAsUi;
import io.jmix.flowui.screen.Install;
import io.jmix.flowui.screen.Screen;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;

@SameAsUi
public class InstalledSupplier implements Supplier<Object> {

    private final Screen controller;
    private final Method method;

    public InstalledSupplier(Screen controller, Method method) {
        this.controller = controller;
        this.method = method;
    }

    @Override
    public Object get() {
        try {
            return method.invoke(controller);
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
        return "InstalledSupplier{" +
                "target=" + controller.getClass() +
                ", method=" + method +
                '}';
    }
}
