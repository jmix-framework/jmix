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

import io.jmix.core.annotation.Internal;
import io.jmix.flowui.view.View;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;

/**
 * Represents a definition of a {@link View} Controller in the application. This includes its identifier,
 * class name of the controller, and optionally associated resource metadata.
 */
@Internal
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

    /**
     * Returns the unique identifier associated with the {@link View}.
     *
     * @return the unique identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the class name of the controller associated with the {@link View}.
     *
     * @return the controller class name
     */
    public String getControllerClassName() {
        return controllerClassName;
    }

    /**
     * Returns the {@link Resource} associated with the {@link View}, or {@code null} if no resource is associated.
     *
     * @return the associated {@link Resource}, or {@code null} if there is none
     */
    @Nullable
    public Resource getResource() {
        return resource;
    }
}
