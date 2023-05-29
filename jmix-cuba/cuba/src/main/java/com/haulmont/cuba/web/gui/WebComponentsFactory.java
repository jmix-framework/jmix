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
package com.haulmont.cuba.web.gui;

import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.components.ComponentGenerationContext;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Timer;
import io.jmix.ui.component.UiComponentsGenerator;
import io.jmix.ui.component.impl.TimerImpl;

import javax.inject.Inject;

@org.springframework.stereotype.Component(ComponentsFactory.NAME)
public class WebComponentsFactory implements ComponentsFactory {

    @Inject
    protected UiComponents uiComponents;
    @Inject
    protected UiComponentsGenerator uiComponentsGenerator;

    @Override
    public <T extends Component> T createComponent(String name) {
        return uiComponents.create(name);
    }

    @Override
    public <T extends Component> T createComponent(Class<T> type) {
        return uiComponents.create(type);
    }

    @Override
    public Component createComponent(ComponentGenerationContext context) {
        return uiComponentsGenerator.generate(context);
    }

    @Override
    public Timer createTimer() {
        return new TimerImpl();
    }
}