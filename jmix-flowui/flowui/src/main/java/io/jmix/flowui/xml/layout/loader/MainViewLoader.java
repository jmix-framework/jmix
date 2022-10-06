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

package io.jmix.flowui.xml.layout.loader;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.Div;
import io.jmix.flowui.app.main.StandardMainView;
import io.jmix.flowui.exception.GuiDevelopmentException;
import org.dom4j.Element;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MainViewLoader extends AbstractViewLoader<StandardMainView> {

    public static final String MAIN_VIEW_ROOT = "mainView";
    public static final String CONTENT_NAME = "appLayout";

    @Override
    public void createContent() {
        Element appLayoutElement = getAppLayoutElement();

        List<Component> navigationBarComponents = createSubComponents(appLayoutElement, "navigationBar");
        if (!navigationBarComponents.isEmpty()) {
            boolean touchOptimized = getLoaderSupport()
                    .loadBoolean(appLayoutElement.element("navigationBar"), "touchOptimized")
                    .orElse(true);

            resultComponent.getContent()
                    .addToNavbar(touchOptimized, navigationBarComponents.toArray(new Component[0]));
        }

        List<Component> drawerLayoutComponents = createSubComponents(appLayoutElement, "drawerLayout");
        if (!drawerLayoutComponents.isEmpty()) {
            resultComponent.getContent().addToDrawer(drawerLayoutComponents.toArray(new Component[0]));
        }
    }

    @Override
    public void loadComponent() {
        getViewLoader().loadData(element);
        getViewLoader().loadActions(element);
        getViewLoader().loadFacets(element);

        loadAppLayout();


        loadSubComponents();
    }

    protected Element getAppLayoutElement() {
        Element appLayout = element.element(CONTENT_NAME);
        if (appLayout == null) {
            throw new GuiDevelopmentException("Required '" + CONTENT_NAME + "' element is not found", context);
        }
        return appLayout;
    }

    protected void loadAppLayout() {
        AppLayout appLayout = resultComponent.getContent();
        Element appLayoutElement = getAppLayoutElement();

        componentLoader().loadClassNames(appLayout, appLayoutElement);

        getLoaderSupport().loadBoolean(appLayoutElement, "drawerOpened", appLayout::setDrawerOpened);
        getLoaderSupport().loadEnum(appLayoutElement, AppLayout.Section.class, "primarySection")
                .ifPresentOrElse(
                        appLayout::setPrimarySection,
                        () -> appLayout.setPrimarySection(AppLayout.Section.DRAWER));
    }

    protected List<Component> createSubComponents(Element appLayout, String contentName) {
        Element contentElement = appLayout.element(contentName);
        if (contentElement != null) {
            Div bufferComponent = new Div();

            createSubComponents(bufferComponent, contentElement);

            return bufferComponent.getChildren().collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
