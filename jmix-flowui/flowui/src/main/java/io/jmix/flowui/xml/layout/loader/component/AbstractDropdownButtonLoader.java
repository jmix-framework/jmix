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

package io.jmix.flowui.xml.layout.loader.component;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.dropdownbutton.AbstractDropdownButton;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.inittask.AssignDropdownButtonActionInitTask;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.loader.LayoutLoader;
import io.jmix.flowui.xml.layout.support.ActionLoaderSupport;
import org.dom4j.Element;

public abstract class AbstractDropdownButtonLoader<T extends AbstractDropdownButton>
        extends AbstractComponentLoader<T> {

    protected ActionLoaderSupport actionLoaderSupport;

    @Override
    public void loadComponent() {
        loadBoolean(element, "openOnHover", resultComponent::setOpenOnHover);

        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadTitle(resultComponent, element, context);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadThemeNames(resultComponent, element);
        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadText(resultComponent, element);
        componentLoader().loadWhiteSpace(resultComponent, element);
        componentLoader().loadIcon(element, resultComponent::setIcon);

        loadContent();
    }

    protected void loadContent() {
        Element items = element.element("items");

        if (items != null) {
            items.elements().forEach(this::loadItem);
        }
    }

    protected void loadItem(Element element) {
        switch (element.getName()) {
            case "actionItem":
                loadActionItem(element);
                break;
            case "componentItem":
                loadComponentItem(element);
                break;
            case "textItem":
                loadTextItem(element);
                break;
            case "separator":
                loadSeparator();
                break;
            default:
                throw new GuiDevelopmentException(
                        String.format("Unexpected dropdownButtonItem with name '%s'", element.getName()),
                        context
                );
        }
    }

    protected void loadActionItem(Element element) {
        String id = getLoaderSupport().loadString(element, "id")
                .orElseThrow(() -> new GuiDevelopmentException("No DropdownActionItem ID provided", context));

        String ref = element.attributeValue("ref");
        Element actionElement = element.element("action");

        if (actionElement != null) {
            Action action = getActionLoaderSupport().loadDeclarativeAction(actionElement);
            resultComponent.addItem(id, action);
        } else if (ref != null) {
            int index = element.getParent().elements().indexOf(element);

            AssignDropdownButtonActionInitTask<?> initTask = new AssignDropdownButtonActionInitTask<>(
                    resultComponent,
                    ref,
                    id,
                    index,
                    getComponentContext().getView()
            );

            getComponentContext().addInitTask(initTask);
        } else {
            throw new GuiDevelopmentException(String.format("No action defined for '%s' actionItem", id), context);
        }
    }

    protected void loadComponentItem(Element element) {
        String id = getLoaderSupport().loadString(element, "id")
                .orElseThrow(() -> new GuiDevelopmentException("No DropdownComponentItem ID provided", context));

        Element subElement = element.elements().stream()
                .findFirst()
                .orElseThrow(() ->
                        new GuiDevelopmentException(
                                String.format("No content defined for '%s' componentItem", id),
                                context)
                );

        LayoutLoader loader = getLayoutLoader();
        ComponentLoader<?> componentLoader = loader.createComponentLoader(subElement);
        componentLoader.initComponent();
        componentLoader.loadComponent();

        Component content = componentLoader.getResultComponent();

        resultComponent.addItem(id, content);
    }

    protected void loadTextItem(Element element) {
        String id = getLoaderSupport().loadString(element, "id")
                .orElseThrow(() -> new GuiDevelopmentException("No DropdownTextItem ID provided", context));

        String text = getLoaderSupport()
                .loadResourceString(element, "text", context.getMessageGroup())
                .orElse(null);

        resultComponent.addItem(id, text);
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
