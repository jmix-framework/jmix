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
import com.vaadin.flow.component.internal.JavaScriptBootstrapUI;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinServlet;
import elemental.json.JsonValue;
import io.jmix.flowui.devserver.frontend.FrontendUtils;

import javax.servlet.ServletContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

@PreserveOnRefresh
public class JmixJavaScriptBootstrapUI extends JavaScriptBootstrapUI {

    private Object editorPanelStorage = null;

    private static final String EDITOR_PANEL_STORAGE_BEAN_ATTRIBUTE = "EditorPanelStorageBean";
    private static final String DESIGNER_ID_ATTRIBUTE = "DesignerId";

    private static final String LOCATION_REQUEST_PARAM = "location";

    private static final String VERTICAL_LAYOUT = "com.vaadin.flow.component.orderedlayout.VerticalLayout";
    private static final String LABEL = "com.vaadin.flow.component.html.Label";

    @Override
    public void init(VaadinRequest request) {
        ServletContext servletContext = VaadinServlet.getCurrent().getServletContext();

        this.editorPanelStorage = servletContext.getAttribute(EDITOR_PANEL_STORAGE_BEAN_ATTRIBUTE);
        final String designerId = (String) servletContext.getAttribute(DESIGNER_ID_ATTRIBUTE);

        ClassLoader classLoader = servletContext.getClassLoader();
        HasComponents mainContent = createVerticalLayout(classLoader);

        if (mainContent instanceof Component) {
            add((Component) mainContent);
        }

        String location = request.getParameter(LOCATION_REQUEST_PARAM);
        if (location == null || location.isBlank()) {
            if (mainContent != null) {
                mainContent.add(createLabel(classLoader, "location parameter is empty"));
            }
            return;
        }

        String[] locationSplit = location.split("-");
        if (locationSplit.length != 2) {
            if (mainContent != null) {
                mainContent.add(createLabel(classLoader, "location " + location + " is incorrect format"));
            }
            return;
        }

        String designerType = locationSplit[0];
        if (!designerId.equals(designerType)) {
            if (mainContent != null) {
                mainContent.add(createLabel(classLoader, "Designer with " + designerType + " not found"));
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
                mainContent.add(createLabel(classLoader, e.getMessage()));
            }
        }
    }

    private Component createAndRegisterDesigner(String id, ClassLoader classLoader) {
        HasComponents editorPanel = createVerticalLayout(classLoader);

        if (editorPanel != null && editorPanelStorage != null) {
            Class<?> editorPanelStorageClass = editorPanelStorage.getClass();
            Method registerMethod = Arrays.stream(editorPanelStorageClass.getMethods())
                    .filter(it -> it.getName().equals("register"))
                    .findFirst()
                    .orElse(null);
            if (registerMethod != null) {
                try {
                    registerMethod.invoke(editorPanelStorage, id, editorPanel);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    FrontendUtils.console("Error when register panel:\n" + e);
                }
            } else {
                FrontendUtils.console("Method with name 'register' not found in " + editorPanelStorageClass);
            }
        } else {
            FrontendUtils.console("EditorPanel or EditorPanelStorage is null");
        }

        return (Component) editorPanel;
    }

    @Override
    public void connectClient(String clientElementTag,
                              String clientElementId,
                              String flowRoute,
                              String appShellTitle,
                              JsonValue historyState) {
        // do nothing
    }

    private HasComponents createVerticalLayout(ClassLoader classLoader) {
        try {
            Class<?> componentClass = classLoader.loadClass(VERTICAL_LAYOUT);
            HasComponents component = (HasComponents) componentClass.getConstructor().newInstance();
            component.getElement().getThemeList().set("padding", false);
            if (component instanceof HasSize) {
                ((HasSize) component).setSizeFull();
            }

            return component;
        } catch (Throwable e) {
            return null;
        }
    }

    private Component createLabel(ClassLoader classLoader, String text) {
        try {
            Class<?> componentClass = classLoader.loadClass(LABEL);
            return (Component) componentClass.getConstructor(String.class).newInstance(text);
        } catch (Throwable e) {
            return null;
        }
    }
}
