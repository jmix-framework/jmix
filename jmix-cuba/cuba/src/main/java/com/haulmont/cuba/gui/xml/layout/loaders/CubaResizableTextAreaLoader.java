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

import com.google.common.base.Strings;
import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.components.DatasourceComponent;
import com.haulmont.cuba.gui.components.ResizableTextArea;
import com.haulmont.cuba.gui.components.TextArea;
import com.haulmont.cuba.gui.xml.data.ComponentLoaderHelper;
import com.haulmont.cuba.gui.xml.data.DatasourceLoaderHelper;
import io.jmix.ui.xml.layout.loader.ResizableTextAreaLoader;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CubaResizableTextAreaLoader extends ResizableTextAreaLoader {

    private static final Logger log = LoggerFactory.getLogger(CubaResizableTextAreaLoader.class);

    @Override
    public void createComponent() {
        UiComponents uiComponents = applicationContext.getBean(UiComponents.class);
        if (element.getName().equals(ResizableTextArea.NAME)) {
            resultComponent = uiComponents.create(ResizableTextArea.NAME);
        }

        if (element.getName().equals(TextArea.NAME)) {
            if (isResizable() || hasResizableDirection()) {
                resultComponent = uiComponents.create(ResizableTextArea.NAME);
                log.warn("The 'resizableTextArea' element must be used in order to create a resizable text area " +
                        "instead of 'textArea'");
            } else {
                resultComponent = uiComponents.create(TextArea.NAME);
            }
        }

        loadId(resultComponent, element);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void loadComponent() {
        super.loadComponent();

        if (resultComponent instanceof TextArea) {
            loadInteger(element, "cols",
                    (columns) -> ((TextArea) resultComponent).setColumns(columns));
            ComponentLoaderHelper.loadValidators((TextArea) resultComponent,
                    element,
                    context,
                    getClassManager(),
                    getMessages());
        }

        if (resultComponent instanceof ResizableTextArea) {
            loadBoolean(element, "resizable",
                    (resizable) -> ((ResizableTextArea) resultComponent).setResizable(resizable));

            ComponentLoaderHelper.loadSettingsEnabled((ResizableTextArea) resultComponent, element);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected void loadData(io.jmix.ui.component.TextArea component, Element element) {
        super.loadData(component, element);

        DatasourceLoaderHelper
                .loadDatasourceIfValueSourceNull((DatasourceComponent) resultComponent, element, context,
                        (ComponentLoaderContext) getComponentContext())
                .ifPresent(component::setValueSource);
    }

    protected boolean isResizable() {
        String resizable = element.attributeValue("resizable");
        if (!Strings.isNullOrEmpty(resizable)) {
            return Boolean.parseBoolean(resizable);
        }

        return false;
    }

    protected boolean hasResizableDirection() {
        String resizableDirection = element.attributeValue("resizableDirection");
        if (!Strings.isNullOrEmpty(resizableDirection)) {
            return ResizableTextArea.ResizeDirection.valueOf(resizableDirection) != ResizableTextArea.ResizeDirection.NONE;
        }

        return false;
    }
}
