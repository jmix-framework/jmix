/*
 * Copyright (c) 2008-2022 Haulmont.
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
import io.jmix.flowui.xml.layout.loader.AbstractContainerLoader;

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

        componentLoader().loadClassName(resultComponent, element);
        componentLoader().loadThemeName(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);

        loadSubComponents();
    }

    protected void setContent(Tab selectedTab) {
        //setContext(selectedTab);
    }

    public static class TabLoader extends AbstractContainerLoader<Tab> {

        @Override
        protected Tab createComponent() {
            return factory.create(Tab.class);
        }

        @Override
        public void initComponent() {
            super.initComponent();
            loadString(element, "label", resultComponent::setLabel);

            createSubComponents(resultComponent, element);
        }

        @Override
        public void loadComponent() {
            //TODO: kremnevda, will be extended by JmixTabSheet 13.04.2022
            loadString(element, "label", resultComponent::setLabel);
            loadDouble(element, "flexGrow", resultComponent::setFlexGrow);

            componentLoader().loadEnabled(resultComponent, element);
            componentLoader().loadThemeName(resultComponent, element);
            componentLoader().loadClassName(resultComponent, element);

            loadSubComponents();
        }
    }
}
