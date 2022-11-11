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

package io.jmix.ui.exception;

import io.jmix.core.common.util.ReflectionHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * DEPRECATED. Use only exception handlers defined as Spring beans.
 */
@Deprecated
public class ExceptionHandlersConfiguration {

    private List<Class> handlerClasses = new ArrayList<>();

    /**
     * Set the list of exception handler class names, usually from spring.xml.
     * @param list  list of class names
     */
    public void setHandlerClasses(List<String> list) {
        for (String className : list) {
            handlerClasses.add(ReflectionHelper.getClass(className));
        }
    }

    /**
     * Get the list of exception handler class names.
     * @return  list of class names
     */
    public List<Class> getHandlerClasses() {
        return handlerClasses;
    }
}