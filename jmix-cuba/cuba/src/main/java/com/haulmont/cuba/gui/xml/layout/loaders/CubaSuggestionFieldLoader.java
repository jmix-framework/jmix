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
import com.haulmont.cuba.gui.components.Field;
import com.haulmont.cuba.gui.components.HasCaptionMode;
import com.haulmont.cuba.gui.xml.data.ComponentLoaderHelper;
import com.haulmont.cuba.gui.xml.data.DatasourceLoaderHelper;
import io.jmix.ui.component.SuggestionField;
import io.jmix.ui.xml.layout.loader.SuggestionFieldLoader;
import org.dom4j.Element;

public class CubaSuggestionFieldLoader extends SuggestionFieldLoader {

    @Override
    public void createComponent() {
        UiComponents uiComponents = applicationContext.getBean(UiComponents.class);
        resultComponent = uiComponents.create(com.haulmont.cuba.gui.components.SuggestionField.NAME);
        loadId(resultComponent, element);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void loadComponent() {
        super.loadComponent();

        loadQuery(resultComponent, element);
        ComponentLoaderHelper.loadValidators((Field) resultComponent, element, context, getClassManager(), getMessages());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected void loadData(io.jmix.ui.component.SuggestionField component, Element element) {
        super.loadData(component, element);

        DatasourceLoaderHelper
                .loadDatasourceIfValueSourceNull((DatasourceComponent) resultComponent, element, context,
                        (ComponentLoaderContext) getComponentContext())
                .ifPresent(component::setValueSource);
    }

    @Override
    protected void loadCaptionProperty(SuggestionField suggestionField, Element element) {
        ComponentLoaderHelper.loadCaptionProperty((HasCaptionMode) suggestionField, element);
    }

    @Override
    protected String loadFetchPlan(Element queryElement) {
        return queryElement.attributeValue("view");
    }
}
