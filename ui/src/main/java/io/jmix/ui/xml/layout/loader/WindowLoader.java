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
import io.jmix.ui.action.Action;
import io.jmix.ui.component.*;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.model.ScreenData;
import io.jmix.ui.model.impl.ScreenDataXmlLoader;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.xml.FacetLoader;
import io.jmix.ui.xml.layout.ComponentRootLoader;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@ParametersAreNonnullByDefault
public class WindowLoader extends ContainerLoader<Window> implements ComponentRootLoader<Window> {

    public void setResultComponent(Window window) {
        this.resultComponent = window;
    }

    @Override
    public void createComponent() {
        throw new UnsupportedOperationException("Window cannot be created from XML element");
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
        loadScreenData(resultComponent, element);

        loadDialogOptions(resultComponent, element);

        assignXmlDescriptor(resultComponent, element);
        loadCaption(resultComponent, element);
        loadDescription(resultComponent, element);
        loadIcon(resultComponent, element);
        loadActions(resultComponent, element);

        Element layoutElement = element.element("layout");
        if (layoutElement == null) {
            throw new GuiDevelopmentException("Required 'layout' element is not found", context);
        }

        loadSpacing(resultComponent, layoutElement);
        loadMargin(resultComponent, layoutElement);
        loadWidth(resultComponent, layoutElement);
        loadHeight(resultComponent, layoutElement);
        loadStyleName(resultComponent, layoutElement);
        loadResponsive(resultComponent, layoutElement);
        loadCss(resultComponent, element);
        loadVisible(resultComponent, layoutElement);

        loadMinMaxSizes(resultComponent, layoutElement);

        loadSubComponentsAndExpand(resultComponent, layoutElement);
        setComponentsRatio(resultComponent, layoutElement);

        loadFocusedComponent(resultComponent, element);

        loadFacets(resultComponent, element);
    }

    protected void loadMinMaxSizes(Window resultComponent, Element layoutElement) {
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
    }

    protected void loadScreenData(Window window, Element element) {
        Element dataEl = element.element("data");
        if (dataEl != null) {
            ScreenDataXmlLoader screenDataXmlLoader = applicationContext.getBean(ScreenDataXmlLoader.class);
            ScreenData screenData = UiControllerUtils.getScreenData(window.getFrameOwner());
            screenDataXmlLoader.load(screenData, dataEl, null);

            ((ComponentLoaderContext) context).setScreenData(screenData);
        }
    }

    protected void loadDialogOptions(Window resultComponent, Element element) {
        Element dialogModeElement = element.element("dialogMode");
        if (dialogModeElement != null
                && resultComponent instanceof DialogWindow) {
            // dialog mode applied only if opened as dialog
            DialogWindow dialog = (DialogWindow) resultComponent;

            String xmlWidthValue = dialogModeElement.attributeValue("width");
            if (StringUtils.isNotEmpty(xmlWidthValue)) {
                String themeWidthValue = loadThemeString(xmlWidthValue);
                if ("auto".equalsIgnoreCase(themeWidthValue)) {
                    dialog.setDialogWidth(Component.AUTO_SIZE);
                } else {
                    dialog.setDialogWidth(themeWidthValue);
                }
            }

            String xmlHeightValue = dialogModeElement.attributeValue("height");
            if (StringUtils.isNotEmpty(xmlHeightValue)) {
                String themeHeightValue = loadThemeString(xmlHeightValue);
                if ("auto".equalsIgnoreCase(themeHeightValue)) {
                    dialog.setDialogHeight(Component.AUTO_SIZE);
                } else {
                    dialog.setDialogHeight(themeHeightValue);
                }
            }

            String closeable = dialogModeElement.attributeValue("closeable");
            if (isNotEmpty(closeable)) {
                dialog.setCloseable(parseBoolean(closeable));
            }

            String resizable = dialogModeElement.attributeValue("resizable");
            if (isNotEmpty(resizable)) {
                dialog.setResizable(parseBoolean(resizable));
            }

            String modal = dialogModeElement.attributeValue("modal");
            if (isNotEmpty(modal)) {
                dialog.setModal(parseBoolean(modal));
            }

            String closeOnClickOutside = dialogModeElement.attributeValue("closeOnClickOutside");
            if (isNotEmpty(closeOnClickOutside)) {
                dialog.setCloseOnClickOutside(parseBoolean(closeOnClickOutside));
            }

            loadEnum(dialogModeElement, WindowMode.class, "windowMode", dialog::setWindowMode);

            String positionX = dialogModeElement.attributeValue("positionX");
            if (isNotEmpty(positionX)) {
                dialog.setPositionX(parseInt(positionX));
            }

            String positionY = dialogModeElement.attributeValue("positionY");
            if (isNotEmpty(positionY)) {
                dialog.setPositionY(parseInt(positionY));
            }
        }
    }

    protected void loadFocusedComponent(Window window, Element element) {
        String focusMode = element.attributeValue("focusMode");
        String componentId = element.attributeValue("focusComponent");
        if (!"NO_FOCUS".equals(focusMode)) {
            window.setFocusComponent(componentId);
        }
    }

    protected void loadFacets(Window resultComponent, Element windowElement) {
        Element facetsElement = windowElement.element("facets");
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
