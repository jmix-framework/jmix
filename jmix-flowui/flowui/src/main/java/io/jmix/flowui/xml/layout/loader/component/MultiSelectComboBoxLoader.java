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

import io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox;

public class MultiSelectComboBoxLoader extends AbstractMultiSelectComboBoxLoader<JmixMultiSelectComboBox<?>> {

    @Override
    protected JmixMultiSelectComboBox<?> createComponent() {
        return factory.create(JmixMultiSelectComboBox.class);
    }

    @Override
    public void loadComponent() {
        getDataLoaderSupport().loadData(resultComponent, element);

        componentLoader().loadTitle(resultComponent, element, context);
        componentLoader().loadRequired(resultComponent, element, context);

        super.loadComponent();

        getDataLoaderSupport().loadItemsQuery(resultComponent, element);

        if (resultComponent.getValueSource() == null) {
            componentLoader().loadMetaClass(resultComponent, element);
        }
    }
}
