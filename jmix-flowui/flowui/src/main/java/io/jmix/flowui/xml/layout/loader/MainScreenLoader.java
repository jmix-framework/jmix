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
import io.jmix.flowui.app.main.StandardMainScreen;
import io.jmix.flowui.exception.GuiDevelopmentException;
import org.dom4j.Element;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MainScreenLoader extends AbstractScreenLoader<StandardMainScreen> {

    public static final String MAIN_SCREEN_ROOT = "mainScreen";
    public static final String CONTENT_NAME = "appLayout";

    @Override
    public void createContent() {
        Element appLayout = element.element("appLayout");
        if (appLayout == null) {
            throw new GuiDevelopmentException("Required '" + CONTENT_NAME + "' element is not found", context);
        }

        List<Component> navigationBarComponents = createSubComponents(appLayout, "navigationBar");
        if (!navigationBarComponents.isEmpty()) {
            boolean touchOptimized = getLoaderSupport()
                    .loadBoolean(appLayout.element("navigationBar"), "touchOptimized")
                    .orElse(true);

            resultComponent.getContent()
                    .addToNavbar(touchOptimized, navigationBarComponents.toArray(new Component[0]));
        }

        List<Component> drawerLayoutComponents = createSubComponents(appLayout, "drawerLayout");
        if (!drawerLayoutComponents.isEmpty()) {
            resultComponent.getContent().addToDrawer(drawerLayoutComponents.toArray(new Component[0]));
        }
    }

    @Override
    public void loadComponent() {
        getScreenLoader().loadScreenData(element);
        getScreenLoader().loadScreenActions(element);
        getScreenLoader().loadFacets(element);

        getLoaderSupport().loadBoolean(element, "drawerOpened",
                drawerOpened -> resultComponent.getContent().setDrawerOpened(drawerOpened));

        getLoaderSupport().loadEnum(element, AppLayout.Section.class, "primarySection")
                .ifPresentOrElse(
                        section -> resultComponent.getContent().setPrimarySection(section),
                        () -> resultComponent.getContent().setPrimarySection(AppLayout.Section.DRAWER));

        loadSubComponents();
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
