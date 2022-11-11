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
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.ThemableLayout;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.view.View;
import io.jmix.flowui.xml.layout.ComponentRootLoader;
import io.jmix.flowui.xml.layout.inittask.FocusComponentInitTask;
import org.dom4j.Element;

public class ViewLoader extends AbstractViewLoader<View<?>> implements ComponentRootLoader<View<?>> {

    public static final String VIEW_ROOT = "view";
    public static final String CONTENT_NAME = "layout";

    @Override
    public void createContent() {
        Element content = element.element(CONTENT_NAME);
        if (content == null) {
            throw new GuiDevelopmentException("Required '" + CONTENT_NAME + "' element is not found", context);
        }

        if (!(resultComponent.getContent() instanceof HasComponents)) {
            throw new GuiDevelopmentException(String.format("%s root layout must be able " +
                            "to contain child components",
                    View.class.getSimpleName()), context);
        }

        createSubComponents(((HasComponents) resultComponent.getContent()), content);
    }

    @Override
    public void loadComponent() {
        Element layoutElement = element.element("layout");
        if (layoutElement == null) {
            throw new GuiDevelopmentException("Required 'layout' element is not found", context);
        }

        getViewLoader().loadData(element);
        getViewLoader().loadActions(element);
        getViewLoader().loadFacets(element);

        loadFocusedComponent(resultComponent, element);

        Component rootComponent = resultComponent.getContent();

        loadThemableAttributes(rootComponent, layoutElement);
        loadFlexibleAttributes(rootComponent, layoutElement);
        loadEnabled(rootComponent, layoutElement);

        if (rootComponent instanceof HasComponents) {
            loadSubComponentsAndExpand(((HasComponents) rootComponent), layoutElement);
        }
    }

    private void loadThemableAttributes(Component rootComponent, Element layoutElement) {
        if (rootComponent instanceof ThemableLayout) {
            componentLoader().loadThemableAttributes(((ThemableLayout) rootComponent), layoutElement);
        }
    }

    private void loadFlexibleAttributes(Component rootComponent, Element layoutElement) {
        if (rootComponent instanceof FlexComponent) {
            componentLoader().loadFlexibleAttributes(((FlexComponent) rootComponent), layoutElement);
        }
    }

    private void loadEnabled(Component rootComponent, Element layoutElement) {
        if (rootComponent instanceof HasEnabled) {
            componentLoader().loadEnabled(((HasEnabled) rootComponent), layoutElement);
        }
    }

    protected void loadFocusedComponent(View<?> view, Element element) {
        String focusComponentId = element.attributeValue("focusComponent");
        getComponentContext().addInitTask(new FocusComponentInitTask(focusComponentId, view));
    }
}
