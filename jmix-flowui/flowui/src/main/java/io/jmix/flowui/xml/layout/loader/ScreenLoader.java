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

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.core.DevelopmentException;
import io.jmix.flowui.component.HasActions;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.model.ScreenData;
import io.jmix.flowui.model.impl.ScreenDataXmlLoader;
import io.jmix.flowui.screen.Screen;
import io.jmix.flowui.screen.ScreenActions;
import io.jmix.flowui.screen.UiControllerUtils;
import io.jmix.flowui.xml.layout.ComponentRootLoader;
import org.dom4j.Element;

public class ScreenLoader extends ContainerLoader<Screen> implements ComponentRootLoader<Screen> {

    public void setResultComponent(Screen screen) {
        this.resultComponent = screen;
    }

    @Override
    public void createComponent() {
        throw new UnsupportedOperationException("Screen cannot be created from XML element");
    }

    @Override
    public void createContent(Element layoutElement) {
        if (layoutElement == null) {
            throw new DevelopmentException("Missing required 'layout' element");
        }

        // TODO: gg, rework
        createSubComponents(resultComponent.getContent(), layoutElement);
    }

    @Override
    public void loadComponent() {
        loadScreenData(resultComponent, element);

//        loadDialogOptions(resultComponent, element);

//        assignXmlDescriptor(resultComponent, element);
//        loadCaption(resultComponent, element);
//        loadDescription(resultComponent, element);
//        loadIcon(resultComponent, element);
        loadScreenActions(resultComponent, element);

        Element layoutElement = element.element("layout");
        if (layoutElement == null) {
            throw new GuiDevelopmentException("Required 'layout' element is not found", context);
        }

        // TODO: gg, add corresponding interfaces to screen itself?
        VerticalLayout screenRootComponents = resultComponent.getContent();
        loadSpacing(screenRootComponents, layoutElement);
//        loadMargin(resultComponent, layoutElement);
        loadWidth(screenRootComponents, layoutElement);
        loadHeight(screenRootComponents, layoutElement);
//        loadStyleName(resultComponent, layoutElement);
//        loadResponsive(resultComponent, layoutElement);
//        loadCss(resultComponent, element);
//        loadVisible(resultComponent, layoutElement);

//        loadMinMaxSizes(resultComponent, layoutElement);

        loadSubComponentsAndExpand(screenRootComponents, layoutElement);
//        setComponentsRatio(resultComponent, layoutElement);

//        loadFocusedComponent(resultComponent, element);

//        loadFacets(resultComponent, element);
    }

    /*protected void loadMinMaxSizes(Window resultComponent, Element layoutElement) {
        String minHeight = layoutElement.attributeValue("minHeight");
        if (isNotEmpty(minHeight)) {
            resultComponent.setMinHeight(minHeight);
        }

        String minWidth = layoutElement.attributeValue("minWidth");
        if (isNotEmpty(minWidth)) {
            resultComponent.setMinWidth(minWidth);
        }

        String maxHeight = layoutElement.attributeValue("maxHeight");
        if (isNotEmpty(maxHeight)) {
            resultComponent.setMaxHeight(maxHeight);
        }

        String maxWidth = layoutElement.attributeValue("maxWidth");
        if (isNotEmpty(maxWidth)) {
            resultComponent.setMaxWidth(maxWidth);
        }
    }*/

    protected void loadScreenData(Screen screen, Element element) {
        Element dataElement = element.element("data");
        if (dataElement != null) {
            ScreenDataXmlLoader screenDataXmlLoader = applicationContext.getBean(ScreenDataXmlLoader.class);
            ScreenData screenData = UiControllerUtils.getScreenData(screen);
            screenDataXmlLoader.load(screenData, dataElement, null);

            ((ComponentLoaderContext) context).setScreenData(screenData);
        }
    }

    protected void loadScreenActions(Screen screen, Element element) {
        Element actionsEl = element.element("actions");
        if (actionsEl == null) {
            return;
        }

        ScreenActions screenActions = UiControllerUtils.getScreenActions(screen);
        for (Element actionEl : actionsEl.elements("action")) {
            screenActions.addAction(loadDeclarativeAction(screenActions, actionEl));
        }

        ((ComponentLoaderContext) context).setScreenActions(screenActions);
    }

    @Override
    protected Action loadDeclarativeAction(HasActions hasActions, Element element) {
        return loadDeclarativeActionByType(element)
                .orElse(super.loadDeclarativeAction(hasActions, element));
    }

    /*protected void loadFocusedComponent(Window window, Element element) {
        String focusMode = element.attributeValue("focusMode");
        String componentId = element.attributeValue("focusComponent");
        if (!"NO_FOCUS".equals(focusMode)) {
            window.setFocusComponent(componentId);
        }
    }*/

    /*protected void loadFacets(Window resultComponent, Element windowElement) {
        Element facetsElement = windowElement.element("facets");
        if (facetsElement != null) {
            List<Element> facetElements = facetsElement.elements();

            for (Element facetElement : facetElements) {
                FacetLoader loader = applicationContext.getBean(FacetLoader.class);
                Facet facet = loader.load(facetElement, getComponentContext());

                resultComponent.addFacet(facet);
            }
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
