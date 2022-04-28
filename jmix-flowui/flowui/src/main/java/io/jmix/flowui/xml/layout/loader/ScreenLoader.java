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
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.model.ScreenData;
import io.jmix.flowui.model.impl.ScreenDataXmlLoader;
import io.jmix.flowui.screen.Screen;
import io.jmix.flowui.screen.ScreenActions;
import io.jmix.flowui.screen.UiControllerUtils;
import io.jmix.flowui.xml.layout.ComponentRootLoader;
import io.jmix.flowui.xml.layout.support.ActionLoaderSupport;
import org.dom4j.Element;

public class ScreenLoader extends AbstractContainerLoader<Screen> implements ComponentRootLoader<Screen> {


    protected ActionLoaderSupport actionLoaderSupport;

    public ActionLoaderSupport getActionLoaderSupport() {
        if (actionLoaderSupport == null) {
            actionLoaderSupport = applicationContext.getBean(ActionLoaderSupport.class, context);
        }
        return actionLoaderSupport;
    }

//    @Autowired
//    ScreenLoader(ComponentLoaderSupport componentLoaderSupport, ActionLoaderSupport actionLoaderSupport) {
//        this.componentLoaderSupport = componentLoaderSupport;
//        this.actionLoaderSupport = actionLoaderSupport;
//    }

    public void setResultComponent(Screen screen) {
        this.resultComponent = screen;
    }

    @Override
    protected Screen createComponent() {
        throw new UnsupportedOperationException("Screen cannot be created from XML element");
    }

    @Override
    public void initComponent() {
        throw new UnsupportedOperationException("Screen cannot be initialized from XML element");
    }

    @Override
    public void createContent(Element layoutElement) {
        Preconditions.checkNotNullArgument(layoutElement);
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
        loadThemableAttributes(screenRootComponents, layoutElement);
//        loadMargin(resultComponent, layoutElement);
        componentLoader().loadSizeAttributes(screenRootComponents, layoutElement);
        loadFlexibleAttributes(screenRootComponents, layoutElement);
//        getComponentLoaderSupport().loadWidth(screenRootComponents, layoutElement);
//        getComponentLoaderSupport().loadHeight(screenRootComponents, layoutElement);
//        loadStyleName(resultComponent, layoutElement);
//        loadResponsive(resultComponent, layoutElement);
//        loadCss(resultComponent, element);
//        loadVisible(resultComponent, layoutElement);


        loadSubComponentsAndExpand(screenRootComponents, layoutElement);
//        setComponentsRatio(resultComponent, layoutElement);

//        loadFocusedComponent(resultComponent, element);

//        loadFacets(resultComponent, element);
    }

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
            screenActions.addAction(loadDeclarativeAction(actionEl));
        }

        ((ComponentLoaderContext) context).setScreenActions(screenActions);
    }

    protected Action loadDeclarativeAction(Element element) {
        return getActionLoaderSupport().loadDeclarativeActionByType(element)
                .orElse(getActionLoaderSupport().loadDeclarativeAction(element));
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
