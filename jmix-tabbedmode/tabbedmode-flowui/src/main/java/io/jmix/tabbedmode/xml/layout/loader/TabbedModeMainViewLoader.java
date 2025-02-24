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

package io.jmix.tabbedmode.xml.layout.loader;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.Div;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.loader.AbstractViewLoader;
import io.jmix.tabbedmode.app.main.StandardTabbedModeMainView;
import io.jmix.tabbedmode.component.workarea.WorkArea;
import org.dom4j.Element;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TabbedModeMainViewLoader extends AbstractViewLoader<StandardTabbedModeMainView> {

    public static final String MAIN_VIEW_ROOT = "mainView";
    public static final String CONTENT_NAME = "appLayout";
    public static final String WORK_AREA_ELEMENT_NAME = "workArea";

    protected ComponentLoader<?> workAreaLoader;

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

        createWorkArea(appLayoutElement);
    }

    @Override
    public void loadComponent() {
        getViewLoader().loadData(element);
        getViewLoader().loadActions(element);
        getViewLoader().loadFacets(element);

        loadAppLayout();
        loadWorkArea();

        loadSubComponents();
    }

    protected void createWorkArea(Element appLayoutElement) {
        Element workAreaElement = appLayoutElement.element(WORK_AREA_ELEMENT_NAME);
        if (workAreaElement == null) {
            return;
        }

        ComponentLoader<?> workAreaLoader = getLayoutLoader().createComponentLoader(workAreaElement);
        workAreaLoader.initComponent();

        WorkArea workArea = (WorkArea) workAreaLoader.getResultComponent();
        resultComponent.setWorkArea(workArea);
    }

    protected void loadWorkArea() {
        if (workAreaLoader != null) {
            workAreaLoader.loadComponent();
        }
    }

    // +
    protected Element getAppLayoutElement() {
        Element appLayout = element.element(CONTENT_NAME);
        if (appLayout == null) {
            throw new GuiDevelopmentException("Required '%s' element is not found".formatted(CONTENT_NAME), context);
        }

        return appLayout;
    }

    // +
    protected void loadAppLayout() {
        AppLayout appLayout = resultComponent.getContent();
        Element appLayoutElement = getAppLayoutElement();

        componentLoader().loadClassNames(appLayout, appLayoutElement);
        componentLoader().loadCss(appLayout, appLayoutElement);

        getLoaderSupport().loadBoolean(appLayoutElement, "drawerOpened", appLayout::setDrawerOpened);
        getLoaderSupport().loadEnum(appLayoutElement, AppLayout.Section.class, "primarySection")
                .ifPresentOrElse(
                        appLayout::setPrimarySection,
                        () -> appLayout.setPrimarySection(AppLayout.Section.DRAWER));
    }

    // +
    protected List<Component> createSubComponents(Element appLayout, String contentName) {
        Element contentElement = appLayout.element(contentName);
        if (contentElement != null) {
            Div bufferComponent = new Div();
            componentLoader().loadCss(bufferComponent, contentElement);

            createSubComponents(bufferComponent, contentElement);

            return bufferComponent.getChildren().collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
