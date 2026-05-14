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

package io.jmix.flowui.devserver;

import java.util.List;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route("")
@AnonymousAllowed
public class MainLayout extends Div implements RouterLayout {

    public static final String PROJECT_STYLE_SHEETS_ATTRIBUTE = "ProjectStyleSheets";

    private static final Logger log = LoggerFactory.getLogger(MainLayout.class);

    public MainLayout() {
        setId("jmix-view-designer-preview-main-layout");
        setSizeFull();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        addProjectStyleSheets(attachEvent.getUI());
    }

    private static void addProjectStyleSheets(UI ui) {
        Object attr = getServletContextAttribute(PROJECT_STYLE_SHEETS_ATTRIBUTE);
        if (!(attr instanceof List<?> list)) {
            return;
        }
        for (Object item : list) {
            if (item instanceof String path && !path.isBlank()) {
                ui.getPage().addStyleSheet(path);
                log.debug("Added project stylesheet: {}", path);
            }
        }
    }

    private static Object getServletContextAttribute(String name) {
        VaadinServlet servlet = VaadinServlet.getCurrent();
        if (servlet == null) {
            return null;
        }
        ServletContext servletContext = servlet.getServletContext();
        return servletContext != null ? servletContext.getAttribute(name) : null;
    }
}
