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

import javax.annotation.Nullable;
import java.util.Optional;

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

    public String getId() {
        return id;
    }

    public String getControllerClassName() {
        return controllerClassName;
    }

    public Class<? extends View<?>> getControllerClass() {
        return controllerClass;
    }

    public Optional<String> getTemplatePath() {
        return Optional.ofNullable(templatePath);
    }
}
