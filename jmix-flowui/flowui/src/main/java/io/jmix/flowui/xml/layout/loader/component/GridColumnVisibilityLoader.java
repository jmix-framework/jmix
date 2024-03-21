/*
 * Copyright 2023 Haulmont.
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

import io.jmix.flowui.component.gridcolumnvisibility.JmixGridColumnVisibility;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.xml.layout.inittask.AssignGridColumnVisibilityPropertiesInitTask;
import io.jmix.flowui.xml.layout.inittask.AssignGridColumnVisibilityPropertiesInitTask.MenuItem;
import io.jmix.flowui.xml.layout.inittask.AssignGridColumnVisibilityPropertiesInitTask.DeferredLoadContext;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import org.dom4j.Element;

import java.util.LinkedHashMap;
import java.util.Map;

public class GridColumnVisibilityLoader extends AbstractComponentLoader<JmixGridColumnVisibility> {

    @Override
    protected JmixGridColumnVisibility createComponent() {
        return factory.create(JmixGridColumnVisibility.class);
    }

    @Override
    public void loadComponent() {
        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadTitle(resultComponent, element, context);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadOverlayClass(resultComponent, element);
        componentLoader().loadFocusableAttributes(resultComponent, element);
        componentLoader().loadThemeNames(resultComponent, element);
        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadText(resultComponent, element);
        componentLoader().loadWhiteSpace(resultComponent, element);
        componentLoader().loadIcon(element, resultComponent::setIcon);

        getLoaderSupport().loadBoolean(element, "showAllEnabled", resultComponent::setShowAllEnabled);
        getLoaderSupport().loadBoolean(element, "hideAllEnabled", resultComponent::setHideAllEnabled);

        loadGridSpecificProperties(element);
    }

    protected void loadGridSpecificProperties(Element element) {
        String gridId = loadDataGridId(element);

        DeferredLoadContext loadContext = new DeferredLoadContext(resultComponent, gridId);

        getLoaderSupport().loadString(element, "include", loadContext::setIncludeColumns);
        getLoaderSupport().loadString(element, "exclude", loadContext::setExcludeColumns);
        loadMenuItems(element, loadContext);

        InitTask initTask = new AssignGridColumnVisibilityPropertiesInitTask(loadContext);
        getComponentContext().addInitTask(initTask);
    }

    protected String loadDataGridId(Element element) {
        return getLoaderSupport().loadString(element, "dataGrid")
                .orElseThrow(() ->
                        new GuiDevelopmentException("Grid id is required for column visibility component", context));
    }

    protected void loadMenuItems(Element rootElement, DeferredLoadContext loadContext) {
        Map<String, MenuItem> menuItems = new LinkedHashMap<>();
        for (Element element : rootElement.elements()) {
            if (element.getName().equals("menuItem")) {
                String refColumn = getLoaderSupport().loadString(element, "refColumn")
                        .orElseThrow(() ->
                                new GuiDevelopmentException("Failed to find refColumn attribute for columnItem", context));
                MenuItem menuItem = new MenuItem(refColumn);
                getLoaderSupport().loadResourceString(element, "text", context.getMessageGroup(),
                        menuItem::setText);
                menuItems.put(refColumn, menuItem);
            } else {
                throw new GuiDevelopmentException("Found invalid child element '%s' in gridColumnVisibility element"
                        .formatted(element.getName()), context);
            }
        }
        loadContext.setMenuItems(menuItems);
    }
}
