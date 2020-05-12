/*
 * Copyright 2020 Haulmont.
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

package io.jmix.dynattrui.impl.model;

import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotations.ModelObject;
import io.jmix.core.metamodel.annotations.ModelProperty;
import io.jmix.data.entity.BaseUuidEntity;

@ModelObject(name = "sys$ScreenAndComponent")
@SystemLevel
public class ScreenAndComponent extends BaseUuidEntity {

    private static final long serialVersionUID = -6064270441563369464L;

    @ModelProperty
    protected String screen;

    @ModelProperty
    protected String component;

    public ScreenAndComponent() {
    }

    public ScreenAndComponent(String screen, String component) {
        this.screen = screen;
        this.component = component;
    }

    public String getScreen() {
        return screen;
    }

    public void setScreen(String screen) {
        this.screen = screen;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }
}
