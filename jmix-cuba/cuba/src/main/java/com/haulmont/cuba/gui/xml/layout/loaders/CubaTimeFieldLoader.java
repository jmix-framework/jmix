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
import com.haulmont.cuba.gui.components.DatasourceComponent;
import com.haulmont.cuba.gui.components.TimeField;
import com.haulmont.cuba.gui.xml.data.DatasourceLoaderHelper;
import io.jmix.ui.xml.layout.loader.TimeFieldLoader;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

public class CubaTimeFieldLoader extends TimeFieldLoader {

    @Override
    public void createComponent() {
        UiComponents uiComponents = applicationContext.getBean(UiComponents.class);
        resultComponent = uiComponents.create(TimeField.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadShowSeconds((TimeField) resultComponent, element);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected void loadData(io.jmix.ui.component.TimeField component, Element element) {
        super.loadData(component, element);

        DatasourceLoaderHelper
                .loadDatasourceIfValueSourceNull((DatasourceComponent) resultComponent, element, context,
                        (ComponentLoaderContext) getComponentContext())
                .ifPresent(component::setValueSource);
    }

    protected void loadShowSeconds(TimeField resultComponent, Element element) {
        String showSeconds = element.attributeValue("showSeconds");
        if (StringUtils.isNotEmpty(showSeconds)) {
            resultComponent.setShowSeconds(Boolean.parseBoolean(showSeconds));
        }
    }
}
