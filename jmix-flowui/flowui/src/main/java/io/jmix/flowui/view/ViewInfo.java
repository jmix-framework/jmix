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

package io.jmix.flowui.view;

import org.springframework.lang.Nullable;

import java.util.Optional;

/**
 * Represents metadata information of a view, including the view's unique identifier,
 * its controller class, and an optional template path.
 */
public class ViewInfo {

    protected String id;
    protected String controllerClassName;
    protected Class<? extends View<?>> controllerClass;
    protected String templatePath;

    public ViewInfo(String id,
                    String controllerClassName,
                    Class<? extends View<?>> controllerClass,
                    @Nullable String templatePath) {
        this.id = id;
        this.controllerClassName = controllerClassName;
        this.controllerClass = controllerClass;
        this.templatePath = templatePath;
    }

    /**
     * Returns the unique identifier of the {@link View}.
     *
     * @return the unique identifier of the {@link View}
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the name of the controller class associated with the {@link View}.
     *
     * @return the name of the controller class
     */
    public String getControllerClassName() {
        return controllerClassName;
    }

    /**
     * Returns the controller class associated with the {@link View}.
     *
     * @return the controller class associated with the {@link View}
     */
    public Class<? extends View<?>> getControllerClass() {
        return controllerClass;
    }

    /**
     * Returns the template path associated with the {@link View}, if available.
     *
     * @return an {@link Optional} containing the template path, or an
     * empty {@link Optional} if the template path is not set
     */
    public Optional<String> getTemplatePath() {
        return Optional.ofNullable(templatePath);
    }
}
