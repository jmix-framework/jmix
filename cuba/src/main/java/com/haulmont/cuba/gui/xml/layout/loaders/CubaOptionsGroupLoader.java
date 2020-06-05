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

import com.haulmont.cuba.gui.components.OptionsGroup;
import com.haulmont.cuba.gui.xml.data.DatasourceLoaderHelper;
import io.jmix.ui.xml.layout.loader.AbstractOptionsBaseLoader;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

public class CubaOptionsGroupLoader extends AbstractOptionsBaseLoader<OptionsGroup> {

    @Override
    public void createComponent() {
        resultComponent = factory.create(OptionsGroup.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        String multiselect = element.attributeValue("multiselect");
        if (StringUtils.isNotEmpty(multiselect)) {
            resultComponent.setMultiSelect(Boolean.parseBoolean(multiselect));
        }

        loadOrientation(resultComponent, element);
        loadCaptionProperty(resultComponent, element);

        loadOptionsEnum(resultComponent, element);
        loadTabIndex(resultComponent, element);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected void loadData(OptionsGroup component, Element element) {
        super.loadData(component, element);

        DatasourceLoaderHelper
                .loadDatasourceIfValueSourceNull(resultComponent, element, context,
                        (ComponentLoaderContext) getComponentContext())
                .ifPresent(component::setValueSource);

        DatasourceLoaderHelper
                .loadOptionsDatasourceIfOptionsNull(resultComponent, element,
                        (ComponentLoaderContext) getComponentContext())
                .ifPresent(component::setOptions);
    }
}
