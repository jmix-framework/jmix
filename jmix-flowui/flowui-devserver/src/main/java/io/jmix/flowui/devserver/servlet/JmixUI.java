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

package io.jmix.flowui.devserver.servlet;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinServlet;
import elemental.json.JsonValue;
import io.jmix.flowui.devserver.frontend.FrontendUtils;
import jakarta.servlet.ServletContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

@PreserveOnRefresh
public class JmixUI extends UI {

    private Object editorPanelStorage = null;

    private static final String EDITOR_PANEL_STORAGE_BEAN_ATTRIBUTE = "EditorPanelStorageBean";
    private static final String DESIGNER_ID_ATTRIBUTE = "DesignerId";

    private static final String LOCATION_REQUEST_PARAM = "location";

    @Override
    public void init(VaadinRequest request) {
        ServletContext servletContext = VaadinServlet.getCurrent().getServletContext();

        this.editorPanelStorage = servletContext.getAttribute(EDITOR_PANEL_STORAGE_BEAN_ATTRIBUTE);
        final String designerId = (String) servletContext.getAttribute(DESIGNER_ID_ATTRIBUTE);

        ClassLoader classLoader = servletContext.getClassLoader();
        HasComponents mainContent = createVerticalLayout();

        if (mainContent instanceof Component) {
            add((Component) mainContent);
        }

        String location = request.getParameter(LOCATION_REQUEST_PARAM);
        if (location == null || location.isBlank()) {
            if (mainContent != null) {
                mainContent.add(new Span("location parameter is empty"));
            }
            return;
        }

        String[] locationSplit = location.split("-");
        if (locationSplit.length != 2) {
            if (mainContent != null) {
                mainContent.add(new Span("location " + location + " is incorrect format"));
            }
            return;
        }

        String designerType = locationSplit[0];
        if (!designerId.equals(designerType)) {
            if (mainContent != null) {
                mainContent.add(new Span("Designer with " + designerType + " not found"));
            }
            return;
        }

        try {
            Component editorPanel = createAndRegisterDesigner(location, classLoader);
            if (mainContent != null) {
                mainContent.add(editorPanel);
            }
        } catch (Throwable e) {
            if (mainContent != null) {
                mainContent.add(new Span(e.getMessage()));
            }
        }
    }

    private Component createAndRegisterDesigner(String id, ClassLoader classLoader) {
        HasComponents editorPanel = createVerticalLayout();

        if (editorPanel != null && editorPanelStorage != null) {
            Class<?> editorPanelStorageClass = editorPanelStorage.getClass();
            Method registerMethod = Arrays.stream(editorPanelStorageClass.getMethods())
                    .filter(it -> it.getName().equals("register"))
                    .findFirst()
                    .orElse(null);
            if (registerMethod != null) {
                try {
                    registerMethod.trySetAccessible();
                    registerMethod.invoke(editorPanelStorage, id, editorPanel);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    FrontendUtils.console("\nError when register panel. Stacktrace:\n"
                            + Arrays.toString(e.getStackTrace()) + "\n");
                }
            } else {
                FrontendUtils.console("\nMethod with name 'register' not found in " + editorPanelStorageClass);
            }
        } else {
            FrontendUtils.console("\nEditorPanel or EditorPanelStorage is null\n");
        }

        return (Component) editorPanel;
    }

    @Override
    public void connectClient(
            String flowRoutePath,
            String flowRouteQuery,
            String appShellTitle,
            JsonValue historyState,
            String trigger
    ) {
        // do nothing
    }

    private HasComponents createVerticalLayout() {
        try {
            HasComponents component = new VerticalLayout();
            component.getElement().getThemeList().set("padding", false);
            ((HasSize) component).setSizeFull();

            return component;
        } catch (Throwable e) {
            return null;
        }
    }
}
