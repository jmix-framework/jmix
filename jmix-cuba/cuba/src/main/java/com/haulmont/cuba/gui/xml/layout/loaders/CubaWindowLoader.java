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

import io.jmix.ui.action.Action;
import io.jmix.ui.component.ActionsHolder;
import io.jmix.ui.component.DialogWindow;
import io.jmix.ui.component.Facet;
import io.jmix.ui.component.Window;
import io.jmix.ui.component.WindowMode;
import io.jmix.ui.xml.FacetLoader;
import io.jmix.ui.xml.layout.loader.WindowLoader;
import org.dom4j.Element;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

import static com.haulmont.cuba.gui.xml.data.ComponentLoaderHelper.loadInvokeAction;
import static java.lang.Boolean.parseBoolean;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.trimToNull;

@ParametersAreNonnullByDefault
public class CubaWindowLoader extends WindowLoader {

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadTimers(resultComponent, element);
    }

    @Deprecated
    protected void loadTimers(Window resultComponent, Element windowElement) {
        Element timersElement = windowElement.element("timers");
        if (timersElement != null) {
            List<Element> facetElements = timersElement.elements("timer");

            for (Element facetElement : facetElements) {
                FacetLoader loader = applicationContext.getBean(FacetLoader.class);
                Facet facet = loader.load(facetElement, getComponentContext());

                resultComponent.addFacet(facet);
            }
        }
    }

    @Override
    protected Action loadDeclarativeAction(ActionsHolder actionsHolder, Element element) {
        return loadInvokeAction(
                context,
                actionsHolder,
                element,
                loadActionId(element),
                loadResourceString(element.attributeValue("caption")),
                loadResourceString(element.attributeValue("description")),
                getIconPath(element.attributeValue("icon")),
                loadShortcut(trimToNull(element.attributeValue("shortcut"))))
                .orElseGet(() ->
                        super.loadDeclarativeAction(actionsHolder, element));
    }

    @Override
    protected void loadDialogOptions(Window resultComponent, Element element) {
        super.loadDialogOptions(resultComponent, element);

        Element dialogModeElement = element.element("dialogMode");
        if (dialogModeElement != null
                && resultComponent instanceof DialogWindow) {
            // dialog mode applied only if opened as dialog
            DialogWindow dialog = (DialogWindow) resultComponent;

            String maximized = dialogModeElement.attributeValue("maximized");
            if (isNotEmpty(maximized) && parseBoolean(maximized)) {
                dialog.setWindowMode(WindowMode.MAXIMIZED);
            }
        }
    }
}
