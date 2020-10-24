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

package io.jmix.ui.xml.layout.loader;

import com.google.common.base.Strings;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.component.JavaScriptComponent;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

public class JavaScriptComponentLoader extends AbstractComponentLoader<JavaScriptComponent> {

    @Override
    public void createComponent() {
        resultComponent = factory.create(JavaScriptComponent.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        assignXmlDescriptor(resultComponent, element);
        assignFrame(resultComponent);

        loadEnable(resultComponent, element);
        loadVisible(resultComponent, element);

        loadStyleName(resultComponent, element);

        loadWidth(resultComponent, element);
        loadHeight(resultComponent, element);
        loadAlign(resultComponent, element);

        loadResponsive(resultComponent, element);
        loadCss(resultComponent, element);

        loadCaption(resultComponent, element);
        loadDescription(resultComponent, element);
        loadIcon(resultComponent, element);
        loadContextHelp(resultComponent, element);

        loadRequiredIndicatorVisible(resultComponent, element);
        loadInitFunctionName(resultComponent, element);
        loadDependencies(resultComponent, element);
    }

    protected void loadInitFunctionName(JavaScriptComponent component, Element element) {
        String initFunctionName = element.attributeValue("initFunctionName");
        if (!Strings.isNullOrEmpty(initFunctionName)) {
            component.setInitFunctionName(initFunctionName);
        }
    }

    protected void loadDependencies(JavaScriptComponent component, Element element) {
        Element dependenciesElement = element.element("dependencies");
        if (dependenciesElement == null) {
            return;
        }

        List<JavaScriptComponent.ClientDependency> dependencies = new ArrayList<>();
        for (Element dependency : dependenciesElement.elements("dependency")) {
            String path = dependency.attributeValue("path");
            if (Strings.isNullOrEmpty(path)) {
                throw new GuiDevelopmentException("No path provided for a JavaScriptComponent dependency", context);
            }

            String type = dependency.attributeValue("type");
            JavaScriptComponent.DependencyType dependencyType = !Strings.isNullOrEmpty(type)
                    ? JavaScriptComponent.DependencyType.valueOf(type)
                    : null;

            dependencies.add(new JavaScriptComponent.ClientDependency(path, dependencyType));
        }

        component.setDependencies(dependencies);
    }
}
