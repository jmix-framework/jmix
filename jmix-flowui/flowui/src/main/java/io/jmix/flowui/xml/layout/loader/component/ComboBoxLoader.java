/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.xml.layout.loader.component;

import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.xml.layout.support.DataLoaderSupport;

public class ComboBoxLoader extends AbstractComboBoxLoader<JmixComboBox<?>> {

    protected DataLoaderSupport dataLoaderSupport;

    @Override
    protected JmixComboBox<?> createComponent() {
        return factory.create(JmixComboBox.class);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        getDataLoaderSupport().loadItems(resultComponent, element);
        getDataLoaderSupport().loadData(resultComponent, element);

        //These properties are loaded after the data provider is loaded,
        // because setting the data provider resets the value of the readOnly attribute to default.
        componentLoader().loadValueAndElementAttributes(resultComponent, element);
        componentLoader().loadTitle(resultComponent, element, context);
        componentLoader().loadRequired(resultComponent, element, context);
    }

    protected DataLoaderSupport getDataLoaderSupport() {
        if (dataLoaderSupport == null) {
            dataLoaderSupport = applicationContext.getBean(DataLoaderSupport.class, context);
        }
        return dataLoaderSupport;
    }
}
