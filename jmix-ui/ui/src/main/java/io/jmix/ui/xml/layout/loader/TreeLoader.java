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

import com.google.common.base.Strings;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.component.ButtonsPanel;
import io.jmix.ui.component.ContentMode;
import io.jmix.ui.component.Tree;
import io.jmix.ui.component.compatibility.CaptionAdapter;
import io.jmix.ui.component.data.tree.ContainerTreeItems;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.model.ScreenData;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.xml.layout.ComponentLoader;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import javax.annotation.Nullable;

public class TreeLoader extends ActionsHolderLoader<Tree> {

    protected Element buttonsPanelElement;
    protected ComponentLoader buttonsPanelLoader;

    @Override
    public void createComponent() {
        resultComponent = factory.create(Tree.NAME);
        loadId(resultComponent, element);
        createButtonsPanel(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        assignXmlDescriptor(resultComponent, element);
        assignFrame(resultComponent);

        loadVisible(resultComponent, element);

        loadEnable(resultComponent, element);
        loadEditable(resultComponent, element);

        loadHeight(resultComponent, element);
        loadWidth(resultComponent, element);

        loadTabIndex(resultComponent, element);

        loadStyleName(resultComponent, element);
        loadResponsive(resultComponent, element);
        loadCss(resultComponent, element);

        loadActions(resultComponent, element);

        loadButtonsPanel(resultComponent);

        // The multiselect attribute was deleted from "layout.xsd", this code is for backward compatibility.
        String multiselect = element.attributeValue("multiselect");
        if (StringUtils.isNotEmpty(multiselect)) {
            resultComponent.setSelectionMode(Tree.SelectionMode.MULTI);
        }

        loadDataContainer(resultComponent, element);
        loadCaptionProperty(resultComponent, element);

        loadHtmlSanitizerEnabled(resultComponent, element);

        loadIcon(resultComponent, element);
        loadCaption(resultComponent, element);
        loadDescription(resultComponent, element);
        loadContextHelp(resultComponent, element);

        loadSelectionMode(resultComponent, element);
        loadContentMode(resultComponent, element);

        loadRowHeight(resultComponent, element);
    }

    protected void loadDataContainer(Tree resultComponent, Element element) {
        String containerId = element.attributeValue("dataContainer");

        if (containerId != null) {
            FrameOwner frameOwner = getComponentContext().getFrame().getFrameOwner();
            ScreenData screenData = UiControllerUtils.getScreenData(frameOwner);
            InstanceContainer container = screenData.getContainer(containerId);
            CollectionContainer collectionContainer;
            if (container instanceof CollectionContainer) {
                collectionContainer = (CollectionContainer) container;
            } else {
                throw new GuiDevelopmentException("Not a CollectionContainer: " + containerId, context);
            }
            String hierarchyProperty = loadHierarchyProperty(element);

            if (Strings.isNullOrEmpty(hierarchyProperty)) {
                throw new GuiDevelopmentException("Tree doesn't have 'hierarchyProperty' attribute of the 'treechildren' element",
                        context, "Tree ID", element.attributeValue("id"));
            }

            String showOrphansAttr = element.attributeValue("showOrphans");
            boolean showOrphans = showOrphansAttr == null || Boolean.parseBoolean(showOrphansAttr);

            resultComponent.setItems(new ContainerTreeItems(collectionContainer, hierarchyProperty, showOrphans));
        }
    }

    @Nullable
    protected String loadHierarchyProperty(Element element) {
        return element.attributeValue("hierarchyProperty");
    }

    protected void loadCaptionProperty(Tree resultComponent, Element element) {
        loadString(element, "captionProperty")
                .ifPresent(captionProperty ->
                        resultComponent.setItemCaptionProvider(
                                new CaptionAdapter(captionProperty,
                                        applicationContext.getBean(Metadata.class),
                                        applicationContext.getBean(MetadataTools.class))));
    }

    protected void createButtonsPanel(Tree resultComponent, Element element) {
        buttonsPanelElement = element.element("buttonsPanel");
        if (buttonsPanelElement != null) {
            ButtonsPanelLoader loader = (ButtonsPanelLoader) getLayoutLoader().getLoader(buttonsPanelElement, ButtonsPanel.NAME);
            loader.createComponent();
            ButtonsPanel panel = loader.getResultComponent();

            resultComponent.setButtonsPanel(panel);

            buttonsPanelLoader = loader;
        }
    }

    protected void loadButtonsPanel(Tree component) {
        if (buttonsPanelLoader != null) {
            buttonsPanelLoader.loadComponent();
            ButtonsPanel panel = (ButtonsPanel) buttonsPanelLoader.getResultComponent();

            String alwaysVisible = buttonsPanelElement.attributeValue("alwaysVisible");
            if (alwaysVisible != null) {
                panel.setAlwaysVisible(Boolean.parseBoolean(alwaysVisible));
            }
        }
    }

    protected void loadSelectionMode(Tree component, Element element) {
        String selectionMode = element.attributeValue("selectionMode");
        if (StringUtils.isNotEmpty(selectionMode)) {
            component.setSelectionMode(Tree.SelectionMode.valueOf(selectionMode));
        }
    }

    protected void loadRowHeight(Tree component, Element element) {
        String rowHeight = element.attributeValue("rowHeight");
        if (!Strings.isNullOrEmpty(rowHeight)) {
            component.setRowHeight(Double.parseDouble(rowHeight));
        }
    }

    protected void loadContentMode(Tree component, Element element) {
        String contentMode = element.attributeValue("contentMode");
        if (!Strings.isNullOrEmpty(contentMode)) {
            component.setContentMode(ContentMode.valueOf(contentMode));
        }
    }
}
