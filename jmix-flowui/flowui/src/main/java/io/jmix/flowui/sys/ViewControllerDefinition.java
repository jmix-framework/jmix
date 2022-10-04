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

package io.jmix.flowui.sys;

import org.springframework.core.io.Resource;

import javax.annotation.Nullable;

public final class ViewControllerDefinition {

    private final String id;
    private final String controllerClassName;
    private final Resource resource;

    public ViewControllerDefinition(String id, String controllerClass) {
        this(id, controllerClass, null);
    }

    public ViewControllerDefinition(String id, String controllerClassName, @Nullable Resource resource) {
        this.id = id;
        this.controllerClassName = controllerClassName;
        this.resource = resource;
    }

    public String getId() {
        return id;
    }

    public String getControllerClassName() {
        return controllerClassName;
    }

    @Nullable
    public Resource getResource() {
        return resource;
    }
}
