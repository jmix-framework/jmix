/*
 * Copyright 2025 Haulmont.
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
import io.jmix.flowui.component.drawerlayout.DrawerLayout;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.kit.component.drawerlayout.DrawerMode;
import io.jmix.flowui.kit.component.drawerlayout.DrawerPlacement;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.loader.LayoutLoader;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DrawerLayoutLoader extends AbstractComponentLoader<DrawerLayout> {

    protected List<ComponentLoader<?>> pendingLoadComponents = new ArrayList<>();

    @Override
    public void initComponent() {
        super.initComponent();
        createSubComponents(resultComponent, element);
    }

    @Override
    protected DrawerLayout createComponent() {
        return factory.create(DrawerLayout.class);
    }

    @Override
    public void loadComponent() {
        componentLoader().loadSizeAttributes(resultComponent, element);

        loadEnum(element, DrawerPlacement.class, "drawerPlacement", resultComponent::setDrawerPlacement);
        loadEnum(element, DrawerMode.class, "drawerMode", resultComponent::setDrawerMode);
        loadBoolean(element, "modal", resultComponent::setModal);
        loadBoolean(element, "closeOnModalityCurtainClick", resultComponent::setCloseOnModalityCurtainClick);
        loadBoolean(element, "displayOverlayOnSmallScreen", resultComponent::setDisplayOverlayOnSmallScreen);
        loadString(element, "drawerHorizontalMaxSize", resultComponent::setDrawerHorizontalMaxSize);
        loadString(element, "drawerHorizontalMinSize", resultComponent::setDrawerHorizontalMinSize);
        loadString(element, "drawerHorizontalSize", resultComponent::setDrawerHorizontalSize);
        loadString(element, "drawerVerticalMaxSize", resultComponent::setDrawerVerticalMaxSize);
        loadString(element, "drawerVerticalMinSize", resultComponent::setDrawerVerticalMinSize);
        loadString(element, "drawerVerticalSize", resultComponent::setDrawerVerticalSize);

        loadSubComponents();
    }

    protected void createSubComponents(DrawerLayout drawerLayout, Element element) {
        List<Element> elements = element.elements();
        if (elements.size() != 2) {
            throw new GuiDevelopmentException(String.format(
                    drawerLayout.getClass().getSimpleName() + " '%s' must contain only two children",
                    resultComponent.getId()), context, "Component ID", resultComponent.getId());
        }

        LayoutLoader layoutLoader = getLayoutLoader();

        ComponentLoader<?> contentLoader = layoutLoader.createComponentLoader(elements.get(0));
        ComponentLoader<?> drawerContentLoader = layoutLoader.createComponentLoader(elements.get(1));

        contentLoader.initComponent();
        drawerContentLoader.initComponent();

        pendingLoadComponents.add(contentLoader);
        pendingLoadComponents.add(drawerContentLoader);

        resultComponent.setContent(contentLoader.getResultComponent());
        resultComponent.setDrawerContent(drawerContentLoader.getResultComponent());
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
