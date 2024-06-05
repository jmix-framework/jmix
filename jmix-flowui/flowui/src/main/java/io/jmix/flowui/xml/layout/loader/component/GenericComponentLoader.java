/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.xml.layout.loader.component;

import com.vaadin.flow.component.Component;
import io.jmix.core.ClassManager;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.loader.PropertiesLoaderSupport;

public class GenericComponentLoader extends AbstractComponentLoader<Component> {

    @Override
    protected Component createComponent() {
        String componentClass = loadString(element, "class")
                .orElseThrow(() ->
                        new GuiDevelopmentException("Missing required 'componentClass' attribute", context));

        Class<?> aClass = applicationContext.getBean(ClassManager.class).loadClass(componentClass);
        if (!Component.class.isAssignableFrom(aClass)) {
            throw new GuiDevelopmentException("Class '" + componentClass + "' is not a component", context);
        }

        return factory.create(aClass.asSubclass(Component.class));
    }

    @Override
    public void loadComponent() {
        if (element.element("properties") != null) {
            PropertiesLoaderSupport propertiesLoader =
                    applicationContext.getBean(PropertiesLoaderSupport.class, context);
            propertiesLoader.loadProperties(resultComponent, element);
        }
    }
}
