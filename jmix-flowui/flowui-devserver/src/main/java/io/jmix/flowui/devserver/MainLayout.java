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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import io.jmix.flowui.devserver.theme.LegacyThemeStyleSheets;
import jakarta.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route("")
@AnonymousAllowed
public class MainLayout extends Div implements RouterLayout {

    public static final String PROJECT_STYLE_SHEETS_ATTRIBUTE = "ProjectStyleSheets";

    // com.vaadin.flow.theme.lumo.Lumo.STYLESHEET, com.vaadin.flow.theme.aura.Aura.STYLESHEET
    private static final Set<String> BASE_THEME_STYLE_SHEETS = Set.of("lumo/lumo.css", "aura/aura.css");

    private static final Logger log = LoggerFactory.getLogger(MainLayout.class);

    public MainLayout() {
        setId("jmix-view-designer-preview-main-layout");
        setSizeFull();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        UI ui = attachEvent.getUI();
        addStyleSheets(ui);
    }

    private static void addStyleSheets(UI ui) {
        Object projectStyleSheets = getServletContextAttribute(PROJECT_STYLE_SHEETS_ATTRIBUTE);
        for (String path : selectStyleSheets(projectStyleSheets, LegacyThemeStyleSheets.getStyleSheets())) {
            ui.getPage().addStyleSheet(path);
            log.debug("Added stylesheet: {}", path);
        }
    }

    /**
     * Project stylesheets replace the legacy {@code @Theme} ones only when they define a theme themselves.
     * Otherwise they complement it: a legacy theme project declares {@code @StyleSheet(Lumo.UTILITY_STYLESHEET)}
     * as Vaadin 25 requires, and older Studio versions pass only the {@code @StyleSheet} values.
     */
    static List<String> selectStyleSheets(Object projectStyleSheets, List<String> legacyStyleSheets) {
        List<String> projectPaths = projectStyleSheets instanceof List<?> list
                ? validStyleSheets(list.stream())
                : List.of();
        if (projectPaths.stream().anyMatch(BASE_THEME_STYLE_SHEETS::contains)) {
            return projectPaths;
        }
        Set<String> result = new LinkedHashSet<>(validStyleSheets(legacyStyleSheets.stream()));
        result.addAll(projectPaths);
        return List.copyOf(result);
    }

    private static List<String> validStyleSheets(Stream<?> styleSheets) {
        return styleSheets
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .filter(path -> !path.isBlank())
                .toList();
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
