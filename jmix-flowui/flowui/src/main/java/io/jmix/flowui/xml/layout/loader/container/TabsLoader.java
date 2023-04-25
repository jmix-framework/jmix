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

import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import io.jmix.flowui.xml.layout.loader.LayoutLoader;
import org.dom4j.Element;

public class TabsLoader extends AbstractTabsLoader<Tabs> {

    @Override
    protected Tabs createComponent() {
        return factory.create(Tabs.class);
    }

    @Override
    public void initComponent() {
        super.initComponent();

        createTabs(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        loadEnum(element, Tabs.Orientation.class, "orientation", resultComponent::setOrientation);

        super.loadComponent();
    }

    protected void createTabs(Tabs resultComponent, Element element) {
        LayoutLoader loader = getLayoutLoader();

        for (Element subElement : element.elements("tab")) {
            TabLoader componentLoader = ((TabLoader) loader.getLoader(subElement, TabLoader.class));
            componentLoader.initComponent();
            pendingLoadComponents.add(componentLoader);

            resultComponent.add(componentLoader.getResultComponent());
        }
    }

    public static class TabLoader extends AbstractTabLoader {

        @Override
        protected Tab createComponent() {
            return factory.create(Tab.class);
        }

        @Override
        public void initComponent() {
            super.initComponent();

            createSubComponents(resultComponent, element);
        }
    }
}
