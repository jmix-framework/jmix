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

package io.jmix.flowui.xml.layout.loader.container;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.tabs.Tab;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.loader.LayoutLoader;
import org.dom4j.Element;

public class TabSheetLoader extends AbstractTabsLoader<JmixTabSheet> {

    @Override
    protected JmixTabSheet createComponent() {
        return factory.create(JmixTabSheet.class);
    }

    @Override
    public void initComponent() {
        super.initComponent();

        createSubComponents();
    }

    protected void createSubComponents() {
        LayoutLoader loader = getLayoutLoader();

        for (Element subElement : element.elements("tab")) {
            ComponentLoader<?> componentLoader = loader.getLoader(subElement, TabLoader.class);
            componentLoader.initComponent();

            pendingLoadComponents.add(componentLoader);
        }
    }

    @Override
    protected void loadSubComponents() {
        for (ComponentLoader<?> componentLoader : pendingLoadComponents) {
            componentLoader.loadComponent();
            Tab tab = (Tab) componentLoader.getResultComponent();
            Component content = ((TabLoader) componentLoader).getContent();

            resultComponent.add(tab, content);
        }

        pendingLoadComponents.clear();
    }

    public static class TabLoader extends AbstractTabLoader {

        protected Component content;

        @Override
        protected Tab createComponent() {
            return factory.create(Tab.class);
        }

        @Override
        public void initComponent() {
            super.initComponent();
            if (resultComponent.getId().isEmpty()) {
                String message = String.format("ID attribute is required for %s",
                        resultComponent.getClass().getSimpleName());
                throw new GuiDevelopmentException(message, context);
            }

            createSubComponents(resultComponent, element);
        }

        @Override
        protected void createSubComponents(HasComponents container, Element containerElement) {
            if (containerElement.elements().size() != 1) {
                String message = String.format("%s should have only one child component",
                        resultComponent.getClass().getSimpleName());

                throw new GuiDevelopmentException(message, context);
            }

            LayoutLoader loader = getLayoutLoader();

            for (Element subElement : containerElement.elements()) {
                ComponentLoader<?> componentLoader = loader.createComponentLoader(subElement);
                componentLoader.initComponent();
                pendingLoadComponents.add(componentLoader);

                content = componentLoader.getResultComponent();
            }
        }

        public Component getContent() {
            return content;
        }
    }
}
