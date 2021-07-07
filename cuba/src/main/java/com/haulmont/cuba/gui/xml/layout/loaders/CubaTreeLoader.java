/*
 * Copyright 2020 Haulmont.
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
import com.haulmont.cuba.gui.components.CaptionMode;
import com.haulmont.cuba.gui.components.HasItemCaptionMode;
import com.haulmont.cuba.gui.components.Tree;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.HierarchicalDatasource;
import com.haulmont.cuba.gui.xml.data.ComponentLoaderHelper;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.ActionsHolder;
import io.jmix.ui.xml.layout.loader.TreeLoader;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import javax.annotation.Nullable;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.trimToNull;

public class CubaTreeLoader extends TreeLoader {

    @Override
    public void createComponent() {
        UiComponents uiComponents = applicationContext.getBean(UiComponents.class);
        resultComponent = uiComponents.create(Tree.NAME);
        loadId(resultComponent, element);
        createButtonsPanel(resultComponent, element);
    }

    @Override
    protected void loadCaptionProperty(io.jmix.ui.component.Tree resultComponent, Element element) {
        Element itemsElem = getTreeChildrenElement(element);

        String captionProperty = element.attributeValue("captionProperty");
        if (captionProperty == null && itemsElem != null) {
            captionProperty = itemsElem.attributeValue("captionProperty");
        }

        if (!StringUtils.isEmpty(captionProperty)) {
            ((HasItemCaptionMode) resultComponent).setCaptionProperty(captionProperty);
            ((HasItemCaptionMode) resultComponent).setCaptionMode(CaptionMode.PROPERTY);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void loadDataContainer(io.jmix.ui.component.Tree resultComponent, Element element) {
        super.loadDataContainer(resultComponent, element);
        if (resultComponent.getItems() != null) {
            // dataContainer has been loaded
            return;
        }

        // load datasource instead
        Element itemsElem = getTreeChildrenElement(element);
        if (itemsElem != null) {
            String datasource = itemsElem.attributeValue("datasource");
            if (!StringUtils.isBlank(datasource)) {

                ComponentLoaderContext loaderContext = (ComponentLoaderContext) getComponentContext();

                Datasource ds = loaderContext.getDsContext().get(datasource);
                if (!(ds instanceof HierarchicalDatasource)) {
                    throw new GuiDevelopmentException("Not a HierarchicalDatasource: " + datasource, context);
                }

                ((Tree) resultComponent).setDatasource((HierarchicalDatasource) ds);
            }
        }
    }

    @Nullable
    protected Element getTreeChildrenElement(Element element) {
        return element.element("treechildren");
    }

    @Nullable
    @Override
    protected String loadHierarchyProperty(Element element) {
        String hierarchyProperty = super.loadHierarchyProperty(element);
        if (hierarchyProperty == null) {
            Element itemsElem = getTreeChildrenElement(element);
            if (itemsElem != null) {
                hierarchyProperty = itemsElem.attributeValue("hierarchyProperty");
            }
        }

        return hierarchyProperty;
    }

    @Override
    protected Action loadDeclarativeAction(ActionsHolder actionsHolder, Element element) {
        Optional<Action> actionOpt = ComponentLoaderHelper.loadInvokeAction(
                context,
                actionsHolder,
                element,
                loadActionId(element),
                loadResourceString(element.attributeValue("caption")),
                loadResourceString(element.attributeValue("description")),
                getIconPath(element.attributeValue("icon")),
                loadShortcut(trimToNull(element.attributeValue("shortcut"))));

        if (actionOpt.isPresent()) {
            return actionOpt.get();
        }

        actionOpt = ComponentLoaderHelper.loadLegacyListAction(
                context,
                actionsHolder,
                element,
                loadResourceString(element.attributeValue("caption")),
                loadResourceString(element.attributeValue("description")),
                getIconPath(element.attributeValue("icon")),
                loadShortcut(trimToNull(element.attributeValue("shortcut"))));

        return actionOpt.orElseGet(() ->
                super.loadDeclarativeAction(actionsHolder, element));
    }
}
