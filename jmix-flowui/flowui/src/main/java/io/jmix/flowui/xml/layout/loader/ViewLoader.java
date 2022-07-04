/*
 * Copyright 2019 Haulmont.
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
import io.jmix.flowui.xml.layout.support.ActionLoaderSupport;
import org.dom4j.Element;

public class ViewLoader extends AbstractViewLoader<View<?>> implements ComponentRootLoader<View<?>> {

    public static final String VIEW_ROOT = "view";
    public static final String CONTENT_NAME = "layout";

    protected ActionLoaderSupport actionLoaderSupport;

    public ActionLoaderSupport getActionLoaderSupport() {
        if (actionLoaderSupport == null) {
            actionLoaderSupport = applicationContext.getBean(ActionLoaderSupport.class, context);
        }
        return actionLoaderSupport;
    }

    @Override
    public void createContent() {
        Element content = element.element(CONTENT_NAME);
        if (content == null) {
            throw new GuiDevelopmentException("Required '" + CONTENT_NAME + "' element is not found", context);
        }

        if (resultComponent.getContent() instanceof HasComponents) {
            createSubComponents(((HasComponents) resultComponent.getContent()), content);
        } else {
            // TODO: gg, throw an exception?
        }
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

        // TODO: gg, implement?
//        loadDialogOptions(resultComponent, element);

//        assignXmlDescriptor(resultComponent, element);
//        loadCaption(resultComponent, element);
//        loadDescription(resultComponent, element);
//        loadIcon(resultComponent, element);


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

    /*protected void loadFocusedComponent(Window window, Element element) {
        String focusMode = element.attributeValue("focusMode");
        String componentId = element.attributeValue("focusComponent");
        if (!"NO_FOCUS".equals(focusMode)) {
            window.setFocusComponent(componentId);
        }
    }*/

    /*@Override
    protected Action loadDeclarativeAction(ActionsHolder actionsHolder, Element element) {
        Action action = loadDeclarativeActionByType(actionsHolder, element);
        if (action != null) {
            return action;
        }

        return super.loadDeclarativeAction(actionsHolder, element);
    }*/
}
