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

package io.jmix.flowui.xml.layout.loader;

import com.vaadin.flow.component.Component;
import io.jmix.core.ClassManager;
import io.jmix.flowui.exception.GuiDevelopmentException;
import org.dom4j.Element;

public class GenericComponentLoader extends AbstractComponentLoader<Component> {

    protected ComponentPropertiesParsingManager propertyParser;

    @Override
    protected Component createComponent() {
        String componentClass = loadString(element, "component")
                .orElseThrow(() -> new GuiDevelopmentException("Missing required 'component' attribute", context));

        Class<?> aClass = applicationContext.getBean(ClassManager.class).loadClass(componentClass);
        if (!Component.class.isAssignableFrom(aClass)) {
            throw new GuiDevelopmentException("Component class '" + componentClass + "' is not a component", context);
        }

        return factory.create(aClass.asSubclass(Component.class));
    }

    @Override
    public void loadComponent() {
        loadProperties();
    }

    protected void loadProperties() {
        Element propertiesElement = element.element("properties");
        if (propertiesElement != null) {
            for (Element propertyElement : propertiesElement.elements("property")) {
                loadProperty(propertyElement);
            }
        }
    }

    protected void loadProperty(Element element) {
        String propertyName = loadString(element, "name")
                .orElseThrow(() -> new GuiDevelopmentException("Missing required 'name' attribute", context));
        String stringValue = loadString(element, "value")
                .orElseThrow(() -> new GuiDevelopmentException("Missing required 'value' attribute", context));
        String type = loadString(element, "type").orElse(null);

        getPropertyParser().parse(
                new ComponentPropertyParsingContext(resultComponent, propertyName, stringValue, type, context));
    }

    protected ComponentPropertiesParsingManager getPropertyParser() {
        if (propertyParser == null) {
            propertyParser = applicationContext.getBean(ComponentPropertiesParsingManager.class);
        }
        return propertyParser;
    }
}
