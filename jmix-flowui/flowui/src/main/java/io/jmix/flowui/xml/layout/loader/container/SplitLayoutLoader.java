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

import com.vaadin.flow.component.splitlayout.SplitLayout;
import io.jmix.flowui.component.splitlayout.JmixSplitLayout;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.loader.LayoutLoader;
import org.dom4j.Element;

import java.util.List;

public class SplitLayoutLoader extends AbstractContainerLoader<JmixSplitLayout> {

    @Override
    protected JmixSplitLayout createComponent() {
        return factory.create(JmixSplitLayout.class);
    }

    @Override
    public void initComponent() {
        super.initComponent();
        createContent(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        loadDouble(element, "splitterPosition", resultComponent::setSplitterPosition);
        loadEnum(element, SplitLayout.Orientation.class, "orientation", resultComponent::setOrientation);

        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadThemeNames(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);

        loadSubComponents();
    }

    protected void createContent(SplitLayout resultComponent, Element element) {
        LayoutLoader loader = getLayoutLoader();
        List<Element> elements = element.elements();

        if (elements.size() != 2) {
            throw new GuiDevelopmentException(String.format(
                    "Split '%s' must contain only two children", resultComponent.getId()), context,
                    "Component ID", resultComponent.getId());
        }

        ComponentLoader<?> primaryComponentLoader = loader.createComponentLoader(elements.get(0));
        ComponentLoader<?> secondaryComponentLoader = loader.createComponentLoader(elements.get(1));

        primaryComponentLoader.initComponent();
        secondaryComponentLoader.initComponent();

        pendingLoadComponents.add(primaryComponentLoader);
        pendingLoadComponents.add(secondaryComponentLoader);

        resultComponent.addToPrimary(primaryComponentLoader.getResultComponent());
        resultComponent.addToSecondary(secondaryComponentLoader.getResultComponent());
    }
}
