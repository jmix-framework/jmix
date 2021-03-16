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
package com.haulmont.cuba.gui.xml.layout.loaders;

import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.xml.data.ComponentLoaderHelper;
import com.haulmont.cuba.gui.xml.data.DatasourceLoaderHelper;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.component.HasFilterMode;
import io.jmix.ui.component.data.options.ContainerOptions;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.model.ScreenData;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.xml.layout.loader.AbstractFieldLoader;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

public class TokenListLoader extends AbstractFieldLoader<TokenList> {
    @Override
    public void createComponent() {
        UiComponents uiComponents = applicationContext.getBean(UiComponents.class);
        resultComponent = uiComponents.create(TokenList.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        assignXmlDescriptor(resultComponent, element);
        assignFrame(resultComponent);

        loadData(resultComponent, element);

        loadVisible(resultComponent, element);
        loadEditable(resultComponent, element);
        loadEnable(resultComponent, element);
        loadRequired(resultComponent, element);

        loadStyleName(resultComponent, element);
        loadCss(resultComponent, element);

        loadCaption(resultComponent, element);
        loadIcon(resultComponent, element);
        loadDescription(resultComponent, element);
        loadContextHelp(resultComponent, element);

        loadValidation(resultComponent, element);

        loadHeight(resultComponent, element);
        loadWidth(resultComponent, element);

        loadTabIndex(resultComponent, element);

        loadCaptionProperty(resultComponent, element);
        loadPosition(resultComponent, element);

        loadInline(resultComponent, element);

        loadLookup(resultComponent, element);

        loadButton(resultComponent, element);
        loadAddButton(resultComponent, element);

        loadSimple(resultComponent, element);

        loadClearEnabled(resultComponent, element);
        loadClearButton(resultComponent, element);

        loadRefreshOptionsOnLookupClose(resultComponent, element);
        loadResponsive(resultComponent, element);

        ComponentLoaderHelper.loadValidators(resultComponent, element, context, getClassManager(), getMessages());
    }

    @Override
    protected void loadData(TokenList component, Element element) {
        super.loadData(component, element);

        DatasourceLoaderHelper
                .loadDatasourceIfValueSourceNull(resultComponent, element, context,
                        (ComponentLoaderContext) getComponentContext())
                .ifPresent(component::setValueSource);
    }

    protected void loadRefreshOptionsOnLookupClose(TokenList component, Element element) {
        String refreshOptionsOnLookupClose = element.attributeValue("refreshOptionsOnLookupClose");
        if (refreshOptionsOnLookupClose != null) {
            component.setRefreshOptionsOnLookupClose(Boolean.parseBoolean(refreshOptionsOnLookupClose));
        }
    }

    protected void loadClearEnabled(TokenList component, Element element) {
        String clearEnabled = element.attributeValue("clearEnabled");
        if (StringUtils.isNotEmpty(clearEnabled)) {
            component.setClearEnabled(Boolean.parseBoolean(clearEnabled));
        }
    }

    protected void loadClearButton(TokenList component, Element element) {
        Element buttonElement = element.element("clearButton");
        if (buttonElement != null) {
            String caption = buttonElement.attributeValue("caption");
            if (caption != null) {
                if (!StringUtils.isEmpty(caption)) {
                    caption = loadResourceString(caption);
                }
                component.setClearButtonCaption(caption);
            }

            String icon = buttonElement.attributeValue("icon");
            if (!StringUtils.isEmpty(icon)) {
                component.setClearButtonIcon(getIconPath(icon));
            }
        }
    }

    protected void loadSimple(TokenList component, Element element) {
        String simple = element.attributeValue("simple");
        if (StringUtils.isNotEmpty(simple)) {
            component.setSimple(Boolean.parseBoolean(simple));
        } else {
            component.setSimple(false);
        }
    }

    protected void loadAddButton(TokenList component, Element element) {
        Element buttonElement = element.element("addButton");
        if (buttonElement != null) {
            String caption = buttonElement.attributeValue("caption");
            if (caption != null) {
                if (StringUtils.isNotEmpty(caption)) {
                    caption = loadResourceString(caption);
                }
                component.setAddButtonCaption(caption);
            }

            String icon = buttonElement.attributeValue("icon");
            if (StringUtils.isNotEmpty(icon)) {
                component.setAddButtonIcon(getIconPath(icon));
            }
        }
    }

    @Deprecated
    protected void loadButton(TokenList component, Element element) {
        Element buttonElement = element.element("button");
        if (buttonElement != null) {
            String caption = buttonElement.attributeValue("caption");
            if (caption != null) {
                if (StringUtils.isNotEmpty(caption)) {
                    caption = loadResourceString(caption);
                }
                component.setAddButtonCaption(caption);
            }

            String icon = buttonElement.attributeValue("icon");
            if (!StringUtils.isEmpty(icon)) {
                component.setAddButtonIcon(getIconPath(icon));
            }
        }
    }

    protected void loadLookup(TokenList component, Element element) {
        Element lookupElement = element.element("lookup");
        if (lookupElement == null) {
            throw new GuiDevelopmentException("'tokenList' must contain 'lookup' element", context,
                    "TokenList ID", element.attributeValue("id"));
        }

        loadOptionsContainer(component, lookupElement);

        loadLookupCaptionProperty(component, lookupElement);

        String lookup = lookupElement.attributeValue("lookup");
        if (StringUtils.isNotEmpty(lookup)) {
            component.setLookup(Boolean.parseBoolean(lookup));
            if (component.isLookup()) {
                String lookupScreen = lookupElement.attributeValue("lookupScreen");
                if (StringUtils.isNotEmpty(lookupScreen)) {
                    component.setLookupScreen(lookupScreen);
                }

                loadLookupOpenMode(component, lookupElement);
            }
        }

        String multiSelect = lookupElement.attributeValue("multiselect");
        if (StringUtils.isNotEmpty(multiSelect)) {
            component.setMultiSelect(Boolean.parseBoolean(multiSelect));
        }

        String inputPrompt = lookupElement.attributeValue("inputPrompt");
        if (StringUtils.isNotEmpty(inputPrompt)) {
            component.setLookupInputPrompt(loadResourceString(inputPrompt));
        }

        loadFilterMode(component, lookupElement);
    }

    protected void loadLookupOpenMode(TokenList tokenList, Element lookupElement) {
        String openType = lookupElement.attributeValue("openType");
        if (StringUtils.isNotEmpty(openType)) {
            ((TokenList) tokenList).setLookupOpenMode(WindowManager.OpenType.valueOf(openType));
        }
    }

    protected void loadLookupCaptionProperty(TokenList component, Element lookupElement) {
        String optionsCaptionProperty = lookupElement.attributeValue("captionProperty");
        if (!StringUtils.isEmpty(optionsCaptionProperty)) {
            (component).setOptionsCaptionMode(CaptionMode.PROPERTY);
            (component).setOptionsCaptionProperty(optionsCaptionProperty);
        }
    }

    protected void loadInline(TokenList component, Element element) {
        String inline = element.attributeValue("inline");
        if (StringUtils.isNotEmpty(inline)) {
            component.setInline(Boolean.parseBoolean(inline));
        }
    }

    protected void loadPosition(TokenList component, Element element) {
        String position = element.attributeValue("position");
        if (StringUtils.isNotEmpty(position)) {
            component.setPosition(TokenList.Position.valueOf(position));
        }
    }

    protected void loadCaptionProperty(TokenList component, Element element) {
        ComponentLoaderHelper.loadCaptionProperty(component, element);
    }

    protected void loadFilterMode(TokenList component, Element element) {
        final String filterMode = element.attributeValue("filterMode");
        if (!StringUtils.isEmpty(filterMode)) {
            component.setFilterMode(HasFilterMode.FilterMode.valueOf(filterMode));
        }
    }

    protected void loadOptionsContainer(TokenList component, Element element) {
        String containerId = element.attributeValue("optionsContainer");
        if (containerId != null) {
            FrameOwner frameOwner = getComponentContext().getFrame().getFrameOwner();
            ScreenData screenData = UiControllerUtils.getScreenData(frameOwner);
            InstanceContainer container = screenData.getContainer(containerId);
            if (!(container instanceof CollectionContainer)) {
                throw new GuiDevelopmentException("Not a CollectionContainer: " + containerId, context);
            }
            //noinspection unchecked
            component.setOptions(new ContainerOptions((CollectionContainer) container));
        }

        if (component.getOptions() == null) {
            DatasourceLoaderHelper
                    .loadOptionsDatasource(element, (ComponentLoaderContext) getComponentContext())
                    .ifPresent(component::setOptions);
        }
    }
}
