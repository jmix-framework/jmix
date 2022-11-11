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

public class TabsLoader extends AbstractContainerLoader<Tabs> {

    @Override
    protected Tabs createComponent() {
        return factory.create(Tabs.class);
    }

    @Override
    public void initComponent() {
        super.initComponent();

        createSubComponents(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        loadEnum(element, Tabs.Orientation.class, "orientation", resultComponent::setOrientation);

        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadThemeNames(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);

        loadSubComponents();
    }

    public static class TabLoader extends AbstractContainerLoader<Tab> {

        @Override
        protected Tab createComponent() {
            return factory.create(Tab.class);
        }

        @Override
        public void initComponent() {
            super.initComponent();

            createSubComponents(resultComponent, element);
        }

        @Override
        public void loadComponent() {
            loadDouble(element, "flexGrow", resultComponent::setFlexGrow);

            componentLoader().loadLabel(resultComponent, element);
            componentLoader().loadEnabled(resultComponent, element);
            componentLoader().loadThemeNames(resultComponent, element);
            componentLoader().loadClassNames(resultComponent, element);

            loadSubComponents();
        }
    }
}
