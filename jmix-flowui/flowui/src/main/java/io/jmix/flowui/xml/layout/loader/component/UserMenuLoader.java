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
import io.jmix.core.ClassManager;
import io.jmix.flowui.component.usermenu.UserMenu;
import io.jmix.flowui.component.usermenu.ViewUserMenuItem;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.usermenu.ActionUserMenuItem;
import io.jmix.flowui.kit.component.usermenu.ComponentUserMenuItem;
import io.jmix.flowui.kit.component.usermenu.TextUserMenuItem;
import io.jmix.flowui.kit.component.usermenu.UserMenuItem;
import io.jmix.flowui.view.View;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.inittask.AssignUserMenuItemActionInitTask;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.loader.LayoutLoader;
import io.jmix.flowui.xml.layout.support.ActionLoaderSupport;
import org.dom4j.Element;

public class UserMenuLoader extends AbstractComponentLoader<UserMenu> {

    protected ActionLoaderSupport actionLoaderSupport;

    @Override
    protected UserMenu createComponent() {
        return factory.create(UserMenu.class);
    }

    @Override
    public void loadComponent() {
        loadBoolean(element, "openOnHover", resultComponent::setOpenOnHover);

        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadTitle(resultComponent, element, context);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadOverlayClass(resultComponent, element);
        componentLoader().loadFocusableAttributes(resultComponent, element);
        componentLoader().loadThemeNames(resultComponent, element);

        loadContent();
    }

    protected void loadContent() {
        Element items = element.element("items");

        if (items != null) {
            items.elements().forEach(this::loadItem);
        }
    }

    protected void loadItem(Element element) {
        // TODO: gg, plugable loaders
        switch (element.getName()) {
            case "textItem":
                loadTextItem(element);
                break;
            case "actionItem":
                loadActionItem(element);
                break;
            case "componentItem":
                loadComponentItem(element);
                break;
            case "viewItem":
                loadViewItem(element);
            case "separator":
                loadSeparator();
                break;
            default:
                throw new GuiDevelopmentException(
                        "Unexpected %s item with name '%s'"
                                .formatted(resultComponent.getClass().getSimpleName(), element.getName()),
                        context
                );
        }
    }

    protected void loadTextItem(Element element) {
        String id = loadItemId(element, TextUserMenuItem.class);

        String text = getLoaderSupport()
                .loadResourceString(element, "text", context.getMessageGroup())
                .orElseThrow(() ->
                        new GuiDevelopmentException("No 'text' provided for %s(%s)"
                                .formatted(TextUserMenuItem.class.getSimpleName(), id), context));

        TextUserMenuItem item = resultComponent.addTextItem(id, text);
        componentLoader().loadIcon(element, item::setIcon);
        loadItem(element, item);
    }

    protected void loadActionItem(Element element) {
        String id = loadItemId(element, ActionUserMenuItem.class);

        String ref = element.attributeValue("ref");
        Element actionElement = element.element("action");

        if (actionElement != null) {
            Action action = getActionLoaderSupport().loadDeclarativeActionByType(actionElement)
                    .orElseGet(() -> getActionLoaderSupport().loadDeclarativeAction(actionElement));
            ActionUserMenuItem item = resultComponent.addActionItem(id, action);
            loadItem(element, item);
        } else if (ref != null) {
            int index = element.getParent().elements().indexOf(element);

            AssignUserMenuItemActionInitTask<?> initTask = new AssignUserMenuItemActionInitTask<>(
                    resultComponent,
                    ref,
                    id,
                    index,
                    item -> loadItem(element, item)
            );

            getContext().addInitTask(initTask);
        } else {
            throw new GuiDevelopmentException("No 'action' defined for %s(%s)"
                    .formatted(ActionUserMenuItem.class.getSimpleName(), id), context);
        }
    }

    protected void loadComponentItem(Element element) {
        String id = loadItemId(element, ComponentUserMenuItem.class);

        Element subElement = element.elements().stream()
                .findFirst()
                .orElseThrow(() ->
                        new GuiDevelopmentException("No 'content' defined for %s(%s)"
                                .formatted(ComponentUserMenuItem.class.getSimpleName(), id), context)
                );

        LayoutLoader loader = getLayoutLoader();
        ComponentLoader<?> componentLoader = loader.createComponentLoader(subElement);
        componentLoader.initComponent();
        componentLoader.loadComponent();

        Component content = componentLoader.getResultComponent();

        ComponentUserMenuItem item = resultComponent.addComponentItem(id, content);
        loadItem(element, item);
    }

    protected void loadViewItem(Element element) {
        String id = loadItemId(element, ViewUserMenuItem.class);

        String text = loadResourceString(element, "text", context.getMessageGroup())
                .orElseThrow(() ->
                        new GuiDevelopmentException("No 'text' provided for %s(%s)"
                                .formatted(TextUserMenuItem.class.getSimpleName(), id), context));

        ViewUserMenuItem item = loadString(element, "viewId")
                .map(viewId -> resultComponent.addViewItem(id, viewId, text))
                .orElse(null);

        if (item == null) {
            Class<?> viewClass = loadString(element, "viewClass")
                    .map(aClass -> applicationContext.getBean(ClassManager.class).loadClass(aClass))
                    .orElseThrow(() -> new GuiDevelopmentException("Neither 'viewId' nor 'viewClass' provided for %s(%s)"
                            .formatted(ViewUserMenuItem.class.getSimpleName(), id), context));

            if (!View.class.isAssignableFrom(viewClass)) {
                throw new GuiDevelopmentException("Class '%s' is not a %s"
                        .formatted(viewClass.getSimpleName(), View.class.getSimpleName()), context);
            }

            //noinspection unchecked,rawtypes
            item = resultComponent.addViewItem(id, (Class) viewClass, text);
        }

        componentLoader().loadIcon(element, item::setIcon);
        loadItem(element, item);
    }

    protected String loadItemId(Element element, Class<?> itemClass) {
        return loadString(element, "id")
                .orElseThrow(() ->
                        new GuiDevelopmentException("No %s 'id' provided"
                                .formatted(itemClass.getSimpleName()), context));
    }

    protected void loadItem(Element element, UserMenuItem item) {
        loadBoolean(element, "enabled", item::setEnabled);
        loadBoolean(element, "visible", item::setVisible);
        loadBoolean(element, "checkable", item::setCheckable);
        loadBoolean(element, "checked", item::setChecked);

        componentLoader().loadThemeNames(item, element);
    }

    protected void loadSeparator() {
        resultComponent.addSeparator();
    }

    protected ActionLoaderSupport getActionLoaderSupport() {
        if (actionLoaderSupport == null) {
            actionLoaderSupport = applicationContext.getBean(ActionLoaderSupport.class, context);
        }
        return actionLoaderSupport;
    }
}
