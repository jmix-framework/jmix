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
package io.jmix.ui.xml.layout.loader;

import io.jmix.core.DevelopmentException;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.ActionsHolder;
import io.jmix.ui.component.Facet;
import io.jmix.ui.component.Fragment;
import io.jmix.ui.model.ScreenData;
import io.jmix.ui.model.impl.ScreenDataXmlLoader;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.xml.FacetLoader;
import io.jmix.ui.xml.layout.ComponentRootLoader;
import org.dom4j.Element;

import java.util.List;

public class FragmentLoader extends ContainerLoader<Fragment> implements ComponentRootLoader<Fragment> {

    public void setResultComponent(Fragment fragment) {
        this.resultComponent = fragment;
    }

    @Override
    public void createComponent() {
        throw new UnsupportedOperationException("Fragment cannot be created from XML element");
    }

    @Override
    public void createContent(Element layoutElement) {
        if (layoutElement == null) {
            throw new DevelopmentException("Missing required 'layout' element");
        }
        createSubComponents(resultComponent, layoutElement);
    }

    @Override
    public void loadComponent() {
        ComponentContext componentContext = getComponentContext();

        if (componentContext.getParent() == null) {
            throw new IllegalStateException("FragmentLoader is always called within parent ComponentLoaderContext");
        }

        assignXmlDescriptor(resultComponent, element);

        Element layoutElement = element.element("layout");
        if (layoutElement == null) {
            throw new GuiDevelopmentException("Required 'layout' element is not found",
                    componentContext.getFullFrameId());
        }

        loadIcon(resultComponent, layoutElement);
        loadCaption(resultComponent, layoutElement);
        loadDescription(resultComponent, layoutElement);

        loadVisible(resultComponent, layoutElement);
        loadEnable(resultComponent, layoutElement);
        loadActions(resultComponent, element);

        loadSpacing(resultComponent, layoutElement);
        loadMargin(resultComponent, layoutElement);
        loadWidth(resultComponent, layoutElement);
        loadHeight(resultComponent, layoutElement);
        loadStyleName(resultComponent, layoutElement);
        loadResponsive(resultComponent, layoutElement);
        loadCss(resultComponent, element);

        loadDataElement(element);

        loadSubComponentsAndExpand(resultComponent, layoutElement);
        setComponentsRatio(resultComponent, layoutElement);

        loadFacets(resultComponent, element);
    }

    protected void loadDataElement(Element element) {
        Element dataEl = element.element("data");
        if (dataEl == null) {
            return;
        }

        ScreenData hostScreenData = null;
        ComponentContext parent = getComponentContext().getParent();
        while (hostScreenData == null && parent != null) {
            hostScreenData = parent.getScreenData();
            parent = parent.getParent();
        }
        ScreenDataXmlLoader screenDataXmlLoader = applicationContext.getBean(ScreenDataXmlLoader.class);
        ScreenData screenData = UiControllerUtils.getScreenData(resultComponent.getFrameOwner());
        screenDataXmlLoader.load(screenData, dataEl, hostScreenData);
        ((ComponentLoaderContext) context).setScreenData(screenData);
    }

    protected void loadFacets(Fragment resultComponent, Element fragmentElement) {
        Element facetsElement = fragmentElement.element("facets");
        if (facetsElement != null) {
            List<Element> facetElements = facetsElement.elements();

            for (Element facetElement : facetElements) {
                FacetLoader loader = applicationContext.getBean(FacetLoader.class);
                Facet facet = loader.load(facetElement, getComponentContext());

                resultComponent.addFacet(facet);
            }
        }
    }

    @Override
    protected Action loadDeclarativeAction(ActionsHolder actionsHolder, Element element) {
        Action action = loadDeclarativeActionByType(actionsHolder, element);
        if (action != null) {
            return action;
        }

        return super.loadDeclarativeAction(actionsHolder, element);
    }
}
