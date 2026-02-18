/*
 * Copyright 2026 Haulmont.
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
import io.jmix.flowui.component.sidepanellayout.SidePanelLayout;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.kit.component.sidepanellayout.SidePanelMode;
import io.jmix.flowui.kit.component.sidepanellayout.SidePanelPosition;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.loader.LayoutLoader;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SidePanelLayoutLoader extends AbstractComponentLoader<SidePanelLayout> {

    protected List<ComponentLoader<?>> pendingLoadComponents = new ArrayList<>();

    @Override
    public void initComponent() {
        super.initComponent();
        createSubComponents(resultComponent, element);
    }

    @Override
    protected SidePanelLayout createComponent() {
        return factory.create(SidePanelLayout.class);
    }

    @Override
    public void loadComponent() {
        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);

        loadEnum(element, SidePanelPosition.class, "sidePanelPosition", resultComponent::setSidePanelPosition);
        loadEnum(element, SidePanelMode.class, "sidePanelMode", resultComponent::setSidePanelMode);
        loadBoolean(element, "modal", resultComponent::setModal);
        loadBoolean(element, "closeOnOutsideClick", resultComponent::setCloseOnOutsideClick);
        loadBoolean(element, "displayAsOverlayOnSmallDevices", resultComponent::setDisplayAsOverlayOnSmallDevices);
        loadString(element, "overlayAriaLabel", resultComponent::setOverlayAriaLabel);
        loadString(element, "sidePanelHorizontalMaxSize", resultComponent::setSidePanelHorizontalMaxSize);
        loadString(element, "sidePanelHorizontalMinSize", resultComponent::setSidePanelHorizontalMinSize);
        loadString(element, "sidePanelHorizontalSize", resultComponent::setSidePanelHorizontalSize);
        loadString(element, "sidePanelVerticalMaxSize", resultComponent::setSidePanelVerticalMaxSize);
        loadString(element, "sidePanelVerticalMinSize", resultComponent::setSidePanelVerticalMinSize);
        loadString(element, "sidePanelVerticalSize", resultComponent::setSidePanelVerticalSize);

        loadSubComponents();
    }

    protected void createSubComponents(SidePanelLayout sidePanelLayout, Element element) {
        List<Element> elements = element.elements();
        if (elements.size() != 2) {
            throw new GuiDevelopmentException(String.format(
                    sidePanelLayout.getClass().getSimpleName() + " '%s' must contain only two children",
                    resultComponent.getId()), context, "Component ID", resultComponent.getId());
        }

        LayoutLoader layoutLoader = getLayoutLoader();

        ComponentLoader<?> contentLoader = layoutLoader.createComponentLoader(elements.get(0));
        ComponentLoader<?> sidePanelContentLoader = layoutLoader.createComponentLoader(elements.get(1));

        contentLoader.initComponent();
        sidePanelContentLoader.initComponent();

        pendingLoadComponents.add(contentLoader);
        pendingLoadComponents.add(sidePanelContentLoader);

        resultComponent.setContent(contentLoader.getResultComponent());
        resultComponent.setSidePanelContent(sidePanelContentLoader.getResultComponent());
    }

    protected void createSubComponents(List<ComponentLoader<?>> loaders, Consumer<List<Component>> setter) {
        List<Component> components = new ArrayList<>();
        for (ComponentLoader<?> loader : loaders) {
            loader.initComponent();
            pendingLoadComponents.add(loader);
            components.add(loader.getResultComponent());
        }
        setter.accept(components);
    }

    protected void loadSubComponents() {
        for (ComponentLoader<?> componentLoader : pendingLoadComponents) {
            componentLoader.loadComponent();
        }
        pendingLoadComponents.clear();
    }
}
